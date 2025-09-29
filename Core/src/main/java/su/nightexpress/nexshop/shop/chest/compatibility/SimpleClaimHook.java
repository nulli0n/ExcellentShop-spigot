package su.nightexpress.nexshop.shop.chest.compatibility;

import fr.xyness.SCS.API.SimpleClaimSystemAPI;
import fr.xyness.SCS.API.SimpleClaimSystemAPI_Provider;
import fr.xyness.SCS.Claim;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SimpleClaimHook implements ClaimHook {

    private final SimpleClaimSystemAPI api = SimpleClaimSystemAPI_Provider.getAPI();

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        Claim claim = api.getClaimAtChunk(player.getChunk());
        return claim != null && claim.getOwner().equalsIgnoreCase(player.getName());
    }
}
