package su.nightexpress.nexshop.shop.chest.compatibility;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GriefPreventionHook implements ClaimHook {

    private final GriefPrevention griefPrevention;

    public GriefPreventionHook() {
        this.griefPrevention = GriefPrevention.instance;
    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        PlayerData playerData = this.griefPrevention.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = this.griefPrevention.dataStore.getClaimAt(block.getLocation(), true, playerData.lastClaim);

        return claim != null && claim.getOwnerID().equals(player.getUniqueId());
    }

}
