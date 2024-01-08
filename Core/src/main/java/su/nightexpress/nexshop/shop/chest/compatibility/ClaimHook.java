package su.nightexpress.nexshop.shop.chest.compatibility;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ClaimHook {

    boolean isInOwnClaim(@NotNull Player player, @NotNull Block block);
}
