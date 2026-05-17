package su.nightexpress.excellentshop.api.playershop;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.geodata.Cuboid;

import java.util.Set;

public interface PlayerShopManager {

    boolean isShop(@NonNull Block block);

    void removeShop(@NonNull PlayerShop shop);

    @NonNull
    Set<? extends PlayerShop> getShopsInArea(@NonNull World world, @NonNull Cuboid cuboid);
}
