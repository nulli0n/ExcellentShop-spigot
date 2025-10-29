package su.nightexpress.excellentshop.integration.claim;

import fr.xyness.SCS.API.SimpleClaimSystemAPI;
import fr.xyness.SCS.API.SimpleClaimSystemAPI_Provider;
import fr.xyness.SCS.Claim;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.api.claim.ClaimHook;

public class SimpleClaimHook implements ClaimHook {

    private final SimpleClaimSystemAPI api = SimpleClaimSystemAPI_Provider.getAPI();

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        Claim claim = api.getClaimAtChunk(player.getChunk());
        return claim != null && claim.getOwner().equalsIgnoreCase(player.getName());
    }
}
