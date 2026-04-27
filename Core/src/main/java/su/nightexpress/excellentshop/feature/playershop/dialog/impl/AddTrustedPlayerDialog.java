package su.nightexpress.excellentshop.feature.playershop.dialog.impl;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import su.nightexpress.excellentshop.feature.playershop.ChestShopModule;
import su.nightexpress.excellentshop.feature.playershop.core.ChestLang;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestShop;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
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

public class AddTrustedPlayerDialog extends Dialog<ChestShop> {

    private static final TextLocale TITLE = ChestLang.builder(
        "Dialog.Shop.AddTrustedPlayer.Title").text(title("Shop", "Add Trusted Player"));

    private static final DialogElementLocale BODY = ChestLang
        .builder("Dialog.Shop.AddTrustedPlayer.Body")
        .dialogElement("Enter the name of the player you want to trust.");

    private static final TextLocale INPUT_NAME = ChestLang.builder(
        "Dialog.Shop.AddTrustedPlayer.Input.Name").text("Player Name");

    private static final String JSON_NAME = "player_name";

    private final ChestShopModule module;

    public AddTrustedPlayerDialog(@NonNull ChestShopModule module) {
        this.module = module;
    }

    @Override
    public @NonNull WrappedDialog create(@NonNull Player player, @NonNull ChestShop shop) {
        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(DialogInputs.text(JSON_NAME, INPUT_NAME).maxLength(32).build())
                .build()
            )
            .type(DialogTypes.confirmation(DialogButtons.confirm(), DialogButtons.cancel()))
            .handleResponse(DialogActions.CONFIRM, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                nbtHolder.getText(JSON_NAME).ifPresent(targetName -> {
                    this.module.addTrustedPlayer(player, shop, targetName, viewer.getCallback());
                });
            })
        );
    }

}
