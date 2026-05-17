package su.nightexpress.excellentshop.api.claim;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public interface ClaimHook {

    boolean isInOwnClaim(@NonNull Player player, @NonNull Block block);
}
