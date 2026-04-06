package su.nightexpress.excellentshop.api.product.stock;

import su.nightexpress.excellentshop.api.data.state.Stateful;

public interface StockData extends Stateful {

    UnlimitedStockData UNLIMITED = new UnlimitedStockData();

    boolean isRestockTime();

    void setExpired();

    void consume(int units);

    void store(int units);

    int getStock();

    void setStock(int units);

    long getRestockDate();

    void setRestockDate(long restockDate);
}
