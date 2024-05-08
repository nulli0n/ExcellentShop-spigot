package su.nightexpress.nexshop.shop.chest.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Container;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class ShopMap {

    private final Map<String, ChestShop>                byId;
    private final Map<String, Map<BlockPos, ChestShop>> byPosition;
    private final Map<UUID, Set<ChestShop>>             byHolder;
    private final Map<String, Set<ChestShop>>           byName;

    public ShopMap() {
        this.byId = new HashMap<>();
        this.byPosition = new HashMap<>();
        this.byHolder = new HashMap<>();
        this.byName = new HashMap<>();
    }

    public void clear() {
        this.byId.clear();
        this.byPosition.clear();
        this.byHolder.clear();
        this.byName.clear();
    }

    @NotNull
    public Collection<ChestShop> getAll() {
        return this.byId.values();
    }

    @NotNull
    public Collection<ChestShop> getActive() {
        return this.byId.values().stream().filter(ChestShop::isActive).collect(Collectors.toSet());
    }

    @NotNull
    public Set<ChestShop> getByOwner(@NotNull UUID playerId) {
        return this.ofPlayerId(playerId).stream().filter(ChestShop::isActive).collect(Collectors.toSet());
    }

    @NotNull
    public Set<ChestShop> getByOwner(@NotNull String playerName) {
        return this.ofPlayerName(playerName).stream().filter(ChestShop::isActive).collect(Collectors.toSet());
    }

    @Nullable
    public ChestShop getById(@NotNull String id) {
        return this.byId.get(id.toLowerCase());
    }

    @Nullable
    public ChestShop getByLocation(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) return null;

        return this.getByWorldPos(world, BlockPos.from(location));
    }

    @Nullable
    public ChestShop getByWorldPos(@NotNull World world, @NotNull BlockPos pos) {
        return this.ofWorld(world).get(pos);
    }

    @NotNull
    public Map<BlockPos, ChestShop> ofWorld(@NotNull World world) {
        return this.ofWorld(world.getName());
    }

    @NotNull
    public Map<BlockPos, ChestShop> ofWorld(@NotNull String worldName) {
        return this.byPosition.computeIfAbsent(worldName.toLowerCase(), k -> new HashMap<>());
    }

    @NotNull
    private Set<ChestShop> ofPlayerId(@NotNull UUID holderId) {
        return this.byHolder.computeIfAbsent(holderId, k -> new HashSet<>());
    }

    @NotNull
    private Set<ChestShop> ofPlayerName(@NotNull String playerName) {
        return this.byName.computeIfAbsent(playerName.toLowerCase(), k -> new HashSet<>());
    }

    public void put(@NotNull ChestShop shop) {
        this.byId.put(shop.getId(), shop);

        this.ofPlayerId(shop.getOwnerId()).add(shop);
        this.ofPlayerName(shop.getOwnerName()).add(shop);
    }

    public void updatePositionCache(@NotNull ChestShop shop) {
        String worldName = shop.getWorldName();
        this.ofWorld(worldName).values().removeIf(has -> has == shop); // Remove all cached shop positions.

        if (shop.isActive()) {
            // Put either single shop position or both ones for double chests.
            Pair<Container, Container> sides = shop.getSides();
            this.ofWorld(worldName).put(BlockPos.from(sides.getFirst().getLocation()), shop);
            this.ofWorld(worldName).put(BlockPos.from(sides.getSecond().getLocation()), shop);
        }
    }

    public void remove(@NotNull ChestShop shop) {
        this.byId.remove(shop.getId());
        this.ofPlayerId(shop.getOwnerId()).remove(shop);
        this.ofPlayerName(shop.getOwnerName()).remove(shop);
        this.ofWorld(shop.getWorldName()).values().removeIf(has -> has == shop); // Remove all cached shop positions.
    }
}
