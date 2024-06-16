package su.nightexpress.nexshop.shop.virtual;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.util.PlaceholderRelMap;
import su.nightexpress.nexshop.shop.util.ShopUtils;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualStock;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Placeholders extends su.nightexpress.nexshop.Placeholders {


    public static final String GENERIC_SELL_MULTIPLIER = "%sell_multiplier%";

    public static final String SHOP_TYPE                = "%shop_type%";
    public static final String SHOP_DESCRIPTION         = "%shop_description%";
    public static final String SHOP_PERMISSION_REQUIRED = "%shop_permission_required%";
    public static final String SHOP_PERMISSION_NODE     = "%shop_permission_node%";
    public static final String SHOP_PAGES               = "%shop_pages%";
    public static final String SHOP_LAYOUT              = "%shop_layout%";
    public static final String SHOP_NPC_IDS             = "%shop_npc_ids%";
    public static final String SHOP_DISCOUNT_AMOUNT     = "%shop_discount_amount%";
    public static final String SHOP_NEXT_ROTATION_DATE  = "%shop_next_rotation_date%";
    public static final String SHOP_NEXT_ROTATION_IN    = "%shop_next_rotation_in%";
    public static final String SHOP_ROTATION_TYPE       = "%shop_rotation_type%";
    public static final String SHOP_ROTATION_INTERVAL   = "%shop_rotation_interval%";

    public static final String SHOP_ROTATION_MIN_PRODUCTS  = "%shop_rotation_min_products%";
    public static final String SHOP_ROTATION_MAX_PRODUCTS  = "%shop_rotation_max_products%";
    public static final String SHOP_ROTATION_PRODUCT_SLOTS = "%shop_rotation_product_slots%";

    public static final String PRODUCT_COMMANDS             = "%product_commands%";
    public static final String PRODUCT_DISCOUNT_ALLOWED     = "%product_discount_allowed%";
    public static final String PRODUCT_DISCOUNT_AMOUNT      = "%product_discount_amount%";
    public static final String PRODUCT_ALLOWED_RANKS        = "%product_allowed_ranks%";
    public static final String PRODUCT_REQUIRED_PERMISSIONS = "%product_required_permissions%";
    public static final String PRODUCT_ROTATION_CHANCE      = "%product_rotation_chance%";

    public static final Function<TradeType, String> STOCK_TYPE = tradeType -> "%stock_global_" + tradeType.name().toLowerCase() + "%";
    public static final Function<TradeType, String> LIMIT_TYPE = tradeType -> "%stock_player_" + tradeType.name().toLowerCase() + "%";

    public static final Function<TradeType, String> PRICE_DYNAMIC = tradeType -> "%price_dynamic_" + tradeType.name().toLowerCase() + "%";


    /**
     * @return PlaceholderMap with <b>additional</b> placeholders only.
     */
    @NotNull
    public static PlaceholderMap forVirtualShop(@NotNull VirtualShop shop) {
        return new PlaceholderMap()
            .add(SHOP_TYPE, () -> VirtualLang.SHOP_TYPES.getLocalized(shop.getType()))
            .add(SHOP_DESCRIPTION, () -> String.join("\n", shop.getDescription()))
            .add(SHOP_PERMISSION_NODE, () -> VirtualPerms.PREFIX_SHOP + shop.getId())
            .add(SHOP_PERMISSION_REQUIRED, () -> Lang.getYesOrNo(shop.isPermissionRequired()))
            .add(SHOP_LAYOUT, shop::getLayoutName)
            .add(SHOP_DISCOUNT_AMOUNT, () -> NumberUtil.format(shop.getDiscountPlain()))
            .add(SHOP_NPC_IDS, () -> String.join(", ", shop.getNPCIds().stream().map(String::valueOf).toList()));
    }

    /**
     * @return PlaceholderMap with <b>additional</b> placeholders only.
     */
    @NotNull
    public static PlaceholderMap forStaticShop(@NotNull StaticShop shop) {
        return new PlaceholderMap()
            .add(SHOP_PAGES, () -> String.valueOf(shop.getPages()));
    }

    /**
     * @return PlaceholderMap with <b>additional</b> placeholders only.
     */
    @NotNull
    public static PlaceholderMap forRotatingShop(@NotNull RotatingShop shop) {
        return new PlaceholderMap()
            .add(SHOP_PAGES, () -> {
                double limit = shop.getProductSlots().length;
                double products = shop.getData().getProducts().size();

                return NumberUtil.format(Math.ceil(products / limit));
            })
            .add(SHOP_NEXT_ROTATION_DATE, () -> {
                LocalDateTime time = shop.getNextRotationTime();
                if (time == null) return Lang.OTHER_NEVER.getString();

                return time.format(ShopUtils.getDateFormatter());
            })
            .add(SHOP_NEXT_ROTATION_IN, () -> {
                LocalDateTime next = shop.getNextRotationTime();
                if (next == null) return Lang.OTHER_NEVER.getString();

                return TimeUtil.formatDuration(TimeUtil.toEpochMillis(next));
            })
            .add(SHOP_ROTATION_TYPE, () -> shop.getRotationType().name())
            .add(SHOP_ROTATION_INTERVAL, () -> TimeUtil.formatTime(shop.getRotationInterval() * 1000L))
            .add(SHOP_ROTATION_MIN_PRODUCTS, () -> NumberUtil.format(shop.getProductMinAmount()))
            .add(SHOP_ROTATION_MAX_PRODUCTS, () -> NumberUtil.format(shop.getProductMaxAmount()))
            .add(SHOP_ROTATION_PRODUCT_SLOTS, () -> Arrays.toString(shop.getProductSlots()));
    }

    /**
     * @return PlaceholderMap with <b>additional</b> placeholders only.
     */
    @NotNull
    public static PlaceholderRelMap<Player> forVirtualProduct(@NotNull VirtualProduct product) {
        return new PlaceholderRelMap<Player>()
                .add(forProductStock(product))
                .add(PRODUCT_DISCOUNT_AMOUNT, player -> NumberUtil.format(product.getShop().getDiscountPlain(product)))
                .add(PRODUCT_DISCOUNT_ALLOWED, player -> Lang.getYesOrNo(product.isDiscountAllowed()))
                .add(PRODUCT_ALLOWED_RANKS, player -> {
                    if (product.getAllowedRanks().isEmpty()) {
                        return Lang.goodEntry(VirtualLang.EDITOR_PRODUCT_NO_RANK_REQUIREMENTS.getString());
                    }
                    return product.getAllowedRanks().stream().map(Lang::goodEntry).collect(Collectors.joining("\n"));
                })
                .add(PRODUCT_REQUIRED_PERMISSIONS, player -> {
                    if (product.getRequiredPermissions().isEmpty()) {
                        return Lang.goodEntry(VirtualLang.EDITOR_PRODUCT_NO_PERM_REQUIREMENTS.getString());
                    }
                    return product.getRequiredPermissions().stream().map(Lang::goodEntry).collect(Collectors.joining("\n"));
                });
    }

    @NotNull
    public static PlaceholderRelMap<Player> forProductStock(@NotNull VirtualProduct product) {
        String never = Lang.OTHER_NEVER.getString();
        String infinity = Lang.OTHER_INFINITY.getString();

        PlaceholderRelMap<Player> map = new PlaceholderRelMap<>();
        VirtualStock stock = (VirtualStock) product.getShop().getStock();

        for (TradeType tradeType : TradeType.values()) {
            map
                .add(PRODUCT_STOCK_AMOUNT_INITIAL.apply(tradeType), player -> {
                    int initialAmount = product.getStockValues().getInitialAmount(tradeType);
                    return initialAmount < 0 ? infinity : String.valueOf(initialAmount);
                })
                .add(PRODUCT_STOCK_AMOUNT_LEFT.apply(tradeType), player -> {
                    int leftAmount = stock.countItem(product, tradeType, null);
                    return leftAmount < 0 ? infinity : String.valueOf(leftAmount);
                })
                .add(PRODUCT_STOCK_RESTOCK_TIME.apply(tradeType), player -> {
                    long cooldown = product.getStockValues().getRestockTime(tradeType);
                    return cooldown < 0 ? never : TimeUtil.formatTime(cooldown);
                })
                .add(PRODUCT_STOCK_RESTOCK_DATE.apply(tradeType), player -> {
                    long restockDate = stock.getRestockDate(product, tradeType);
                    if (restockDate == 0L) return TimeUtil.formatTime(product.getStockValues().getRestockTime(tradeType));

                    return restockDate < 0 ? never : TimeUtil.formatDuration(restockDate);
                })
                .add(PRODUCT_LIMIT_AMOUNT_INITIAL.apply(tradeType), player -> {
                    int initialAmount = product.getLimitValues().getInitialAmount(tradeType);
                    return initialAmount < 0 ? infinity : String.valueOf(initialAmount);
                })
                .add(PRODUCT_LIMIT_RESTOCK_TIME.apply(tradeType), player -> {
                    long cooldown = product.getLimitValues().getRestockTime(tradeType);
                    return cooldown < 0 ? never : TimeUtil.formatTime(cooldown);
                })
                .add(PRODUCT_LIMIT_AMOUNT_LEFT.apply(tradeType), player -> {
                    int leftAmount = stock.countItem(product, tradeType, player);
                    return leftAmount < 0 ? infinity : String.valueOf(leftAmount);
                })
                .add(PRODUCT_LIMIT_RESTOCK_DATE.apply(tradeType), player -> {
                    long restockDate = stock.getRestockDate(product, tradeType, player);
                    if (restockDate == 0L) return TimeUtil.formatTime(product.getLimitValues().getRestockTime(tradeType));

                    return restockDate < 0 ? never : TimeUtil.formatDuration(restockDate);
                });
        }

        return map;
    }
}
