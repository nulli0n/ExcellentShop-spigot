package su.nightexpress.excellentshop.data.legacy;

import org.jspecify.annotations.NonNull;

public class LegacyStockData {

    private final String shopId;
    private final String productId;
    private final String holder;

    private final int  buyStock;
    private final int  sellStock;
    private final long restockDate;

    private boolean isRemoved;

    public LegacyStockData(@NonNull String shopId,
                           @NonNull String productId,
                           @NonNull String holder,
                           int buyStock,
                           int sellStock,
                           long restockDate) {
        this.shopId = shopId.toLowerCase();
        this.productId = productId.toLowerCase();
        this.holder = holder.toLowerCase();
        this.buyStock = buyStock;
        this.sellStock = sellStock;
        this.restockDate = restockDate;
    }

    @NonNull
    public String getShopId() {
        return this.shopId;
    }

    @NonNull
    public String getProductId() {
        return this.productId;
    }

    @NonNull
    public String getHolder() {
        return this.holder;
    }

    public int getBuyStock() {
        return this.buyStock;
    }

    public int getSellStock() {
        return this.sellStock;
    }

    public long getRestockDate() {
        return this.restockDate;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }
}
