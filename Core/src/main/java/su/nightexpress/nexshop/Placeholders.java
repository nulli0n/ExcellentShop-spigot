package su.nightexpress.nexshop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.product.PreparedProduct;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.typing.CommandTyping;
import su.nightexpress.nexshop.api.shop.product.typing.VanillaTyping;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.shop.RotationData;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nexshop.product.price.impl.DynamicPricer;
import su.nightexpress.nexshop.product.price.impl.FloatPricer;
import su.nightexpress.nexshop.product.price.impl.PlayersPricer;
import su.nightexpress.nexshop.product.price.impl.RangedPricer;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.rent.RentSettings;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.impl.Rotation;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.util.ItemNbt;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.PlaceholderList;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Placeholders extends su.nightexpress.nightcore.util.Placeholders {

    public static final String URL_WIKI               = "https://nightexpress.gitbook.io/excellentshop";
    public static final String URL_WIKI_PLACEHOLDERS  = URL_WIKI + "/utility/placeholders#internal-placeholders";
    public static final String URL_WIKI_PRODUCT_STOCK = URL_WIKI + "/features/product-stock";
    public static final String URL_WIKI_CURRENCY      = URL_WIKI + "/features/multi-currency";

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
    public static final String GENERIC_WEIGHT     = "%weight%";

    public static final String                      GENERIC_PRODUCT_NAME    = "%product_name%";
    public static final String                      GENERIC_PRODUCT_STOCK   = "%product_stock%";
    public static final Function<TradeType, String> GENERIC_PRODUCT_PRICE   = tradeType -> "%product_price_" + tradeType.getLowerCase() + "%";
    public static final String                      GENERIC_SELL_MULTIPLIER = "%sell_multiplier%";

    public static final Function<TradeType, String> STOCK_TYPE    = tradeType -> "%stock_global_" + tradeType.name().toLowerCase() + "%";
    public static final Function<TradeType, String> LIMIT_TYPE    = tradeType -> "%stock_player_" + tradeType.name().toLowerCase() + "%";
    public static final Function<TradeType, String> PRICE_DYNAMIC = tradeType -> "%price_dynamic_" + tradeType.name().toLowerCase() + "%";

    public static final String ITEM_NAME = "%item_name%";
    public static final String ITEM_LORE = "%item_lore%";

    public static final String SHOP_ID           = "%shop_id%";
    public static final String SHOP_NAME           = "%shop_name%";
    public static final String SHOP_BUYING_ALLOWED  = "%shop_buy_allowed%";
    public static final String SHOP_SELLING_ALLOWED = "%shop_sell_allowed%";
    public static final String SHOP_PRODUCTS        = "%shop_products%";

    // Chest shop
    public static final String CHEST_SHOP_OWNER            = "%shop_owner%";
    public static final String CHEST_SHOP_X                = "%shop_location_x%";
    public static final String CHEST_SHOP_Y                = "%shop_location_y%";
    public static final String CHEST_SHOP_Z                = "%shop_location_z%";
    public static final String CHEST_SHOP_WORLD            = "%shop_location_world%";
    public static final String CHEST_SHOP_IS_ADMIN         = "%shop_is_admin%";
    public static final String CHEST_SHOP_BANK_BALANCE     = "%shop_bank_balance%";
    public static final String CHEST_SHOP_HOLOGRAM_ENABLED = "%shop_hologram_enabled%";
    public static final String CHEST_SHOP_SHOWCASE_ENABLED = "%shop_showcase_enabled%";
    public static final String CHEST_SHOP_RENT_EXPIRES_IN  = "%shop_rent_expires_in%";
    public static final String CHEST_SHOP_RENTER_NAME      = "%shop_rented_by%";
    public static final String CHEST_SHOP_RENT_DURATION      = "%shop_rent_duration%";
    public static final String CHEST_SHOP_RENT_PRICE      = "%shop_rent_price%";

    public static final String RENT_ENABLED = "%rent_enabled%";
    public static final String RENT_CURRENCY = "%rent_currency%";
    public static final String RENT_CURRENCY_NAME = "%rent_currency_name%";
    public static final String RENT_PRICE = "%rent_price%";
    public static final String RENT_DURATION = "%rent_duration%";

    // Virtual shop
    public static final String VIRTUAL_SHOP_ICON_NAME           = "%shop_icon_name%";
    public static final String VIRTUAL_SHOP_DESCRIPTION         = "%shop_description%";
    public static final String VIRTUAL_SHOP_PERMISSION_REQUIRED = "%shop_permission_required%";
    public static final String VIRTUAL_SHOP_PERMISSION_NODE     = "%shop_permission_node%";
    public static final String VIRTUAL_SHOP_PAGES               = "%shop_pages%";
    public static final String VIRTUAL_SHOP_ALIASES             = "%shop_aliases%";
    public static final String VIRTUAL_SHOP_MENU_SLOT           = "%shop_menu_slot%";
    public static final String VIRTUAL_SHOP_DEFAULT_LAYOUT      = "%shop_layout%";
    public static final String VIRTUAL_SHOP_DISCOUNT_AMOUNT     = "%shop_discount_amount%";

    public static final Function<Integer, String>              CHEST_SHOP_PRODUCT_NAME  = (slot) -> "%shop_product_name_" + slot + "%";
    public static final BiFunction<TradeType, Integer, String> CHEST_SHOP_PRODUCT_PRICE = (tradeType, slot) -> "%shop_product_price_" + tradeType.getLowerCase() + "_" + slot + "%";

    public static final String PRODUCT_HANDLER                  = "%product_handler%";
    public static final String PRODUCT_PRICE_TYPE               = "%product_price_type%";
    public static final String PRODUCT_PRICE_SELL_ALL           = "%product_price_sell_all%";
    public static final String PRODUCT_PRICE_SELL_ALL_FORMATTED = "%product_price_sell_all_formatted%";

    public static final Function<TradeType, String> PRODUCT_PRICE                    = type -> "%product_price_" + type.getLowerCase() + "%";
    public static final Function<TradeType, String> PRODUCT_PRICE_FORMATTED          = type -> "%product_price_" + type.getLowerCase() + "_formatted%";
    public static final Function<TradeType, String> PRODUCT_PRICE_AVERAGE            = type -> "%product_price_avg_" + type.getLowerCase() + "%";
    public static final Function<TradeType, String> PRODUCT_PRICE_AVERAGE_DIFFERENCE = type -> "%product_price_avg_diff_" + type.getLowerCase() + "%";

    public static final Function<TradeType, String> PRICER_RANGED_BOUNDS_MIN = type -> "%product_pricer_" + type.getLowerCase() + "_min%";
    public static final Function<TradeType, String> PRICER_RANGED_BOUNDS_MAX = type -> "%product_pricer_" + type.getLowerCase() + "_max%";

    public static final String PRICER_FLOAT_REFRESH_TYPE     = "%product_pricer_float_refresh_type%";
    public static final String PRICER_FLOAT_REFRESH_INTERVAL = "%product_pricer_float_refresh_interval%";
    public static final String PRICER_FLOAT_REFRESH_DAYS     = "%product_pricer_float_refresh_days%";
    public static final String PRICER_FLOAT_REFRESH_TIMES    = "%product_pricer_float_refresh_times%";
    public static final String PRICER_FLOAT_ROUND_DECIMALS   = "%product_pricer_float_round_decimals%";

    public static final String PRICER_DYNAMIC_INITIAL_BUY  = "%product_pricer_dynamic_initial_buy%";
    public static final String PRICER_DYNAMIC_INITIAL_SELL = "%product_pricer_dynamic_initial_sell%";
    public static final String PRICER_DYNAMIC_STEP_BUY     = "%product_pricer_dynamic_step_buy%";
    public static final String PRICER_DYNAMIC_STEP_SELL    = "%product_pricer_dynamic_step_sell%";

    public static final String PRICER_PLAYERS_ADJUST_AMOUNT_BUY  = "%product_pricer_players_adjust_amount_buy%";
    public static final String PRICER_PLAYERS_ADJUST_AMOUNT_SELL = "%product_pricer_players_adjust_amount_sell%";
    public static final String PRICER_PLAYERS_ADJUST_STEP        = "%product_pricer_players_adjust_step%";

    public static final String PRODUCT_ID           = "%product_id%";
    public static final String PRODUCT_RESPECT_META = "%product_item_meta_enabled%";
    public static final String PRODUCT_PREVIEW_NAME = "%product_preview_name%";
    public static final String PRODUCT_PREVIEW_LORE = "%product_preview_lore%";
    public static final String PRODUCT_UNIT_AMOUNT  = "%product_unit_amount%";
    public static final String PRODUCT_CURRENCY     = "%product_currency%";

    public static final Function<TradeType, String> PRODUCT_STOCK_AMOUNT_INITIAL = type -> "%product_stock_global_" + type.getLowerCase() + "_amount_initial%";
    public static final Function<TradeType, String> PRODUCT_STOCK_AMOUNT_LEFT    = type -> "%product_stock_global_" + type.getLowerCase() + "_amount_left%";

    public static final Function<TradeType, String> PRODUCT_LIMIT_AMOUNT_INITIAL = type -> "%product_stock_player_" + type.getLowerCase() + "_amount_initial%";
    public static final Function<TradeType, String> PRODUCT_LIMIT_AMOUNT_LEFT    = type -> "%product_stock_player_" + type.getLowerCase() + "_amount_left%";

    @Deprecated
    public static final Function<TradeType, String> PRODUCT_LIMIT_RESTOCK_DATE   = type -> "%product_stock_player_" + type.getLowerCase() + "_restock_date%";
    @Deprecated
    public static final Function<TradeType, String> PRODUCT_STOCK_RESTOCK_DATE   = type -> "%product_stock_global_" + type.getLowerCase() + "_restock_date%";

    public static final String PRODUCT_STOCKS_RESET_IN   = "%product_stocks_restock_date%";
    public static final String PRODUCT_STOCKS_RESET_TIME = "%product_stocks_restock_time%";
    public static final String PRODUCT_LIMITS_RESET_IN   = "%product_limits_restock_date%";
    public static final String PRODUCT_LIMITS_RESET_TIME = "%product_limits_restock_time%";

    // Virtual
    public static final String PRODUCT_COMMANDS             = "%product_commands%";
    public static final String PRODUCT_DISCOUNT_ALLOWED     = "%product_discount_allowed%";
    public static final String PRODUCT_DISCOUNT_AMOUNT      = "%product_discount_amount%";
    public static final String PRODUCT_ALLOWED_RANKS        = "%product_allowed_ranks%";
    public static final String PRODUCT_REQUIRED_PERMISSIONS = "%product_required_permissions%";
    public static final String PRODUCT_FORBIDDEN_PERMISSIONS = "%product_forbidden_permissions%";

    public static final String DISCOUNT_CONFIG_AMOUNT   = "%discount_amount%";
    public static final String DISCOUNT_CONFIG_DURATION = "%discount_duration%";
    public static final String DISCOUNT_CONFIG_DAYS     = "%discount_days%";
    public static final String DISCOUNT_CONFIG_TIMES    = "%discount_times%";

    public static final String ROTATION_ID         = "%rotation_id%";
    public static final String ROTATION_TYPE         = "%rotation_type%";
    public static final String ROTATION_INTERVAL     = "%rotation_interval%";
    public static final String ROTATION_SLOTS_AMOUNT = "%rotation_slots_amount%";
    public static final String ROTATION_ITEMS_AMOUNT = "%rotation_items_amount%";

    public static final Function<Rotation, String> SHOP_ROTATION_NEXT_DATE = rotation -> "%shop_rotation_" + rotation.getId() + "_next_date%";
    public static final Function<Rotation, String> SHOP_ROTATION_NEXT_IN = rotation -> "%shop_rotation_" + rotation.getId() + "_next_in%";

    // Auction
    public static final String LISTING_ITEM_NAME     = "%listing_item_name%";
    public static final String LISTING_ITEM_LORE     = "%listing_item_lore%";
    public static final String LISTING_ITEM_AMOUNT   = "%listing_item_amount%";
    public static final String LISTING_ITEM_VALUE    = "%listing_item_value%";
    public static final String LISTING_SELLER        = "%listing_seller%";
    public static final String LISTING_PRICE         = "%listing_price%";
    public static final String LISTING_DATE_CREATION = "%listing_date_creation%";
    public static final String LISTING_DELETES_IN    = "%listing_deletes_in%";
    public static final String LISTING_DELETE_DATE   = "%listing_delete_date%";
    public static final String LISTING_EXPIRES_IN    = "%listing_expires_in%";
    public static final String LISTING_EXPIRE_DATE   = "%listing_expire_date%";
    public static final String LISTING_BUYER         = "%listing_buyer%";
    public static final String LISTING_BUY_DATE      = "%listing_buy_date%";

    public static final String CATEGORY_ID   = "%category_id%";
    public static final String CATEGORY_NAME = "%category_name%";

    public record ProductPOV<T extends Product>(@NotNull T product, @Nullable Player player){

        @Nullable
        public UUID playerId() {
            return this.player == null ? null : this.player.getUniqueId();
        }
    }

    @NotNull
    public static final PlaceholderList<Transaction> TRANSACTION = PlaceholderList.create(list -> list
        .add(GENERIC_TYPE, transaction -> Lang.TRADE_TYPES.getLocalized(transaction.getTradeType()))
        .add(GENERIC_AMOUNT, transaction -> String.valueOf(transaction.getAmount()))
        .add(GENERIC_UNITS, transaction -> String.valueOf(transaction.getUnits()))
        .add(GENERIC_PRICE, transaction -> transaction.getProduct().getCurrency().format(transaction.getPrice()))
        .add(GENERIC_ITEM, transaction -> ItemUtil.getSerializedName(transaction.getProduct().getPreview()))
    );

    // ------------------------------
    // Shops
    // ------------------------------

    @NotNull
    public static final PlaceholderList<Shop> SHOP = PlaceholderList.create(list -> list
        .add(SHOP_ID, Shop::getId)
        .add(SHOP_NAME, Shop::getName)
        .add(SHOP_BUYING_ALLOWED, shop -> Lang.getEnabledOrDisabled(shop.isBuyingAllowed()))
        .add(SHOP_SELLING_ALLOWED, shop -> Lang.getEnabledOrDisabled(shop.isSellingAllowed()))
        .add(SHOP_PRODUCTS, shop -> NumberUtil.format(shop.countProducts()))
    );

    @NotNull
    public static final PlaceholderList<ChestShop> CHEST_SHOP = PlaceholderList.create(list -> {
        list
            .add(SHOP)
            .add(CHEST_SHOP_BANK_BALANCE, shop -> shop.getOwnerBank().getBalanceMap().keySet().stream().map(EconomyBridge::getCurrency)
                .filter(Objects::nonNull)
                .map(currencyId -> currencyId.format(shop.getOwnerBank().getBalance(currencyId))).collect(Collectors.joining(", ")))
            .add(CHEST_SHOP_OWNER, shop -> shop.isAdminShop() ? ChestConfig.ADMIN_SHOP_NAME.get() : shop.getOwnerName())
            .add(CHEST_SHOP_X, shop -> NumberUtil.format(shop.getBlockPos().getX()))
            .add(CHEST_SHOP_Y, shop -> NumberUtil.format(shop.getBlockPos().getY()))
            .add(CHEST_SHOP_Z, shop -> NumberUtil.format(shop.getBlockPos().getZ()))
            .add(CHEST_SHOP_WORLD, shop -> shop.isActive() ? LangAssets.get(shop.getWorld()) : shop.getWorldName())
            .add(CHEST_SHOP_IS_ADMIN, shop -> ChestLang.getYesOrNo(shop.isAdminShop()))
            .add(CHEST_SHOP_HOLOGRAM_ENABLED, shop -> ChestLang.getYesOrNo(shop.isHologramEnabled()))
            .add(CHEST_SHOP_SHOWCASE_ENABLED, shop -> ChestLang.getYesOrNo(shop.isShowcaseEnabled()))
            .add(CHEST_SHOP_RENT_EXPIRES_IN, shop -> shop.isRented() ? TimeFormats.formatDuration(shop.getRentedUntil(), TimeFormatType.LITERAL) : "-")
            .add(CHEST_SHOP_RENTER_NAME, shop -> shop.isRented() ? shop.getRenterName() : "-")
            .add(CHEST_SHOP_RENT_DURATION, shop -> shop.isRentable() ? TimeFormats.toLiteral(shop.getRentSettings().getDurationMillis()) : "-")
            .add(CHEST_SHOP_RENT_PRICE, shop -> shop.isRentable() ? shop.getRentSettings().getPriceFormatted() : "-");

        for (int slot = 0; slot < 27; slot++) {
            int index = slot;
            for (TradeType tradeType : TradeType.values()) {
                list.add(CHEST_SHOP_PRODUCT_PRICE.apply(tradeType, slot), shop -> {
                    ChestProduct product = shop.getProductAtSlot(index);
                    return product == null ? "-" : product.getCurrency().format(product.getPricer().getPrice(tradeType));
                });
            }
            list.add(CHEST_SHOP_PRODUCT_NAME.apply(slot), shop -> {
                ChestProduct product = shop.getProductAtSlot(index);
                return product == null ? "-" : ItemUtil.getSerializedName(product.getPreview());
            });
        }
    });

    @NotNull
    public static final PlaceholderList<VirtualShop> VIRTUAL_SHOP = PlaceholderList.create(list -> list
        .add(SHOP)
        .add(VIRTUAL_SHOP_ICON_NAME, shop -> {
            NightItem icon = shop.getIcon();
            if (icon.getItemName() != null) return icon.getItemName();
            if (icon.getDisplayName() != null) return icon.getDisplayName();
            return shop.getName();
        })
        .add(VIRTUAL_SHOP_DESCRIPTION, shop -> String.join("\n", shop.getDescription()))
        .add(VIRTUAL_SHOP_PAGES, shop -> String.valueOf(shop.getPages()))
        .add(VIRTUAL_SHOP_DISCOUNT_AMOUNT, shop -> NumberUtil.format(shop.getDiscountPlain()))
    );

    @NotNull
    public static final PlaceholderList<VirtualShop> VIRTUAL_SHOP_INTERNAL = PlaceholderList.create(list -> list
        .add(VIRTUAL_SHOP)
        .add(VIRTUAL_SHOP_PERMISSION_NODE, shop -> VirtualPerms.PREFIX_SHOP + shop.getId())
        .add(VIRTUAL_SHOP_PERMISSION_REQUIRED, shop -> Lang.getYesOrNo(shop.isPermissionRequired()))
        .add(VIRTUAL_SHOP_MENU_SLOT, shop -> shop.isMainMenuSlotDisabled() ? Lang.OTHER_DISABLED.getString() : String.valueOf(shop.getMainMenuSlot()))
        .add(VIRTUAL_SHOP_ALIASES, shop -> shop.getAliases().stream().map(str -> "/" + str).collect(Collectors.joining(", ")))
        .add(VIRTUAL_SHOP_DEFAULT_LAYOUT, VirtualShop::getDefaultLayout)
    );

    @NotNull
    public static final PlaceholderList<Rotation> ROTATION = PlaceholderList.create(list -> list
        .add(ROTATION_ID, Rotation::getId)
        .add(ROTATION_TYPE, rotation -> rotation.getRotationType().name())
        .add(ROTATION_INTERVAL, rotation -> TimeFormats.toLiteral(rotation.getRotationInterval() * 1000L))
        .add(ROTATION_SLOTS_AMOUNT, rotation -> String.valueOf(rotation.countAllSlots()))
        .add(ROTATION_ITEMS_AMOUNT, rotation -> String.valueOf(rotation.countItems()))
    );

    // ------------------------------
    // Products
    // ------------------------------

    public static final PlaceholderList<PreparedProduct> PREPARED_PRODUCT = PlaceholderList.create(list -> list
        .add(GENERIC_ITEM, product -> ItemUtil.getSerializedName(product.getProduct().getPreview()))
        .add(GENERIC_AMOUNT, product -> String.valueOf(product.getAmount()))
        .add(GENERIC_UNITS, product -> String.valueOf(product.getUnits()))
        .add(GENERIC_TYPE, product -> Lang.TRADE_TYPES.getLocalized(product.getTradeType()))
        .add(GENERIC_PRICE, product -> product.getProduct().getCurrency().format(product.getPrice()))
    );

    public static final PlaceholderList<ProductPOV<? extends Product>> PRODUCT = PlaceholderList.create(list -> {
        list
            .add(PRODUCT_ID, pov -> pov.product.getId())
            .add(PRODUCT_HANDLER, pov -> pov.product.getType().getName())
            .add(PRODUCT_CURRENCY, pov -> pov.product.getCurrency().getName())
            .add(PRODUCT_UNIT_AMOUNT, pov -> NumberUtil.format(pov.product.getUnitAmount()))
            .add(PRODUCT_PRICE_TYPE, pov -> Lang.PRICE_TYPES.getLocalized(pov.product.getPricer().getType()))
            .add(PRODUCT_PRICE_SELL_ALL, pov -> NumberUtil.format(pov.player == null ? 0D : pov.product.getPriceSellAll(pov.player)))
            .add(PRODUCT_PRICE_SELL_ALL_FORMATTED, pov -> {
                if (!pov.product.isSellable()) return Lang.OTHER_PRICE_DISABLED.getString();

                double price = pov.player == null ? 0D : pov.product.getPriceSellAll(pov.player);
                return price >= 0 ? pov.product.getCurrency().format(price) : Lang.OTHER_PRICE_DISABLED.getString();
            })
            .add(PRODUCT_PREVIEW_NAME, pov -> ItemUtil.getSerializedName(pov.product.getPreview()))
            .add(PRODUCT_PREVIEW_LORE, pov -> String.join("\n", ItemUtil.getSerializedLore(pov.product.getPreview())));


        for (TradeType tradeType : TradeType.values()) {

            list.add(PRODUCT_PRICE.apply(tradeType), pov -> {
                Currency currency = pov.product.getCurrency();
                return pov.player == null ? currency.formatValue(pov.product.getPricer().getPrice(tradeType)) : currency.formatValue(pov.product.getPrice(tradeType, pov.player));
            });

            list.add(PRODUCT_PRICE_FORMATTED.apply(tradeType), pov -> {
                AbstractProductPricer pricer = pov.product.getPricer();
                double price = pov.player == null ? pricer.getPrice(tradeType) : pov.product.getPrice(tradeType, pov.player);

                return price >= 0 ? pov.product.getCurrency().format(price) : Lang.OTHER_PRICE_DISABLED.getString();
            });

            list.add(PRODUCT_PRICE_AVERAGE.apply(tradeType), pov -> {
                AbstractProductPricer pricer = pov.product.getPricer();
                if (!(pricer instanceof RangedPricer rangedPricer)) return NumberUtil.format(pricer.getPrice(tradeType));

                return pov.product.getCurrency().format(rangedPricer.getPriceAverage(tradeType));
            });

            list.add(PRODUCT_PRICE_AVERAGE_DIFFERENCE.apply(tradeType), pov -> {
                AbstractProductPricer pricer = pov.product.getPricer();
                if (!(pricer instanceof RangedPricer rangedPricer)) return NumberUtil.format(0D);

                double percent = rangedPricer.getAverageDifferencePercent(tradeType);

                return (percent > 0 ? VirtualLang.PRICE_AVG_DIFF_UP : VirtualLang.PRICE_AVG_DIFF_DOWN).getString()
                    .replace(GENERIC_VALUE, NumberUtil.format(Math.abs(percent)));
            });
        }
    });

    public static final PlaceholderList<ProductPOV<ChestProduct>> CHEST_PRODUCT = PlaceholderList.create(list -> {
        list.add(PRODUCT);

        for (TradeType tradeType : TradeType.values()) {
            list
                .add(PRODUCT_STOCK_AMOUNT_LEFT.apply(tradeType), pov -> {
                    int leftAmount = pov.product.countStock(tradeType, null);
                    return leftAmount < 0 ? ChestLang.OTHER_INFINITY.getString() : NumberUtil.format(leftAmount);
                });
        }
    });

    public static final PlaceholderList<ProductPOV<? extends VirtualProduct>> VIRTUAL_PRODUCT = PlaceholderList.create(list -> {
        list
            .add(PRODUCT)
            .add(PRODUCT_DISCOUNT_ALLOWED, pov -> Lang.getYesOrNo(pov.product.isDiscountAllowed()))
            .add(PRODUCT_DISCOUNT_AMOUNT, pov -> NumberUtil.format(pov.product.getShop().getDiscountPlain(pov.product)))
            .add(PRODUCT_ALLOWED_RANKS, pov -> {
                if (pov.product.getAllowedRanks().isEmpty()) {
                    return Lang.goodEntry(VirtualLang.EDITOR_PRODUCT_NO_RANK_REQUIREMENTS.getString());
                }
                return pov.product.getAllowedRanks().stream().map(Lang::goodEntry).collect(Collectors.joining("\n"));
            })
            .add(PRODUCT_REQUIRED_PERMISSIONS, pov -> {
                if (!pov.product.hasRequiredPermissions()) {
                    return Lang.goodEntry(VirtualLang.EDITOR_PRODUCT_NO_PERM_REQUIREMENTS.getString());
                }
                return pov.product.getRequiredPermissions().stream().map(Lang::goodEntry).collect(Collectors.joining("\n"));
            })
            .add(PRODUCT_FORBIDDEN_PERMISSIONS, pov -> {
                if (!pov.product.hasForbiddenPermissions()) {
                    return Lang.goodEntry(VirtualLang.EDITOR_PRODUCT_NO_FORBIDDEN_PERMS.getString());
                }
                return pov.product.getForbiddenPermissions().stream().map(Lang::goodEntry).collect(Collectors.joining("\n"));
            })
            .add(PRODUCT_STOCKS_RESET_IN, pov -> {
                long restockDate = pov.product.getRestockDate(null);
                if (restockDate == 0L) return TimeFormats.toLiteral(pov.product.getStockValues().getRestockTimeMillis());

                return restockDate < 0 ? Lang.OTHER_NEVER.getString() : TimeFormats.formatDuration(restockDate, TimeFormatType.LITERAL);
            })
            .add(PRODUCT_LIMITS_RESET_IN, pov -> {
                long restockDate = pov.product.getRestockDate(pov.playerId());
                if (restockDate == 0L) return TimeFormats.toLiteral(pov.product.getLimitValues().getRestockTimeMillis());

                return restockDate < 0 ? Lang.OTHER_NEVER.getString() : TimeFormats.formatDuration(restockDate, TimeFormatType.LITERAL);
            })
            .add(PRODUCT_STOCKS_RESET_TIME, pov -> {
                long restockTime = pov.product.getStockValues().getRestockTimeMillis();
                return restockTime < 0 ? Lang.OTHER_NEVER.getString() : TimeFormats.toLiteral(restockTime);
            })
            .add(PRODUCT_LIMITS_RESET_TIME, pov -> {
                long cooldown = pov.product.getLimitValues().getRestockTimeMillis();
                return cooldown < 0 ? Lang.OTHER_NEVER.getString() : TimeFormats.toLiteral(cooldown);
            });

        for (TradeType tradeType : TradeType.values()) {
            list
                .add(PRODUCT_STOCK_AMOUNT_INITIAL.apply(tradeType), pov -> {
                    int initialAmount = pov.product.getStockValues().getInitialAmount(tradeType);
                    return initialAmount < 0 ? Lang.OTHER_INFINITY.getString() : String.valueOf(initialAmount);
                })
                .add(PRODUCT_STOCK_AMOUNT_LEFT.apply(tradeType), pov -> {
                    int leftAmount = pov.product.countStock(tradeType, null);
                    return leftAmount < 0 ? Lang.OTHER_INFINITY.getString() : String.valueOf(leftAmount);
                })
                .add(PRODUCT_LIMIT_AMOUNT_INITIAL.apply(tradeType), pov -> {
                    int initialAmount = pov.product.getLimitValues().getInitialAmount(tradeType);
                    return initialAmount < 0 ? Lang.OTHER_INFINITY.getString() : String.valueOf(initialAmount);
                })
                .add(PRODUCT_LIMIT_AMOUNT_LEFT.apply(tradeType), pov -> {
                    int leftAmount = pov.product.countStock(tradeType, pov.playerId());
                    return leftAmount < 0 ? Lang.OTHER_INFINITY.getString() : String.valueOf(leftAmount);
                });
        }
    });

    // ------------------------------
    // Product Packers
    // ------------------------------

    public static final PlaceholderList<VanillaTyping> VANILLA_TYPING = PlaceholderList.create(list -> list
        .add(PRODUCT_RESPECT_META, typing -> Lang.getYesOrNo(typing.isRespectMeta()))
    );

    public static final PlaceholderList<CommandTyping> COMMAND_TYPING = PlaceholderList.create(list -> list
        .add(PRODUCT_COMMANDS, typing -> typing.getCommands().stream().map(Lang::goodEntry).collect(Collectors.joining("\n")))
    );

    // ------------------------------
    // Product Pricers
    // ------------------------------

    public static final PlaceholderList<? super RangedPricer> RANGED_PRICER = PlaceholderList.create(list -> {
        for (TradeType tradeType : TradeType.values()) {
            list
                .add(PRICER_RANGED_BOUNDS_MIN.apply(tradeType), pricer -> String.valueOf(pricer.getPriceMin(tradeType)))
                .add(PRICER_RANGED_BOUNDS_MAX.apply(tradeType), pricer -> String.valueOf(pricer.getPriceMax(tradeType)));
        }
    });

    public static final PlaceholderList<FloatPricer> FLOAT_PRICER = PlaceholderList.create(list -> list
        .add(RANGED_PRICER)
        .add(PRICER_FLOAT_REFRESH_TYPE, pricer -> pricer.getRefreshType().name())
        .add(PRICER_FLOAT_REFRESH_INTERVAL, pricer -> TimeFormats.toLiteral(pricer.getRefreshIntervalMillis()))
        .add(PRICER_FLOAT_REFRESH_DAYS, pricer -> {
            if (pricer.getDays().isEmpty()) {
                return Lang.badEntry(Lang.EDITOR_PRICE_FLOAT_NO_DAYS.getString());
            }
            return pricer.getDays().stream().map(day -> Lang.goodEntry(Lang.DAYS.getLocalized(day))).collect(Collectors.joining("\n"));
        })
        .add(PRICER_FLOAT_REFRESH_TIMES, pricer -> {
            if (pricer.getTimes().isEmpty()) {
                return Lang.badEntry(Lang.EDITOR_PRICE_FLOAT_NO_TIMES.getString());
            }
            return pricer.getTimes().stream().map(ShopUtils.TIME_FORMATTER::format).map(Lang::goodEntry).collect(Collectors.joining("\n"));
        })
        .add(PRICER_FLOAT_ROUND_DECIMALS, pricer -> Lang.getYesOrNo(pricer.isRoundDecimals()))
    );

    public static final PlaceholderList<DynamicPricer> DYNAMIC_PRICER = PlaceholderList.create(list -> list
        .add(RANGED_PRICER)
        .add(PRICER_DYNAMIC_INITIAL_BUY, pricer -> NumberUtil.format(pricer.getInitial(TradeType.BUY)))
        .add(PRICER_DYNAMIC_INITIAL_SELL, pricer -> NumberUtil.format(pricer.getInitial(TradeType.SELL)))
        .add(PRICER_DYNAMIC_STEP_BUY, pricer -> NumberUtil.format(pricer.getStep(TradeType.BUY)))
        .add(PRICER_DYNAMIC_STEP_SELL, pricer -> NumberUtil.format(pricer.getStep(TradeType.SELL)))
    );

    public static final PlaceholderList<PlayersPricer> PLAYERS_PRICER = PlaceholderList.create(list -> list
        .add(RANGED_PRICER)
        .add(PRICER_DYNAMIC_INITIAL_BUY, pricer -> NumberUtil.format(pricer.getInitial(TradeType.BUY)))
        .add(PRICER_DYNAMIC_INITIAL_SELL, pricer -> NumberUtil.format(pricer.getInitial(TradeType.SELL)))
        .add(PRICER_PLAYERS_ADJUST_AMOUNT_BUY, pricer -> NumberUtil.format(pricer.getAdjustAmount(TradeType.BUY)))
        .add(PRICER_PLAYERS_ADJUST_AMOUNT_SELL, pricer -> NumberUtil.format(pricer.getAdjustAmount(TradeType.SELL)))
        .add(PRICER_PLAYERS_ADJUST_STEP, pricer -> NumberUtil.format(pricer.getAdjustStep()))
    );

    // ------------------------------
    // Auction
    // ------------------------------

    public static final PlaceholderList<AbstractListing> LISTING = PlaceholderList.create(list -> list
        .add(LISTING_SELLER, AbstractListing::getOwnerName)
        .add(LISTING_PRICE, listing -> listing.getCurrency().format(listing.getPrice()))
        .add(LISTING_DATE_CREATION, listing -> ShopUtils.getDateFormatter().format(TimeUtil.getLocalDateTimeOf(listing.getCreationDate())))
        .add(LISTING_ITEM_AMOUNT, listing -> String.valueOf(listing.getItemStack().getAmount()))
        .add(LISTING_ITEM_NAME, listing -> ItemUtil.getSerializedName(listing.getItemStack()))
        .add(LISTING_ITEM_LORE, listing -> String.join("\n", ItemUtil.getLore(listing.getItemStack())))
        .add(LISTING_ITEM_VALUE, listing -> String.valueOf(ItemNbt.compress(listing.getItemStack())))
        .add(LISTING_DELETES_IN, listing -> TimeFormats.formatDuration(listing.getDeleteDate(), TimeFormatType.LITERAL))
        .add(LISTING_DELETE_DATE, listing -> ShopUtils.getDateFormatter().format(TimeUtil.getLocalDateTimeOf(listing.getDeleteDate())))
    );


    public static final PlaceholderList<RentSettings> RENT_SETTINGS = PlaceholderList.create(list -> list
        .add(RENT_ENABLED, rentSettings -> Lang.getYesOrNo(rentSettings.isEnabled()))
        .add(RENT_CURRENCY, RentSettings::getCurrencyId)
        .add(RENT_CURRENCY_NAME, RentSettings::getCurrencyName)
        .add(RENT_PRICE, RentSettings::getPriceFormatted)
        .add(RENT_DURATION, rentSettings -> TimeFormats.toLiteral(rentSettings.getDurationMillis()))
    );

    public static final PlaceholderList<ActiveListing> ACTIVE_LISTING = PlaceholderList.create(list -> list
        .add(LISTING)
        .add(LISTING_EXPIRES_IN, listing -> TimeFormats.formatDuration(listing.getExpireDate(), TimeFormatType.LITERAL))
        .add(LISTING_EXPIRE_DATE, listing -> ShopUtils.getDateFormatter().format(TimeUtil.getLocalDateTimeOf(listing.getExpireDate())))
    );

    public static final PlaceholderList<CompletedListing> COMPLETED_LISTING = PlaceholderList.create(list -> list
        .add(LISTING)
        .add(LISTING_BUYER, CompletedListing::getBuyerName)
        .add(LISTING_BUY_DATE, listing -> ShopUtils.getDateFormatter().format(TimeUtil.getLocalDateTimeOf(listing.getBuyDate())))
    );

    @NotNull
    public static UnaryOperator<String> forProduct(@NotNull Product product, @Nullable Player player) {
        return PRODUCT.replacer(new ProductPOV<>(product, player));
    }

    @NotNull
    public static UnaryOperator<String> forChestProduct(@NotNull ChestProduct product, @Nullable Player player) {
        return CHEST_PRODUCT.replacer(new ProductPOV<>(product, player));
    }

    @NotNull
    public static UnaryOperator<String> forVirtualProduct(@NotNull VirtualProduct product, @Nullable Player player) {
        return VIRTUAL_PRODUCT.replacer(new ProductPOV<>(product, player));
    }

    @NotNull
    public static UnaryOperator<String> forChestShop(@NotNull ChestShop shop) {
        return CHEST_SHOP.replacer(shop);
    }

    @NotNull
    public static UnaryOperator<String> forVirtualShop(@NotNull VirtualShop shop) {
        PlaceholderList<VirtualShop> list = new PlaceholderList<>(VIRTUAL_SHOP);

        shop.getRotations().forEach(rotation -> {
            list.add(SHOP_ROTATION_NEXT_DATE.apply(rotation), shop1 -> {
                RotationData data = ShopAPI.getDataManager().getRotationData(rotation);
                if (data == null) return Lang.OTHER_NEVER.getString();

                return TimeUtil.getLocalDateTimeOf(data.getNextRotationDate()).format(ShopUtils.getDateFormatter());
            });

            list.add(SHOP_ROTATION_NEXT_IN.apply(rotation), shop1 -> {
                RotationData data = ShopAPI.getDataManager().getRotationData(rotation);
                if (data == null) return Lang.OTHER_NEVER.getString();

                return TimeFormats.formatDuration(data.getNextRotationDate(), TimeFormatType.LITERAL);
            });
        });

        return list.replacer(shop);
    }

    @NotNull
    public static UnaryOperator<String> forVirtualShopEditor(@NotNull VirtualShop shop) {
        return VIRTUAL_SHOP_INTERNAL.replacer(shop);
    }

    @NotNull
    public static UnaryOperator<String> forActiveListing(@NotNull ActiveListing listing) {
        return ACTIVE_LISTING.replacer(listing);
    }

    @NotNull
    public static UnaryOperator<String> forCompletedListing(@NotNull CompletedListing listing) {
        return COMPLETED_LISTING.replacer(listing);
    }
}
