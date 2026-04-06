package su.nightexpress.excellentshop.shop.data;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.data.state.StatefulData;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.limit.LimitData;
import su.nightexpress.nightcore.util.TimeUtil;

import java.util.UUID;

public class ProductLimitData extends StatefulData implements LimitData {

    private final UUID playerId;
    private final UUID productId;

    private int purchases;
    private int sales;
    private long restockDate;

    public ProductLimitData(@NonNull UUID playerId, @NonNull UUID productId, int purchases, int sales, long restockDate) {
        this.playerId = playerId;
        this.productId = productId;
        this.setPurchases(purchases);
        this.setSales(sales);
        this.setRestockDate(restockDate);
    }

    @Override
    public void reset() {
        this.setPurchases(0);
        this.setSales(0);
        this.setRestockDate(-1L);
    }

    @Override
    public boolean isRestockTime() {
        return TimeUtil.isPassed(this.restockDate);
    }

    @Override
    public boolean isActive() {
        return this.purchases > 0 || this.sales > 0;
    }

    @Override
    public void addPurchases(int amount) {
        this.setPurchases(this.purchases + amount);
    }

    @Override
    public void addSales(int amount) {
        this.setSales(this.sales + amount);
    }

    @Override
    public int getTrades(@NonNull TradeType type) {
        return switch (type) {
            case BUY -> this.purchases;
            case SELL -> this.sales;
        };
    }

    @NonNull
    public UUID getPlayerId() {
        return this.playerId;
    }

    @NonNull
    public UUID getProductId() {
        return this.productId;
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
    public long getRestockDate() {
        return this.restockDate;
    }

    @Override
    public void setRestockDate(long restockDate) {
        this.restockDate = restockDate;
    }
}
