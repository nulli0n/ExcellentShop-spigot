package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.impl.ShopSettings;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ShopPagesDialog extends VirtualDialogProvider<VirtualShop> {

    private static final String JSON_PAGES = "pages";

    public static final TextLocale          TITLE       = VirtualLang.builder("Dialog.ShopPages.Title").text(TITLE_PREFIX + "Pages Amount");
    public static final DialogElementLocale BODY        = VirtualLang.builder("Dialog.ShopPages.Body").dialogElement(
        400,
        "Defines how many pages the shop will have.",
        "",
        SOFT_RED.wrap("→") + "Ensure that " + SOFT_RED.wrap("Shop Layout(s)") + " config file contains " + SOFT_RED.wrap("page buttons") + "!",
        ""
    );
    public static final DialogElementLocale INPUT_PAGES = VirtualLang.builder("Dialog.ShopPages.Input.Pages").dialogElement("Pages");

    public ShopPagesDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @NotNull VirtualShop shop) {
        Dialogs.createAndShow(player, builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.numberRange(JSON_PAGES, INPUT_PAGES, ShopSettings.MIN_PAGES, ShopSettings.MAX_PAGES)
                        .initial((float) shop.getPages())
                        .step(1F)
                        .build()
                )
                .build()
            )
            .type(
                DialogTypes.multiAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_APPLY).action(DialogActions.customClick(ACTION_APPLY)).build())
                    .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).build())
                    .columns(2)
                    .build()
            )
            .handleResponse(ACTION_APPLY, (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                int pages = nbtHolder.getInt(JSON_PAGES, -1);
                if (pages <= 0) return;

                shop.setPages(pages);
                shop.markDirty();
                this.closeAndThen(user.getPlayer(), shop, this.module::openShopOptions);
            })
        );
    }
}
