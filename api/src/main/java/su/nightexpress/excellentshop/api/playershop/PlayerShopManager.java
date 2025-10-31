package su.nightexpress.excellentshop.api.playershop;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.geodata.Cuboid;

import java.util.Set;

public interface PlayerShopManager {

    boolean isShop(@NotNull Block block);

    void removeShop(@NotNull PlayerShop shop);

    @NotNull Set<? extends PlayerShop> getShopsInArea(@NotNull World world, @NotNull Cuboid cuboid);
}
