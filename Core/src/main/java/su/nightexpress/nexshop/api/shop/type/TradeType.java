package su.nightexpress.nexshop.api.shop.type;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public TradeType getOpposite() {
        return this == BUY ? SELL : BUY;
    }

    @NotNull
    public String getLowerCase() {
        return this.name().toLowerCase();
    }
}