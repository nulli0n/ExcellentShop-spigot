package su.nightexpress.excellentshop.playershop.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.nightcore.util.geodata.pos.ChunkPos;

public class ShopLocation {

    private final World    world;
    private final BlockPos blockPos;
    private final ChunkPos chunkPos;
    //private final Material blockType;

    public ShopLocation(@NonNull World world, @NonNull BlockPos blockPos) {
        this.world = world;
        this.blockPos = blockPos;
        this.chunkPos = ChunkPos.from(blockPos);
        //this.blockType = this.getBlock().getType();
    }

    public boolean isValid() {
        return this.getBlock().getState() instanceof Container;
    }

    public boolean isChunkLoaded() {
        return this.chunkPos.isLoaded(this.world);
    }

    @NonNull
    public World getWorld() {
        return this.world;
    }

    @NonNull
    public Location toLocation(@NonNull BlockPos blockPos) {
        return blockPos.toLocation(this.world);
    }

    @NonNull
    public Location getLocation() {
        return this.toLocation(this.blockPos);
    }

    @NonNull
    public Location getTeleportLocation() {
        Location location = this.getLocation();
        Block block = location.getBlock();
        BlockData data = block.getBlockData();
        if (data instanceof Directional directional) {
            Block opposite = block.getRelative(directional.getFacing()).getLocation().clone().add(0, 0.5, 0).getBlock();
            location = LocationUtil.setCenter3D(opposite.getLocation());
            location.setDirection(directional.getFacing().getOppositeFace().getDirection());
            location.setPitch(35F);
        }

        return location;
    }

    @NonNull
    public Block getBlock() {
        return this.blockPos.toBlock(this.world);
    }

    @NonNull
    public Material getBlockType() {
        return this.getBlock().getType();
    }
}
