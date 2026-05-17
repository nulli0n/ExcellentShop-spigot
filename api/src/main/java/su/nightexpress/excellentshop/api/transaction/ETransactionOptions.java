package su.nightexpress.excellentshop.api.transaction;

import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;

/**
 * Represents the configuration options for a transaction.
 *
 * @param userInventory The inventory the transaction interacts with.
 * @param preview       If true, the transaction simulates the result without applying changes.
 * @param strict        If true, the transaction fails immediately upon any validation error.
 * @param silent        If true, no notifications are sent to the player regarding the transaction state.
 */
@NullMarked
public record ETransactionOptions(Inventory userInventory, boolean preview, boolean strict, boolean silent) {

}
