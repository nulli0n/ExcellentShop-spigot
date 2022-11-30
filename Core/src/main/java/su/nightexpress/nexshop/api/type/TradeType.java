package su.nightexpress.nexshop.api.type;

import org.jetbrains.annotations.NotNull;

public enum TradeType {

    BUY, SELL;

    @NotNull
    public TradeType getOpposite() {
        return this == BUY ? SELL : BUY;
    }
}