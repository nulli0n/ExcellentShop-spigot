package su.nightexpress.excellentshop.feature.virtualshop.dialog;

import su.nightexpress.excellentshop.product.click.ProductClickContext;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.nightcore.ui.dialog.wrap.DialogKey;

public class VSDialogKeys {

    public static final DialogKey<ProductClickContext> PURCHASE_OPTIONS = new DialogKey<>("vshop_purchase_options");

    public static final DialogKey<VirtualShopModule> SHOP_CREATION     = new DialogKey<>("vshop_shop_creation");
    public static final DialogKey<VirtualShop>       SHOP_ALIASES      = new DialogKey<>("vshop_shop_aliases");
    public static final DialogKey<VirtualShop>       SHOP_DESCRIPTION  = new DialogKey<>("vshop_shop_description");
    public static final DialogKey<VirtualShop>       SHOP_PAGE_LAYOUTS = new DialogKey<>("vshop_shop_page_layouts");
    public static final DialogKey<VirtualShop>       SHOP_MENU_SLOTS   = new DialogKey<>("vshop_shop_menu_slots");
    public static final DialogKey<VirtualShop>       SHOP_NAME         = new DialogKey<>("vshop_shop_name");
    public static final DialogKey<VirtualShop>       SHOP_PAGES        = new DialogKey<>("vshop_shop_pages");

    public static final DialogKey<VirtualProduct> PRODUCT_CONTENT_TYPE            = new DialogKey<>("vshop_product_content_type");
    public static final DialogKey<VirtualProduct> PRODUCT_COMMANDS                = new DialogKey<>("vshop_product_commands");
    public static final DialogKey<VirtualProduct> PRODUCT_PRICE                   = new DialogKey<>("vshop_product_price");
    public static final DialogKey<VirtualProduct> PRODUCT_CURRENCY                = new DialogKey<>("vshop_product_currency");
    public static final DialogKey<VirtualProduct> PRODUCT_FLOAT_PRICE_TIMINGS     = new DialogKey<>("vshop_product_float_price_timings");
    public static final DialogKey<VirtualProduct> PRODUCT_STOCKS                  = new DialogKey<>("vshop_product_stocks");
    public static final DialogKey<VirtualProduct> PRODUCT_LIMITS                  = new DialogKey<>("vshop_product_limits");
    public static final DialogKey<VirtualProduct> PRODUCT_RANK_REQUIREMENTS       = new DialogKey<>("vshop_product_rank_requirements");
    public static final DialogKey<VirtualProduct> PRODUCT_PERMISSION_REQUIREMENTS = new DialogKey<>("vshop_product_permission_requirements");

}
