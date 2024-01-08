package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;

public class Transaction implements Placeholder {

    private final ExcellentShop  plugin;
    private final Product        product;
    private final TradeType      tradeType;
    private final PlaceholderMap placeholderMap;

    private int    units;
    private double price;
    private Result result;

    public Transaction(@NotNull ExcellentShop plugin,
                       @NotNull Product product,
                       @NotNull TradeType tradeType,
                       int units,
                       double price,
                       @NotNull Result result) {
        this.plugin = plugin;
        this.product = product;
        this.tradeType = tradeType;
        this.units = units;
        this.price = price;
        this.result = result;
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.GENERIC_TYPE, plugin.getLangManager().getEnum(this.getTradeType()))
            .add(Placeholders.GENERIC_AMOUNT, String.valueOf(product.getUnitAmount() * this.getUnits()))
            .add(Placeholders.GENERIC_UNITS, String.valueOf(this.getUnits()))
            .add(Placeholders.GENERIC_PRICE, product.getCurrency().format(this.getPrice()))
            .add(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(product.getPreview()));
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    public void sendError(@NotNull Player player) {
        LangMessage message = this.getErrorMessage();
        if (message != null) message.send(player);
    }

    @Nullable
    public LangMessage getErrorMessage() {
        LangKey key = switch (this.getResult()) {
            case TOO_EXPENSIVE -> Lang.SHOP_PRODUCT_ERROR_TOO_EXPENSIVE;
            case NOT_ENOUGH_ITEMS -> Lang.SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS;
            case OUT_OF_STOCK -> Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK;
            case OUT_OF_MONEY -> Lang.SHOP_PRODUCT_ERROR_OUT_OF_FUNDS;
            case OUT_OF_SPACE -> Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE;
            default -> null;
        };
        return key == null ? null : this.plugin.getMessage(key).replace(this.replacePlaceholders());
    }

    @NotNull
    public Product getProduct() {
        return product;
    }

    @NotNull
    public TradeType getTradeType() {
        return tradeType;
    }

    public int getAmount() {
        return this.getProduct().getUnitAmount() * this.getUnits();
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
    public Result getResult() {
        return result;
    }

    public void setResult(@NotNull Result result) {
        this.result = result;
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
