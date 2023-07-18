package su.nightexpress.nexshop.shop.chest.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.*;

public class ShopMap {

    private final Map<Location, ChestShop> byLocation;
    private final Map<UUID, Set<ChestShop>> byHolder;

    public ShopMap() {
        this.byLocation = new HashMap<>();
        this.byHolder = new HashMap<>();
    }

    public void clear() {
        this.byLocation.clear();
        this.byHolder.clear();
    }

    @Nullable
    public ChestShop get(@NotNull Location location) {
        return this.byLocation.get(location);
    }

    @NotNull
    public Collection<ChestShop> getAll() {
        return this.byLocation.values();
    }

    @NotNull
    public Set<ChestShop> get(@NotNull UUID holderId) {
        return this.byHolder.computeIfAbsent(holderId, k -> new HashSet<>());
    }

    public void put(@NotNull ChestShop shop) {
        var sides = shop.getSides();
        this.byLocation.put(sides.getFirst().getLocation(), shop);
        this.byLocation.put(sides.getSecond().getLocation(), shop);
        this.get(shop.getOwnerId()).add(shop);
    }

    public void remove(@NotNull ChestShop shop) {
        var sides = shop.getSides();
        this.remove(sides.getFirst().getLocation());
        this.remove(sides.getSecond().getLocation());
    }

    public void remove(@NotNull Location location) {
        ChestShop shop = this.byLocation.remove(location);
        if (shop != null) this.get(shop.getOwnerId()).remove(shop);
    }
}
