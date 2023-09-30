package su.nightexpress.nexshop.shop.price;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.api.shop.ProductPricer;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;

public class FlatPricer extends ProductPricer {

    public FlatPricer() {

    }

    @NotNull
    public static FlatPricer read(@NotNull JYML cfg, @NotNull String path) {
        FlatPricer pricer = new FlatPricer();
        for (TradeType tradeType : TradeType.values()) {
            pricer.setPrice(tradeType, cfg.getDouble(path + "." + tradeType.name()));
        }
        return pricer;
    }

    @Override
    public void write(@NotNull JYML cfg, @NotNull String path) {
        this.priceCurrent.forEach(((tradeType, amount) -> cfg.set(path + "." + tradeType.name(), amount)));
    }

    @Override
    public void update() {

    }

    @Override
    @NotNull
    public PriceType getType() {
        return PriceType.FLAT;
    }
}
