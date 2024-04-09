package su.nightexpress.nexshop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;
import su.nightexpress.nexshop.shop.impl.AbstractProductPricer;
import su.nightexpress.nexshop.shop.impl.price.FloatPricer;
import su.nightexpress.nexshop.shop.util.PlaceholderRelMap;

import java.util.function.Function;

public class Placeholders extends su.nexmedia.engine.utils.Placeholders {

    public static final String URL_WIKI               = "https://github.com/nulli0n/ExcellentShop-spigot/wiki/";
    public static final String URL_WIKI_PLACEHOLDERS  = URL_WIKI + "Internal-Placeholders";
    public static final String URL_WIKI_PRODUCT_STOCK = URL_WIKI + "Product-Stock-Feature";

    public static final String EDITOR_VIRTUAL_TITLE = "Virtual Shop Editor";

    public static final String GENERIC_NAME     = "%name%";
    public static final String GENERIC_ITEM     = "%item%";
    public static final String GENERIC_TOTAL    = "%total%";
    public static final String GENERIC_LORE     = "%lore%";
    public static final String GENERIC_AMOUNT   = "%amount%";
    public static final String GENERIC_UNITS    = "%units%";
    public static final String GENERIC_TYPE     = "%type%";
    public static final String GENERIC_TIME     = "%time%";
    public static final String GENERIC_PRICE    = "%price%";
    public static final String GENERIC_BALANCE  = "%balance%";
    public static final String GENERIC_DISCOUNT = "%discount%";
    public static final String GENERIC_TAX      = "%tax%";

    public static final String ITEM_NAME = "%item_name%";
    public static final String ITEM_LORE = "%item_lore%";

    public static final String CURRENCY_NAME = "%currency_name%";
    public static final String CURRENCY_ID   = "%currency_id%";

    public static final String SHOP_ID              = "%shop_id%";
    public static final String SHOP_NAME            = "%shop_name%";
    public static final String SHOP_BUY_ALLOWED     = "%shop_buy_allowed%";
    public static final String SHOP_SELL_ALLOWED    = "%shop_sell_allowed%";

    public static final String PRODUCT_HANDLER                  = "%product_handler%";
    public static final String PRODUCT_PRICE_TYPE               = "%product_price_type%";
    public static final String PRODUCT_PRICE_SELL_ALL           = "%product_price_sell_all%";
    public static final String PRODUCT_PRICE_SELL_ALL_FORMATTED = "%product_price_sell_all_formatted%";

    public static final Function<TradeType, String> PRODUCT_PRICE           = type -> "%product_price_" + type.getLowerCase() + "%";
    public static final Function<TradeType, String> PRODUCT_PRICE_FORMATTED = type -> "%product_price_" + type.getLowerCase() + "_formatted%";
    public static final Function<TradeType, String> PRODUCT_PRICE_AVG       = type -> "%product_price_avg_" + type.getLowerCase() + "%";
    public static final Function<TradeType, String> PRODUCT_PRICE_AVG_DIFF  = type -> "%product_price_avg_diff_" + type.getLowerCase() + "%";

    public static final String PRODUCT_PRICER_BUY_MIN              = "%product_pricer_buy_min%";
    public static final String PRODUCT_PRICER_BUY_MAX              = "%product_pricer_buy_max%";
    public static final String PRODUCT_PRICER_SELL_MIN             = "%product_pricer_sell_min%";
    public static final String PRODUCT_PRICER_SELL_MAX             = "%product_pricer_sell_max%";
    public static final String PRODUCT_PRICER_FLOAT_REFRESH_DAYS   = "%product_pricer_float_refresh_days%";
    public static final String PRODUCT_PRICER_FLOAT_REFRESH_TIMES  = "%product_pricer_float_refresh_times%";
    public static final String PRODUCT_PRICER_FLOAT_ROUND_DECIMALS  = "%product_pricer_float_round_decimals%";
    public static final String PRODUCT_PRICER_DYNAMIC_INITIAL_BUY  = "%product_pricer_dynamic_initial_buy%";
    public static final String PRODUCT_PRICER_DYNAMIC_INITIAL_SELL = "%product_pricer_dynamic_initial_sell%";
    public static final String PRODUCT_PRICER_DYNAMIC_STEP_BUY     = "%product_pricer_dynamic_step_buy%";
    public static final String PRODUCT_PRICER_DYNAMIC_STEP_SELL    = "%product_pricer_dynamic_step_sell%";

    public static final String PRODUCT_DISCOUNT_ALLOWED  = "%product_discount_allowed%";
    public static final String PRODUCT_DISCOUNT_AMOUNT   = "%product_discount_amount%";
    public static final String PRODUCT_ITEM_META_ENABLED = "%product_item_meta_enabled%";
    public static final String PRODUCT_PREVIEW_NAME      = "%product_preview_name%";
    public static final String PRODUCT_PREVIEW_LORE      = "%product_preview_lore%";
    public static final String PRODUCT_CURRENCY          = "%product_currency%";

    public static final Function<TradeType, String> PRODUCT_STOCK_AMOUNT_INITIAL = type -> "%product_stock_global_" + type.getLowerCase() + "_amount_initial%";
    public static final Function<TradeType, String> PRODUCT_STOCK_AMOUNT_LEFT    = type -> "%product_stock_global_" + type.getLowerCase() + "_amount_left%";
    public static final Function<TradeType, String> PRODUCT_STOCK_RESTOCK_TIME   = type -> "%product_stock_global_" + type.getLowerCase() + "_restock_time%";
    public static final Function<TradeType, String> PRODUCT_STOCK_RESTOCK_DATE   = type -> "%product_stock_global_" + type.getLowerCase() + "_restock_date%";

    public static final Function<TradeType, String> PRODUCT_LIMIT_AMOUNT_INITIAL = type -> "%product_stock_player_" + type.getLowerCase() + "_amount_initial%";
    public static final Function<TradeType, String> PRODUCT_LIMIT_AMOUNT_LEFT    = type -> "%product_stock_player_" + type.getLowerCase() + "_amount_left%";
    public static final Function<TradeType, String> PRODUCT_LIMIT_RESTOCK_TIME   = type -> "%product_stock_player_" + type.getLowerCase() + "_restock_time%";
    public static final Function<TradeType, String> PRODUCT_LIMIT_RESTOCK_DATE   = type -> "%product_stock_player_" + type.getLowerCase() + "_restock_date%";

    public static final String DISCOUNT_CONFIG_AMOUNT   = "%discount_amount%";
    public static final String DISCOUNT_CONFIG_DURATION = "%discount_duration%";
    public static final String DISCOUNT_CONFIG_DAYS     = "%discount_days%";
    public static final String DISCOUNT_CONFIG_TIMES    = "%discount_times%";

    @NotNull
    public static PlaceholderRelMap<Player> forProduct(@NotNull AbstractProduct<?> product) {
        PlaceholderRelMap<Player> map = new PlaceholderRelMap<>();

        map
            .add(Placeholders.PRODUCT_HANDLER, player -> product.getHandler().getName())
            .add(Placeholders.PRODUCT_CURRENCY, player -> product.getCurrency().getName())
            .add(Placeholders.PRODUCT_PRICE_TYPE, player -> product.getShop().plugin().getLangManager().getEnum(product.getPricer().getType()))
            .add(Placeholders.PRODUCT_PRICE_SELL_ALL, player -> NumberUtil.format(player == null ? 0D : product.getPriceSellAll(player)))
            .add(Placeholders.PRODUCT_PRICE_SELL_ALL_FORMATTED, player -> {
                double price = player == null ? 0D : product.getPriceSellAll(player);

                return price >= 0 ? product.getCurrency().format(price) : "-";
            })
            .add(Placeholders.PRODUCT_PREVIEW_NAME, player -> ItemUtil.getItemName(product.getPreview()))
            .add(Placeholders.PRODUCT_PREVIEW_LORE, player -> String.join("\n", ItemUtil.getLore(product.getPreview())));


        for (TradeType tradeType : TradeType.values()) {

            map.add(Placeholders.PRODUCT_PRICE.apply(tradeType), player -> {
                return player == null ? NumberUtil.format(product.getPricer().getPrice(tradeType)) : NumberUtil.format(product.getPrice(player, tradeType));
            });

            map.add(Placeholders.PRODUCT_PRICE_FORMATTED.apply(tradeType), player -> {
                AbstractProductPricer pricer = product.getPricer();
                double price = player == null ? pricer.getPrice(tradeType) : product.getPrice(player, tradeType);

                return price >= 0 ? product.getCurrency().format(price) : "-";
            });

            // TODO Better format
            map.add(Placeholders.PRODUCT_PRICE_AVG.apply(tradeType), player -> {
                AbstractProductPricer pricer = product.getPricer();
                if (!(pricer instanceof FloatPricer floatPricer)) return NumberUtil.format(pricer.getPrice(tradeType));

                return NumberUtil.format(floatPricer.getPriceAverage(tradeType));
            });

            map.add(Placeholders.PRODUCT_PRICE_AVG_DIFF.apply(tradeType), player -> {
                AbstractProductPricer pricer = product.getPricer();
                if (!(pricer instanceof FloatPricer floatPricer)) return NumberUtil.format(0D);

                double current = floatPricer.getPrice(tradeType);
                double avg = floatPricer.getPriceAverage(tradeType);
                double diff = Math.abs(1D - (current / avg)) * 100D;
                String format = NumberUtil.format(diff) + "%";
                return current > avg ? "+" + format : "-" + format;
            });
        }

        return map;
    }
}
