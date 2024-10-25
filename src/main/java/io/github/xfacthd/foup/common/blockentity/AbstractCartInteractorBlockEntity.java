package io.github.xfacthd.foup.common.blockentity;

import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.menu.AbstractCartInteractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.IntFunction;

public abstract sealed class AbstractCartInteractorBlockEntity extends BaseBlockEntity implements AbstractCartInteractorMenu.StateProvider
        permits FoupLoaderBlockEntity, FoupStorageInterfaceBlockEntity
{
    private final Type type;
    @Nullable
    private BlockPos linkedStation = null;
    // TODO: consider displaying state on the block itself
    private State state = State.IDLE;
    private int delayCounter = 0;
    @Nullable
    protected Action currAction = null;
    @Nullable
    private OverheadCartEntity currCart = null;
    @Nullable
    private UUID currCartUuid = null;

    protected AbstractCartInteractorBlockEntity(BlockEntityType<?> beType, BlockPos pos, BlockState state, Type type)
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
                if (canStartAction(cart, Objects.requireNonNull(currAction)).isTrue())
                {
                    setState(State.PRE_INTERACT_DELAY);
                }
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
                startInteraction(cart, Objects.requireNonNull(currAction));
                setState(State.INTERACTING);
            }
            case INTERACTING ->
            {
                finishInteraction(cart, Objects.requireNonNull(currAction));
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

    /**
     * Returns whether the action can be started ({@link TriState#TRUE}), the action cannot be started and can be skipped
     * ({@link TriState#DEFAULT}) or the action cannot be started and the cart must be blocked ({@link TriState#FALSE}).
     */
    protected abstract TriState canStartAction(OverheadCartEntity cart, Action action);

    protected abstract void startInteraction(OverheadCartEntity cart, Action action);

    protected abstract void finishInteraction(OverheadCartEntity cart, Action action);

    @Nullable
    protected final OverheadCartEntity getCart()
    {
        if (currCart == null && currCartUuid != null && level instanceof ServerLevel serverLevel)
        {
            currCart = serverLevel.getEntity(currCartUuid) instanceof OverheadCartEntity cart ? cart : null;
            if (currCart == null)
            {
                clearCart(false);
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

        currAction = null;
        currCart = null;
        currCartUuid = null;
        setState(State.IDLE);
    }

    void notifyArrival(OverheadCartEntity cart, Action action)
    {
        currCart = cart;
        currCartUuid = cart.getUUID();
        switch (canStartAction(cart, action))
        {
            case TRUE -> setState(State.PRE_INTERACT_DELAY); // Continue
            case DEFAULT -> setState(State.POST_INTERACT_DELAY); // Skip
            case FALSE -> setState(State.BLOCKED); // Hold
        }
        currAction = state == State.POST_INTERACT_DELAY ? null : action;
    }

    @Override
    public final State getState()
    {
        return state;
    }

    @Nullable
    @Override
    public final Action getActiveAction()
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

    void clearLink()
    {
        linkedStation = null;
        setChangedWithoutSignalUpdate();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        linkedStation = tag.contains("linked_station") ? BlockPos.of(tag.getLong("linked_station")) : null;
        state = State.BY_ID.apply(tag.getInt("state"));
        delayCounter = tag.getInt("delay_counter");
        currAction = tag.contains("current_action") ? Action.BY_ID.apply(tag.getInt("current_action")) : null;
        currCartUuid = tag.contains("current_cart") ? tag.getUUID("current_cart") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        if (linkedStation != null)
        {
            tag.putLong("linked_station", linkedStation.asLong());
        }
        tag.putInt("state", state.ordinal());
        tag.putInt("delay_counter", delayCounter);
        if (currAction != null)
        {
            tag.putInt("current_action", currAction.ordinal());
        }
        if (currCartUuid != null)
        {
            tag.putUUID("current_cart", currCartUuid);
        }
    }

    public enum Type
    {
        LOADER,
        STORAGE
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

        public int getDuration(Type type)
        {
            return switch (type)
            {
                case LOADER -> loaderDuration;
                case STORAGE -> storageDuration;
            };
        }

        public static State byId(int id)
        {
            return BY_ID.apply(id);
        }
    }

    public enum Action
    {
        LOAD,
        UNLOAD;

        static final IntFunction<Action> BY_ID = ByIdMap.continuous(Action::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

        public static Action byId(int id)
        {
            return BY_ID.apply(id);
        }
    }
}
