package su.nightexpress.nexshop.shop.chest.compatibility;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GriefDefenderHook implements ClaimHook {

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        Claim claim = GriefDefender.getCore().getClaimAt(block.getLocation());
        if (claim == null || claim.isWilderness()) return false;

        UUID playerId = player.getUniqueId();

        return claim.getOwnerUniqueId() == playerId || claim.isUserTrusted(playerId, TrustTypes.BUILDER);
    }
}
