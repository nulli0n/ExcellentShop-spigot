package su.nightexpress.nexshop.shop.price;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.IPurchaseListener;
import su.nightexpress.nexshop.api.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.price.ProductPriceData;
import su.nightexpress.nexshop.data.price.ProductPriceStorage;

import java.util.HashMap;
import java.util.Map;

public class DynamicProductPricer extends RangedProductPricer implements IPurchaseListener {

    private final Map<TradeType, Double> priceInitial;
    private final Map<TradeType, Double> priceStep;

    public DynamicProductPricer() {
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
    public static DynamicProductPricer read(@NotNull JYML cfg, @NotNull String path) {
        DynamicProductPricer pricer = new DynamicProductPricer();
        for (TradeType tradeType : TradeType.values()) {
            double min = cfg.getDouble(path + "." + tradeType.name() + ".Min", -1D);
            double max = cfg.getDouble(path + "." + tradeType.name() + ".Max", -1D);
            double init = cfg.getDouble(path + "." + tradeType.name() + ".Initial", 0D);
            double step = cfg.getDouble(path + "." + tradeType.name() + ".Step", 0D);
            pricer.setPriceMin(tradeType, min);
            pricer.setPriceMax(tradeType, max);
            pricer.setInitial(tradeType, init);
            pricer.setStep(tradeType, step);
        }
        return pricer;
    }

    @Override
    public void write(@NotNull JYML cfg, @NotNull String path) {
        for (TradeType tradeType : TradeType.values()) {
            cfg.set(path + "." + tradeType.name() + ".Min", this.getPriceMin(tradeType));
            cfg.set(path + "." + tradeType.name() + ".Max", this.getPriceMax(tradeType));
            cfg.set(path + "." + tradeType.name() + ".Initial", this.getInitial(tradeType));
            cfg.set(path + "." + tradeType.name() + ".Step", this.getStep(tradeType));
        }
    }

    @Override
    public void update() {
        ProductPriceData priceData = this.getData();
        boolean hasData = priceData != null;
        if (!hasData) {
            priceData = new ProductPriceData(this);
            for (TradeType tradeType : TradeType.values()) {
                this.setPrice(tradeType, this.getInitial(tradeType));
            }
        }
        else {
            double difference = priceData.getPurchases() - priceData.getSales();
            for (TradeType tradeType : TradeType.values()) {
                double min = this.getPriceMin(tradeType);
                double max = this.getPriceMax(tradeType);

                double price = this.getInitial(tradeType) + (difference * this.getStep(tradeType));
                if (price > max) price = max;
                else if (price < min) price = min;

                this.setPrice(tradeType, price);
            }
        }

        priceData.setLastBuyPrice(this.getPrice(TradeType.BUY));
        priceData.setLastSellPrice(this.getPrice(TradeType.SELL));
        priceData.setLastUpdated(System.currentTimeMillis());
        if (!hasData) {
            ProductPriceStorage.createData(priceData);
        }
        else {
            ProductPriceStorage.saveData(priceData);
        }
    }

    @Override
    public void onPurchase(@NotNull ShopTransactionEvent<?> event) {
        if (!event.getResult().getProduct().getId().equalsIgnoreCase(this.getProduct().getId())) return;

        TradeType tradeType = event.getResult().getTradeType();
        ProductPriceData priceData = this.getData();
        if (priceData != null) {
            if (tradeType == TradeType.BUY) priceData.setPurchases(priceData.getPurchases() + 1);
            else priceData.setSales(priceData.getSales() + 1);
        }
        this.update();
    }

    @Override
    @NotNull
    public PriceType getType() {
        return PriceType.DYNAMIC;
    }

    @Nullable
    public ProductPriceData getData() {
        return ProductPriceStorage.getData(this.getProduct().getShop().getId(), this.getProduct().getId());
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
