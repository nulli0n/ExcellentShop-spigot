package su.nightexpress.excellentshop.data.legacy;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;

public class LegacyPriceData {

    private final String shopId;
    private final String productId;

    private final double buyOffset;
    private final double sellOffset;
    private final long expireDate;
    private final int purchases;
    private final int sales;

    private boolean removed;

    public LegacyPriceData(@NonNull String shopId,
                           @NonNull String productId,
                           double buyOffset,
                           double sellOffset,
                           long expireDate,
                           int purchases,
                           int sales) {
        this.shopId = shopId.toLowerCase();
        this.productId = productId.toLowerCase();
        this.buyOffset = buyOffset;
        this.sellOffset = sellOffset;
        this.expireDate = expireDate;
        this.purchases = purchases;
        this.sales = sales;
    }

    public double getOffset(@NonNull TradeType type) {
        return switch (type) {
            case BUY -> this.buyOffset;
            case SELL -> this.sellOffset;
        };
    }

    @NonNull
    public String getShopId() {
        return this.shopId;
    }

    @NonNull
    public String getProductId() {
        return this.productId;
    }

    public double getBuyOffset() {
        return this.buyOffset;
    }

    public double getSellOffset() {
        return this.sellOffset;
    }

    public long getExpireDate() {
        return this.expireDate;
    }

    public int getPurchases() {
        return this.purchases;
    }

    public int getSales() {
        return this.sales;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
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
