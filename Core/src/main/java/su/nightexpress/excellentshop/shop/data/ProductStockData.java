package su.nightexpress.excellentshop.shop.data;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.data.state.StatefulData;
import su.nightexpress.excellentshop.api.product.stock.StockData;
import su.nightexpress.nightcore.util.TimeUtil;

import java.util.UUID;

public class ProductStockData extends StatefulData implements StockData {

    private final UUID productId;

    private int stock;
    private long restockDate;

    public ProductStockData(@NonNull UUID productId, int stock, long restockDate) {
        this.productId = productId;
        this.stock = stock;
        this.restockDate = restockDate;
    }

    @Override
    public boolean isRestockTime() {
        return TimeUtil.isPassed(this.restockDate);
    }

    @Override
    public void consume(int units) {
        this.setStock(this.stock - units);
    }

    @Override
    public void store(int units) {
        this.setStock(this.stock + units);
    }

    @Override
    public void setExpired() {
        this.setRestockDate(0L);
    }

    @NonNull
    public UUID getProductId() {
        return this.productId;
    }

    @Override
    public int getStock() {
        return this.stock;
    }

    @Override
    public void setStock(int units) {
        this.stock = Math.max(0, units);
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
