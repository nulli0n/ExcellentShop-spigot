package su.nightexpress.nexshop.shop.impl.price;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.impl.AbstractProductPricer;

public class FlatPricer extends AbstractProductPricer {

    public FlatPricer() {
        super(PriceType.FLAT);
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
    protected void writeAdditional(@NotNull JYML cfg, @NotNull String path) {
        this.priceCurrent.forEach(((tradeType, amount) -> cfg.set(path + "." + tradeType.name(), amount)));
    }
}
