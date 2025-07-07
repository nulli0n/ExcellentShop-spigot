package su.nightexpress.nexshop.data.product;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.util.TimeUtil;

public class PriceData extends AbstractData {

    private double latestBuyPrice;
    private double latestSellPrice;
    private long   latestUpdateDate;
    private long   expireDate;
    private int    purchases;
    private int    sales;

    @NotNull
    public static PriceData create(@NotNull Product product) {
        String shopId = product.getShop().getId();
        String productId = product.getId();
        double buyPrice = product.getPricer().getBuyPrice();
        double sellPrice = product.getPricer().getSellPrice();
        long latestUpdated = System.currentTimeMillis();
        long expireDate = 0L; // Will trigger isExpired to update values.
        int purchases = 0;
        int sales = 0;

        return new PriceData(shopId, productId, buyPrice, sellPrice, latestUpdated, expireDate, purchases, sales);
    }

    public PriceData(@NotNull String shopId,
                     @NotNull String productId,
                     double latestBuyPrice,
                     double latestSellPrice,
                     long latestUpdateDate,
                     long expireDate,
                     int purchases,
                     int sales) {
        super(shopId, productId);
        this.setLatestBuyPrice(latestBuyPrice);
        this.setLatestSellPrice(latestSellPrice);
        this.setLatestUpdateDate(latestUpdateDate);
        this.setExpireDate(expireDate);
        this.setPurchases(purchases);
        this.setSales(sales);
    }

    public void countTransaction(@NotNull TradeType tradeType, int amount) {
        if (tradeType == TradeType.BUY) {
            this.setPurchases(this.purchases + amount);
        }
        else {
            this.setSales(this.sales + amount);
        }
    }

    public void reset() {
        this.setPurchases(0);
        this.setSales(0);
        this.setExpired();
    }

    public boolean isExpired() {
        return TimeUtil.isPassed(this.expireDate);
    }

    public void setExpired() {
        this.setExpireDate(0L);
    }

    public double getLatestBuyPrice() {
        return this.latestBuyPrice;
    }

    public void setLatestBuyPrice(double latestBuyPrice) {
        this.latestBuyPrice = latestBuyPrice;
    }

    public double getLatestSellPrice() {
        return this.latestSellPrice;
    }

    public void setLatestSellPrice(double latestSellPrice) {
        this.latestSellPrice = latestSellPrice;
    }

    public long getLatestUpdateDate() {
        return this.latestUpdateDate;
    }

    public void setLatestUpdateDate(long latestUpdateDate) {
        this.latestUpdateDate = latestUpdateDate;
    }

    public long getExpireDate() {
        return this.expireDate;
    }

    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }

    public int getPurchases() {
        return this.purchases;
    }

    public void setPurchases(int purchases) {
        this.purchases = Math.max(0, purchases);
    }

    public int getSales() {
        return this.sales;
    }

    public void setSales(int sales) {
        this.sales = Math.max(0, sales);
    }

    @Override
    public String toString() {
        return "PriceData{" +
            "latestBuyPrice=" + latestBuyPrice +
            ", latestSellPrice=" + latestSellPrice +
            ", latestUpdateDate=" + latestUpdateDate +
            ", expireDate=" + expireDate +
            ", purchases=" + purchases +
            ", sales=" + sales +
            '}';
    }
}
