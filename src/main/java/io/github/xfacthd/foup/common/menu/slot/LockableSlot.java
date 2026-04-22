package io.github.xfacthd.foup.common.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import java.util.function.IntPredicate;

public final class LockableSlot extends ResourceHandlerSlot {
    private final int index;
    private final IntPredicate isLocked;

    public LockableSlot(ItemStacksResourceHandler itemHandler, int index, int xPosition, int yPosition, IntPredicate isLocked) {
        super(itemHandler, itemHandler::set, index, xPosition, yPosition);
        this.index = index;
        this.isLocked = isLocked;
    }

    @Override
    public boolean mayPickup(Player player) {
        return !isLocked() && super.mayPickup(player);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !isLocked() && super.mayPlace(stack);
    }

    public boolean isLocked() {
        return isLocked.test(index);
    }
}
