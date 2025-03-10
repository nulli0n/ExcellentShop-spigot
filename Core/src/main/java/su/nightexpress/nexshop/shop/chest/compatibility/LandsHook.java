package su.nightexpress.nexshop.shop.chest.compatibility;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nightcore.util.geodata.ChunkPos;

import java.util.UUID;

public class LandsHook implements ClaimHook {

    private final LandsIntegration lands;

    public LandsHook(@NotNull ShopPlugin plugin) {
        this.lands = LandsIntegration.of(plugin);
    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        ChunkPos chunkPos = ChunkPos.from(block);
        Land land = lands.getLandByChunk(block.getWorld(), chunkPos.getX(), chunkPos.getZ());
        UUID id = player.getUniqueId();
        return land != null && (land.getOwnerUID().equals(id) || land.getTrustedPlayers().contains(id));
    }
}
