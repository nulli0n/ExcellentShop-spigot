package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.language.entry.LangText;

import java.util.function.UnaryOperator;

public class Transaction {

    private final Product   product;
    private final TradeType tradeType;

    private int    units;
    private double price;
    private Result result;

    // TODO Save currency field, bc product currency may be changed at any time after transaction.

    public Transaction(@NotNull ShopPlugin plugin,
                       @NotNull Product product,
                       @NotNull TradeType tradeType,
                       int units,
                       double price,
                       @NotNull Result result) {
        //this.plugin = plugin;
        this.product = product;
        this.tradeType = tradeType;
        this.units = units;
        this.price = price;
        this.result = result;
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.TRANSACTION.replacer(this);
    }

    public void sendError(@NotNull Player player) {
        LangText text = switch (this.result) {
            case TOO_EXPENSIVE -> Lang.SHOP_PRODUCT_ERROR_TOO_EXPENSIVE;
            case NOT_ENOUGH_ITEMS -> Lang.SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS;
            case OUT_OF_STOCK -> Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK;
            case OUT_OF_MONEY -> Lang.SHOP_PRODUCT_ERROR_OUT_OF_FUNDS;
            case OUT_OF_SPACE -> Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE;
            default -> null;
        };
        if (text == null) return;

        text.getMessage().send(player, replacer -> replacer.replace(this.replacePlaceholders()));
    }

    @NotNull
    public Product getProduct() {
        return this.product;
    }

    @NotNull
    public Currency getCurrency() {
        return this.product.getCurrency();
    }

    @NotNull
    public TradeType getTradeType() {
        return this.tradeType;
    }

    public int getAmount() {
        return UnitUtils.unitsToAmount(this.product, this.units);
        //return this.product.getUnitAmount() * this.getUnits();
    }

    public int getUnits() {
        return this.units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @NotNull
    public Result getResult() {
        return this.result;
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
