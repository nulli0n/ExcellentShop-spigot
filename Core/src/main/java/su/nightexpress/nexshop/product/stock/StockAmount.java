package su.nightexpress.nexshop.product.stock;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public class StockAmount {

    private int  itemsLeft;
    private long restockDate;

    public StockAmount() {
        this(0, System.currentTimeMillis());
    }

    public StockAmount(int itemsLeft, long restockDate) {
        this.itemsLeft = itemsLeft;
        this.restockDate = restockDate;
    }

    public boolean isAwaiting() {
        return this.restockDate == 0L;
    }

    public void restock(@NotNull StockValues values, @NotNull TradeType type) {
        this.itemsLeft = values.getInitialAmount(type);
        this.restockDate = 0L;
    }

    public void updateRestockDate(@NotNull StockValues values, @NotNull TradeType type) {
        if (values.isRestockable(type)) {
            this.restockDate = values.generateRestockTimestamp(type);
        }
        else {
            this.restockDate = -1L;
        }
    }

    public int getItemsLeft() {
        return this.itemsLeft;
    }

    public void setItemsLeft(int itemsLeft) {
        this.itemsLeft = Math.max(0, itemsLeft);
    }

    public long getRestockDate() {
        return this.restockDate;
    }

    public void setRestockDate(long restockDate) {
        this.restockDate = restockDate;
    }

    public boolean isRestockTime() {
        return this.getRestockDate() > 0 && System.currentTimeMillis() >= this.getRestockDate();
    }
}
