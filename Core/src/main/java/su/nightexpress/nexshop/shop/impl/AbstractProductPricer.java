package su.nightexpress.nexshop.shop.impl;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.impl.price.DynamicPricer;
import su.nightexpress.nexshop.shop.impl.price.FlatPricer;
import su.nightexpress.nexshop.shop.impl.price.FloatPricer;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProductPricer implements Placeholder {

    protected final PriceType type;
    protected final Map<TradeType, Double> priceCurrent;
    protected final PlaceholderMap placeholderMap;

    public AbstractProductPricer(@NotNull PriceType type) {
        this.type = type;
        this.priceCurrent = new HashMap<>();
        this.setPrice(TradeType.BUY, -1D);
        this.setPrice(TradeType.SELL, -1D);
        this.placeholderMap = new PlaceholderMap();
    }

    @NotNull
    public static AbstractProductPricer read(@NotNull JYML cfg, @NotNull String path) {
        PriceType priceType = cfg.getEnum(path + ".Type", PriceType.class, PriceType.FLAT);

        return switch (priceType) {
            case FLAT -> FlatPricer.read(cfg, path);
            case FLOAT -> FloatPricer.read(cfg, path);
            case DYNAMIC -> DynamicPricer.read(cfg, path);
        };
    }

    @NotNull
    public static AbstractProductPricer from(@NotNull PriceType priceType) {
        return switch (priceType) {
            case FLAT -> new FlatPricer();
            case FLOAT -> new FloatPricer();
            case DYNAMIC -> new DynamicPricer();
        };
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Type", this.getType().name());
        this.writeAdditional(cfg, path);
    }

    protected abstract void writeAdditional(@NotNull JYML cfg, @NotNull String path);

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    public PriceType getType() {
        return this.type;
    }

    public double getPrice(@NotNull TradeType tradeType) {
        return this.priceCurrent.computeIfAbsent(tradeType, b -> -1D);
    }

    public void setPrice(@NotNull TradeType tradeType, double price) {
        this.priceCurrent.put(tradeType, price);
    }

    public double getBuyPrice() {
        return this.getPrice(TradeType.BUY);
    }

    public double getSellPrice() {
        return this.getPrice(TradeType.SELL);
    }
}
