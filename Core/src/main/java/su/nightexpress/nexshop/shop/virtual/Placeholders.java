package su.nightexpress.nexshop.shop.virtual;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualProduct;
import su.nightexpress.nexshop.shop.util.PlaceholderRelMap;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualStock;

public class Placeholders extends su.nightexpress.nexshop.Placeholders {

    public static final String SHOP_TYPE                = "%shop_type%";
    public static final String SHOP_DESCRIPTION         = "%shop_description%";
    public static final String SHOP_PERMISSION_REQUIRED = "%shop_permission_required%";
    public static final String SHOP_PERMISSION_NODE     = "%shop_permission_node%";
    public static final String SHOP_PAGES               = "%shop_pages%";
    public static final String SHOP_VIEW_SIZE           = "%shop_view_size%";
    public static final String SHOP_VIEW_TITLE          = "%shop_view_title%";
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
    public static final String PRODUCT_ALLOWED_RANKS        = "%product_allowed_ranks%";
    public static final String PRODUCT_REQUIRED_PERMISSIONS = "%product_required_permissions%";
    public static final String PRODUCT_ROTATION_CHANCE      = "%product_rotation_chance%";

    /**
     * @return PlaceholderMap with <b>additional</b> placeholders only.
     */
    @NotNull
    public static PlaceholderRelMap<Player> forVirtualProduct(@NotNull AbstractVirtualProduct<?> product) {
        return new PlaceholderRelMap<Player>()
                .add(forProductStock(product))
                .add(PRODUCT_DISCOUNT_AMOUNT, player -> NumberUtil.format(product.getShop().getDiscountPlain(product)))
                .add(PRODUCT_DISCOUNT_ALLOWED, player -> LangManager.getBoolean(product.isDiscountAllowed()))
                .add(PRODUCT_ALLOWED_RANKS, player -> String.join("\n", product.getAllowedRanks()))
                .add(PRODUCT_REQUIRED_PERMISSIONS, player -> String.join("\n", product.getRequiredPermissions()));
    }

    @NotNull
    public static PlaceholderRelMap<Player> forProductStock(@NotNull AbstractVirtualProduct<?> product) {
        String never = LangManager.getPlain(Lang.OTHER_NEVER);
        String infin = LangManager.getPlain(Lang.OTHER_INFINITY);

        PlaceholderRelMap<Player> map = new PlaceholderRelMap<>();
        VirtualStock stock = product.getShop().getStock();

        for (TradeType tradeType : TradeType.values()) {
            map
                .add(PRODUCT_STOCK_AMOUNT_INITIAL.apply(tradeType), player -> {
                    int initialAmount = product.getStockValues().getInitialAmount(tradeType);
                    return initialAmount < 0 ? infin : String.valueOf(initialAmount);
                })
                .add(PRODUCT_STOCK_AMOUNT_LEFT.apply(tradeType), player -> {
                    int leftAmount = stock.countItem(product, tradeType, null);
                    return leftAmount < 0 ? infin : String.valueOf(leftAmount);
                })
                .add(PRODUCT_STOCK_RESTOCK_TIME.apply(tradeType), player -> {
                    long cooldown = product.getStockValues().getRestockTime(tradeType);
                    return cooldown < 0 ? never : TimeUtil.formatTime(cooldown);
                })
                .add(PRODUCT_STOCK_RESTOCK_DATE.apply(tradeType), player -> {
                    long restockDate = stock.getRestockDate(product, tradeType);
                    if (restockDate == 0L) return TimeUtil.formatTime(product.getStockValues().getRestockTime(tradeType));

                    return restockDate < 0 ? never : TimeUtil.formatTimeLeft(restockDate);
                })
                .add(PRODUCT_LIMIT_AMOUNT_INITIAL.apply(tradeType), player -> {
                    int initialAmount = product.getLimitValues().getInitialAmount(tradeType);
                    return initialAmount < 0 ? infin : String.valueOf(initialAmount);
                })
                .add(PRODUCT_LIMIT_RESTOCK_TIME.apply(tradeType), player -> {
                    long cooldown = product.getLimitValues().getRestockTime(tradeType);
                    return cooldown < 0 ? never : TimeUtil.formatTime(cooldown);
                })
                .add(PRODUCT_LIMIT_AMOUNT_LEFT.apply(tradeType), player -> {
                    int leftAmount = stock.countItem(product, tradeType, player);
                    return leftAmount < 0 ? infin : String.valueOf(leftAmount);
                })
                .add(PRODUCT_LIMIT_RESTOCK_DATE.apply(tradeType), player -> {
                    long restockDate = stock.getRestockDate(product, tradeType, player);
                    if (restockDate == 0L) return TimeUtil.formatTime(product.getLimitValues().getRestockTime(tradeType));

                    return restockDate < 0 ? never : TimeUtil.formatTimeLeft(restockDate);
                });
        }

        return map;
    }
}
