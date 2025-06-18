package io.github.xfacthd.foup.common.entity;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.component.HeldFoup;
import io.github.xfacthd.foup.common.data.component.ScheduleSnapshot;
import io.github.xfacthd.foup.common.data.railnet.RailNetwork;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.menu.OverheadCartMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class OverheadCartEntity extends Entity
{
    public static final double PLACEMENT_Y_OFFSET = -7.5/16D;
    // Distance in "pixels" between the bottom of the pod when retracted and to bottom of the block partially occupied by the lower part of the cart
    public static final float CART_BASE_DIST = 8.5F;
    // Height in "pixels" of the base of loader and storage
    public static final float STATION_BASE_HEIGHT = 2F;
    private static final int INTERPOLATION_STEPS = 4;
    static final EntityDataAccessor<OverheadCartAction> ACTION = SynchedEntityData.defineId(
            OverheadCartEntity.class, FoupContent.ENTITY_DATA_SERIALIZER_CART_ACTION.value()
    );
    private static final EntityDataAccessor<Boolean> HAS_FOUP = SynchedEntityData.defineId(
            OverheadCartEntity.class, EntityDataSerializers.BOOLEAN
    );
    private static final EntityDataAccessor<ItemStack> FOUP_CONTENT = SynchedEntityData.defineId(
            OverheadCartEntity.class, EntityDataSerializers.ITEM_STACK
    );
    static final EntityDataAccessor<OverheadCartIssue> ISSUE = SynchedEntityData.defineId(
            OverheadCartEntity.class, FoupContent.ENTITY_DATA_SERIALIZER_CART_ISSUE.value()
    );

    private final OverheadCartBehaviour behaviour = new OverheadCartBehaviour(this);
    private final InterpolationHandler interpolation = new OverheadCartInterpolationHandler(this);
    private int actionStart = -1;

    @Nullable
    private ItemStack foupContent;

    public OverheadCartEntity(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        builder.define(ACTION, OverheadCartAction.DEFAULT);
        builder.define(HAS_FOUP, false);
        builder.define(FOUP_CONTENT, ItemStack.EMPTY);
        builder.define(ISSUE, OverheadCartIssue.NONE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key)
    {
        if (key == ACTION)
        {
            actionStart = tickCount;
        }
    }

    @Override
    public void tick()
    {
        boolean firstTick = this.firstTick;
        super.tick();
        if (!level().isClientSide())
        {
            behaviour.tick(firstTick);
        }
        else
        {
            if (isInterpolating()) // Lerping code copied from AbstractMinecart
            {
                interpolation.interpolate();
            }
            else
            {
                reapplyPosition();
                setRot(getYRot(), getXRot());
            }
        }
    }

    public OverheadCartState getState()
    {
        return entityData.get(ACTION).state();
    }

    public int getActionStart()
    {
        return actionStart;
    }

    public int getActionDuration()
    {
        return entityData.get(ACTION).duration();
    }

    public int getHeightDiff()
    {
        return entityData.get(ACTION).heightDiff();
    }

    public boolean getHasFoup()
    {
        return entityData.get(HAS_FOUP);
    }

    public static float calculateHoistDistance(float heightDiff)
    {
        return heightDiff * 16F + OverheadCartEntity.CART_BASE_DIST - OverheadCartEntity.STATION_BASE_HEIGHT;
    }

    public void notifyReadyForDeparture()
    {
        behaviour.notifyReadyForDeparture();
    }

    public void notifyRetry(boolean retry)
    {
        behaviour.notifyRetry(retry);
    }

    /**
     * Returns the contents of the held FOUP, an empty stack if the FOUP is empty or null if no FOUP is present
     */
    @Nullable
    public ItemStack getFoupContent()
    {
        return foupContent;
    }

    public ItemStack getFoupContentClient()
    {
        if (getHasFoup())
        {
            return entityData.get(FOUP_CONTENT);
        }
        return ItemStack.EMPTY;
    }

    public void setFoupContent(@Nullable ItemStack stack)
    {
        entityData.set(HAS_FOUP, stack != null);
        entityData.set(FOUP_CONTENT, Objects.requireNonNullElse(stack, ItemStack.EMPTY));
        foupContent = stack;
    }

    @Override
    public InterpolationHandler getInterpolation()
    {
        return interpolation;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty())
        {
            if (player.isShiftKeyDown() && player.mayBuild())
            {
                if (level() instanceof ServerLevel level)
                {
                    killAndDrop(level, player);
                }
                return InteractionResult.SUCCESS;
            }
            if (!player.isShiftKeyDown())
            {
                if (player instanceof ServerPlayer)
                {
                    RailNetwork network = behaviour.getOwningNetwork();
                    CartMenuProvider menuProvider = new CartMenuProvider(
                            this, behaviour.getSchedule().getEntriesCopy(), network.getStations()
                    );
                    player.openMenu(menuProvider, menuProvider::encodeClientData);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void move(MoverType type, Vec3 pos)
    {
        if (level() instanceof ServerLevel level && !isRemoved() && pos.lengthSqr() > 0D)
        {
            killAndDrop(level, null);
        }
    }

    public void killAndDrop(ServerLevel level, @Nullable Player player)
    {
        kill(level);

        ItemStack stack = FoupContent.ITEM_CART.toStack();
        stack.set(FoupContent.DC_TYPE_HELD_FOUP, HeldFoup.of(foupContent));
        if (!getSchedule().isEmpty())
        {
            stack.set(FoupContent.DC_TYPE_SCHEDULE, new ScheduleSnapshot(getSchedule().getEntriesCopy()));
        }
        if (player != null)
        {
            if (player.isCreative() && player.getInventory().contains(stack))
            {
                // Don't give the player the cart if they already have this exact one
                return;
            }
            if (!player.getInventory().add(stack))
            {
                player.drop(stack, false);
            }
        }
        else if (level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
        {
            spawnAtLocation(level, stack);
        }
    }

    public Schedule getSchedule()
    {
        return behaviour.getSchedule();
    }

    public Map<String, StationType> getAvailableStations()
    {
        return behaviour.getOwningNetwork().getStations();
    }

    public boolean executeSchedule()
    {
        return behaviour.executeSchedule();
    }

    public void stopSchedule()
    {
        behaviour.stopSchedule();
    }

    public boolean isIdle()
    {
        return getState() == OverheadCartState.IDLE;
    }

    public boolean isUsableByPlayer(Player player)
    {
        return player.distanceToSqr(this) < 64D;
    }

    @Nullable
    public OverheadCartIssue getIssue()
    {
        OverheadCartIssue issue = entityData.get(ISSUE);
        return issue == OverheadCartIssue.NONE ? null : issue;
    }

    public void rescue()
    {
        behaviour.rescue();
    }

    @Override
    public void remove(RemovalReason reason)
    {
        if (!level().isClientSide() && reason.shouldDestroy())
        {
            behaviour.destroy();
        }
        super.remove(reason);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount)
    {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput)
    {
        behaviour.load(valueInput);
        setFoupContent(valueInput.read("foup_content", ItemStack.OPTIONAL_CODEC).orElse(null));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput)
    {
        behaviour.save(valueOutput);
        valueOutput.storeNullable("foup_content", ItemStack.OPTIONAL_CODEC, foupContent);
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity other)
    {
        return true;
    }

    @Override
    public boolean isPushedByFluid(FluidType type)
    {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPushedByFluid()
    {
        return false;
    }

    @Override
    public boolean canTeleport(Level fromLevel, Level toLevel)
    {
        return false;
    }

    private static final class OverheadCartInterpolationHandler extends InterpolationHandler
    {
        private final OverheadCartEntity entity;

        public OverheadCartInterpolationHandler(OverheadCartEntity entity)
        {
            super(entity, INTERPOLATION_STEPS);
            this.entity = entity;
        }

        // Pos-only and rot-only packets overwrite the existing target rotation and position respectively with the
        // last interpolation result instead of the existing interpolation target
        @Override
        public void interpolateTo(Vec3 pos, float yRot, float xRot)
        {
            if (hasActiveInterpolation())
            {
                if (pos.equals(entity.position()))
                {
                    pos = position();
                }
                if (yRot == entity.getYRot())
                {
                    yRot = yRot();
                }
            }
            super.interpolateTo(pos, yRot, xRot);
        }
    }

    private record CartMenuProvider(
            OverheadCartEntity cart,
            List<Schedule.Entry> entries,
            Map<String, StationType> stations
    ) implements MenuProvider
    {
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
        {
            return new OverheadCartMenu(containerId, inventory, cart, entries, stations);
        }

        @Override
        public Component getDisplayName()
        {
            return OverheadCartMenu.MENU_TITLE;
        }

        public void encodeClientData(RegistryFriendlyByteBuf buf)
        {
            OverheadCartMenu.ClientData.STREAM_CODEC.encode(buf, new OverheadCartMenu.ClientData(cart.getId(), entries, stations));
        }
    }
}
