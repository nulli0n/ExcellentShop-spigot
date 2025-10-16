package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.menu.ShopLayout;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogAfterAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionEntry;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ShopLayoutsDialog extends VirtualDialogProvider<VirtualShop> {

    private static final String ACTION_MODE = "mode";
    private static final String JSON_MODE   = "flag";

    public static final TextLocale          TITLE = VirtualLang.builder("Dialog.ShopLayouts.Title").text(TITLE_PREFIX + "Page Layouts");
    public static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ShopLayouts.Body").dialogElement(
        400,
        "Layouts defines the appearance of the shop's GUI.",
        "",
        "You can customize and create your own layouts in the " + SOFT_YELLOW.wrap(GENERIC_PATH) + " directory.",
        "",
        "In " + GOLD.wrap("Global") + " mode, you can set one layout for the entire shop.",
        "",
        "In " + BLUE.wrap("Paginated") + " mode, you can set a separate layout for each shop page."
    );

    public static final TextLocale   INPUT_PAGE            = VirtualLang.builder("Dialog.ShopLayouts.Input.Page").text(SOFT_YELLOW.wrap("Page #" + GENERIC_PAGE));
    public static final TextLocale   INPUT_DEFAULT         = VirtualLang.builder("Dialog.ShopLayouts.Input.Default").text(SOFT_YELLOW.wrap("Global Layout"));
    public static final ButtonLocale BUTTON_MODE_GLOBAL    = VirtualLang.builder("Dialog.ShopLayouts.Button.ModeGlobal").button(SOFT_YELLOW.wrap("→") + " Switch to " + SOFT_YELLOW.wrap("Global") + " mode");
    public static final ButtonLocale BUTTON_MODE_PAGINATED = VirtualLang.builder("Dialog.ShopLayouts.Button.ModePaginated").button(SOFT_YELLOW.wrap("→") + " Switch to " + SOFT_YELLOW.wrap("Paginated") + " mode");

    public ShopLayoutsDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @NotNull VirtualShop shop) {
        Map<String, ShopLayout> currentLayouts = module.getLayoutByIdMap();

        List<WrappedDialogInput> inputs = new ArrayList<>();
        boolean isPerPage = shop.isPaginatedLayouts();
        int startPage = isPerPage ? 1 : 0;
        int maxPages = (isPerPage ? shop.getPages() : 0) + 1;
        TextLocale inputLocale = isPerPage ? INPUT_PAGE : INPUT_DEFAULT;
        ButtonLocale buttonLocale = isPerPage ? BUTTON_MODE_GLOBAL : BUTTON_MODE_PAGINATED;

        for (int page = startPage; page < maxPages; page++) {
            String pageLayout = shop.getLayout(page);

            List<WrappedSingleOptionEntry> entries = new ArrayList<>();
            currentLayouts.forEach((layoutId, layout) -> {
                entries.add(new WrappedSingleOptionEntry(layoutId, layoutId, pageLayout.equalsIgnoreCase(layoutId)));
            });

            inputs.add(DialogInputs.singleOption(String.valueOf(page), inputLocale.text().replace(Placeholders.GENERIC_PAGE, String.valueOf(page)), entries).build());
        }

        Dialogs.createAndShow(player, builder -> builder
                .base(DialogBases.builder(TITLE)
                    .body(DialogBodies.plainMessage(BODY.replace(str -> str.replace(Placeholders.GENERIC_PATH, this.module.getLocalPathTo(VirtualShopModule.DIR_LAYOUTS)))))
                    .inputs(inputs)
                    .afterAction(WrappedDialogAfterAction.NONE)
                    .build()
                )
                .type(
                    DialogTypes.multiAction(
                            DialogButtons.action(buttonLocale).action(DialogActions.customClick(ACTION_MODE, NightNbtHolder.builder().put(JSON_MODE, !isPerPage).build())).build(),
                            DialogButtons.action(VirtualLang.DIALOG_BUTTON_APPLY).action(DialogActions.customClick(ACTION_APPLY)).build()
                        )
                        .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).action(DialogActions.customClick(ACTION_BACK)).build())
                        .columns(1)
                        .build()
                )
                .handleResponse(ACTION_MODE, (user, identifier, nbtHolder) -> {
                    if (nbtHolder == null) return;

                    boolean value = nbtHolder.getBoolean(JSON_MODE, false);
                    shop.setPaginatedLayouts(value);
                    shop.markDirty();
                    this.showNextTick(user.getPlayer(), shop);
                })
                .handleResponse(ACTION_BACK, (user, identifier, nbtHolder) -> {
                    this.close(user.getPlayer());
                })
                .handleResponse(ACTION_APPLY, (user, identifier, nbtHolder) -> {
                    if (nbtHolder == null) return;

                    for (int page = startPage; page < maxPages; page++) {
                        String layoutId = nbtHolder.getText(String.valueOf(page)).orElse(null);
                        if (layoutId == null) continue;

                        shop.setPageLayout(page, layoutId);
                    }
                    shop.markDirty();
                    this.closeAndThen(user.getPlayer(), shop, this.module::openShopOptions);
                })
        );
    }
}
