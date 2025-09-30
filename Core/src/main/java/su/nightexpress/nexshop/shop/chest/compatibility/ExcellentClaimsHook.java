package su.nightexpress.nexshop.shop.chest.compatibility;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentclaims.ClaimsAPI;
import su.nightexpress.excellentclaims.api.claim.Claim;

public class ExcellentClaimsHook implements ClaimHook {

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        Claim claim = ClaimsAPI.getClaimManager().getPrioritizedClaim(player.getLocation());
        return claim != null && claim.isOwner(player);
    }
}
