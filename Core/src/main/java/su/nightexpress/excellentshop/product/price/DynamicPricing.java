package su.nightexpress.excellentshop.product.price;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.product.PriceType;
import su.nightexpress.excellentshop.product.ProductPricing;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.wrapper.UniDouble;

import java.util.HashMap;
import java.util.Map;

public class DynamicPricing extends ProductPricing {

    private final Map<TradeType, PriceUnit> priceUnits;

    private int stabilizeInterval;
    private double stabilizeAmount;

    public DynamicPricing() {
        super(PriceType.DYNAMIC);
        this.priceUnits = new HashMap<>();
    }

    public record PriceUnit(double start, double buyOffset, double sellOffset, double minOffset, double maxOffset) implements Writeable {

        public double offset(@NonNull TradeType type) {
            return switch (type) {
                case BUY -> this.buyOffset;
                case SELL -> this.sellOffset;
            };
        }

        public double clampOffset(double offset) {
            double min = Math.min(this.minOffset, this.maxOffset);
            double max = Math.max(this.minOffset, this.maxOffset);

            return Math.clamp(offset, min, max);
        }

        public static DynamicPricing.@NonNull PriceUnit read(@NonNull FileConfig config, @NonNull String path) {
            double start = config.getDouble(path + ".StartValue");
            double buyOffset = config.getDouble(path + ".BuyOffset");
            double sellOffset = config.getDouble(path + ".SellOffset");
            double minOffset = config.getDouble(path + ".MinOffset");
            double maxOffset = config.getDouble(path + ".MaxOffset");

            return new PriceUnit(start, buyOffset, sellOffset, minOffset, maxOffset);
        }

        @Override
        public void write(@NonNull FileConfig config, @NonNull String path) {
            config.set(path + ".StartValue", this.start);
            config.set(path + ".BuyOffset", this.buyOffset);
            config.set(path + ".SellOffset", this.sellOffset);
            config.set(path + ".MinOffset", this.minOffset);
            config.set(path + ".MaxOffset", this.maxOffset);
        }
    }

    @NonNull
    public static DynamicPricing read(@NonNull FileConfig config, @NonNull String path) {
        DynamicPricing pricing = new DynamicPricing();

        for (TradeType tradeType : TradeType.values()) {
            String typePath = path + "." + tradeType.name();

            if (config.contains(typePath + ".Initial")) {
                UniDouble price = UniDouble.read(config, typePath);
                double init = config.getDouble(typePath + ".Initial", 0D);
                double step = config.getDouble(typePath + ".Step", 0D);

                double minOld = price.getMinValue();
                double maxOld = price.getMaxValue();

                double stepOffset = step / init * 100D;

                double minOffset = minOld / init * 100D;
                double maxOffset = maxOld / init * 100D;

                PriceUnit unit = new PriceUnit(init, stepOffset, -stepOffset, minOffset, maxOffset);

                config.remove(typePath);
                unit.write(config, typePath);
            }

            PriceUnit unit = PriceUnit.read(config, typePath);
            pricing.setPriceUnit(tradeType, unit);
        }

        pricing.setStabilizeInterval(ConfigValue.create(path + ".Stabilization.Interval", -1).read(config));
        pricing.setStabilizeAmount(ConfigValue.create(path + ".Stabilization.Amount", 0D).read(config));

        return pricing;
    }

    @Override
    protected void writeAdditional(@NonNull FileConfig config, @NonNull String path) {
        for (TradeType tradeType : TradeType.values()) {
            config.set(path + "." + tradeType.name(), this.getPriceUnit(tradeType));
        }
        config.set(path + ".Stabilization.Interval", this.stabilizeInterval);
        config.set(path + ".Stabilization.Amount", this.stabilizeAmount);
    }

    @Override
    public boolean shouldResetOnExpire() {
        return false;
    }

    @Override
    public void handleTransaction(@NonNull ECompletedTransaction transaction, @NonNull Product product, int units, @NonNull PriceData priceData) {
        TradeType currentType = transaction.type();

        // Delay the stabilization process
        priceData.setExpireDate(TimeUtil.createFutureTimestamp(this.stabilizeInterval));
        priceData.markDirty();

        for (TradeType unitType : TradeType.values()) {
            PriceUnit unit = this.getPriceUnit(unitType);

            double offset = unit.offset(currentType) * units;
            double currentOffset = priceData.getOffset(unitType) * 100D;
            double clampedOffset = unit.clampOffset(currentOffset + offset);
            double finalOffset = clampedOffset / 100D;

            priceData.setOffset(unitType, finalOffset);
        }

        this.updatePrice(product, priceData);
    }

    @Override
    public void updatePrice(@NonNull Product product, @NonNull PriceData priceData) {
        boolean needStabilize = priceData.isExpired();

        for (TradeType tradeType : TradeType.values()) {
            PriceUnit unit = this.getPriceUnit(tradeType);

            double start = unit.start();
            double offset = priceData.getOffset(tradeType);

            if (needStabilize && offset != 0D) {
                double step = this.stabilizeAmount / 100D;
                offset = offset > 0D ? Math.max(0, offset - step) : Math.min(0, offset + step);
                priceData.setOffset(tradeType, offset);
            }

            double clampedOffset = unit.clampOffset(offset * 100D);
            double finalOffset = 1D + (clampedOffset / 100D);

            double price = start * finalOffset;
            product.setPrice(tradeType, price);
        }

        if (needStabilize) {
            priceData.setExpireDate(TimeUtil.createFutureTimestamp(this.stabilizeInterval));
            priceData.markDirty();
        }
    }

    @Override
    public double getAveragePrice(@NonNull TradeType type) {
        return this.getPriceUnit(type).start();
    }

    public DynamicPricing.@NonNull PriceUnit getPriceUnit(@NonNull TradeType type) {
        return this.priceUnits.computeIfAbsent(type, k -> new PriceUnit(-1D, 0, 0, 0, 0));
    }

    public void setPriceUnit(@NonNull TradeType type, DynamicPricing.@NonNull PriceUnit unit) {
        this.priceUnits.put(type, unit);
    }

    public void setPriceUnit(@NonNull TradeType type, double start, double buyOffset, double sellOffset, double minOffset, double maxOffset) {
        this.priceUnits.put(type, new PriceUnit(start, buyOffset, sellOffset, minOffset, maxOffset));
    }

    public int getStabilizeInterval() {
        return this.stabilizeInterval;
    }

    public void setStabilizeInterval(int stabilizeInterval) {
        this.stabilizeInterval = stabilizeInterval;
    }

    public double getStabilizeAmount() {
        return this.stabilizeAmount;
    }

    public void setStabilizeAmount(double stabilizeAmount) {
        this.stabilizeAmount = stabilizeAmount;
    }
}
