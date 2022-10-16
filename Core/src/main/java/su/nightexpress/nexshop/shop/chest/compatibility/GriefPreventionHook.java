package su.nightexpress.nexshop.shop.chest.compatibility;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.hook.AbstractHook;
import su.nightexpress.nexshop.ExcellentShop;

public class GriefPreventionHook extends AbstractHook<ExcellentShop> implements ClaimHook {

    private GriefPrevention griefPrevention;

    public GriefPreventionHook(@NotNull ExcellentShop plugin, @NotNull String pluginName) {
        super(plugin, pluginName);
    }

    @Override
    public boolean setup() {
        this.griefPrevention = GriefPrevention.instance;
        return this.griefPrevention != null;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        return this.griefPrevention.allowBuild(player, block.getLocation()) == null;
    }

}
