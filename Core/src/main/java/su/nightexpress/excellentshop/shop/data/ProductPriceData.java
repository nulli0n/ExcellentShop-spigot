package su.nightexpress.excellentshop.shop.data;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.data.state.StatefulData;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.nightcore.util.TimeUtil;

import java.util.UUID;

public class ProductPriceData extends StatefulData implements PriceData {

    private final UUID productId;

    private double buyOffset;
    private double sellOffset;
    private long   expireDate;
    private int    purchases;
    private int    sales;

    public ProductPriceData(@NonNull UUID productId,
                            double buyOffset,
                            double sellOffset,
                            long expireDate,
                            int purchases,
                            int sales) {
        this.productId = productId;
        this.setBuyOffset(buyOffset);
        this.setSellOffset(sellOffset);
        this.setExpireDate(expireDate);
        this.setPurchases(purchases);
        this.setSales(sales);
    }

    @Override
    public void countTransaction(@NonNull TradeType tradeType, int amount) {
        if (tradeType == TradeType.BUY) {
            this.setPurchases(this.purchases + amount);
        }
        else {
            this.setSales(this.sales + amount);
        }
    }

    @Override
    public double getOffset(@NonNull TradeType type) {
        return switch (type) {
            case BUY -> this.buyOffset;
            case SELL -> this.sellOffset;
        };
    }

    @Override
    public void setOffset(@NonNull TradeType type, double offset) {
        switch (type) {
            case BUY -> this.setBuyOffset(offset);
            case SELL -> this.setSellOffset(offset);
        }
    }

    @Override
    public void reset() {
        this.setBuyOffset(0);
        this.setSellOffset(0);
        this.setPurchases(0);
        this.setSales(0);
        this.setExpired();
    }

    @Override
    public boolean isExpired() {
        return TimeUtil.isPassed(this.expireDate);
    }

    @Override
    public boolean isExpirable() {
        return this.expireDate >= 0L;
    }

    @Override
    public void setExpired() {
        this.setExpireDate(System.currentTimeMillis() - 1000L);
    }

    @NonNull
    public UUID getProductId() {
        return this.productId;
    }

    @Override
    public double getBuyOffset() {
        return this.buyOffset;
    }

    @Override
    public void setBuyOffset(double buyOffset) {
        this.buyOffset = buyOffset;
    }

    @Override
    public double getSellOffset() {
        return this.sellOffset;
    }

    @Override
    public void setSellOffset(double sellOffset) {
        this.sellOffset = sellOffset;
    }

    @Override
    public long getExpireDate() {
        return this.expireDate;
    }

    @Override
    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }

    @Override
    public int getPurchases() {
        return this.purchases;
    }

    @Override
    public void setPurchases(int purchases) {
        this.purchases = Math.max(0, purchases);
    }

    @Override
    public int getSales() {
        return this.sales;
    }

    @Override
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
