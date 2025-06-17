package su.nightexpress.nexshop.shop.chest.lookup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.nightcore.util.geodata.pos.ChunkPos;

import java.util.*;

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

    @NotNull
    public Set<ChestShop> getAll() {
        return new HashSet<>(this.byBlockPos.values());
    }

    @Nullable
    public ChestShop getByBlockPos(@NotNull BlockPos blockPos) {
        return this.byBlockPos.get(blockPos);
    }

    @NotNull
    public Set<ChestShop> getByChunkPos(@NotNull ChunkPos chunkPos) {
        return this.byChunkPos.getOrDefault(chunkPos, Collections.emptySet());
    }

    public void add(@NotNull ChestShop shop) {
        BlockPos blockPos = shop.getBlockPos();
        ChunkPos chunkPos = blockPos.toChunkPos();

        this.byBlockPos.put(blockPos, shop);
        this.byChunkPos.computeIfAbsent(chunkPos, k -> new HashSet<>()).add(shop);
    }

    public void remove(@NotNull ChestShop shop) {
        BlockPos blockPos = shop.getBlockPos();
        ChunkPos chunkPos = blockPos.toChunkPos();

        this.byBlockPos.remove(blockPos);
        ShopLookup.removeFrom(this.byChunkPos, chunkPos, shop);
    }
}
