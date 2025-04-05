package su.nightexpress.nexshop.shop.chest.compatibility;

import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HuskClaimsHook implements ClaimHook {

    private final BukkitHuskClaimsAPI huskClaimsAPI;

    public HuskClaimsHook() {
        this.huskClaimsAPI = BukkitHuskClaimsAPI.getInstance();
    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        Claim claim = huskClaimsAPI.getClaimAt(huskClaimsAPI.getPosition(block.getLocation())).orElse(null);
        if (claim == null) return false;

        UUID ownerId = claim.getOwner().orElse(null);
        return ownerId != null && ownerId.equals(player.getUniqueId());
    }
}
