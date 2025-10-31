package su.nightexpress.excellentshop.integration.claim;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.player.KingdomPlayer;
import su.nightexpress.excellentshop.api.claim.ClaimHook;

public class KingdomsHook implements ClaimHook {

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        Land land = Land.getLand(player.getLocation());
        if (land == null || !land.isClaimed()) return false;

        Kingdom kingdom = land.getKingdom();
        KingdomPlayer kingdomPlayer = KingdomPlayer.getKingdomPlayer(player.getUniqueId());
        return kingdomPlayer.getKingdom() == kingdom;
    }
}
