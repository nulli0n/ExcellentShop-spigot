package su.nightexpress.excellentshop.feature.virtualshop.dialog.shop;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.virtualshop.VSFiles;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.excellentshop.feature.virtualshop.shop.menu.ShopMenu;
import su.nightexpress.nightcore.bridge.common.NightNbtHolder;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.base.WrappedDialogAfterAction;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.WrappedDialogInput;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.single.WrappedSingleOptionEntry;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static su.nightexpress.excellentshop.ShopPlaceholders.GENERIC_PAGE;
import static su.nightexpress.excellentshop.ShopPlaceholders.GENERIC_PATH;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ShopLayoutsDialog extends Dialog<VirtualShop> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.ShopLayouts.Title").text(title("Shop", "Page Layouts"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ShopLayouts.Body").dialogElement(
        400,
        "Layouts defines the appearance of the shop's GUI.",
        "",
        "You can customize and create your own layouts in the " + SOFT_YELLOW.wrap(GENERIC_PATH) + " directory.",
        "",
        "In " + GOLD.wrap("Global") + " mode, you can set one layout for the entire shop.",
        "",
        "In " + BLUE.wrap("Paginated") + " mode, you can set a separate layout for each shop page."
    );

    private static final TextLocale INPUT_PAGE    = VirtualLang.builder("Dialog.ShopLayouts.Input.Page").text(SOFT_YELLOW.wrap("Page #" + GENERIC_PAGE));
    private static final TextLocale INPUT_DEFAULT = VirtualLang.builder("Dialog.ShopLayouts.Input.Default").text(SOFT_YELLOW.wrap("Global Layout"));

    private static final ButtonLocale BUTTON_MODE_GLOBAL = VirtualLang.builder("Dialog.ShopLayouts.Button.ModeGlobal")
        .button(SOFT_YELLOW.wrap("→") + " Switch to " + SOFT_YELLOW.wrap("Global") + " mode");

    private static final ButtonLocale BUTTON_MODE_PAGINATED = VirtualLang.builder("Dialog.ShopLayouts.Button.ModePaginated")
        .button(SOFT_YELLOW.wrap("→") + " Switch to " + SOFT_YELLOW.wrap("Paginated") + " mode");

    private static final String ACTION_MODE = "mode";

    private static final String JSON_MODE = "flag";

    private final VirtualShopModule module;

    public ShopLayoutsDialog(@NonNull VirtualShopModule module) {
        this.module = module;
    }

    @Override
    @NonNull
    public WrappedDialog create(@NonNull Player player, @NonNull VirtualShop shop) {
        Map<String, ShopMenu> currentLayouts = this.module.getLayoutByIdMap();

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

            inputs.add(DialogInputs.singleOption(String.valueOf(page), inputLocale.text().replace(ShopPlaceholders.GENERIC_PAGE, String.valueOf(page)), entries).build());
        }

        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY.replace(str -> str.replace(ShopPlaceholders.GENERIC_PATH, module.getPath().resolve(VSFiles.DIR_LAYOUTS).toString().replace(File.separatorChar, '/')))))
                .inputs(inputs)
                .afterAction(WrappedDialogAfterAction.NONE)
                .build()
            )
            .type(
                DialogTypes.multiAction(
                        DialogButtons.action(buttonLocale).action(DialogActions.customClick(ACTION_MODE, NightNbtHolder.builder().put(JSON_MODE, !isPerPage).build())).build(),
                        DialogButtons.apply()
                    )
                    .exitAction(DialogButtons.back())
                    .columns(1)
                    .build()
            )
            .handleResponse(ACTION_MODE, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                boolean value = nbtHolder.getBoolean(JSON_MODE, false);
                shop.setPaginatedLayouts(value);
                shop.markDirty();
                this.show(viewer.getPlayer(), shop, viewer.getCallback());
            })
            .handleResponse(DialogActions.BACK, (viewer, identifier, nbtHolder) -> {
                viewer.closeFully();
            })
            .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                for (int page = startPage; page < maxPages; page++) {
                    String layoutId = nbtHolder.getText(String.valueOf(page)).orElse(null);
                    if (layoutId == null) continue;

                    shop.setPageLayout(page, layoutId);
                }
                shop.markDirty();
                this.module.openShopOptions(viewer.getPlayer(), shop);
            })
        );
    }
}
