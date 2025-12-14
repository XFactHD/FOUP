package io.github.xfacthd.foup.common.blockentity;

import io.github.xfacthd.foup.common.data.StationAction;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.menu.AbstractCartInteractorMenu;
import io.github.xfacthd.foup.common.util.FoupCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public abstract sealed class AbstractCartInteractorBlockEntity extends BaseBlockEntity implements AbstractCartInteractorMenu.StateProvider
        permits FoupLoaderBlockEntity, FoupStorageInterfaceBlockEntity
{
    private final StationType type;
    @Nullable
    private BlockPos linkedStation = null;
    // TODO: consider displaying state on the block itself
    private State state = State.IDLE;
    private int delayCounter = 0;
    private Schedule.@Nullable Entry currScheduleEntry = null;
    @Nullable
    protected StationAction currAction = null;
    @Nullable
    private OverheadCartEntity currCart = null;
    @Nullable
    private UUID currCartUuid = null;
    private boolean delayCartResolveOnFail = false;

    protected AbstractCartInteractorBlockEntity(BlockEntityType<?> beType, BlockPos pos, BlockState state, StationType type)
    {
        super(beType, pos, state);
        this.type = type;
    }

    @SuppressWarnings("unused")
    public static void tick(Level level, BlockPos pos, BlockState state, AbstractCartInteractorBlockEntity be)
    {
        be.tickInteraction();
        be.tickInternal();
    }

    private void tickInteraction()
    {
        if (state == State.IDLE) return;

        if (state == State.BLOCKED)
        {
            OverheadCartEntity cart = getCart();
            if (cart != null && level().getGameTime() % 20 == 0)
            {
                checkCanStartAction(cart, Objects.requireNonNull(currScheduleEntry));
            }
            return;
        }

        if (delayCounter > 0)
        {
            delayCounter--;
            setChangedWithoutSignalUpdate();
            return;
        }

        OverheadCartEntity cart = getCart();
        if (cart == null) return;

        switch (state)
        {
            case PRE_INTERACT_DELAY ->
            {
                startInteraction(cart, Objects.requireNonNull(currScheduleEntry));
                setState(State.INTERACTING);
            }
            case INTERACTING ->
            {
                finishInteraction(cart, Objects.requireNonNull(currScheduleEntry));
                setState(State.POST_INTERACT_DELAY);
            }
            case POST_INTERACT_DELAY -> clearCart(true);
        }
    }

    protected void tickInternal() { }

    private void setState(State state)
    {
        this.state = state;
        delayCounter = state.getDuration(type);
        setChangedWithoutSignalUpdate();
    }

    protected boolean isBlocked()
    {
        return state == State.BLOCKED;
    }

    protected abstract StartCheck canStartAction(OverheadCartEntity cart, Schedule.Entry scheduleEntry);

    protected abstract void startInteraction(OverheadCartEntity cart, Schedule.Entry scheduleEntry);

    protected abstract void finishInteraction(OverheadCartEntity cart, Schedule.Entry scheduleEntry);

    @Nullable
    protected final OverheadCartEntity getCart()
    {
        if (currCart == null && currCartUuid != null && level instanceof ServerLevel serverLevel)
        {
            currCart = serverLevel.getEntity(currCartUuid) instanceof OverheadCartEntity cart ? cart : null;
            if (currCart == null && !delayCartResolveOnFail)
            {
                clearCart(false);
            }
            else if (currCart != null)
            {
                delayCartResolveOnFail = false;
            }
        }
        if (currCart != null && currCart.isRemoved())
        {
            clearCart(false);
        }
        return currCart;
    }

    protected final void clearCart(boolean notifyCart)
    {
        if (notifyCart)
        {
            Objects.requireNonNull(getCart()).notifyReadyForDeparture();
        }

        currScheduleEntry = null;
        currAction = null;
        currCart = null;
        currCartUuid = null;
        setState(State.IDLE);
    }

    void notifyArrival(OverheadCartEntity cart, Schedule.Entry scheduleEntry)
    {
        currCart = cart;
        currCartUuid = cart.getUUID();
        if (checkCanStartAction(cart, scheduleEntry))
        {
            currScheduleEntry = scheduleEntry;
            currAction = scheduleEntry.action();
        }
    }

    private boolean checkCanStartAction(OverheadCartEntity cart, Schedule.Entry scheduleEntry)
    {
        StartCheck startCheck = canStartAction(cart, scheduleEntry);
        switch (startCheck)
        {
            case EXECUTE -> setState(State.PRE_INTERACT_DELAY);
            case WAIT -> setState(State.BLOCKED);
            case RETRY, SKIP -> setState(State.POST_INTERACT_DELAY);
        }
        if (startCheck.depart)
        {
            cart.notifyRetry(startCheck.retry);
            currScheduleEntry = null;
            currAction = null;
            return false;
        }
        return true;
    }

    @Override
    public final State getState()
    {
        return state;
    }

    @Nullable
    @Override
    public final StationAction getActiveAction()
    {
        return currAction;
    }

    @Override
    public final int getRemainingDuration()
    {
        return delayCounter;
    }

    public void unlink()
    {
        //noinspection ConstantConditions
        if (linkedStation != null && level().getBlockEntity(linkedStation) instanceof OverheadRailStationBlockEntity be)
        {
            be.unlink(false);
        }
        clearLink();
    }

    void notifyLinked(BlockPos pos)
    {
        linkedStation = pos;
        setChangedWithoutSignalUpdate();
    }

    void clearLink()
    {
        linkedStation = null;
        setChangedWithoutSignalUpdate();
    }

    public final StationType getStationType()
    {
        return type;
    }

    public abstract void dropContents(Consumer<ItemStack> dropper);

    @Override
    protected void loadAdditional(ValueInput valueInput)
    {
        super.loadAdditional(valueInput);
        linkedStation = valueInput.read("linked_station", FoupCodecs.POS_AS_LONG).orElse(null);
        state = State.BY_ID.apply(valueInput.getIntOr("state", 0));
        delayCounter = valueInput.getIntOr("delay_counter", 0);
        currScheduleEntry = valueInput.read("current_schedule_entry", Schedule.Entry.CODEC).orElse(null);
        currAction = currScheduleEntry != null ? currScheduleEntry.action() : null;
        currCartUuid = valueInput.read("current_cart", UUIDUtil.CODEC).orElse(null);
        delayCartResolveOnFail = currCartUuid != null;
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput)
    {
        super.saveAdditional(valueOutput);
        valueOutput.storeNullable("linked_station", FoupCodecs.POS_AS_LONG, linkedStation);
        valueOutput.putInt("state", state.ordinal());
        valueOutput.putInt("delay_counter", delayCounter);
        valueOutput.storeNullable("current_schedule_entry", Schedule.Entry.CODEC, currScheduleEntry);
        valueOutput.storeNullable("current_cart", UUIDUtil.CODEC, currCartUuid);
    }

    public enum State
    {
        IDLE(0),
        PRE_INTERACT_DELAY(10),
        INTERACTING(30, 50),
        POST_INTERACT_DELAY(10),
        BLOCKED(0);

        private static final IntFunction<State> BY_ID = ByIdMap.continuous(State::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

        private final int loaderDuration;
        private final int storageDuration;

        State(int duration)
        {
            this(duration, duration);
        }

        State(int loaderDuration, int storageDuration)
        {
            this.loaderDuration = loaderDuration;
            this.storageDuration = storageDuration;
        }

        public int getDuration(StationType type)
        {
            return switch (type)
            {
                case UNKNOWN -> 0;
                case LOADER -> loaderDuration;
                case STORAGE -> storageDuration;
            };
        }

        public static State byId(int id)
        {
            return BY_ID.apply(id);
        }
    }

    protected enum StartCheck
    {
        EXECUTE(false, false),
        WAIT(false, false),
        RETRY(true, true),
        SKIP(true, false)
        ;

        private final boolean depart;
        private final boolean retry;

        StartCheck(boolean depart, boolean retry)
        {
            this.depart = depart;
            this.retry = retry;
        }
    }
}
