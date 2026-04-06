package su.nightexpress.excellentshop;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.Rotation;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.feature.playershop.core.ChestConfig;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestProduct;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestShop;
import su.nightexpress.excellentshop.feature.playershop.rent.RentSettings;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.util.ItemTag;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.PlaceholderList;
import su.nightexpress.nightcore.util.placeholder.TypedPlaceholder;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public class ShopPlaceholders extends su.nightexpress.nightcore.util.Placeholders {

    public static final String URL_WIKI              = "https://nightexpressdev.com/excellentshop/";
    public static final String URL_WIKI_PLACEHOLDERS = URL_WIKI + "placeholders";

    public static final String URL_CUSTOM_ITEMS = "https://nightexpressdev.com/nightcore/integrations/items/";
    public static final String URL_CURRENCIES   = "https://nightexpressdev.com/nightcore/integrations/currencies/";

    @Deprecated
    public static final String GENERIC_BUY          = "%buy%";
    @Deprecated
    public static final String GENERIC_SELL         = "%sell%";
    public static final String GENERIC_NAME         = "%name%";
    public static final String GENERIC_ITEM         = "%item%";
    public static final String GENERIC_TOTAL        = "%total%";
    public static final String GENERIC_PRODUCTS     = "%products%";
    public static final String GENERIC_LOOSE        = "%loose%";
    public static final String GENERIC_LOOSE_SIZE   = "%loose_size%";
    public static final String GENERIC_MAX_PRODUCTS = "%max_products%";
    public static final String GENERIC_MAX_SHOPS    = "%max_shops%";
    public static final String GENERIC_SHOPS_AMOUNT = "%shops_amount%";
    public static final String GENERIC_LORE         = "%lore%";
    public static final String GENERIC_SIZE         = "%size%";
    public static final String GENERIC_AMOUNT       = "%amount%";
    public static final String GENERIC_UNITS        = "%units%";
    public static final String GENERIC_TOTAL_AMOUNT = "%total_amount%";
    public static final String GENERIC_TOTAL_UNITS  = "%total_units%";
    public static final String GENERIC_TYPE         = "%type%";
    //public static final String GENERIC_CURRENCY = "%currency%";
    public static final String GENERIC_TIME         = "%time%";
    public static final String GENERIC_PRICE        = "%price%";
    public static final String GENERIC_WORTH        = "%worth%";
    public static final String GENERIC_BALANCE      = "%balance%";
    public static final String GENERIC_DISCOUNT     = "%discount%";
    public static final String GENERIC_PERMISSION   = "%permission%";
    public static final String GENERIC_TAX          = "%tax%";
    public static final String GENERIC_EXPIRE       = "%expire%";
    public static final String GENERIC_PAGE         = "%page%";
    public static final String GENERIC_PAGES        = "%pages%";
    public static final String GENERIC_WEIGHT       = "%weight%";
    public static final String GENERIC_PATH         = "%path%";
    public static final String GENERIC_STATE        = "%state%";
    public static final String GENERIC_TREND        = "%trend%";

    public static final String GENERIC_SELL_MULTIPLIER = "%sell_multiplier%";

    public static final String ITEM_NAME = "%item_name%";
    public static final String ITEM_LORE = "%item_lore%";

    public static final String SHOP_ID              = "%shop_id%";
    public static final String SHOP_NAME            = "%shop_name%";
    public static final String SHOP_BUYING_ALLOWED  = "%shop_buy_allowed%";
    public static final String SHOP_SELLING_ALLOWED = "%shop_sell_allowed%";
    public static final String SHOP_PRODUCTS        = "%shop_products%";

    public static final String CHEST_SHOP_OWNER            = "%shop_owner%";
    public static final String CHEST_SHOP_X                = "%shop_location_x%";
    public static final String CHEST_SHOP_Y                = "%shop_location_y%";
    public static final String CHEST_SHOP_Z                = "%shop_location_z%";
    public static final String CHEST_SHOP_WORLD            = "%shop_location_world%";
    public static final String CHEST_SHOP_IS_ADMIN         = "%shop_is_admin%";
    public static final String CHEST_SHOP_HOLOGRAM_ENABLED = "%shop_hologram_enabled%";
    public static final String CHEST_SHOP_SHOWCASE_ENABLED = "%shop_showcase_enabled%";
    public static final String CHEST_SHOP_RENT_EXPIRES_IN  = "%shop_rent_expires_in%";
    public static final String CHEST_SHOP_RENTER_NAME      = "%shop_rented_by%";
    public static final String CHEST_SHOP_RENT_DURATION    = "%shop_rent_duration%";
    public static final String CHEST_SHOP_RENT_PRICE       = "%shop_rent_price%";

    public static final String RENT_ENABLED       = "%rent_enabled%";
    public static final String RENT_CURRENCY      = "%rent_currency%";
    public static final String RENT_CURRENCY_NAME = "%rent_currency_name%";
    public static final String RENT_PRICE         = "%rent_price%";
    public static final String RENT_DURATION      = "%rent_duration%";

    @Deprecated
    public static final String VIRTUAL_SHOP_ICON_NAME           = "%shop_icon_name%";
    public static final String VIRTUAL_SHOP_DESCRIPTION         = "%shop_description%";
    @Deprecated
    public static final String VIRTUAL_SHOP_PERMISSION_REQUIRED = "%shop_permission_required%";
    @Deprecated
    public static final String VIRTUAL_SHOP_PERMISSION_NODE     = "%shop_permission_node%";
    @Deprecated
    public static final String VIRTUAL_SHOP_PAGES               = "%shop_pages%";
    @Deprecated
    public static final String VIRTUAL_SHOP_ALIASES             = "%shop_aliases%";
    @Deprecated
    public static final String VIRTUAL_SHOP_MENU_SLOTS          = "%shop_menu_slot%";

    public static final String PRODUCT_HANDLER                  = "%product_handler%";
    public static final String PRODUCT_PRICE_TYPE               = "%product_price_type%";
    public static final String PRODUCT_PRICE_SELL_ALL           = "%product_price_sell_all%";
    public static final String PRODUCT_PRICE_SELL_ALL_FORMATTED = "%product_price_sell_all_formatted%";

    public static final Function<TradeType, String> PRODUCT_PRICE           = type -> "%product_price_" + type.getLowerCase() + "%";
    public static final Function<TradeType, String> PRODUCT_PRICE_FORMATTED = type -> "%product_price_" + type.getLowerCase() + "_formatted%";
    public static final Function<TradeType, String> PRODUCT_PRICE_AVERAGE   = type -> "%product_price_avg_" + type.getLowerCase() + "%";

    public static final String PRODUCT_ID           = "%product_id%";
    public static final String PRODUCT_PREVIEW_NAME = "%product_preview_name%";
    public static final String PRODUCT_PREVIEW_LORE = "%product_preview_lore%";
    public static final String PRODUCT_UNIT_AMOUNT  = "%product_unit_amount%";
    public static final String PRODUCT_CURRENCY     = "%product_currency%";
    public static final String PRODUCT_BUY_LIMIT    = "%product_buy_limit%";
    public static final String PRODUCT_SELL_LIMIT   = "%product_sell_limit%";
    public static final String PRODUCT_CAPACITY     = "%product_capacity%";
    public static final String PRODUCT_SPACE        = "%product_space%";
    public static final String PRODUCT_STOCK        = "%product_stock%";

    public static final String ROTATION_ID           = "%rotation_id%";
    public static final String ROTATION_TYPE         = "%rotation_type%";
    public static final String ROTATION_INTERVAL     = "%rotation_interval%";
    public static final String ROTATION_SLOTS_AMOUNT = "%rotation_slots_amount%";
    public static final String ROTATION_ITEMS_AMOUNT = "%rotation_items_amount%";

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

    public static final TypedPlaceholder<Shop> SHOP = TypedPlaceholder.builder(Shop.class)
        .with(SHOP_ID, Shop::getId)
        .with(SHOP_NAME, Shop::getName)
        .with(SHOP_BUYING_ALLOWED, shop -> CoreLang.STATE_ENABLED_DISALBED.get(shop.isBuyingAllowed()))
        .with(SHOP_SELLING_ALLOWED, shop -> CoreLang.STATE_ENABLED_DISALBED.get(shop.isSellingAllowed()))
        .with(SHOP_PRODUCTS, shop -> NumberUtil.format(shop.countProducts()))
        .build();

    @NonNull
    public static final TypedPlaceholder<ChestShop> CHEST_SHOP = TypedPlaceholder.builder(ChestShop.class)
        .include(SHOP)
        .with(CHEST_SHOP_OWNER, shop -> shop.isAdminShop() ? ChestConfig.ADMIN_SHOP_NAME.get() : shop.getOwnerName())
        .with(CHEST_SHOP_X, shop -> NumberUtil.format(shop.getBlockPos().getX()))
        .with(CHEST_SHOP_Y, shop -> NumberUtil.format(shop.getBlockPos().getY()))
        .with(CHEST_SHOP_Z, shop -> NumberUtil.format(shop.getBlockPos().getZ()))
        .with(CHEST_SHOP_WORLD, shop -> shop.isAccessible() ? LangAssets.get(shop.getBlock().getWorld()) : shop.getWorldName())
        .with(CHEST_SHOP_IS_ADMIN, shop -> CoreLang.STATE_YES_NO.get(shop.isAdminShop()))
        .with(CHEST_SHOP_HOLOGRAM_ENABLED, shop -> CoreLang.STATE_YES_NO.get(shop.isHologramEnabled()))
        .with(CHEST_SHOP_SHOWCASE_ENABLED, shop -> CoreLang.STATE_YES_NO.get(shop.isShowcaseEnabled()))
        .with(CHEST_SHOP_RENT_EXPIRES_IN, shop -> shop.isRented() ? TimeFormats.formatDuration(shop.getRentedUntil(), TimeFormatType.LITERAL) : Lang.OTHER_NO_RENT.text())
        .with(CHEST_SHOP_RENTER_NAME, shop -> shop.isRented() ? shop.getRenterName() : Lang.OTHER_NO_RENT.text())
        .with(CHEST_SHOP_RENT_DURATION, shop -> shop.isRentable() ? TimeFormats.toLiteral(shop.getRentSettings().getDurationMillis()) : "-")
        .with(CHEST_SHOP_RENT_PRICE, shop -> shop.isRentable() ? shop.getRentSettings().getPriceFormatted() : "-")
        .build();

    @NonNull
    public static final TypedPlaceholder<VirtualShop> VIRTUAL_SHOP = TypedPlaceholder.builder(VirtualShop.class)
        .include(SHOP)
        .with(VIRTUAL_SHOP_ICON_NAME, shop -> {
            NightItem icon = shop.getIcon();
            if (icon.getItemName() != null) return icon.getItemName();
            if (icon.getDisplayName() != null) return icon.getDisplayName();
            return shop.getName();
        })
        .with(VIRTUAL_SHOP_DESCRIPTION, shop -> String.join(TagWrappers.BR, shop.getDescription()))
        .with(VIRTUAL_SHOP_PAGES, shop -> String.valueOf(shop.getPages()))
        .build();

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

    public static final TypedPlaceholder<Product> PRODUCT;

    static {
        var productBuilder = TypedPlaceholder.builder(Product.class)
            .with(PRODUCT_ID, Product::getId)
            .with(PRODUCT_HANDLER, product -> product.getContent().getName())
            .with(PRODUCT_CURRENCY, product -> product.getCurrency().getName())
            .with(PRODUCT_UNIT_AMOUNT, product -> NumberUtil.format(product.getUnitSize()))
            .with(PRODUCT_PRICE_TYPE, product -> Lang.PRICE_TYPES.getLocalized(product.getPricingType()))
            .with(PRODUCT_PREVIEW_NAME, product -> ItemUtil.getNameSerialized(product.getEffectivePreview()))
            .with(PRODUCT_PREVIEW_LORE, product -> String.join("\n", ItemUtil.getLoreSerialized(product.getEffectivePreview())))
            .with("%product_amount%", product -> ShopUtils.formatOrInfinite(product.getStock()))
            .with(PRODUCT_STOCK, product -> ShopUtils.formatOrInfinite(product.getStock()))
            .with(PRODUCT_SPACE, product -> ShopUtils.formatOrInfinite(product.getSpace()))
            .with(PRODUCT_CAPACITY, product -> ShopUtils.formatOrInfinite(product.getCapacity()));

        for (TradeType tradeType : TradeType.values()) {
            productBuilder.with(PRODUCT_PRICE.apply(tradeType), product -> {
                Currency currency = product.getCurrency();
                return currency.formatValue(product.getPrice(tradeType));
            });

            productBuilder.with(PRODUCT_PRICE_FORMATTED.apply(tradeType), product -> {
                double price = product.getPrice(tradeType);

                return price >= 0 ? product.getCurrency().format(price) : Lang.OTHER_N_A.text();
            });

            productBuilder.with(PRODUCT_PRICE_AVERAGE.apply(tradeType), product -> {
                return product.getCurrency().format(product.getPricing().getAveragePrice(tradeType));
            });
        }

        PRODUCT = productBuilder.build();
    }

    public static final TypedPlaceholder<ChestProduct> CHEST_PRODUCT = TypedPlaceholder.builder(ChestProduct.class)
        .include(PRODUCT)
        .with("%product_amount%", product -> ShopUtils.formatOrInfinite(product.getCachedAmount()))
        .with(PRODUCT_SPACE, product -> ShopUtils.formatOrInfinite(product.getCachedSpace()))
        .with(PRODUCT_CAPACITY, product -> ShopUtils.formatOrInfinite(product.getCachedCapacity()))
        .with("%product_stock_global_buy_amount_left%", product -> ShopUtils.formatOrInfinite(product.getCachedAmount()))
        .with("%product_stock_global_sell_amount_left%", product -> ShopUtils.formatOrInfinite(product.getCachedSpace()))
        .build();

    public static final TypedPlaceholder<VirtualProduct> VIRTUAL_PRODUCT_TYPED = TypedPlaceholder.builder(VirtualProduct.class)
        .include(PRODUCT)
        .with(PRODUCT_BUY_LIMIT, product -> ShopUtils.formatOrInfinite(product.getLimitOptions().getBuyLimit()))
        .with(PRODUCT_SELL_LIMIT, product -> ShopUtils.formatOrInfinite(product.getLimitOptions().getSellLimit()))
        .build();

    // ------------------------------
    // Auction
    // ------------------------------

    public static final PlaceholderList<AbstractListing> LISTING = PlaceholderList.create(list -> list
        .add(LISTING_SELLER, AbstractListing::getOwnerName)
        .add(LISTING_PRICE, listing -> listing.getCurrency().format(listing.getPrice()))
        .add(LISTING_DATE_CREATION, listing -> TimeFormats.formatDateTime(listing.getCreationDate()))
        .add(LISTING_ITEM_AMOUNT, listing -> String.valueOf(listing.getItemStack().getAmount()))
        .add(LISTING_ITEM_NAME, listing -> ItemUtil.getNameSerialized(listing.getItemStack()))
        .add(LISTING_ITEM_LORE, listing -> String.join("\n", ItemUtil.getLoreSerialized(listing.getItemStack())))
        .add(LISTING_ITEM_VALUE, listing -> ItemTag.getTagStringEncoded(listing.getItemStack()))
        .add(LISTING_DELETES_IN, listing -> TimeFormats.formatDuration(listing.getDeleteDate(), TimeFormatType.LITERAL))
        .add(LISTING_DELETE_DATE, listing -> TimeFormats.formatDateTime(listing.getDeleteDate()))
    );


    public static final PlaceholderList<RentSettings> RENT_SETTINGS = PlaceholderList.create(list -> list
        .add(RENT_ENABLED, rentSettings -> CoreLang.STATE_YES_NO.get(rentSettings.isEnabled()))
        .add(RENT_CURRENCY, RentSettings::getCurrencyId)
        .add(RENT_CURRENCY_NAME, RentSettings::getCurrencyName)
        .add(RENT_PRICE, RentSettings::getPriceFormatted)
        .add(RENT_DURATION, rentSettings -> TimeFormats.toLiteral(rentSettings.getDurationMillis()))
    );

    public static final PlaceholderList<ActiveListing> ACTIVE_LISTING = PlaceholderList.create(list -> list
        .add(LISTING)
        .add(LISTING_EXPIRES_IN, listing -> TimeFormats.formatDuration(listing.getExpireDate(), TimeFormatType.LITERAL))
        .add(LISTING_EXPIRE_DATE, listing -> TimeFormats.formatDateTime(listing.getExpireDate()))
    );

    public static final PlaceholderList<CompletedListing> COMPLETED_LISTING = PlaceholderList.create(list -> list
        .add(LISTING)
        .add(LISTING_BUYER, CompletedListing::getBuyerName)
        .add(LISTING_BUY_DATE, listing -> TimeFormats.formatDateTime(listing.getBuyDate()))
    );

    @NonNull
    public static UnaryOperator<String> forActiveListing(@NonNull ActiveListing listing) {
        return ACTIVE_LISTING.replacer(listing);
    }

    @NonNull
    public static UnaryOperator<String> forCompletedListing(@NonNull CompletedListing listing) {
        return COMPLETED_LISTING.replacer(listing);
    }
}
