package su.nightexpress.excellentshop.product.price;

import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.product.PriceType;
import su.nightexpress.excellentshop.product.ProductPricing;
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

        @NonNull
        public static PriceUnit read(@NonNull FileConfig config, @NonNull String path) {
            double start = config.getDouble(path + ".StartValue");
            double offset = config.getDouble(path + ".Offset");
            double minOffset = config.getDouble(path + ".MinOffset");
            double maxOffset = config.getDouble(path + ".MaxOffset");

            return new PriceUnit(start, offset, minOffset, maxOffset);
        }

        @Override
        public void write(@NonNull FileConfig config, @NonNull String path) {
            config.set(path + ".StartValue", this.start);
            config.set(path + ".Offset", this.offset);
            config.set(path + ".MinOffset", this.minOffset);
            config.set(path + ".MaxOffset", this.maxOffset);
        }
    }

    @NonNull
    public static PlayersPricing read(@NonNull FileConfig config, @NonNull String path) {
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
    protected void writeAdditional(@NonNull FileConfig config, @NonNull String path) {
        for (TradeType tradeType : TradeType.values()) {
            config.set(path + "." + tradeType.name(), this.getPriceUnit(tradeType));
        }
    }

    @Override
    public boolean shouldResetOnExpire() {
        return false;
    }

    @Override
    public void handleTransaction(@NonNull ECompletedTransaction transaction, @NonNull Product product, int units, @NonNull PriceData priceData) {

    }

    @Override
    public void updatePrice(@NonNull Product product, @NonNull PriceData priceData) {
        for (TradeType tradeType : TradeType.values()) {
            product.setPrice(tradeType, this.calculatePrice(tradeType));
        }
    }

    public double calculatePrice(@NonNull TradeType tradeType) {
        PriceUnit unit = this.getPriceUnit(tradeType);
        double start = unit.start();

        double online = Bukkit.getServer().getOnlinePlayers().size();
        double offset = online * unit.offset();
        double finalOffset = unit.clampOffset(offset) / 100D;

        return start * (1D + finalOffset);
    }

    @Override
    public double getAveragePrice(@NonNull TradeType tradeType) {
        return this.getPriceUnit(tradeType).start();
    }

    @NonNull
    public PriceUnit getPriceUnit(@NonNull TradeType type) {
        return this.priceUnits.computeIfAbsent(type, k -> new PriceUnit(DISABLED, 0, 0, 0));
    }

    public void setPriceUnit(@NonNull TradeType type, @NonNull PriceUnit unit) {
        this.priceUnits.put(type, unit);
    }

    public void setPriceUnit(@NonNull TradeType type, double start, double offset, double minOffset, double maxOffset) {
        this.priceUnits.put(type, new PriceUnit(start, offset, minOffset, maxOffset));
    }
}
