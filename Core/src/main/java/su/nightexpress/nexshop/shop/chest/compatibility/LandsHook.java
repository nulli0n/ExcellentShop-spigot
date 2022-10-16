package su.nightexpress.nexshop.shop.chest.compatibility;

import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.hook.AbstractHook;
import su.nightexpress.nexshop.ExcellentShop;

import java.util.UUID;

public class LandsHook extends AbstractHook<ExcellentShop> implements ClaimHook {

    private LandsIntegration lands;

    public LandsHook(@NotNull ExcellentShop plugin, @NotNull String pluginName) {
        super(plugin, pluginName);
    }

    @Override
    public boolean setup() {
        this.lands = new LandsIntegration(this.plugin);
        return true;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        Land land = lands.getLand(block.getLocation());
        UUID id = player.getUniqueId();
        return land != null && (land.getOwnerUID().equals(id) || land.getTrustedPlayers().contains(id));
    }
}
