package su.nightexpress.excellentshop.feature.playershop.dialog;

import su.nightexpress.excellentshop.feature.playershop.bank.Bank;
import su.nightexpress.excellentshop.product.click.ProductClickContext;
import su.nightexpress.nightcore.ui.dialog.wrap.DialogKey;

public class PSDialogKeys {

    public static final DialogKey<ProductClickContext> PRODUCT_PURCHASE_OPTIONS = new DialogKey<>("pshop_product_purchase_options");

    public static final DialogKey<Bank> BANK_MANAGEMENT = new DialogKey<>("pshop_bank_management");
}
