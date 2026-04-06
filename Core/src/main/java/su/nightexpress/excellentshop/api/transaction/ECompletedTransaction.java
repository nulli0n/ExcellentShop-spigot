package su.nightexpress.excellentshop.api.transaction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.nexshop.util.BalanceHolder;
import su.nightexpress.nexshop.util.UnitUtils;

import java.util.List;

public record ECompletedTransaction(
    @NonNull Player player,
    @NonNull TradeType type,
    @NonNull List<ETransactionItem> items,
    @NonNull List<ETransactionItem> looseItems,
    @NonNull Inventory userInventory,
    @NonNull BalanceHolder worth,
    @NonNull ETransactionResult result,
    boolean silent
) {

    @NonNull
    public static ECompletedTransaction create(@NonNull EPreparedTransaction origin, @NonNull ETransactionResult result, @NonNull BalanceHolder worth) {
        return new ECompletedTransaction(
            origin.getPlayer(),
            origin.getType(),
            List.copyOf(origin.getItemsList()),
            List.copyOf(origin.getLooseItems()),
            origin.getUserInventory(),
            worth,
            result,
            origin.isSilent()
        );
    }

    public boolean isSingleItem() {
        return this.items.size() == 1;
    }

    public boolean isMutlipleItems() {
        return this.items.size() > 1;
    }

    public int countTotalUnits() {
        return this.items.stream().mapToInt(ETransactionItem::units).sum();
    }

    public int countTotalAmount() {
        return this.items.stream().mapToInt(quantified -> UnitUtils.unitsToAmount(quantified.product(), quantified.units())).sum();
    }

    public boolean successful() {
        return this.result == ETransactionResult.SUCCESS;
    }
}
