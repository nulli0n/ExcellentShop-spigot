package su.nightexpress.excellentshop.shop.dialog.impl;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.shop.AbstractShopModule;
import su.nightexpress.excellentshop.shop.dialog.impl.ProductCustomBuyAmountDialog.Data;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.LangEntry;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.DialogActions;
import su.nightexpress.nightcore.ui.dialog.build.DialogBases;
import su.nightexpress.nightcore.ui.dialog.build.DialogBodies;
import su.nightexpress.nightcore.ui.dialog.build.DialogButtons;
import su.nightexpress.nightcore.ui.dialog.build.DialogInputs;
import su.nightexpress.nightcore.ui.dialog.build.DialogTypes;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

public class ProductCustomBuyAmountDialog extends Dialog<Data> {

    public record Data(@NonNull AbstractShopModule module, @NonNull Product product, int shopPage, int initialUnits) {
    }

    private static final TextLocale TITLE = LangEntry.builder("Dialog.Product.CustomBuyAmount.Title")
        .text(title("Buying", "Custom Amount"));

    private static final DialogElementLocale BODY = LangEntry.builder("Dialog.Product.CustomBuyAmount.Body")
        .dialogElement(
            250,
            "Enter the desired quantity for the selected item.",
            "",
            TagWrappers.RED.and(TagWrappers.BOLD).wrap("NOTE:") +
                " The final item quantity may change depending on item limits and your capacities."
        );

    private static final DialogElementLocale INPUT_AMOUNT = LangEntry
        .builder("Dialog.Product.CustomBuyAmount.Input.Amount")
        .dialogElement("Quantity");

    private static final String JSON_AMOUNT = "amount";

    @Override
    public @NonNull WrappedDialog create(@NonNull Player player, @NonNull Data data) {
        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(
                    DialogBodies.item(data.product.getEffectivePreview()).build(),
                    DialogBodies.plainMessage(BODY)
                )
                .inputs(DialogInputs.text(JSON_AMOUNT, INPUT_AMOUNT)
                    .initial(String.valueOf(data.initialUnits))
                    .maxLength(6)
                    .build())
                .build()
            )
            .type(DialogTypes.confirmation(DialogButtons.confirm(), DialogButtons.cancel()))
            .handleResponse(DialogActions.CONFIRM, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                int units = nbtHolder.getInt(JSON_AMOUNT).orElse(data.initialUnits);

                data.module.openBuyingMenu(player, data.product, data.shopPage, units);
                viewer.closeFully();
            })
        );
    }
}
