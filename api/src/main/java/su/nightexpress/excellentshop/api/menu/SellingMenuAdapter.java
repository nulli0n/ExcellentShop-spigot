package su.nightexpress.excellentshop.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.packet.PacketReceiver;

public interface SellingMenuAdapter extends PacketReceiver {

    void callPlayerInventoryPacket(@NonNull Player player, int slot, @NonNull ItemStack itemStack);
}
