package su.nightexpress.excellentshop.feature.playershop.repository;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestShop;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.nightcore.util.geodata.pos.ChunkPos;

import java.util.*;

public class ShopLookup {

    private final Map<String, ChestShop>      byId;
    private final Map<UUID, Set<ChestShop>>   byOwnerId;
    private final Map<String, Set<ChestShop>> byOwnerName;
    private final Map<String, WorldLookup>    byWorld;

    public ShopLookup() {
        this.byId = new HashMap<>();
        this.byWorld = new HashMap<>();
        this.byOwnerId = new HashMap<>();
        this.byOwnerName = new HashMap<>();
    }

    public void clear() {
        this.byId.clear();
        this.byWorld.clear();
        this.byOwnerId.clear();
        this.byOwnerName.clear();
    }

    public int countShops() {
        return this.getAll().size();
    }

    @NonNull
    public Optional<WorldLookup> worldLookup(@NonNull World world) {
        return this.worldLookup(world.getName());
    }

    @NonNull
    public Optional<WorldLookup> worldLookup(@NonNull String worldName) {
        return Optional.ofNullable(this.byWorld.get(worldName));
    }

    @NonNull
    public Set<ChestShop> getAll() {
        return new HashSet<>(this.byId.values());
    }

    @NonNull
    public Set<ChestShop> getAll(@NonNull World world) {
        return this.worldLookup(world).map(WorldLookup::getAll).orElse(Collections.emptySet());
    }

    @NonNull
    public Set<ChestShop> getAll(@NonNull Chunk chunk) {
        return this.getAll(chunk.getWorld(), ChunkPos.from(chunk));
    }

    @NonNull
    public Set<ChestShop> getAll(@NonNull World world, @NonNull ChunkPos chunkPos) {
        return this.worldLookup(world).map(worldLookup -> worldLookup.getByChunkPos(chunkPos)).orElse(Collections.emptySet());
    }

    @NonNull
    public Set<ChestShop> getOwnedBy(@NonNull UUID playerId) {
        return new HashSet<>(this.byOwnerId.getOrDefault(playerId, Collections.emptySet()));
    }

    @NonNull
    public Set<ChestShop> getOwnedBy(@NonNull String playerName) {
        return new HashSet<>(this.byOwnerName.getOrDefault(playerName.toLowerCase(), Collections.emptySet()));
    }

    @Nullable
    public ChestShop getById(@NonNull String id) {
        return this.byId.get(id.toLowerCase());
    }

    @Nullable
    public ChestShop getAt(@NonNull Block block) {
        return this.getAt(block.getWorld(), BlockPos.from(block));
    }

    @Nullable
    public ChestShop getAt(@NonNull Location location) {
        World world = location.getWorld();
        if (world == null) return null;

        return this.getAt(world, BlockPos.from(location));
    }

    @Nullable
    public ChestShop getAt(@NonNull World world, @NonNull BlockPos pos) {
        return this.worldLookup(world).map(worldLookup -> worldLookup.getByBlockPos(pos)).orElse(null);
    }

    public void put(@NonNull ChestShop shop) {
        this.byId.put(shop.getId(), shop);

        this.byOwnerId.computeIfAbsent(shop.getOwnerId(), k -> new HashSet<>()).add(shop);
        this.byOwnerName.computeIfAbsent(shop.getOwnerName().toLowerCase(), k -> new HashSet<>()).add(shop);
        this.byWorld.computeIfAbsent(shop.getWorldName(), k -> new WorldLookup()).add(shop);
    }

    public void remove(@NonNull ChestShop shop) {
        this.byId.remove(shop.getId());
        removeFrom(this.byOwnerId, shop.getOwnerId(), shop);
        removeFrom(this.byOwnerName, shop.getOwnerName().toLowerCase(), shop);
        this.worldLookup(shop.getWorldName()).ifPresent(worldLookup -> worldLookup.remove(shop));
    }

    public static <T> void removeFrom(@NonNull Map<T, Set<ChestShop>> map, @NonNull T key, @NonNull ChestShop shop) {
        map.computeIfPresent(key, (presentKey, shops) -> {
            shops.remove(shop);
            return shops.isEmpty() ? null : shops;
        });
    }
}
