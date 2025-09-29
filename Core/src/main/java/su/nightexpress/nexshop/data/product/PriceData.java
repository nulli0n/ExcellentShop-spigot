package su.nightexpress.nexshop.data.product;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.util.TimeUtil;

public class PriceData extends AbstractData {

    private double buyOffset;
    private double sellOffset;
    private long   expireDate;
    private int    purchases;
    private int    sales;

    @NotNull
    public static PriceData create(@NotNull Product product) {
        String shopId = product.getShop().getId();
        String productId = product.getId();

        int purchases = 0;
        int sales = 0;

        PriceData data = new PriceData(shopId, productId, 0D, 0D, 0L, purchases, sales);
        data.setExpired();
        return data;
    }

    public PriceData(@NotNull String shopId,
                     @NotNull String productId,
                     double buyOffset,
                     double sellOffset,
                     long expireDate,
                     int purchases,
                     int sales) {
        super(shopId, productId);
        this.setBuyOffset(buyOffset);
        this.setSellOffset(sellOffset);
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

    public double getOffset(@NotNull TradeType type) {
        return switch (type) {
            case BUY -> this.buyOffset;
            case SELL -> this.sellOffset;
        };
    }

    public void setOffset(@NotNull TradeType type, double offset) {
        switch (type) {
            case BUY -> this.setBuyOffset(offset);
            case SELL -> this.setSellOffset(offset);
        }
    }

    public void reset() {
        this.setBuyOffset(0);
        this.setSellOffset(0);
        this.setPurchases(0);
        this.setSales(0);
        this.setExpired();
    }

    public boolean isExpired() {
        return TimeUtil.isPassed(this.expireDate);
    }

    public void setExpired() {
        this.setExpireDate(System.currentTimeMillis() - 1000L);
    }

    public double getBuyOffset() {
        return this.buyOffset;
    }

    public void setBuyOffset(double buyOffset) {
        this.buyOffset = buyOffset;
    }

    public double getSellOffset() {
        return this.sellOffset;
    }

    public void setSellOffset(double sellOffset) {
        this.sellOffset = sellOffset;
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
            "buyOffset=" + buyOffset +
            ", sellOffset=" + sellOffset +
            ", expireDate=" + expireDate +
            ", purchases=" + purchases +
            ", sales=" + sales +
            '}';
    }
}
