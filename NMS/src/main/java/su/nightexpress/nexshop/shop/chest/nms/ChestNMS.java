package su.nightexpress.nexshop.shop.chest.nms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ChestNMS {

    ItemStack UNKNOWN = new ItemStack(Material.BARRIER);

    int createHologram(@NotNull Location location, @NotNull ItemStack showcase, @NotNull String text);

    int createItem(@NotNull Location location, @NotNull ItemStack product);

    void deleteEntity(int... ids);
}
