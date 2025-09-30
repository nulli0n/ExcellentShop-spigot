package su.nightexpress.nexshop.product.price.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nexshop.product.price.ProductPricing;
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

        public double offset(@NotNull TradeType type) {
            return switch (type) {
                case BUY -> this.buyOffset;
                case SELL -> this.sellOffset;
            };
        }

        public double clampOffset(double offset) {
            return Math.clamp(offset, this.minOffset, this.maxOffset);
        }

        @NotNull
        public static DynamicPricing.PriceUnit read(@NotNull FileConfig config, @NotNull String path) {
            double start = config.getDouble(path + ".StartValue");
            double buyOffset = config.getDouble(path + ".BuyOffset");
            double sellOffset = config.getDouble(path + ".SellOffset");
            double minOffset = config.getDouble(path + ".MinOffset");
            double maxOffset = config.getDouble(path + ".MaxOffset");

            return new PriceUnit(start, buyOffset, sellOffset, minOffset, maxOffset);
        }

        @Override
        public void write(@NotNull FileConfig config, @NotNull String path) {
            config.set(path + ".StartValue", this.start);
            config.set(path + ".BuyOffset", this.buyOffset);
            config.set(path + ".SellOffset", this.sellOffset);
            config.set(path + ".MinOffset", this.minOffset);
            config.set(path + ".MaxOffset", this.maxOffset);
        }
    }

    @NotNull
    public static DynamicPricing read(@NotNull FileConfig config, @NotNull String path) {
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
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        for (TradeType tradeType : TradeType.values()) {
            config.set(path + "." + tradeType.name(), this.getPriceUnit(tradeType));
        }
        config.set(path + ".Stabilization.Interval", this.stabilizeInterval);
        config.set(path + ".Stabilization.Amount", this.stabilizeAmount);
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event, @NotNull Product product, @NotNull PriceData priceData) {
        TradeType currentType = event.getTransaction().getTradeType();

        // Delay the stabilization process
        priceData.setExpireDate(TimeUtil.createFutureTimestamp(this.stabilizeInterval));
        priceData.setSaveRequired(true);

        for (TradeType unitType : TradeType.values()) {
            PriceUnit unit = this.getPriceUnit(unitType);

            double offset = unit.offset(currentType);
            double currentOffset = priceData.getOffset(unitType) * 100D;
            double clampedOffset = unit.clampOffset(currentOffset + offset);
            double finalOffset = clampedOffset / 100D;

            priceData.setOffset(unitType, finalOffset);
        }

        this.updatePrice(product, priceData);
    }

    @Override
    public void updatePrice(@NotNull Product product, @NotNull PriceData priceData) {
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
            priceData.setSaveRequired(true);
        }
    }

    @Override
    public double getAveragePrice(@NotNull TradeType type) {
        return this.getPriceUnit(type).start();
    }

    @NotNull
    public DynamicPricing.PriceUnit getPriceUnit(@NotNull TradeType type) {
        return this.priceUnits.computeIfAbsent(type, k -> new PriceUnit(-1D, 0, 0, 0, 0));
    }

    public void setPriceUnit(@NotNull TradeType type, @NotNull DynamicPricing.PriceUnit unit) {
        this.priceUnits.put(type, unit);
    }

    public void setPriceUnit(@NotNull TradeType type, double start, double buyOffset, double sellOffset, double minOffset, double maxOffset) {
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
