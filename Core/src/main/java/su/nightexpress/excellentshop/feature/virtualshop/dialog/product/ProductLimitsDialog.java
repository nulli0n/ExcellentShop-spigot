package su.nightexpress.excellentshop.feature.virtualshop.dialog.product;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.feature.virtualshop.product.LimitOptions;
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

public class ProductLimitsDialog extends Dialog<VirtualProduct> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.Product.Limits.Title").text(title("Product", "Limit Settings"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.Product.Limits.Body").dialogElement(
        400,
        "Configure the individual limits for this product.",
        "",
        TagWrappers.HEAD_HAT.apply("Steve") + TagWrappers.SOFT_YELLOW.wrap(" Individual Limit") + " is a unique limit for each player. ",
        "",
        "Once a player reaches their limit, they cannot buy or sell the product until their limit resets. ",
        "Unlike global stock, individual limits are " + TagWrappers.RED.wrap("not replenished") + " by buying or selling.",
        "",
        TagWrappers.SOFT_YELLOW.wrap("→ Tip:") + " Use a value of " + TagWrappers.SOFT_YELLOW.wrap("-1") + " for disable a buy/sell limit or automatic reset."
    );

    private static final TextLocale INPUT_BUY_LIMIT = VirtualLang.builder("Dialog.Product.Limits.Input.BuyLimit")
        .text(TagWrappers.GREEN.wrap("↓") + " Buy Limit");

    private static final TextLocale INPUT_SELL_LIMIT = VirtualLang.builder("Dialog.Product.Limits.Input.SellLimit")
        .text(TagWrappers.RED.wrap("↑") + " Sell Limit");

    private static final TextLocale INPUT_REFRESH_INTERVAL = VirtualLang.builder("Dialog.Product.Limits.Input.RefreshInterval")
        .text(TagWrappers.SPRITE_ITEMS.apply("item/clock_12") + " Refresh Interval " + TagWrappers.GRAY.wrap("(in seconds)"));

    private static final ButtonLocale BUTTON_ENABLE = VirtualLang.builder("Dialog.Product.Limits.Button.Enable")
        .button(TagWrappers.SPRITE_ITEM.apply(Material.RED_DYE) + " Status: " + TagWrappers.RED.wrap("Disabled"),
            "Click to " + TagWrappers.GREEN.wrap("enable") + " the feature."
        );

    private static final ButtonLocale BUTTON_DISABLE = VirtualLang.builder("Dialog.Product.Limits.Button.Disable")
        .button(TagWrappers.SPRITE_ITEM.apply(Material.LIME_DYE) + " Status: " + TagWrappers.GREEN.wrap("Enabled"),
            "Click to " + TagWrappers.RED.wrap("disable") + " the feature."
        );

    private static final ButtonLocale BUTTON_RESET_DATA = VirtualLang.builder("Dialog.Product.Limits.Button.ResetData")
        .button(TagWrappers.SPRITE_ITEM.apply(Material.LAVA_BUCKET) + " Reset Data", "Resets all current player limit datas for this product.");

    private static final String JSON_BUY_LIMIT        = "buy_limit";
    private static final String JSON_SELL_LIMIT       = "sell_limit";
    private static final String JSON_REFRESH_INTERVAL = "refresh_interval";

    private static final String ACTION_RESET_DATA    = "reset";
    private static final String ACTION_TOGGLE = "toggle";

    @Override
    @NonNull
    public WrappedDialog create(@NotNull Player player, @NotNull VirtualProduct product) {
        LimitOptions limitOptions = product.getLimitOptions();

        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.text(JSON_BUY_LIMIT, INPUT_BUY_LIMIT).initial(String.valueOf(limitOptions.getLimit(TradeType.BUY))).maxLength(10).build(),
                    DialogInputs.text(JSON_SELL_LIMIT, INPUT_SELL_LIMIT).initial(String.valueOf(limitOptions.getLimit(TradeType.SELL))).maxLength(10).build(),
                    DialogInputs.text(JSON_REFRESH_INTERVAL, INPUT_REFRESH_INTERVAL).initial(String.valueOf(limitOptions.getRestockTime())).maxLength(20).build()
                )
                .build()
            )
            .type(
                DialogTypes.multiAction(
                        DialogButtons.action(limitOptions.isEnabled() ? BUTTON_DISABLE : BUTTON_ENABLE).action(DialogActions.customClick(ACTION_TOGGLE)).build(),
                        DialogButtons.apply(),
                        DialogButtons.action(BUTTON_RESET_DATA).action(DialogActions.customClick(ACTION_RESET_DATA)).build()
                    )
                    .exitAction(DialogButtons.back())
                    .columns(1)
                    .build()
            )
            .handleResponse(ACTION_RESET_DATA, (viewer, identifier, nbtHolder) -> {
                product.invalidatePlayerLimits();
                VanillaSound.of(Sound.ENTITY_GENERIC_EXPLODE, 0.8f).play(viewer.getPlayer());
                viewer.closeFully();
            })
            .handleResponse(ACTION_TOGGLE, (viewer, identifier, nbtHolder) -> {
                limitOptions.setEnabled(!limitOptions.isEnabled());
                if (nbtHolder != null) {
                    this.saveFields(limitOptions, nbtHolder);
                }
                this.show(player, product, viewer.getCallback());
            })
            .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                this.saveFields(limitOptions, nbtHolder);
                product.getShop().markDirty();
                viewer.callback();
            })
        );
    }

    private void saveFields(@NonNull LimitOptions limitOptions, @NonNull NightNbtHolder nbtHolder) {
        limitOptions.setBuyLimit(nbtHolder.getInt(JSON_BUY_LIMIT, limitOptions.getBuyLimit()));
        limitOptions.setSellLimit(nbtHolder.getInt(JSON_SELL_LIMIT, limitOptions.getSellLimit()));
        limitOptions.setRestockTime(nbtHolder.getInt(JSON_REFRESH_INTERVAL, (int) limitOptions.getRestockTime()));
    }
}
