package su.nightexpress.excellentshop.feature.virtualshop.dialog.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;

import static su.nightexpress.excellentshop.ShopPlaceholders.SHOP_NAME;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.SOFT_YELLOW;

public class ShopNameDialog extends Dialog<VirtualShop> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.ShopName.Title").text(title("Shop", "Name"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ShopName.Body").dialogElement(
        250,
        "Sets custom shop display name.",
        "",
        "Feel free to use the " + SOFT_YELLOW.wrap(SHOP_NAME) + " placeholder in configuration files to \"insert\" shop name in GUIs and messages.",
        ""
    );

    private static final DialogElementLocale INPUT_NAME = VirtualLang.builder("Dialog.ShopName.Input.ShopName").dialogElement("Name");

    private static final String JSON_NAME = "name";

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull VirtualShop shop) {
        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(DialogInputs.text(JSON_NAME, INPUT_NAME)
                    .initial(shop.getName())
                    .maxLength(100)
                    .build())
                .build()
            )
            .type(DialogTypes.multiAction(DialogButtons.apply())
                .exitAction(DialogButtons.back())
                .columns(2)
                .build()
            )
            .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String name = nbtHolder.getText(JSON_NAME).orElse(null);
                if (name == null || name.isBlank()) return;

                shop.setName(name);
                shop.markDirty();
                viewer.closeFully();
            })
        );
    }
}
