package su.nightexpress.excellentshop.shop.dialog;

import su.nightexpress.excellentshop.product.click.ProductClickContext;
import su.nightexpress.excellentshop.shop.dialog.impl.ProductCustomBuyAmountDialog;
import su.nightexpress.nightcore.ui.dialog.wrap.DialogKey;

public class ShopDialogKeys {

    private ShopDialogKeys() {
    }

    public static final DialogKey<ProductClickContext>               PRODUCT_PURCHASE_OPTIONS  = new DialogKey<>("product_purchase_options");
    public static final DialogKey<ProductCustomBuyAmountDialog.Data> PRODUCT_BUY_CUSTOM_AMOUNT = new DialogKey<>("vshop_product_buy_custom_amount");
}
