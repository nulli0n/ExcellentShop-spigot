package su.nightexpress.nexshop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.PreparedProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;
import su.nightexpress.nexshop.shop.impl.AbstractProductPricer;
import su.nightexpress.nexshop.shop.impl.price.DynamicPricer;
import su.nightexpress.nexshop.shop.impl.price.FloatPricer;
import su.nightexpress.nexshop.shop.impl.price.PlayersPricer;
import su.nightexpress.nexshop.shop.impl.price.RangedPricer;
import su.nightexpress.nexshop.shop.util.PlaceholderRelMap;
import su.nightexpress.nexshop.shop.util.ShopUtils;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.util.function.Function;
import java.util.stream.Collectors;

public class Placeholders extends su.nightexpress.nightcore.util.Placeholders {

    public static final String URL_WIKI               = "https://nightexpress.gitbook.io/excellentshop";
    public static final String URL_WIKI_PLACEHOLDERS  = URL_WIKI + "/utility/placeholders#internal-placeholders";
    public static final String URL_WIKI_PRODUCT_STOCK = URL_WIKI + "/features/product-stock";
    public static final String URL_WIKI_CURRENCY      = URL_WIKI + "/features/multi-currency";

    //public static final String CHECK_MARK = Tags.GREEN.enclose("✔");
    //public static final String WRONG_MARK = Tags.RED.enclose("✘");
    //public static final String WARN_MARK  = Tags.ORANGE.enclose("[❗]");

    public static final String GENERIC_BUY        = "%buy%";
    public static final String GENERIC_SELL       = "%sell%";
    public static final String GENERIC_NAME       = "%name%";
    public static final String GENERIC_ITEM       = "%item%";
    public static final String GENERIC_TOTAL      = "%total%";
    public static final String GENERIC_LORE       = "%lore%";
    public static final String GENERIC_AMOUNT     = "%amount%";
    public static final String GENERIC_UNITS      = "%units%";
    public static final String GENERIC_TYPE       = "%type%";
    public static final String GENERIC_TIME       = "%time%";
    public static final String GENERIC_PRICE      = "%price%";
    public static final String GENERIC_BALANCE    = "%balance%";
    public static final String GENERIC_DISCOUNT   = "%discount%";
    public static final String GENERIC_PERMISSION = "%permission%";
    public static final String GENERIC_TAX        = "%tax%";
    public static final String GENERIC_EXPIRE     = "%expire%";
    public static final String GENERIC_PAGE       = "%page%";
    public static final String GENERIC_PAGES      = "%pages%";

    public static final String ITEM_NAME = "%item_name%";
    public static final String ITEM_LORE = "%item_lore%";

    public static final String CURRENCY_NAME = "%currency_name%";
    public static final String CURRENCY_ID   = "%currency_id%";

    public static final String SHOP_ID           = "%shop_id%";
    public static final String SHOP_NAME         = "%shop_name%";
    public static final String SHOP_BUY_ALLOWED  = "%shop_buy_allowed%";
    public static final String SHOP_SELL_ALLOWED = "%shop_sell_allowed%";

    public static final String PRODUCT_HANDLER                  = "%product_handler%";
    public static final String PRODUCT_PRICE_TYPE               = "%product_price_type%";
    public static final String PRODUCT_PRICE_SELL_ALL           = "%product_price_sell_all%";
    public static final String PRODUCT_PRICE_SELL_ALL_FORMATTED = "%product_price_sell_all_formatted%";

    public static final Function<TradeType, String> PRODUCT_PRICE                    = type -> "%product_price_" + type.getLowerCase() + "%";
    public static final Function<TradeType, String> PRODUCT_PRICE_FORMATTED          = type -> "%product_price_" + type.getLowerCase() + "_formatted%";
    public static final Function<TradeType, String> PRODUCT_PRICE_AVERAGE            = type -> "%product_price_avg_" + type.getLowerCase() + "%";
    public static final Function<TradeType, String> PRODUCT_PRICE_AVERAGE_DIFFERENCE = type -> "%product_price_avg_diff_" + type.getLowerCase() + "%";

    public static final Function<TradeType, String> PRODUCT_PRICER_RANGE_MIN = type -> "%product_pricer_" + type.getLowerCase() + "_min%";
    public static final Function<TradeType, String> PRODUCT_PRICER_RANGE_MAX = type -> "%product_pricer_" + type.getLowerCase() + "_max%";

    public static final String PRODUCT_PRICER_FLOAT_REFRESH_DAYS   = "%product_pricer_float_refresh_days%";
    public static final String PRODUCT_PRICER_FLOAT_REFRESH_TIMES  = "%product_pricer_float_refresh_times%";
    public static final String PRODUCT_PRICER_FLOAT_ROUND_DECIMALS = "%product_pricer_float_round_decimals%";

    public static final String PRODUCT_PRICER_DYNAMIC_INITIAL_BUY  = "%product_pricer_dynamic_initial_buy%";
    public static final String PRODUCT_PRICER_DYNAMIC_INITIAL_SELL = "%product_pricer_dynamic_initial_sell%";
    public static final String PRODUCT_PRICER_DYNAMIC_STEP_BUY     = "%product_pricer_dynamic_step_buy%";
    public static final String PRODUCT_PRICER_DYNAMIC_STEP_SELL    = "%product_pricer_dynamic_step_sell%";

    public static final String PRODUCT_PRICER_PLAYERS_ADJUST_AMOUNT_BUY  = "%product_pricer_players_adjust_amount_buy%";
    public static final String PRODUCT_PRICER_PLAYERS_ADJUST_AMOUNT_SELL = "%product_pricer_players_adjust_amount_sell%";
    public static final String PRODUCT_PRICER_PLAYERS_ADJUST_STEP        = "%product_pricer_players_adjust_step%";

    public static final String PRODUCT_ID = "%product_id%";
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

    /*@NotNull
    public static String good(@NotNull String text) {
        return CHECK_MARK + Tags.GRAY.enclose(text);
    }

    @NotNull
    public static String bad(@NotNull String text) {
        return WRONG_MARK + Tags.GRAY.enclose(text);
    }

    @NotNull
    public static String warn(@NotNull String text) {
        return WARN_MARK + Tags.GRAY.enclose(text);
    }*/

    @NotNull
    public static PlaceholderMap forShop(@NotNull Shop shop) {
        return new PlaceholderMap()
            .add(SHOP_ID, shop::getId)
            .add(SHOP_NAME, shop::getName)
            .add(SHOP_BUY_ALLOWED, () -> Lang.getYesOrNo(shop.isTransactionEnabled(TradeType.BUY)))
            .add(SHOP_SELL_ALLOWED, () -> Lang.getYesOrNo(shop.isTransactionEnabled(TradeType.SELL)));
    }

    @NotNull
    public static PlaceholderRelMap<Player> forProduct(@NotNull AbstractProduct<?> product) {
        PlaceholderRelMap<Player> map = new PlaceholderRelMap<>();

        map
            .add(PRODUCT_ID, player -> product.getId())
            .add(PRODUCT_HANDLER, player -> product.getHandler().getName())
            .add(PRODUCT_CURRENCY, player -> product.getCurrency().getName())
            .add(PRODUCT_PRICE_TYPE, player -> Lang.PRICE_TYPES.getLocalized(product.getPricer().getType()))
            .add(PRODUCT_PRICE_SELL_ALL, player -> NumberUtil.format(player == null ? 0D : product.getPriceSellAll(player)))
            .add(PRODUCT_PRICE_SELL_ALL_FORMATTED, player -> {
                if (!product.isSellable()) return "-";

                double price = player == null ? 0D : product.getPriceSellAll(player);
                return price >= 0 ? product.getCurrency().format(price) : "-";
            })
            .add(PRODUCT_PREVIEW_NAME, player -> ItemUtil.getItemName(product.getPreview()))
            .add(PRODUCT_PREVIEW_LORE, player -> String.join("\n", ItemUtil.getLore(product.getPreview())));


        for (TradeType tradeType : TradeType.values()) {

            map.add(PRODUCT_PRICE.apply(tradeType), player -> {
                Currency currency = product.getCurrency();
                return player == null ? currency.format(product.getPricer().getPrice(tradeType)) : currency.format(product.getPrice(tradeType, player));
            });

            map.add(PRODUCT_PRICE_FORMATTED.apply(tradeType), player -> {
                AbstractProductPricer pricer = product.getPricer();
                double price = player == null ? pricer.getPrice(tradeType) : product.getPrice(tradeType, player);

                return price >= 0 ? product.getCurrency().format(price) : "-";
            });

            map.add(PRODUCT_PRICE_AVERAGE.apply(tradeType), player -> {
                AbstractProductPricer pricer = product.getPricer();
                if (!(pricer instanceof RangedPricer rangedPricer)) return NumberUtil.format(pricer.getPrice(tradeType));

                return product.getCurrency().format(rangedPricer.getPriceAverage(tradeType));
            });

            map.add(PRODUCT_PRICE_AVERAGE_DIFFERENCE.apply(tradeType), player -> {
                AbstractProductPricer pricer = product.getPricer();
                if (!(pricer instanceof RangedPricer rangedPricer)) return NumberUtil.format(0D);

                double percent = rangedPricer.getAverageDifferencePercent(tradeType);

                return (percent > 0 ? VirtualLang.PRICE_AVG_DIFF_UP : VirtualLang.PRICE_AVG_DIFF_DOWN).getString()
                    .replace(GENERIC_VALUE, NumberUtil.format(Math.abs(percent)));
            });
        }

        return map;
    }

    @NotNull
    public static PlaceholderMap forPreparedProduct(@NotNull PreparedProduct product) {
        return new PlaceholderMap()
            .add(GENERIC_ITEM, () -> ItemUtil.getItemName(product.getProduct().getPreview()))
            .add(GENERIC_AMOUNT, () -> String.valueOf(product.getAmount()))
            .add(GENERIC_UNITS, () -> String.valueOf(product.getUnits()))
            .add(GENERIC_TYPE, () -> Lang.TRADE_TYPES.getLocalized(product.getTradeType()))
            .add(GENERIC_PRICE, () -> product.getProduct().getCurrency().format(product.getPrice()))
        ;
    }

    @NotNull
    public static PlaceholderMap forRangedPricer(@NotNull RangedPricer pricer) {
        PlaceholderMap placeholderMap = new PlaceholderMap();

        for (TradeType tradeType : TradeType.values()) {
            placeholderMap
                .add(PRODUCT_PRICER_RANGE_MIN.apply(tradeType), () -> String.valueOf(pricer.getPriceMin(tradeType)))
                .add(PRODUCT_PRICER_RANGE_MAX.apply(tradeType), () -> String.valueOf(pricer.getPriceMax(tradeType)));
        }

        return placeholderMap;
    }

    @NotNull
    public static PlaceholderMap forFloatPricer(@NotNull FloatPricer pricer) {
        return new PlaceholderMap()
            .add(PRODUCT_PRICER_FLOAT_REFRESH_DAYS, () -> {
                if (pricer.getDays().isEmpty()) {
                    return Lang.badEntry(Lang.EDITOR_PRICE_FLOAT_NO_DAYS.getString());
                }
                return pricer.getDays().stream().map(day -> Lang.goodEntry(Lang.DAYS.getLocalized(day))).collect(Collectors.joining("\n"));
            })
            .add(PRODUCT_PRICER_FLOAT_REFRESH_TIMES, () -> {
                if (pricer.getTimes().isEmpty()) {
                    return Lang.badEntry(Lang.EDITOR_PRICE_FLOAT_NO_TIMES.getString());
                }
                return pricer.getTimes().stream().map(ShopUtils.TIME_FORMATTER::format).map(Lang::goodEntry).collect(Collectors.joining("\n"));
            })
            .add(PRODUCT_PRICER_FLOAT_ROUND_DECIMALS, () -> Lang.getYesOrNo(pricer.isRoundDecimals()));
    }

    @NotNull
    public static PlaceholderMap forDynamicPricer(@NotNull DynamicPricer pricer) {
        return new PlaceholderMap()
            .add(PRODUCT_PRICER_DYNAMIC_INITIAL_BUY, () -> NumberUtil.format(pricer.getInitial(TradeType.BUY)))
            .add(PRODUCT_PRICER_DYNAMIC_INITIAL_SELL, () -> NumberUtil.format(pricer.getInitial(TradeType.SELL)))
            .add(PRODUCT_PRICER_DYNAMIC_STEP_BUY, () -> NumberUtil.format(pricer.getStep(TradeType.BUY)))
            .add(PRODUCT_PRICER_DYNAMIC_STEP_SELL, () -> NumberUtil.format(pricer.getStep(TradeType.SELL)));
    }

    @NotNull
    public static PlaceholderMap forPlayersPricer(@NotNull PlayersPricer pricer) {
        return new PlaceholderMap()
            .add(PRODUCT_PRICER_DYNAMIC_INITIAL_BUY, () -> NumberUtil.format(pricer.getInitial(TradeType.BUY)))
            .add(PRODUCT_PRICER_DYNAMIC_INITIAL_SELL, () -> NumberUtil.format(pricer.getInitial(TradeType.SELL)))
            .add(PRODUCT_PRICER_PLAYERS_ADJUST_AMOUNT_BUY, () -> NumberUtil.format(pricer.getAdjustAmount(TradeType.BUY)))
            .add(PRODUCT_PRICER_PLAYERS_ADJUST_AMOUNT_SELL, () -> NumberUtil.format(pricer.getAdjustAmount(TradeType.SELL)))
            .add(PRODUCT_PRICER_PLAYERS_ADJUST_STEP, () -> NumberUtil.format(pricer.getAdjustStep()));
    }
}
