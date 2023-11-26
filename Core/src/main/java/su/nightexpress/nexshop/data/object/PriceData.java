package su.nightexpress.nexshop.data.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public class PriceData {

    private final String shopId;
    private final String productId;

    private double lastBuyPrice;
    private double lastSellPrice;
    private long   lastUpdated;
    private int    purchases;
    private int    sales;

    public PriceData(@NotNull Product product) {
        this(
            product.getShop().getId(),
            product.getId(),
            product.getPricer().getBuyPrice(),
            product.getPricer().getSellPrice(),
            System.currentTimeMillis(),
            0, 0
        );
    }

    public PriceData(@NotNull String shopId, @NotNull String productId,
                     double lastBuyPrice, double lastSellPrice, long lastUpdated,
                     int purchases, int sales) {
        this.shopId = shopId.toLowerCase();
        this.productId = productId.toLowerCase();
        this.setLastBuyPrice(lastBuyPrice);
        this.setLastSellPrice(lastSellPrice);
        this.setLastUpdated(lastUpdated);
        this.setPurchases(purchases);
        this.setSales(sales);
    }

    public void countTransaction(@NotNull TradeType tradeType, int amount) {
        if (tradeType == TradeType.BUY) {
            this.setPurchases(this.getPurchases() + amount);
        }
        else {
            this.setSales(this.getSales() + amount);
        }
    }

    @NotNull
    public String getShopId() {
        return shopId;
    }

    @NotNull
    public String getProductId() {
        return productId;
    }

    public double getLastBuyPrice() {
        return lastBuyPrice;
    }

    public void setLastBuyPrice(double lastBuyPrice) {
        this.lastBuyPrice = lastBuyPrice;
    }

    public double getLastSellPrice() {
        return lastSellPrice;
    }

    public void setLastSellPrice(double lastSellPrice) {
        this.lastSellPrice = lastSellPrice;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getPurchases() {
        return purchases;
    }

    public void setPurchases(int purchases) {
        this.purchases = purchases;
    }

    public int getSales() {
        return sales;
    }

    public void setSales(int sales) {
        this.sales = sales;
    }
}
