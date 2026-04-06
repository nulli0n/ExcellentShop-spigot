package su.nightexpress.excellentshop.feature.virtualshop.dialog.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.virtualshop.VSFiles;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;

import java.io.File;

import static su.nightexpress.excellentshop.ShopPlaceholders.GENERIC_PATH;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ShopCreationDialog extends Dialog<VirtualShopModule> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.ShopCreation.Title").text(title("Shop", "Creation"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ShopCreation.Body").dialogElement(
        400,
        "Provide an " + SOFT_YELLOW.wrap("internal name") + " for a new shop.",
        "This name is not visible for players and " + SOFT_YELLOW.wrap("must be unique") + " for each shop.",
        "",
        GREEN.wrap("✔") + " Valid Input: " + GREEN.wrap("letters " + GRAY.wrap("(abc)")) + ", " + GREEN.wrap("numbers " + GRAY.wrap("(shop123)")) + ", " + GREEN.wrap("underscore " + GRAY.wrap("(my_shop)")),
        "",
        SOFT_RED.wrap("✘") + " Invalid Input: " + SOFT_RED.wrap("spaces " + GRAY.wrap("(my shop)")) + ", " + SOFT_RED.wrap("colors " + GRAY.wrap("(\\&dMyShop)")) + ", " + SOFT_RED.wrap("emojis " + GRAY.wrap("(♥my_shop♥)")),
        "",
        SOFT_YELLOW.wrap("→") + " Shop files will be created in the " + SOFT_YELLOW.wrap(GENERIC_PATH) + " directory.",
        ""
    );

    private static final DialogElementLocale INPUT_SHOP_ID = VirtualLang.builder("Dialog.ShopCreation.Input.ShopId").dialogElement("Shop ID");

    private static final ButtonLocale BUTTON_CREATE = VirtualLang.builder("Dialog.ShopCreation.Button.Create").button(SOFT_YELLOW.wrap("Create!"));

    private static final String JSON_ID = "id";

    @Override
    @NotNull
    public WrappedDialog create(@NonNull Player player, @NonNull VirtualShopModule module) {
        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY.replace(str -> str.replace(ShopPlaceholders.GENERIC_PATH, module.getPath().resolve(VSFiles.DIR_SHOPS).toString().replace(File.separatorChar, '/')))))
                .inputs(DialogInputs.text(JSON_ID, INPUT_SHOP_ID).initial("my_amazing_shop").build())
                .build()
            )
            .type(DialogTypes.multiAction(DialogButtons.action(BUTTON_CREATE).action(DialogActions.customClick(DialogActions.APPLY)).build())
                .exitAction(DialogButtons.back())
                .columns(2)
                .build()
            )
            .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String id = nbtHolder.getText(JSON_ID).orElse(null);
                if (id == null || id.isBlank()) return;

                module.createShop(viewer.getPlayer(), id);
                viewer.closeFully();
            })
        );
    }
}
