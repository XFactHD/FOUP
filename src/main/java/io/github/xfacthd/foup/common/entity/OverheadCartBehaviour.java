package io.github.xfacthd.foup.common.entity;

import dev.gigaherz.graph3.Graph;
import io.github.xfacthd.foup.common.blockentity.AbstractOverheadRailBlockEntity;
import io.github.xfacthd.foup.common.data.TrackShape;
import io.github.xfacthd.foup.common.data.railnet.Dijkstra;
import io.github.xfacthd.foup.common.data.railnet.RailNetwork;
import io.github.xfacthd.foup.common.data.railnet.TrackNode;
import io.github.xfacthd.foup.common.data.railnet.TrackPath;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.util.FoupCodecs;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

final class OverheadCartBehaviour
{
    // The minimum offset needed to compute the rail's BlockPos from the entity's position
    private static final double ENTITY_TO_RAIL_POS_OFFSET = .6;
    // The amount of ticks it takes the hoist to travel up/down one block
    private static final int HOIST_TICKS_PER_BLOCK = 16;
    private static final double MOVE_BLOCKS_PER_TICK = 1D/12D;
    // The amount of ticks to wait after arrival before hoisting and after hoisting before departure
    private static final int PARK_DURATION = 10;

    private final OverheadCartEntity cart;
    private final Schedule schedule;
    private OverheadCartAction action = OverheadCartAction.DEFAULT;
    private int actionStart = -1;
    @Nullable
    private TrackPath path;
    @Nullable
    private BlockPos prevNodePos;
    @Nullable
    private BlockPos currNodePos;
    @Nullable
    private TrackNode prevNode;
    @Nullable
    private TrackNode currNode;
    private boolean rotating = false;
    private boolean haltRequested = false;
    private boolean retry = false;

    OverheadCartBehaviour(OverheadCartEntity cart)
    {
        this.cart = cart;
        this.schedule = new Schedule(cart);
    }

    void tick(boolean firstTick)
    {
        if (currNode == null)
        {
            if (!firstTick && cart.tickCount % 20 != 0) return;

            currNode = findTrackNode(currNodePos, true);
            if (currNode == null) return;

            currNode.setOccupied(true);
            currNodePos = null;

            if (path != null)
            {
                currNode.getNetwork().registerPath(path);
            }
        }
        if (prevNodePos != null && (firstTick || cart.tickCount % 20 == 0))
        {
            prevNode = findTrackNode(prevNodePos, false);
            if (prevNode != null)
            {
                prevNode.setOccupied(true);
                prevNodePos = null;
            }
        }
        if (prevNode != null && prevNode.isInvalid())
        {
            // Rail block of previous node broken and node removed from graph
            prevNode = null;
        }

        switch (action.state())
        {
            case IDLE, POD_IN_LOADER_OR_STORAGE -> { }
            case MOVING ->
            {
                if (prevNode == null && prevNodePos != null)
                {
                    break;
                }
                if (path == null || !path.isValid())
                {
                    if (path != null)
                    {
                        currNode.getNetwork().removePath(path);
                        path = null;
                    }
                    setIdleOnError(OverheadCartIssue.Type.PATH_INVALID, null);
                    break;
                }

                boolean done;
                boolean errored = false;
                try
                {
                    done = move(path, prevNode, currNode, path.peek(currNode.getNetwork()));
                }
                catch (Throwable ignored)
                {
                    done = true;
                    errored = true;
                }
                if (done)
                {
                    currNode.getNetwork().removePath(path);
                    path = null;
                    if (errored)
                    {
                        setIdleOnError(OverheadCartIssue.Type.MOVEMENT_ERROR, null);
                    }
                    else
                    {
                        setAction(OverheadCartState.PARK_AFTER_ARRIVAL, PARK_DURATION, 0);
                    }
                }
            }
            case PARK_AFTER_ARRIVAL ->
            {
                if (cart.tickCount - actionStart > action.duration())
                {
                    int heightDiff;
                    AbstractOverheadRailBlockEntity owner = currNode.getOwner();
                    if (!currNode.isStation() || owner == null || (heightDiff = owner.getStationHeightDifference()) <= 0)
                    {
                        setIdleOnError(OverheadCartIssue.Type.TARGET_INVALID, schedule.getActiveEntry().station());
                        return;
                    }

                    startHoist(true, heightDiff);
                }
            }
            case LOWERING_HOIST ->
            {
                if (cart.tickCount - actionStart > action.duration())
                {
                    if (schedule.isEmpty())
                    {
                        setIdleOnError(OverheadCartIssue.Type.EMPTY_SCHEDULE, null);
                        break;
                    }

                    setAction(OverheadCartState.POD_IN_LOADER_OR_STORAGE, 0, action.heightDiff());
                    currNode.notifyArrival(cart, schedule.getActiveEntry());
                }
            }
            case RAISING_HOIST ->
            {
                if (cart.tickCount - actionStart > action.duration())
                {
                    setAction(OverheadCartState.PARK_BEFORE_DEPARTURE, PARK_DURATION, 0);
                }
            }
            case PARK_BEFORE_DEPARTURE ->
            {
                if (cart.tickCount - actionStart > action.duration())
                {
                    if (!retry)
                    {
                        schedule.advance();
                    }
                    retry = false;

                    if (haltRequested)
                    {
                        setAction(OverheadCartState.IDLE, 0, 0);
                        haltRequested = false;
                    }
                    else
                    {
                        setAction(OverheadCartState.PATHING, 0, 0);
                    }
                }
            }
            case PATHING ->
            {
                if (schedule.isEmpty())
                {
                    setIdleOnError(OverheadCartIssue.Type.EMPTY_SCHEDULE, null);
                    break;
                }

                Graph<RailNetwork> graph = Objects.requireNonNull(currNode.getGraph());
                RailNetwork network = graph.getContextData();
                String station = schedule.getActiveEntry().station();
                TrackNode targetNode = network.getStation(station);
                if (targetNode == null)
                {
                    setIdleOnError(OverheadCartIssue.Type.TARGET_MISSING, station);
                    break;
                }

                path = Dijkstra.getShortestPath(graph, currNode, targetNode);
                if (!path.isValid())
                {
                    setIdleOnError(OverheadCartIssue.Type.TARGET_UNREACHABLE, station);
                    break;
                }
                if (path.peek(network) == currNode)
                {
                    // Drop the first path node if it's the one the cart is on
                    path.remove(network);
                }
                setAction(OverheadCartState.MOVING, 0, 0);
            }
        }
    }

    private boolean move(TrackPath path, @Nullable TrackNode prevNode, TrackNode currNode, @Nullable TrackNode nextNode)
    {
        Direction dirOne = prevNode != null ? Utils.getDirByNormal(prevNode.getPos(), currNode.getPos()) : Utils.getDirByViewVec(cart);
        Direction dirTwo = nextNode != null ? Utils.getDirByNormal(currNode.getPos(), nextNode.getPos()) : dirOne;
        TrackShape shape = TrackShape.byDirPair(dirOne, dirTwo);
        Vec3 pos = cart.getPosition(1F);
        Vec3 newPos;
        if (shape.isStraight())
        {
            boolean done = false;
            Vec3 diff = dirOne.getUnitVec3().multiply(MOVE_BLOCKS_PER_TICK, 0, MOVE_BLOCKS_PER_TICK);
            newPos = pos.add(diff);
            if (nextNode == null || nextNode.isOccupied())
            {
                double frac = Utils.fractionInDir(newPos, dirOne);
                if (frac > .5)
                {
                    double value = Math.floor(dirOne.getAxis().choose(newPos.x, 0, newPos.z));
                    newPos = Utils.setAlongAxis(newPos, value + .5, dirOne);
                    done = nextNode == null;
                }
            }
            cart.setPos(newPos);
            if (done)
            {
                return true;
            }
        }
        else
        {
            if (rotating)
            {
                newPos = pos;

                float srcRot = cart.getYRot();
                float destRot = dirTwo.toYRot();
                float maximumChange = 9F;
                float diff = Mth.wrapDegrees(destRot - srcRot);
                diff = Math.clamp(diff, -maximumChange, maximumChange);

                float newRot = srcRot + diff;
                if (newRot < 0.0F)
                {
                    newRot += 360.0F;
                }
                else if (newRot >= 360.0F)
                {
                    newRot -= 360.0F;
                }

                if (Mth.equal(newRot, destRot))
                {
                    newRot = destRot;
                    rotating = false;
                }
                cart.setYRot(newRot);
            }
            else
            {
                Direction dir = Utils.getDirByViewVec(cart);
                Vec3 diff = dir.getUnitVec3().multiply(MOVE_BLOCKS_PER_TICK, 0, MOVE_BLOCKS_PER_TICK);
                newPos = pos.add(diff);
                if (dir == dirOne)
                {
                    double frac = Utils.fractionInDir(newPos, dirOne);
                    if (frac > .5)
                    {
                        double value = Math.floor(dirOne.getAxis().choose(newPos.x, 0, newPos.z));
                        newPos = Utils.setAlongAxis(newPos, value + .5, dirOne);
                        rotating = true;
                    }
                }
                cart.setPos(newPos);
            }
        }

        // Handle moving between blocks
        if (Math.floor(pos.x) != Math.floor(newPos.x) || Math.floor(pos.z) != Math.floor(newPos.z))
        {
            if (prevNode != null)
            {
                prevNode.setOccupied(false);
            }
            this.prevNode = currNode;
            // Next node cannot be null when a block boundary is crossed
            this.currNode = Objects.requireNonNull(nextNode);
            this.currNode.setOccupied(true);
            path.remove(currNode.getNetwork());
        }
        return false;
    }

    @Nullable
    private TrackNode findTrackNode(@Nullable BlockPos trackPos, boolean computeIfNull)
    {
        if (trackPos == null)
        {
            if (!computeIfNull)
            {
                return null;
            }
            trackPos = BlockPos.containing(cart.getX(), cart.getY() + ENTITY_TO_RAIL_POS_OFFSET, cart.getZ());
        }
        if (cart.level().getBlockEntity(trackPos) instanceof AbstractOverheadRailBlockEntity be)
        {
            return be.getTrackNode();
        }
        return null;
    }

    void notifyReadyForDeparture()
    {
        if (action.state() == OverheadCartState.POD_IN_LOADER_OR_STORAGE)
        {
            startHoist(false, action.heightDiff());
        }
    }

    void notifyRetry(boolean retry)
    {
        this.retry = retry;
    }

    private void startHoist(boolean downward, int heightDiff)
    {
        OverheadCartState state = downward ? OverheadCartState.LOWERING_HOIST : OverheadCartState.RAISING_HOIST;
        float dist = OverheadCartEntity.calculateHoistDistance(heightDiff);
        int duration = (int) Math.ceil(dist / 16F * HOIST_TICKS_PER_BLOCK);
        setAction(state, duration, heightDiff);
    }

    void setAction(OverheadCartState state, int duration, int heightDiff)
    {
        this.actionStart = cart.tickCount;
        this.action = new OverheadCartAction(state, duration, heightDiff);
        cart.getEntityData().set(OverheadCartEntity.ACTION, action);
        if (state != OverheadCartState.IDLE)
        {
            cart.getEntityData().set(OverheadCartEntity.ISSUE, OverheadCartIssue.NONE);
        }
    }

    void setIdleOnError(OverheadCartIssue.Type type, @Nullable String detail)
    {
        setAction(OverheadCartState.IDLE, 0, 0);
        cart.getEntityData().set(OverheadCartEntity.ISSUE, new OverheadCartIssue(type, Optional.ofNullable(detail)));
    }

    Schedule getSchedule()
    {
        return schedule;
    }

    RailNetwork getOwningNetwork()
    {
        return Objects.requireNonNull(currNode).getNetwork();
    }

    boolean executeSchedule()
    {
        RailNetwork network = getOwningNetwork();
        if (action.state() == OverheadCartState.IDLE && schedule.isValid(network))
        {
            TrackNode node = network.getStation(schedule.getActiveEntry().station());
            if (node == currNode && isOnPosition())
            {
                setAction(OverheadCartState.PARK_AFTER_ARRIVAL, PARK_DURATION, 0);
            }
            else
            {
                setAction(OverheadCartState.PATHING, 0, 0);
            }
            return true;
        }
        return false;
    }

    private boolean isOnPosition()
    {
        return Mth.equal(Mth.frac(Math.abs(cart.getX())), .5) && Mth.equal(Mth.frac(Math.abs(cart.getZ())), .5);
    }

    void stopSchedule()
    {
        OverheadCartState state = action.state();
        if (state == OverheadCartState.IDLE) return;

        if (state == OverheadCartState.PATHING || state == OverheadCartState.MOVING)
        {
            setAction(OverheadCartState.IDLE, 0, 0);
        }
        else
        {
            haltRequested = true;
        }
    }

    void rescue()
    {
        if (action.state() == OverheadCartState.POD_IN_LOADER_OR_STORAGE)
        {
            startHoist(false, action.heightDiff());
            retry = true;
        }
    }

    void destroy()
    {
        if (currNode != null)
        {
            currNode.setOccupied(false);
            if (path != null)
            {
                currNode.getNetwork().removePath(path);
            }
        }
        if (prevNode != null)
        {
            prevNode.setOccupied(false);
        }
    }

    void save(ValueOutput valueOutput)
    {
        valueOutput.putInt("state", action.state().ordinal());
        valueOutput.putInt("action_start", actionStart);
        valueOutput.putInt("action_duration", action.duration());
        valueOutput.putInt("height_diff", action.heightDiff());
        valueOutput.putBoolean("rotating", rotating);
        valueOutput.putBoolean("halt_requested", haltRequested);
        valueOutput.putBoolean("retry", retry);
        if (prevNode != null)
        {
            valueOutput.putLong("prev_node", prevNode.getPos().asLong());
        }
        if (currNode != null)
        {
            valueOutput.putLong("curr_node", currNode.getPos().asLong());
        }
        if (path != null)
        {
            path.save(valueOutput.list("path", FoupCodecs.POS_AS_LONG));
        }
        if (!schedule.isEmpty())
        {
            schedule.save(valueOutput.child("schedule"));
        }
        OverheadCartIssue issue = cart.getIssue();
        if (issue != null)
        {
            ValueOutput issueOutput = valueOutput.child("issue");
            issueOutput.putString("type", issue.type().getSerializedName());
            if (issue.detail().isPresent())
            {
                issueOutput.putString("detail", issue.detail().get());
            }
        }
    }

    void load(ValueInput valueInput)
    {
        actionStart = valueInput.getIntOr("action_start", -1);
        OverheadCartState state = OverheadCartState.of(valueInput.getIntOr("state", 0));
        setAction(state, valueInput.getIntOr("action_duration", 0), valueInput.getIntOr("height_diff", 0));
        rotating = valueInput.getBooleanOr("rotating", false);
        haltRequested = valueInput.getBooleanOr("halt_requested", false);
        retry = valueInput.getBooleanOr("retry", false);
        prevNodePos = valueInput.getLong("prev_node").map(BlockPos::of).orElse(null);
        currNodePos = valueInput.getLong("curr_node").map(BlockPos::of).orElse(null);
        path = valueInput.list("path", FoupCodecs.POS_AS_LONG).map(TrackPath::load).orElse(null);
        valueInput.child("schedule").ifPresent(schedule::load);
        valueInput.child("issue").ifPresent(issueInput ->
        {
            OverheadCartIssue.Type type = OverheadCartIssue.Type.byName(issueInput.getStringOr("type", ""));
            if (type != null)
            {
                Optional<String> detail = issueInput.getString("detail");
                cart.getEntityData().set(OverheadCartEntity.ISSUE, new OverheadCartIssue(type, detail));
            }
        });
    }
}
