package su.nightexpress.nexshop.shop.chest.compatibility;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
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
        return this.griefPrevention.allowBuild(player, block.getLocation()) == null;
    }

}
