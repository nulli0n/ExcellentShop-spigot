package su.nightexpress.excellentshop.feature.virtualshop.dialog.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShopDefinition;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.SOFT_RED;

public class ShopPagesDialog extends Dialog<VirtualShop> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.ShopPages.Title").text(title("Shop", "Number of Pages"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ShopPages.Body").dialogElement(
        400,
        "Defines how many pages the shop will have.",
        "",
        SOFT_RED.wrap("→") + "Ensure that " + SOFT_RED.wrap("Shop Layout(s)") + " config file contains " + SOFT_RED.wrap("page buttons") + "!",
        ""
    );

    private static final DialogElementLocale INPUT_PAGES = VirtualLang.builder("Dialog.ShopPages.Input.Pages").dialogElement("Pages");

    private static final String JSON_PAGES = "pages";

    @Override
    @NotNull
    public WrappedDialog create(@NotNull Player player, @NotNull VirtualShop shop) {
        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.numberRange(JSON_PAGES, INPUT_PAGES, VirtualShopDefinition.MIN_PAGES, VirtualShopDefinition.MAX_PAGES)
                        .initial((float) shop.getPages())
                        .step(1F)
                        .build()
                )
                .build()
            )
            .type(
                DialogTypes.multiAction(DialogButtons.apply())
                    .exitAction(DialogButtons.back())
                    .columns(2)
                    .build()
            )
            .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                int pages = nbtHolder.getInt(JSON_PAGES, -1);
                if (pages <= 0) return;

                shop.setPages(pages);
                shop.markDirty();
                viewer.closeFully();
            })
        );
    }
}
