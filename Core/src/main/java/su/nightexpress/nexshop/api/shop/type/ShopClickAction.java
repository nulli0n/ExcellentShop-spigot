package su.nightexpress.nexshop.api.shop.type;

import org.jetbrains.annotations.Nullable;

public enum ShopClickAction {

    BUY_SELECTION(TradeType.BUY),
    SELL_SELECTION(TradeType.SELL),
    SELL_ALL(TradeType.SELL),
    BUY_SINGLE(TradeType.BUY),
    SELL_SINGLE(TradeType.SELL),
    PURCHASE_OPTION(null),
    UNDEFINED(null)
    ;

    private final TradeType tradeType;

    ShopClickAction(@Nullable TradeType tradeType) {
        this.tradeType = tradeType;
    }

    @Nullable
    public TradeType getTradeType() {
        return this.tradeType;
    }
}
