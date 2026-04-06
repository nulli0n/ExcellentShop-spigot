package su.nightexpress.excellentshop.api.product.price;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.data.state.Stateful;
import su.nightexpress.excellentshop.api.product.TradeType;

public interface PriceData extends Stateful {

    EmptyPriceData EMPTY = new EmptyPriceData();

    void countTransaction(@NonNull TradeType tradeType, int amount);

    double getOffset(@NonNull TradeType type);

    void setOffset(@NonNull TradeType type, double offset);

    void reset();

    boolean isExpired();

    boolean isExpirable();

    void setExpired();

    double getBuyOffset();

    void setBuyOffset(double buyOffset);

    double getSellOffset();

    void setSellOffset(double sellOffset);

    long getExpireDate();

    void setExpireDate(long expireDate);

    int getPurchases();

    void setPurchases(int purchases);

    int getSales();

    void setSales(int sales);
}
