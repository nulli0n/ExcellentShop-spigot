package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.DynamicProductPricer;
import su.nightexpress.nexshop.shop.FlatProductPricer;
import su.nightexpress.nexshop.shop.FloatProductPricer;

import java.util.HashMap;
import java.util.Map;

public abstract class ProductPricer implements IPlaceholder, JOption.Writer {

    protected final Map<TradeType, Double> priceCurrent;
    protected       Product<?, ?, ?>       product;

    public ProductPricer() {
        this.priceCurrent = new HashMap<>();
        this.setPrice(TradeType.BUY, -1D);
        this.setPrice(TradeType.SELL, -1D);
    }

    @NotNull
    public static ProductPricer read(@NotNull PriceType priceType, @NotNull JYML cfg, @NotNull String path) {
        return switch (priceType) {
            case FLAT -> FlatProductPricer.read(cfg, path);
            case FLOAT -> FloatProductPricer.read(cfg, path);
            case DYNAMIC -> DynamicProductPricer.read(cfg, path);
        };
    }

    public abstract void update();

    @NotNull
    public abstract PriceType getType();

    @NotNull
    public Product<?, ?, ?> getProduct() {
        if (this.product == null) {
            throw new IllegalStateException("Product is undefined!");
        }
        return this.product;
    }

    public void setProduct(@NotNull Product<?, ?, ?> product) {
        this.product = product;
    }

    public double getPrice(@NotNull TradeType tradeType) {
        double price = this.priceCurrent.computeIfAbsent(tradeType, b -> -1D);

        if (tradeType == TradeType.BUY && price > 0 && this.getProduct().isDiscountAllowed()) {
            price *= this.getProduct().getShop().getDiscountModifier();
        }
        return price;
    }

    public void setPrice(@NotNull TradeType tradeType, double price) {
        this.priceCurrent.put(tradeType, price);
    }

    public double getPriceBuy() {
        return this.getPrice(TradeType.BUY);
    }

    public double getPriceSell() {
        return this.getPrice(TradeType.SELL);
    }

    public double getPriceSellAll(@NotNull Player player) {
        int amountHas = this.getProduct().countItem(player);
        int amountCan = this.getProduct().getStock().getPossibleAmount(TradeType.SELL, player);

        int balance = Math.min((amountCan < 0 ? amountHas : amountCan), amountHas);
        return balance * this.getPriceSell();
    }
}
