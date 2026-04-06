package su.nightexpress.excellentshop.feature.virtualshop.dialog.shop;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.util.Lists;

import java.util.Collections;

import static su.nightexpress.excellentshop.ShopPlaceholders.VIRTUAL_SHOP_DESCRIPTION;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.SOFT_YELLOW;

public class ShopDescriptionDialog extends Dialog<VirtualShop> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.ShopDescription.Title").text(title("Shop", "Description"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ShopDescription.Body").dialogElement(
        300,
        "Sets custom shop description.",
        "",
        "Feel free to use the " + SOFT_YELLOW.wrap(VIRTUAL_SHOP_DESCRIPTION) + " placeholder in configuration files to \"insert\" shop description in GUIs and messages.",
        ""
    );

    private static final DialogElementLocale INPUT_DESCRIPTION = VirtualLang.builder("Dialog.ShopDescription.Input.Text").dialogElement("Description");

    private static final String JSON_DESCRIPTION = "description";

    @Override
    @NonNull
    public WrappedDialog create(@NonNull Player player, @NonNull VirtualShop shop) {
        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(DialogInputs.text(JSON_DESCRIPTION, INPUT_DESCRIPTION)
                    .initial(String.join("\n", shop.getDescription()))
                    .maxLength(500)
                    .multiline(new WrappedMultilineOptions(10, 100))
                    .build())
                .build()
            )
            .type(DialogTypes.multiAction(DialogButtons.apply(), DialogButtons.reset())
                .exitAction(DialogButtons.back())
                .columns(2)
                .build()
            )
            .handleResponse(DialogActions.RESET, (viewer, identifier, nbtHolder) -> {
                this.setDescription(shop, null);
                this.show(viewer.getPlayer(), shop, viewer.getCallback());
            })
            .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String description = nbtHolder.getText(JSON_DESCRIPTION).orElse(null);
                if (description == null) return;

                this.setDescription(shop, description);
                viewer.closeFully();
            })
        );
    }

    private void setDescription(@NonNull VirtualShop shop, @Nullable String desc) {
        shop.setDescription(desc == null ? Collections.emptyList() : Lists.newList(desc.split("\n")));
        shop.markDirty();
    }
}
