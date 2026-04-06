package su.nightexpress.excellentshop.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public interface SellingMenuProvider {

    boolean isViewer(@NonNull Player player);

    boolean isImmuneSlot(@NonNull Player player, int slot);

    boolean isProductSlot(int slot);

    @Nullable ItemStack onSlotRender(@NonNull Player player, @NonNull ItemStack itemStack);
}
