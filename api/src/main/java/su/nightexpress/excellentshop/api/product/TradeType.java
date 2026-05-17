package su.nightexpress.excellentshop.api.product;

import org.jspecify.annotations.NonNull;

public enum TradeType {

    BUY(0),
    SELL(1);

    private final int index;

    TradeType(int index) {
        this.index = index;
    }

    public int index() {
        return this.index;
    }

    @NonNull
    public TradeType opposite() {
        return this == BUY ? SELL : BUY;
    }

    @NonNull
    public String getLowerCase() {
        return this.name().toLowerCase();
    }
}