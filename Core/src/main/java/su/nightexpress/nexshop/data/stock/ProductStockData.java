package su.nightexpress.nexshop.data.stock;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.ProductStock;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;

public class ProductStockData {

    private final TradeType tradeType;
    private final StockType stockType;
    private final String    shopId;
    private final String    productId;

    private int  itemsLeft;
    private long restockDate;

    public ProductStockData(@NotNull ProductStock<?> stock, @NotNull TradeType tradeType, @NotNull StockType stockType) {
        this.tradeType = tradeType;
        this.stockType = stockType;
        this.shopId = stock.getProduct().getShop().getId();
        this.productId = stock.getProduct().getId();
        this.restock(stock);
    }

    public ProductStockData(
        @NotNull TradeType tradeType,
        @NotNull StockType stockType,
        @NotNull String shopId,
        @NotNull String productId,
        int itemsLeft,
        long restockDate) {
        this.tradeType = tradeType;
        this.stockType = stockType;
        this.shopId = shopId;
        this.productId = productId;
        this.itemsLeft = itemsLeft;
        this.restockDate = restockDate;
    }

    public void restock(@NotNull ProductStock<?> stock) {
        this.itemsLeft = stock.getInitialAmount(this.getStockType(), this.getTradeType());
        if (stock.isRestockPossible(this.getStockType(), this.getTradeType())) {
            this.restockDate = System.currentTimeMillis() + stock.getRestockCooldown(this.getStockType(), this.getTradeType()) * 1000L;
        }
        else {
            this.restockDate = -1L;
        }
    }

    @NotNull
    public TradeType getTradeType() {
        return tradeType;
    }

    @NotNull
    public StockType getStockType() {
        return stockType;
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
        return this.getRestockDate() >= 0 && System.currentTimeMillis() > this.getRestockDate();
    }
}
