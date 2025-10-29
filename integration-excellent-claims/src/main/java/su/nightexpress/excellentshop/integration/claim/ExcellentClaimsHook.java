package su.nightexpress.excellentshop.integration.claim;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentclaims.ClaimsAPI;
import su.nightexpress.excellentclaims.api.claim.Claim;
import su.nightexpress.excellentshop.api.claim.ClaimHook;

public class ExcellentClaimsHook implements ClaimHook {

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        Claim claim = ClaimsAPI.getClaimManager().getPrioritizedClaim(player.getLocation());
        return claim != null && claim.isOwner(player);
    }
}
