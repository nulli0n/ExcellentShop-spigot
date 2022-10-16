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

    // Very cool fix for shop display entities.
    // I have no idea why there must be a real player in a chunk to spawn them proreply,
    // but this works so who cares xD
    /*default boolean isSafeCreation(@NotNull Location loc) {
        return Arrays.stream(loc.getChunk().getEntities()).anyMatch(e -> e.getType() == EntityType.PLAYER && !this.isNPC(e));
    }

    default boolean isNPC(@NotNull Entity entity) {
        if (Bukkit.getServer().getPluginManager().getPlugin("Citizens") == null) return false;
        return CitizensAPI.getNPCRegistry().isNPC(entity);
    }*/
}
