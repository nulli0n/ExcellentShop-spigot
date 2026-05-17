package su.nightexpress.excellentshop.integration.claim;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentclaims.ClaimsAPI;
import su.nightexpress.excellentclaims.api.claim.Claim;
import su.nightexpress.excellentshop.api.claim.ClaimHook;

public class ExcellentClaimsHook implements ClaimHook {

    @Override
    public boolean isInOwnClaim(@NonNull Player player, @NonNull Block block) {
        Claim claim = ClaimsAPI.getClaimManager().getPrioritizedClaim(player.getLocation());
        return claim != null && claim.isOwner(player);
    }
}
