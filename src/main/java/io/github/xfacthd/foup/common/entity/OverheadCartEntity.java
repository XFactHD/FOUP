package io.github.xfacthd.foup.common.entity;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.component.HeldFoup;
import io.github.xfacthd.foup.common.data.component.ScheduleSnapshot;
import io.github.xfacthd.foup.common.data.railnet.RailNetwork;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.menu.OverheadCartMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
    private int actionStart = -1;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;

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
            if (lerpSteps > 0) // Lerping code copied from AbstractMinecart
            {
                lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, lerpXRot);
                lerpSteps--;
            }
            else
            {
                reapplyPosition();
                setRot(getYRot(), getXRot());
            }

            // Handle rotation wrapping around and causing interpolation hitch
            double diff = getYRot() - yRotO;
            if (diff < -270F)
            {
                yRotO -= 360F;
            }
            else if (diff > 270F)
            {
                yRotO += 360F;
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
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps)
    {
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYRot = yRot;
        lerpXRot = xRot;
        lerpSteps = steps + 1;
    }

    @Override
    public double lerpTargetX()
    {
        return lerpSteps > 0 ? lerpX : getX();
    }

    @Override
    public double lerpTargetY()
    {
        return lerpSteps > 0 ? lerpY : getY();
    }

    @Override
    public double lerpTargetZ()
    {
        return lerpSteps > 0 ? lerpZ : getZ();
    }

    @Override
    public float lerpTargetXRot()
    {
        return lerpSteps > 0 ? (float) lerpXRot : getXRot();
    }

    @Override
    public float lerpTargetYRot()
    {
        return lerpSteps > 0 ? (float) lerpYRot : getYRot();
    }

    @Override
    public AABB getBoundingBoxForCulling()
    {
        AABB aabb = super.getBoundingBoxForCulling();
        OverheadCartState state = getState();
        if (state != null && state.hasMovingHoist())
        {
            double diff = getHeightDiff() + (CART_BASE_DIST / 16F) - (STATION_BASE_HEIGHT / 16F);
            aabb = aabb.expandTowards(0, -diff, 0);
        }
        return aabb;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty() && player.isShiftKeyDown())
        {
            if (!level().isClientSide())
            {
                killAndDrop(player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty() && !player.isShiftKeyDown())
        {
            if (player instanceof ServerPlayer)
            {
                RailNetwork network = behaviour.getOwningNetwork();
                CartMenuProvider menuProvider = new CartMenuProvider(
                        this, behaviour.getSchedule().getEntriesCopy(), network.getStations()
                );
                player.openMenu(menuProvider, menuProvider::encodeClientData);
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public void move(MoverType type, Vec3 pos)
    {
        if (!level().isClientSide && !isRemoved() && pos.lengthSqr() > 0D)
        {
            killAndDrop(null);
        }
    }

    public void killAndDrop(@Nullable Player player)
    {
        kill();

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
        else if (level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
        {
            spawnAtLocation(stack);
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
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        behaviour.load(tag, level().registryAccess());
        setFoupContent(tag.contains("foup_content") ? ItemStack.parseOptional(level().registryAccess(), tag.getCompound("foup_content")) : null);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        behaviour.save(tag, level().registryAccess());
        if (foupContent != null)
        {
            tag.put("foup_content", foupContent.saveOptional(level().registryAccess()));
        }
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public boolean canBeCollidedWith()
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
    public boolean canChangeDimensions(Level oldLevel, Level newLevel)
    {
        return false;
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
