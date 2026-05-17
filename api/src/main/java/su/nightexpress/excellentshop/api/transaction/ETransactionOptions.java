package su.nightexpress.excellentshop.api.transaction;

import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;

public record ETransactionOptions(@NonNull Inventory userInventory, boolean preview, boolean strict, boolean silent) {

    // TODO Multiplier

}
