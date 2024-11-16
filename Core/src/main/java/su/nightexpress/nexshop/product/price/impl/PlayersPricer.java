package su.nightexpress.nexshop.product.price.impl;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class PlayersPricer extends RangedPricer {

    private final Map<TradeType, Double> priceInitial;
    private final Map<TradeType, Double> adjustAmount;
    private int adjustStep;

    public PlayersPricer() {
        super(PriceType.PLAYER_AMOUNT);
        this.priceInitial = new HashMap<>();
        this.adjustAmount = new HashMap<>();
        this.adjustStep = 1;
    }

    @NotNull
    public static PlayersPricer read(@NotNull FileConfig config, @NotNull String path) {
        PlayersPricer pricer = new PlayersPricer();

        for (TradeType tradeType : TradeType.values()) {
            UniDouble price = UniDouble.read(config, path + "." + tradeType.name());

            double initialPrice = ConfigValue.create(path + "." + tradeType.name() + ".Initial", 0).read(config);
            double adjustAmount = ConfigValue.create(path + "." + tradeType.name() + ".Adjust_Amount", 100).read(config);

            pricer.setPriceRange(tradeType, price);
            pricer.setInitial(tradeType, initialPrice);
            pricer.setAdjustAmount(tradeType, adjustAmount);
        }
        pricer.setAdjustStep(ConfigValue.create(path + ".Adjust_Step", 1).read(config));

        return pricer;
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        for (TradeType tradeType : TradeType.values()) {
            this.getPriceRange(tradeType).write(config, path + "." + tradeType.name());

            config.set(path + "." + tradeType.name() + ".Initial", this.getInitial(tradeType));
            config.set(path + "." + tradeType.name() + ".Adjust_Amount", this.getAdjustAmount(tradeType));
        }
        config.set(path + ".Adjust_Step", this.getAdjustStep());
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.PLAYERS_PRICER.replacer(this);
    }

    @Override
    public double getPrice(@NotNull TradeType tradeType) {
        double price = this.getInitial(tradeType);

        double step = this.getAdjustStep();
        if (step > 0) {
            double online = Bukkit.getServer().getOnlinePlayers().size();
            double points = Math.floor(online / Math.max(1, step));

            price += this.getAdjustAmount(tradeType) * points;

            double min = this.getPriceMin(tradeType);
            double max = this.getPriceMax(tradeType);

            if (price > max && max >= 0) price = max;
            else if (price < min) price = min;
        }

        return price;
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

    @NotNull
    public Map<TradeType, Double> getAdjustAmount() {
        return adjustAmount;
    }

    public double getAdjustAmount(@NotNull TradeType tradeType) {
        return this.adjustAmount.getOrDefault(tradeType, 0D);
    }

    public void setAdjustAmount(@NotNull TradeType tradeType, double value) {
        this.adjustAmount.put(tradeType, value);
    }

    public int getAdjustStep() {
        return adjustStep;
    }

    public void setAdjustStep(int adjustStep) {
        this.adjustStep = adjustStep;
    }
}
