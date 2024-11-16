package su.nightexpress.nexshop.product.price.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class DynamicPricer extends RangedPricer {

    private final Map<TradeType, Double> priceInitial;
    private final Map<TradeType, Double> priceStep;

    public DynamicPricer() {
        super(PriceType.DYNAMIC);
        this.priceInitial = new HashMap<>();
        this.priceStep = new HashMap<>();
    }

    @NotNull
    public static DynamicPricer read(@NotNull FileConfig cfg, @NotNull String path) {
        DynamicPricer pricer = new DynamicPricer();
        for (TradeType tradeType : TradeType.values()) {
            UniDouble price = UniDouble.read(cfg, path + "." + tradeType.name());
            double init = cfg.getDouble(path + "." + tradeType.name() + ".Initial", 0D);
            double step = cfg.getDouble(path + "." + tradeType.name() + ".Step", 0D);
            pricer.setPriceRange(tradeType, price);
            pricer.setInitial(tradeType, init);
            pricer.setStep(tradeType, step);
        }
        return pricer;
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig cfg, @NotNull String path) {
        for (TradeType tradeType : TradeType.values()) {
            this.getPriceRange(tradeType).write(cfg, path + "." + tradeType.name());
            cfg.set(path + "." + tradeType.name() + ".Initial", this.getInitial(tradeType));
            cfg.set(path + "." + tradeType.name() + ".Step", this.getStep(tradeType));
        }
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.DYNAMIC_PRICER.replacer(this);
    }

    @Override
    public double getPriceAverage(@NotNull TradeType tradeType) {
        return this.getInitial(tradeType);
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
