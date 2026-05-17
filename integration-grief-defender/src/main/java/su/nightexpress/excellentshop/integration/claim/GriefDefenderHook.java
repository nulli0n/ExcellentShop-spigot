package su.nightexpress.excellentshop.integration.claim;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.claim.ClaimHook;

import java.util.UUID;

public class GriefDefenderHook implements ClaimHook {

    @Override
    public boolean isInOwnClaim(@NonNull Player player, @NonNull Block block) {
        Claim claim = GriefDefender.getCore().getClaimAt(block.getLocation());
        if (claim == null || claim.isWilderness()) return false;

        UUID playerId = player.getUniqueId();

        return claim.getOwnerUniqueId() == playerId || claim.isUserTrusted(playerId, TrustTypes.BUILDER);
    }
}
