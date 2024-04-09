package su.nightexpress.nexshop.data.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public class StockData {

    private final TradeType tradeType;
    private final String    shopId;
    private final String    productId;

    private int  itemsLeft;
    private long restockDate;

    public StockData(@NotNull VirtualProduct product, @NotNull StockValues values, @NotNull TradeType tradeType) {
        this(tradeType, product.getShop().getId(), product.getId(), 0, 0);
        this.restock(values);
    }

    public StockData(
        @NotNull TradeType tradeType,
        @NotNull String shopId,
        @NotNull String productId,
        int itemsLeft,
        long restockDate) {
        this.tradeType = tradeType;
        this.shopId = shopId;
        this.productId = productId;
        this.itemsLeft = itemsLeft;
        this.restockDate = restockDate;
    }

    public void restock(@NotNull StockValues values) {
        this.itemsLeft = values.getInitialAmount(this.getTradeType());
        this.restockDate = 0L;
    }

    public void updateRestockDate(@NotNull StockValues values) {
        if (values.isRestockable(this.tradeType)) {
            this.restockDate = values.generateRestockTimestamp(this.tradeType);
        }
        else {
            this.restockDate = -1L;
        }
    }

    public boolean isAwaiting() {
        return this.restockDate == 0L;
    }

    @NotNull
    public TradeType getTradeType() {
        return tradeType;
    }

    @NotNull
    public String getShopId() {
        return this.shopId;
    }

    @NotNull
    public String getProductId() {
        return this.productId;
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

    public boolean isRestockTime() {
        return this.getRestockDate() > 0 && System.currentTimeMillis() > this.getRestockDate();
    }

    @Override
    public String toString() {
        return "StockData{" +
            "tradeType=" + tradeType +
            ", shopId='" + shopId + '\'' +
            ", productId='" + productId + '\'' +
            ", itemsLeft=" + itemsLeft +
            ", restockDate=" + restockDate +
            '}';
    }
}
