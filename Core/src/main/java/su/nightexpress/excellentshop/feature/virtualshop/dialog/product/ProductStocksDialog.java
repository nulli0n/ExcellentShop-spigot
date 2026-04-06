package su.nightexpress.excellentshop.feature.virtualshop.dialog.product;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.virtualshop.product.StockOptions;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.util.sound.VanillaSound;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

public class ProductStocksDialog extends Dialog<VirtualProduct> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.Product.Stocks.Title").text(title("Product", "Stock Settings"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.Product.Stocks.Body").dialogElement(
        400,
        "Configure the global stock for this product.",
        "",
        TagWrappers.SPRITE_ITEM.apply(Material.BUNDLE) + TagWrappers.SOFT_YELLOW.wrap(" Global Stock") + " is a shared pool for all players. ",
        "",
        "When the stock is " + TagWrappers.RED.wrap("depleted") + ", the product will be unavailable for purchase until someone replenishes it by "
            + TagWrappers.GREEN.wrap("selling") + " or the "
            + TagWrappers.GREEN.wrap("automatic restock") + " time occurs.",
        "",
        "When the product stock reaches its " + TagWrappers.RED.wrap("maximum capacity") + " " + TagWrappers.GRAY.wrap("(if set)")
            + ", selling will be disabled until someone " + TagWrappers.GREEN.wrap("buys")
            + " a portion of the product to free up space.",
        "",
        TagWrappers.SOFT_YELLOW.wrap("→ Tip:") + " Use a value of " + TagWrappers.SOFT_YELLOW.wrap("-1") + " for unlimited capacity or to disable automatic restocks."
    );

    private static final TextLocale INPUT_CAPACITY = VirtualLang.builder("Dialog.Product.Stocks.Input.Capacity")
        .text(TagWrappers.SPRITE_ITEM.apply(Material.BUNDLE) + " Capacity");

    private static final TextLocale INPUT_MIN_RESTOCK_AMOUNT = VirtualLang.builder("Dialog.Product.Stocks.Input.MinRestockAmount")
        .text(TagWrappers.RED.wrap("↑") + " Min. Restock Amount");

    private static final TextLocale INPUT_MAX_RESTOCK_AMOUNT = VirtualLang.builder("Dialog.Product.Stocks.Input.MaxRestockAmount")
        .text(TagWrappers.RED.wrap("↑") + " Max. Restock Amount");

    private static final TextLocale INPUT_RESTOCK_INTERVAL = VirtualLang.builder("Dialog.Product.Stocks.Input.RestockInterval")
        .text(TagWrappers.SPRITE_ITEMS.apply("item/clock_12") + " Restock Interval " + TagWrappers.GRAY.wrap("(in seconds)"));

    private static final ButtonLocale BUTTON_ENABLE = VirtualLang.builder("Dialog.Product.Limits.Button.Enable")
        .button(TagWrappers.SPRITE_ITEM.apply(Material.RED_DYE) + " Status: " + TagWrappers.RED.wrap("Disabled"),
            "Click to " + TagWrappers.GREEN.wrap("enable") + " the feature."
        );

    private static final ButtonLocale BUTTON_DISABLE = VirtualLang.builder("Dialog.Product.Limits.Button.Disable")
        .button(TagWrappers.SPRITE_ITEM.apply(Material.LIME_DYE) + " Status: " + TagWrappers.GREEN.wrap("Enabled"),
            "Click to " + TagWrappers.RED.wrap("disable") + " the feature."
        );

    private static final ButtonLocale BUTTON_RESET_DATA = VirtualLang.builder("Dialog.Product.Stocks.Button.ResetData")
        .button(TagWrappers.SPRITE_ITEM.apply(Material.LAVA_BUCKET) + " Reset Data", "Resets current stock data for this product.");

    private static final String JSON_CAPACITY           = "capacity";
    private static final String JSON_MIN_RESTOCK_AMOUNT = "min_restock_amount";
    private static final String JSON_MAX_RESTOCK_AMOUNT = "max_restock_amount";
    private static final String JSON_RESTOCK_INTERVAL   = "restock_interval";

    private static final String ACTION_RESET_DATA = "reset_data";
    private static final String ACTION_TOGGLE     = "toggle";

    @Override
    @NonNull
    public WrappedDialog create(@NotNull Player player, @NotNull VirtualProduct product) {
        StockOptions stockOptions = product.getStockOptions();

        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.text(JSON_CAPACITY, INPUT_CAPACITY).initial(String.valueOf(stockOptions.getCapacity())).maxLength(10).build(),
                    DialogInputs.text(JSON_MIN_RESTOCK_AMOUNT, INPUT_MIN_RESTOCK_AMOUNT).initial(String.valueOf(stockOptions.getRestockMinAmount())).maxLength(10).build(),
                    DialogInputs.text(JSON_MAX_RESTOCK_AMOUNT, INPUT_MAX_RESTOCK_AMOUNT).initial(String.valueOf(stockOptions.getRestockMaxAmount())).maxLength(10).build(),
                    DialogInputs.text(JSON_RESTOCK_INTERVAL, INPUT_RESTOCK_INTERVAL).initial(String.valueOf(stockOptions.getRestockTime())).maxLength(20).build()
                )
                .build()
            )
            .type(
                DialogTypes.multiAction(
                        DialogButtons.action(stockOptions.isEnabled() ? BUTTON_DISABLE : BUTTON_ENABLE).action(DialogActions.customClick(ACTION_TOGGLE)).build(),
                        DialogButtons.apply(),
                        DialogButtons.action(BUTTON_RESET_DATA).action(DialogActions.customClick(ACTION_RESET_DATA)).build()
                    )
                    .exitAction(DialogButtons.back())
                    .columns(1)
                    .build()
            )
            .handleResponse(ACTION_RESET_DATA, (viewer, identifier, nbtHolder) -> {
                product.getStockData().markRemoved();
                VanillaSound.of(Sound.ENTITY_GENERIC_EXPLODE, 0.8f).play(viewer.getPlayer());
                viewer.closeFully();
            })
            .handleResponse(ACTION_TOGGLE, (viewer, identifier, nbtHolder) -> {
                stockOptions.setEnabled(!stockOptions.isEnabled());
                if (nbtHolder != null) {
                    this.saveFields(stockOptions, nbtHolder);
                }
                this.show(player, product, viewer.getCallback());
            })
            .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                this.saveFields(stockOptions, nbtHolder);
                product.getShop().markDirty();
                viewer.callback();
            })
        );
    }

    private void saveFields(@NonNull StockOptions stockOptions, @NonNull NightNbtHolder nbtHolder) {
        stockOptions.setCapacity(nbtHolder.getInt(JSON_CAPACITY, stockOptions.getCapacity()));
        stockOptions.setRestockMinAmount(nbtHolder.getInt(JSON_MIN_RESTOCK_AMOUNT, stockOptions.getRestockMinAmount()));
        stockOptions.setRestockMaxAmount(nbtHolder.getInt(JSON_MAX_RESTOCK_AMOUNT, stockOptions.getRestockMaxAmount()));
        stockOptions.setRestockTime(nbtHolder.getInt(JSON_RESTOCK_INTERVAL, (int) stockOptions.getRestockTime()));
    }
}
