package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public class Transaction implements Placeholder {

    private final Product        product;
    private final TradeType      tradeType;
    private final Result         result;
    private final PlaceholderMap placeholderMap;

    private int    units;
    private double price;

    public Transaction(@NotNull Product product, @NotNull TradeType tradeType, int units, double price, @NotNull Result result) {
        this.product = product;
        this.tradeType = tradeType;
        this.units = units;
        this.price = price;
        this.result = result;
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.GENERIC_TYPE, ShopAPI.PLUGIN.getLangManager().getEnum(this.getTradeType()))
            .add(Placeholders.GENERIC_AMOUNT, String.valueOf(product.getUnitAmount() * this.getUnits()))
            .add(Placeholders.GENERIC_UNITS, String.valueOf(this.getUnits()))
            .add(Placeholders.GENERIC_PRICE, product.getCurrency().format(this.getPrice()))
            .add(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(product.getPreview()))
        ;
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    public Product getProduct() {
        return product;
    }

    @NotNull
    public TradeType getTradeType() {
        return tradeType;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @NotNull
    public Transaction.Result getResult() {
        return result;
    }

    public enum Result {
        TOO_EXPENSIVE,
        NOT_ENOUGH_ITEMS,
        OUT_OF_STOCK,
        OUT_OF_MONEY,
        OUT_OF_SPACE,
        SUCCESS,
        FAILURE,
    }
}
