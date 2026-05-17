package su.nightexpress.excellentshop.api.transaction;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentshop.api.BalanceHolder;
import su.nightexpress.excellentshop.api.UnitUtils;
import su.nightexpress.excellentshop.api.product.TradeType;

/**
 * A completed transaction that describes the final result of a trade operation.
 *
 * @param player        The player who performed the transaction.
 * @param type          The type of trade (BUY or SELL).
 * @param items         The list of items successfully processed.
 * @param looseItems    The list of items that could not be processed.
 * @param userInventory The inventory used during the transaction.
 * @param worth         The total balance exchanged.
 * @param result        The final status of the transaction.
 * @param silent        Whether the transaction was silent.
 */
@NullMarked
public record ECompletedTransaction(Player player,
                                    TradeType type,
                                    List<ETransactionItem> items,
                                    List<ETransactionItem> looseItems,
                                    Inventory userInventory,
                                    BalanceHolder worth,
                                    ETransactionResult result,
                                    boolean silent) {

    /**
     * Checks whether this transaction contains only a single item.
     *
     * @return True if there is exactly one item, false otherwise.
     */
    public boolean isSingleItem() {
        return this.items.size() == 1;
    }

    /**
     * Checks whether this transaction contains multiple (more than 1) items.
     *
     * @return True if there are multiple items, false otherwise.
     */
    public boolean isMutlipleItems() {
        return this.items.size() > 1;
    }

    /**
     * Calculates the total unit amount of all processed items.
     *
     * @return The total sum of units.
     */
    public int countTotalUnits() {
        return this.items.stream().mapToInt(ETransactionItem::units).sum();
    }

    /**
     * Calculates the total item stack amount of all processed items.
     *
     * @return The total sum of item stack amounts.
     */
    public int countTotalAmount() {
        return this.items.stream().mapToInt(item -> UnitUtils.unitsToAmount(item.product(), item.units())).sum();
    }

    /**
     * Checks whether this transaction was successful.
     *
     * @return True if the result is SUCCESS, false otherwise.
     */
    public boolean successful() {
        return this.result == ETransactionResult.SUCCESS;
    }
}
