package io.github.xfacthd.foup.common.menu;

import io.github.xfacthd.foup.common.FoupContent;
import io.github.xfacthd.foup.common.data.StationType;
import io.github.xfacthd.foup.common.data.railnet.Schedule;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OverheadCartMenu extends AbstractContainerMenu {
    public static final Component MENU_TITLE = Component.translatable("foup.container.overhead_hoist_cart");

    private final OverheadCartEntity cart;
    private final List<Schedule.Entry> scheduleEntries;
    private final Map<String, StationType> stations;

    public OverheadCartMenu(int containerId, Inventory inventory, OverheadCartEntity cart, List<Schedule.Entry> scheduleEntries, Map<String, StationType> stations) {
        super(FoupContent.MENU_TYPE_OVERHEAD_CART.value(), containerId);
        this.cart = cart;
        this.scheduleEntries = scheduleEntries;
        this.stations = stations;
        Utils.addPlayerInvSlots(this::addSlot, inventory, 70, 159);
    }

    public static OverheadCartMenu createClient(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        ClientData data = ClientData.STREAM_CODEC.decode(buf);
        Entity entity = inventory.player.level().getEntity(data.cartId);
        if (!(entity instanceof OverheadCartEntity cart)) {
            throw new IllegalStateException("Failed to find OverheadCartEntity with ID " + data.cartId);
        }
        return new OverheadCartMenu(containerId, inventory, cart, data.entries, data.stations);
    }

    public OverheadCartEntity getCart() {
        return cart;
    }

    public List<Schedule.Entry> getInitialScheduleEntries() {
        return scheduleEntries;
    }

    public Map<String, StationType> getInitialStations() {
        return stations;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return cart.isUsableByPlayer(player);
    }

    public record ClientData(int cartId, List<Schedule.Entry> entries, Map<String, StationType> stations) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                ClientData::cartId,
                Schedule.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
                ClientData::entries,
                ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, StationType.STREAM_CODEC),
                ClientData::stations,
                ClientData::new
        );
    }
}
