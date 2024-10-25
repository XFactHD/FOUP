package io.github.xfacthd.foup.common.menu;

import io.github.xfacthd.foup.common.blockentity.AbstractCartInteractorBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public abstract class AbstractCartInteractorMenu extends AbstractContainerMenu
{
    protected static final StateProvider DUMMY = new StateProvider()
    {
        @Override
        public AbstractCartInteractorBlockEntity.State getState()
        {
            throw new UnsupportedOperationException();
        }

        @Nullable
        @Override
        public AbstractCartInteractorBlockEntity.Action getActiveAction()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getRemainingDuration()
        {
            throw new UnsupportedOperationException();
        }
    };

    private final Predicate<Player> stillValid;
    private final StateProvider stateProvider;
    private final DataSlot stateSlot;
    private final DataSlot actionSlot;
    private final DataSlot timeLeftSlot;

    protected AbstractCartInteractorMenu(MenuType<?> menuType, int containerId, Predicate<Player> stillValid, StateProvider stateProvider)
    {
        super(menuType, containerId);
        this.stillValid = stillValid;
        this.stateProvider = stateProvider;
        this.stateSlot = addDataSlot(DataSlot.standalone());
        this.actionSlot = addDataSlot(DataSlot.standalone());
        this.timeLeftSlot = addDataSlot(DataSlot.standalone());
        if (stateProvider != DUMMY)
        {
            updateState();
        }
    }

    @Override
    public void broadcastChanges()
    {
        updateState();
        super.broadcastChanges();
    }

    private void updateState()
    {
        stateSlot.set(stateProvider.getState().ordinal());
        AbstractCartInteractorBlockEntity.Action action = stateProvider.getActiveAction();
        actionSlot.set(action != null ? action.ordinal() : -1);
        timeLeftSlot.set(stateProvider.getRemainingDuration());
    }

    public final AbstractCartInteractorBlockEntity.State getState()
    {
        return AbstractCartInteractorBlockEntity.State.byId(stateSlot.get());
    }

    @Nullable
    public final AbstractCartInteractorBlockEntity.Action getActiveAction()
    {
        int id = actionSlot.get();
        return id == -1 ? null : AbstractCartInteractorBlockEntity.Action.byId(id);
    }

    public final int getRemainingDuration()
    {
        return timeLeftSlot.get();
    }

    @Override
    public final boolean stillValid(Player player)
    {
        return stillValid.test(player);
    }

    public interface StateProvider
    {
        AbstractCartInteractorBlockEntity.State getState();

        @Nullable
        AbstractCartInteractorBlockEntity.Action getActiveAction();

        int getRemainingDuration();
    }
}
