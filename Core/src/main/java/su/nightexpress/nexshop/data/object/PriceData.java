package su.nightexpress.nexshop.data.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.impl.price.FloatPricer;

public class PriceData {

    private final String shopId;
    private final String productId;

    private double lastBuyPrice;
    private double lastSellPrice;
    private long   lastUpdated;
    private long   expireDate;
    private int    purchases;
    private int    sales;

    public PriceData(@NotNull Product product) {
        this(
            product.getShop().getId(),
            product.getId(),
            product.getPricer().getBuyPrice(),
            product.getPricer().getSellPrice(),
            System.currentTimeMillis(),
            generateExpireDate(product),
            0,
            0
        );
    }

    public PriceData(@NotNull String shopId,
                     @NotNull String productId,
                     double lastBuyPrice,
                     double lastSellPrice,
                     long lastUpdated,
                     long expireDate,
                     int purchases,
                     int sales) {
        this.shopId = shopId.toLowerCase();
        this.productId = productId.toLowerCase();
        this.setLastBuyPrice(lastBuyPrice);
        this.setLastSellPrice(lastSellPrice);
        this.setLastUpdated(lastUpdated);
        this.setExpireDate(expireDate);
        this.setPurchases(purchases);
        this.setSales(sales);
    }

    private static long generateExpireDate(@NotNull Product product) {
        if (product.getPricer() instanceof FloatPricer pricer) {
            return pricer.getClosestTimestamp();
        }
        return -1L;
    }

    public void countTransaction(@NotNull TradeType tradeType, int amount) {
        if (tradeType == TradeType.BUY) {
            this.setPurchases(this.getPurchases() + amount);
        }
        else {
            this.setSales(this.getSales() + amount);
        }
    }

    public boolean isExpired() {
        return this.expireDate >= 0 && System.currentTimeMillis() > this.expireDate;
    }

    public void expire() {
        this.setExpireDate(0L);
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

    public long getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
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
