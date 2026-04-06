package su.nightexpress.excellentshop.api.product.limit;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.data.state.Stateful;
import su.nightexpress.excellentshop.api.product.TradeType;

public interface LimitData extends Stateful {

    EmptyLimitData EMPTY = new EmptyLimitData();

    void reset();

    boolean isRestockTime();

    boolean isActive();

    int getTrades(@NonNull TradeType type);

    void addPurchases(int amount);

    void addSales(int amount);

    int getPurchases();

    void setPurchases(int purchases);

    int getSales();

    void setSales(int sales);

    long getRestockDate();

    void setRestockDate(long restockDate);
}
