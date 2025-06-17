package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.nightcore.util.geodata.pos.ChunkPos;

public class ShopLocation {

    private final World    world;
    private final BlockPos blockPos;
    private final ChunkPos chunkPos;
    //private final Material blockType;

    public ShopLocation(@NotNull World world, @NotNull BlockPos blockPos) {
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

    @NotNull
    public World getWorld() {
        return this.world;
    }

    @NotNull
    public Location toLocation(@NotNull BlockPos blockPos) {
        return blockPos.toLocation(this.world);
    }

    @NotNull
    public Location getLocation() {
        return this.toLocation(this.blockPos);
    }

    @NotNull
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

    @NotNull
    public Block getBlock() {
        return this.blockPos.toBlock(this.world);
    }

    @NotNull
    public Material getBlockType() {
        return this.getBlock().getType();
    }
}
