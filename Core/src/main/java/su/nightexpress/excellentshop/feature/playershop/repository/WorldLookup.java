package su.nightexpress.excellentshop.feature.playershop.repository;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestShop;
import su.nightexpress.nightcore.util.geodata.Cuboid;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.nightcore.util.geodata.pos.ChunkPos;

import java.util.*;
import java.util.stream.Collectors;

public class WorldLookup {

    private final Map<BlockPos, ChestShop>      byBlockPos;
    private final Map<ChunkPos, Set<ChestShop>> byChunkPos;

    public WorldLookup() {
        this.byBlockPos = new HashMap<>();
        this.byChunkPos = new HashMap<>();
    }

    public void clear() {
        this.byBlockPos.clear();
        this.byChunkPos.clear();
    }

    @NonNull
    public Set<ChestShop> getAll() {
        return new HashSet<>(this.byBlockPos.values());
    }

    @NonNull
    public Set<ChestShop> getAllIn(@NonNull Cuboid cuboid) {
        return this.byBlockPos.values().stream().filter(shop -> cuboid.contains(shop.getBlockPos())).collect(Collectors.toCollection(HashSet::new));
    }

    @Nullable
    public ChestShop getByBlockPos(@NonNull BlockPos blockPos) {
        return this.byBlockPos.get(blockPos);
    }

    @NonNull
    public Set<ChestShop> getByChunkPos(@NonNull ChunkPos chunkPos) {
        return this.byChunkPos.getOrDefault(chunkPos, Collections.emptySet());
    }

    public void add(@NonNull ChestShop shop) {
        BlockPos blockPos = shop.getBlockPos();
        ChunkPos chunkPos = blockPos.toChunkPos();

        this.byBlockPos.put(blockPos, shop);
        this.byChunkPos.computeIfAbsent(chunkPos, k -> new HashSet<>()).add(shop);
    }

    public void remove(@NonNull ChestShop shop) {
        BlockPos blockPos = shop.getBlockPos();
        ChunkPos chunkPos = blockPos.toChunkPos();

        this.byBlockPos.remove(blockPos);
        ShopLookup.removeFrom(this.byChunkPos, chunkPos, shop);
    }
}
