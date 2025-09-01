package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import static su.nightexpress.nexshop.Placeholders.GENERIC_PATH;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ShopCreationDialog extends VirtualDialogProvider<Void> {

    private static final String JSON_ID = "id";

    public static final TextLocale          TITLE         = VirtualLang.builder("Dialog.ShopCreation.Title").text(SOFT_AQUA.and(BOLD).wrap(TITLE_PREFIX + "Shop Creation"));
    public static final DialogElementLocale BODY          = VirtualLang.builder("Dialog.ShopCreation.Body").dialogElement(
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
    public static final DialogElementLocale INPUT_SHOP_ID = VirtualLang.builder("Dialog.ShopCreation.Input.ShopId").dialogElement("Shop ID");
    public static final ButtonLocale        BUTTON_CREATE = VirtualLang.builder("Dialog.ShopCreation.Button.Create").button(SOFT_YELLOW.wrap("Create!"));

    public ShopCreationDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @Nullable Void unused) {
        Dialogs.createAndShow(player, builder -> builder
                .base(DialogBases.builder(TITLE)
                    .body(DialogBodies.plainMessage(BODY))
                    .inputs(DialogInputs.text(JSON_ID, INPUT_SHOP_ID).initial("my_amazing_shop").build())
                    .build()
                )
                .type(DialogTypes.multiAction(DialogButtons.action(BUTTON_CREATE).action(DialogActions.customClick(ACTION_APPLY)).build())
                    .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).build())
                    .columns(2)
                    .build()
                )
                .handleResponse(ACTION_APPLY, (user, identifier, nbtHolder) -> {
                    if (nbtHolder == null) return;

                    String id = nbtHolder.getText(JSON_ID).orElse(null);
                    if (id == null || id.isBlank()) return;

                    this.module.createShop(user, id);
                    this.closeAndThen(user, unused, this.module::openShopsEditor);
                })
            , replacer -> replacer.replace(Placeholders.GENERIC_PATH, () -> this.module.getLocalPathTo(VirtualShopModule.DIR_SHOPS)));
    }
}
