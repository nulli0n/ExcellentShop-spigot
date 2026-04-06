package su.nightexpress.excellentshop.integration.protocollib.adapter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.menu.SellingMenuAdapter;
import su.nightexpress.excellentshop.api.menu.SellingMenuProvider;
import su.nightexpress.excellentshop.integration.protocollib.ProtocolLibPacketReceiver;

import java.util.List;

public class ProtocolLibSellingMenuAdapter extends ProtocolLibPacketReceiver implements SellingMenuAdapter {

    private static final PacketType[] PACKET_TYPES = {
        PacketType.Play.Server.SET_SLOT,
        PacketType.Play.Server.WINDOW_ITEMS,
        PacketType.Play.Server.SET_PLAYER_INVENTORY,
        PacketType.Play.Server.SET_CURSOR_ITEM
    };

    private final SellingMenuProvider provider;

    public ProtocolLibSellingMenuAdapter(@NonNull Plugin plugin, @NonNull SellingMenuProvider provider) {
        super(plugin, PACKET_TYPES);
        this.provider = provider;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        this.handlePacketSending(event);
    }

    private void handlePacketSending(@NonNull PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();

        PacketType type = packet.getType();
        if (type == PacketType.Play.Server.SET_SLOT) {
            int slot = packet.getIntegers().read(2);
            ItemStack itemStack = packet.getItemModifier().read(0);

            if (!this.provider.isImmuneSlot(player, slot)) {
                packet.getItemModifier().write(0, this.render(player, itemStack));
            }
        }
        else if (type == PacketType.Play.Server.SET_PLAYER_INVENTORY) {
            ItemStack itemStack = packet.getItemModifier().read(0);
            packet.getItemModifier().write(0, this.render(player, itemStack));
        }
        else if (type == PacketType.Play.Server.SET_CURSOR_ITEM) {
            ItemStack itemStack = packet.getItemModifier().read(0);
            packet.getItemModifier().write(0, this.render(player, itemStack));
        }
        else if (type == PacketType.Play.Server.WINDOW_ITEMS) {
            List<ItemStack> items = packet.getItemListModifier().readSafely(0);
            for (int index = 0; index < items.size(); index++) {
                if (!this.provider.isProductSlot(index)) continue;

                items.set(index, this.render(player, items.get(index)));
            }

            packet.getItemListModifier().write(0, items);
        }
    }

    @NonNull
    private ItemStack render(@NonNull Player player, ItemStack itemStack) {
        ItemStack result = this.provider.onSlotRender(player, itemStack);
        return result == null ? new ItemStack(Material.AIR) : result;
    }

    @Override
    public void callPlayerInventoryPacket(@NonNull Player player, int slot, @NonNull ItemStack itemStack) {
        PacketContainer setPlayerSlotPacket = new PacketContainer(PacketType.Play.Server.SET_PLAYER_INVENTORY);
        setPlayerSlotPacket.getIntegers().write(0, slot);
        setPlayerSlotPacket.getItemModifier().write(0, itemStack);

        ProtocolLibrary.getProtocolManager().sendServerPacket(player, setPlayerSlotPacket);
    }
}
