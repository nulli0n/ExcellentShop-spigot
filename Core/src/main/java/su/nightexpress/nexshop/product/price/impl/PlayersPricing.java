package su.nightexpress.nexshop.product.price.impl;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nexshop.product.price.ProductPricing;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.util.HashMap;
import java.util.Map;

public class PlayersPricing extends ProductPricing {

    private final Map<TradeType, PriceUnit> priceUnits;

    public PlayersPricing() {
        super(PriceType.PLAYER_AMOUNT);
        this.priceUnits = new HashMap<>();
    }

    public record PriceUnit(double start, double offset, double minOffset, double maxOffset) implements Writeable {

        public double clampOffset(double offset) {
            return Math.clamp(offset, this.minOffset, this.maxOffset);
        }

        @NotNull
        public static PriceUnit read(@NotNull FileConfig config, @NotNull String path) {
            double start = config.getDouble(path + ".StartValue");
            double offset = config.getDouble(path + ".Offset");
            double minOffset = config.getDouble(path + ".MinOffset");
            double maxOffset = config.getDouble(path + ".MaxOffset");

            return new PriceUnit(start, offset, minOffset, maxOffset);
        }

        @Override
        public void write(@NotNull FileConfig config, @NotNull String path) {
            config.set(path + ".StartValue", this.start);
            config.set(path + ".Offset", this.offset);
            config.set(path + ".MinOffset", this.minOffset);
            config.set(path + ".MaxOffset", this.maxOffset);
        }
    }

    @NotNull
    public static PlayersPricing read(@NotNull FileConfig config, @NotNull String path) {
        PlayersPricing pricing = new PlayersPricing();

        for (TradeType tradeType : TradeType.values()) {
            String typePath = path + "." + tradeType.name();

            if (config.contains(typePath + ".Initial")) {
                UniDouble price = UniDouble.read(config, typePath);
                double init = config.getDouble(typePath + ".Initial", 0D);
                double oldOffset = config.getDouble(typePath + ".Adjust_Amount", 0D);
                int step = config.getInt(path + ".Adjust_Step");

                double minOld = price.getMinValue();
                double maxOld = price.getMaxValue();

                double offset = oldOffset / step / init * 100D;

                double minOffset = minOld / init * 100D;
                double maxOffset = maxOld / init * 100D;

                PriceUnit unit = new PriceUnit(init, offset, minOffset, maxOffset);

                config.remove(typePath);
                unit.write(config, typePath);
            }

            PriceUnit unit = PriceUnit.read(config, typePath);
            pricing.setPriceUnit(tradeType, unit);
        }

        return pricing;
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        for (TradeType tradeType : TradeType.values()) {
            config.set(path + "." + tradeType.name(), this.getPriceUnit(tradeType));
        }
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event, @NotNull Product product, @NotNull PriceData priceData) {

    }

    @Override
    public void updatePrice(@NotNull Product product, @NotNull PriceData priceData) {
        for (TradeType tradeType : TradeType.values()) {
            product.setPrice(tradeType, this.calculatePrice(tradeType));
        }
    }

    public double calculatePrice(@NotNull TradeType tradeType) {
        PriceUnit unit = this.getPriceUnit(tradeType);
        double start = unit.start();

        double online = Bukkit.getServer().getOnlinePlayers().size();
        double offset = online * unit.offset();
        double finalOffset = unit.clampOffset(offset) / 100D;

        return start * (1D + finalOffset);
    }

    @Override
    public double getAveragePrice(@NotNull TradeType tradeType) {
        return this.getPriceUnit(tradeType).start();
    }

    @NotNull
    public PriceUnit getPriceUnit(@NotNull TradeType type) {
        return this.priceUnits.computeIfAbsent(type, k -> new PriceUnit(DISABLED, 0, 0, 0));
    }

    public void setPriceUnit(@NotNull TradeType type, @NotNull PriceUnit unit) {
        this.priceUnits.put(type, unit);
    }

    public void setPriceUnit(@NotNull TradeType type, double start, double offset, double minOffset, double maxOffset) {
        this.priceUnits.put(type, new PriceUnit(start, offset, minOffset, maxOffset));
    }
}
