package su.nightexpress.nexshop.shop.impl.price;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.values.UniDouble;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;

import java.util.HashMap;
import java.util.Map;

public class DynamicPricer extends RangedPricer {

    private final Map<TradeType, Double> priceInitial;
    private final Map<TradeType, Double> priceStep;

    public DynamicPricer() {
        super(PriceType.DYNAMIC);
        this.priceInitial = new HashMap<>();
        this.priceStep = new HashMap<>();

        this.placeholderMap
            .add(Placeholders.PRODUCT_PRICER_BUY_MIN, () -> String.valueOf(this.getPriceMin(TradeType.BUY)))
            .add(Placeholders.PRODUCT_PRICER_BUY_MAX, () -> String.valueOf(this.getPriceMax(TradeType.BUY)))
            .add(Placeholders.PRODUCT_PRICER_SELL_MIN, () -> String.valueOf(this.getPriceMin(TradeType.SELL)))
            .add(Placeholders.PRODUCT_PRICER_SELL_MAX, () -> String.valueOf(this.getPriceMax(TradeType.SELL)))
            .add(Placeholders.PRODUCT_PRICER_DYNAMIC_INITIAL_BUY, () -> NumberUtil.format(this.getInitial(TradeType.BUY)))
            .add(Placeholders.PRODUCT_PRICER_DYNAMIC_INITIAL_SELL, () -> NumberUtil.format(this.getInitial(TradeType.SELL)))
            .add(Placeholders.PRODUCT_PRICER_DYNAMIC_STEP_BUY, () -> NumberUtil.format(this.getStep(TradeType.BUY)))
            .add(Placeholders.PRODUCT_PRICER_DYNAMIC_STEP_SELL, () -> NumberUtil.format(this.getStep(TradeType.SELL)))
        ;
    }

    @NotNull
    public static DynamicPricer read(@NotNull JYML cfg, @NotNull String path) {
        DynamicPricer pricer = new DynamicPricer();
        for (TradeType tradeType : TradeType.values()) {
            UniDouble price = UniDouble.read(cfg, path + "." + tradeType.name());
            double init = cfg.getDouble(path + "." + tradeType.name() + ".Initial", 0D);
            double step = cfg.getDouble(path + "." + tradeType.name() + ".Step", 0D);
            pricer.setPrice(tradeType, price);
            pricer.setInitial(tradeType, init);
            pricer.setStep(tradeType, step);
        }
        return pricer;
    }

    @Override
    protected void writeAdditional(@NotNull JYML cfg, @NotNull String path) {
        for (TradeType tradeType : TradeType.values()) {
            this.getPriceRange(tradeType).write(cfg, path + "." + tradeType.name());
            cfg.set(path + "." + tradeType.name() + ".Initial", this.getInitial(tradeType));
            cfg.set(path + "." + tradeType.name() + ".Step", this.getStep(tradeType));
        }
    }

    public double getInitial(@NotNull TradeType tradeType) {
        return this.priceInitial.getOrDefault(tradeType, 0D);
    }

    public void setInitial(@NotNull TradeType tradeType, double initial) {
        this.priceInitial.put(tradeType, initial);
    }

    public double getStep(@NotNull TradeType tradeType) {
        return this.priceStep.getOrDefault(tradeType, 0D);
    }

    public void setStep(@NotNull TradeType tradeType, double step) {
        this.priceStep.put(tradeType, step);
    }
}
