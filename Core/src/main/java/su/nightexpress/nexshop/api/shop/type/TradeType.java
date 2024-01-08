package su.nightexpress.nexshop.api.shop.type;

import org.jetbrains.annotations.NotNull;

public enum TradeType {

    BUY, SELL;

    @NotNull
    public TradeType getOpposite() {
        return this == BUY ? SELL : BUY;
    }

    @NotNull
    public String getLowerCase() {
        return this.name().toLowerCase();
    }
}