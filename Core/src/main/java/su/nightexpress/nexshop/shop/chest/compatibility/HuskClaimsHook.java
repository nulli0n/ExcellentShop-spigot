package su.nightexpress.nexshop.shop.chest.compatibility;

import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.libraries.cloplib.operation.OperationType;
import net.william278.huskclaims.position.Position;
import net.william278.huskclaims.user.OnlineUser;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HuskClaimsHook implements ClaimHook {
    private final BukkitHuskClaimsAPI huskClaimsAPI;

    public HuskClaimsHook() {
        this.huskClaimsAPI = BukkitHuskClaimsAPI.getInstance();
    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        OnlineUser user = huskClaimsAPI.getOnlineUser(player.getUniqueId());
        Optional<Claim> claimOptional = huskClaimsAPI.getClaimAt(user);
        if (claimOptional.isEmpty()) {
            return false;
        }

        Position position = huskClaimsAPI.getPosition(block.getLocation());
        return huskClaimsAPI.isOperationAllowed(user, OperationType.BLOCK_PLACE, position);
    }
}
