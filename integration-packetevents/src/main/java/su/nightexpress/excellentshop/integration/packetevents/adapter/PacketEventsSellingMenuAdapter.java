package su.nightexpress.excellentshop.integration.packetevents.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPlayerInventory;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.api.menu.SellingMenuAdapter;
import su.nightexpress.excellentshop.api.menu.SellingMenuProvider;
import su.nightexpress.excellentshop.integration.packetevents.PacketEventsPacketReceiver;

import java.util.List;
import java.util.Optional;

public class PacketEventsSellingMenuAdapter extends PacketEventsPacketReceiver implements SellingMenuAdapter {

    private final SellingMenuProvider provider;

    public PacketEventsSellingMenuAdapter(@NonNull SellingMenuProvider provider) {
        this.provider = provider;
    }

    @Override
    public void callPlayerInventoryPacket(@NonNull Player player, int slot, @NonNull ItemStack itemStack) {
        WrapperPlayServerSetPlayerInventory setPlayerInventory = new WrapperPlayServerSetPlayerInventory(slot, this.fromBukkit(itemStack));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, setPlayerInventory);
    }

    @Override
    public void onPacketSend(@NonNull PacketSendEvent event) {
        PacketTypeCommon type = event.getPacketType();
        Player player = event.getPlayer();
        if (player == null || player.getGameMode() == GameMode.CREATIVE || !this.provider.isViewer(player)) return;

        switch (type) {
            case PacketType.Play.Server.SET_SLOT -> {
                WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(event);

                if (!this.provider.isImmuneSlot(player, setSlot.getSlot())) {
                    setSlot.setItem(this.render(player, setSlot.getItem()));
                }
            }
            case PacketType.Play.Server.WINDOW_ITEMS -> {
                WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(event);

                List<com.github.retrooper.packetevents.protocol.item.ItemStack> items = windowItems.getItems();
                for (int index = 0; index < items.size(); index++) {
                    if (!this.provider.isProductSlot(index)) continue;

                    items.set(index, this.render(player, items.get(index)));
                }
            }
            case PacketType.Play.Server.SET_PLAYER_INVENTORY -> {
                WrapperPlayServerSetPlayerInventory setPlayerInventory = new WrapperPlayServerSetPlayerInventory(event);

                setPlayerInventory.setStack(this.render(player, setPlayerInventory.getStack()));
            }
            case PacketType.Play.Server.SET_CURSOR_ITEM -> {
                WrapperPlayServerSetCursorItem setCursorItem = new WrapperPlayServerSetCursorItem(event);

                setCursorItem.setStack(this.render(player, setCursorItem.getStack()));
            }
            default -> {
                return;
            }
        }

        event.markForReEncode(true);
    }

    private com.github.retrooper.packetevents.protocol.item.@NonNull ItemStack render(@NonNull Player player, com.github.retrooper.packetevents.protocol.item.ItemStack stack) {
        return this.asBukkit(stack).map(itemStack -> {
            ItemStack result = this.provider.onSlotRender(player, itemStack);
            return this.fromBukkit(result == null ? new ItemStack(Material.AIR) : result);
        }).orElse(stack);
    }

    @NonNull
    private Optional<ItemStack> asBukkit(com.github.retrooper.packetevents.protocol.item.@Nullable ItemStack pooperStack) {
        return Optional.ofNullable(pooperStack).map(SpigotConversionUtil::toBukkitItemStack);
    }

    private com.github.retrooper.packetevents.protocol.item.@NonNull ItemStack fromBukkit(@NonNull ItemStack itemStack) {
        return SpigotConversionUtil.fromBukkitItemStack(itemStack);
    }
}
