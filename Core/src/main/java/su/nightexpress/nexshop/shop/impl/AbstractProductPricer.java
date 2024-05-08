package su.nightexpress.nexshop.shop.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.impl.price.DynamicPricer;
import su.nightexpress.nexshop.shop.impl.price.FlatPricer;
import su.nightexpress.nexshop.shop.impl.price.FloatPricer;
import su.nightexpress.nexshop.shop.impl.price.PlayersPricer;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.placeholder.Placeholder;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProductPricer implements Placeholder {

    protected final PriceType              type;
    protected final Map<TradeType, Double> priceCurrent;
    protected final PlaceholderMap         placeholderMap;

    public AbstractProductPricer(@NotNull PriceType type) {
        this.type = type;
        this.priceCurrent = new HashMap<>();
        this.setPrice(TradeType.BUY, -1D);
        this.setPrice(TradeType.SELL, -1D);
        this.placeholderMap = new PlaceholderMap();
    }

    @NotNull
    public static AbstractProductPricer read(@NotNull FileConfig config, @NotNull String path) {
        PriceType priceType = config.getEnum(path + ".Type", PriceType.class, PriceType.FLAT);

        return switch (priceType) {
            case FLAT -> FlatPricer.read(config, path);
            case FLOAT -> FloatPricer.read(config, path);
            case DYNAMIC -> DynamicPricer.read(config, path);
            case PLAYER_AMOUNT -> PlayersPricer.read(config, path);
        };
    }

    @NotNull
    public static AbstractProductPricer from(@NotNull PriceType priceType) {
        return switch (priceType) {
            case FLAT -> new FlatPricer();
            case FLOAT -> new FloatPricer();
            case DYNAMIC -> new DynamicPricer();
            case PLAYER_AMOUNT -> new PlayersPricer();
        };
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Type", this.getType().name());
        this.writeAdditional(config, path);
    }

    protected abstract void writeAdditional(@NotNull FileConfig config, @NotNull String path);

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
