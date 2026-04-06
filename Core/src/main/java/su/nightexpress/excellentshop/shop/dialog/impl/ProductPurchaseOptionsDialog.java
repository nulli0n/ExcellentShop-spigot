package su.nightexpress.excellentshop.shop.dialog.impl;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.click.ProductClickAction;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.core.Perms;
import su.nightexpress.excellentshop.product.click.ProductClickContext;
import su.nightexpress.nexshop.module.AbstractShopModule;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.util.Strings;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.ArrayList;
import java.util.List;

public class ProductPurchaseOptionsDialog extends Dialog<ProductClickContext> {

    private static final ProductClickAction[] CLICK_ACTIONS = {
        ProductClickAction.BUY_ONE, ProductClickAction.SELL_ONE,
        ProductClickAction.BUY_ALL, ProductClickAction.SELL_ALL,
        ProductClickAction.OPEN_BUY_MENU, ProductClickAction.OPEN_SELL_MENU,
    };

    private static final TextLocale TITLE = LangEntry.builder("Shop.UI.Dialog.Product.PurchaseOptions.Title")
        .text(ShopPlaceholders.SHOP_NAME + TagWrappers.DARK_GRAY.wrap(" » ") + ShopPlaceholders.PRODUCT_PREVIEW_NAME);

    private static final DialogElementLocale BODY = LangEntry.builder("Shop.UI.Dialog.Product.PurchaseOptions.Body").dialogElement(400,
        "What you would like to do?");

    private static final ButtonLocale BUY_BUTTON_LOCALE = LangEntry.builder("Shop.UI.Dialog.Product.PurchaseOptions.BuyButton")
        .button(TagWrappers.GREEN.wrap("↓") + " " + ShopPlaceholders.GENERIC_TYPE + TagWrappers.GRAY.wrap(" (" + TagWrappers.GREEN.wrap(ShopPlaceholders.GENERIC_PRICE) + ")"));

    private static final ButtonLocale SELL_BUTTON_LOCALE = LangEntry.builder("Shop.UI.Dialog.Product.PurchaseOptions.SellButton")
        .button(TagWrappers.RED.wrap("↑") + " " + ShopPlaceholders.GENERIC_TYPE + TagWrappers.GRAY.wrap(" (" + TagWrappers.RED.wrap(ShopPlaceholders.GENERIC_PRICE) + ")"));

    private static final ButtonLocale MENU_BUTTON_LOCALE = LangEntry.builder("Shop.UI.Dialog.Product.PurchaseOptions.MenuButton")
        .button(TagWrappers.ORANGE.wrap("→") + " " + ShopPlaceholders.GENERIC_TYPE);

    private final AbstractShopModule module;

    public ProductPurchaseOptionsDialog(@NonNull AbstractShopModule module) {
        this.module = module;
    }

    @Override
    @NonNull
    public WrappedDialog create(@NonNull Player player, @NonNull ProductClickContext context) {
        Product product = context.product();

        return Dialogs.create(builder -> {
            List<WrappedActionButton> buttons = new ArrayList<>();

            boolean isBuyable = product.isBuyable();
            boolean isSellable = product.isSellable();
            for (ProductClickAction action : CLICK_ACTIONS) {
                TradeType type = switch (action) {
                    case BUY_ONE, BUY_ALL, OPEN_BUY_MENU -> TradeType.BUY;
                    case SELL_ONE, SELL_ALL, OPEN_SELL_MENU -> TradeType.SELL;
                    default -> throw new IllegalStateException("Unexpected value: " + action);
                };

                if (type == TradeType.BUY && !isBuyable) continue;
                if (type == TradeType.SELL && !isSellable) continue;

                if (action == ProductClickAction.OPEN_BUY_MENU && !product.isBuyMenuAllowed()) continue;
                if (action == ProductClickAction.OPEN_SELL_MENU && !product.isSellMenuAllowed()) continue;
                if (action == ProductClickAction.SELL_ALL && !player.hasPermission(Perms.KEY_SELL_ALL)) continue;
                if (action == ProductClickAction.BUY_ALL && !player.hasPermission(Perms.KEY_BUY_ALL)) continue;

                String jsonKey = Strings.varStyle(action.name()).orElse(null);
                if (jsonKey == null) continue;

                ButtonLocale locale;
                if (action == ProductClickAction.OPEN_SELL_MENU || action == ProductClickAction.OPEN_BUY_MENU) {
                    locale = MENU_BUTTON_LOCALE;
                }
                else locale = type == TradeType.BUY ? BUY_BUTTON_LOCALE : SELL_BUTTON_LOCALE;

                PlaceholderContext placeholderContext = PlaceholderContext.builder()
                    .with(ShopPlaceholders.GENERIC_TYPE, () -> Lang.PRODUCT_CLICK_ACTION.getLocalized(action))
                    .with(ShopPlaceholders.GENERIC_PRICE, () -> {
                        Currency currency = product.getCurrency();
                        double finalPrice = product.getFinalPrice(type);

                        if (action == ProductClickAction.BUY_ALL) {
                            finalPrice *= product.getMaxBuyableUnitAmount(player, player.getInventory());
                        }
                        else if (action == ProductClickAction.SELL_ALL) {
                            finalPrice *= product.getMaxSellableUnitAmount(player, player.getInventory());
                        }

                        return currency.format(finalPrice);
                    })
                    .build();

                buttons.add(DialogButtons.action(locale.replace(placeholderContext)).action(DialogActions.customClick(jsonKey)).build());
                builder.handleResponse(jsonKey, (viewer, identifier, nbtHolder) -> {
                    this.module.handleProductClickAction(context, action);
                });
            }

            PlaceholderContext placeholderContext = PlaceholderContext.builder()
                .with(product.placeholders())
                .with(product.getShop().placeholders())
                .build();

            builder
                .base(DialogBases.builder(placeholderContext.apply(TITLE.text()))
                    .body(
                        DialogBodies.item(context.product().getEffectivePreview()).build(),
                        DialogBodies.plainMessage(BODY.replace(placeholderContext))
                    )
                    .build()
                )
                .type(DialogTypes.multiAction(buttons).columns(2).exitAction(DialogButtons.back()).build())
                .build();

        });
    }
}
