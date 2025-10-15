package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ShopNameDialog extends VirtualDialogProvider<VirtualShop> {

    private static final String JSON_NAME = "name";

    public static final TextLocale          TITLE      = VirtualLang.builder("Dialog.ShopName.Title").text(TITLE_PREFIX + "Shop Name");
    public static final DialogElementLocale BODY       = VirtualLang.builder("Dialog.ShopName.Body").dialogElement(
        250,
        "Sets custom shop display name.",
        "",
        "Feel free to use the " + SOFT_YELLOW.wrap(SHOP_NAME) + " placeholder in configuration files to \"insert\" shop name in GUIs and messages.",
        ""
    );
    public static final DialogElementLocale INPUT_NAME = VirtualLang.builder("Dialog.ShopName.Input.ShopName").dialogElement("Name");

    public ShopNameDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @NotNull VirtualShop shop) {
        Dialogs.createAndShow(player, builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(DialogInputs.text(JSON_NAME, INPUT_NAME)
                    .initial(shop.getName())
                    .maxLength(100)
                    .build())
                .build()
            )
            .type(DialogTypes.multiAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_APPLY).action(DialogActions.customClick(ACTION_APPLY)).build())
                .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).build())
                .columns(2)
                .build()
            )
            .handleResponse(ACTION_APPLY, (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String name = nbtHolder.getText(JSON_NAME).orElse(null);
                if (name == null || name.isBlank()) return;

                shop.setName(name);
                shop.markDirty();
                this.closeAndThen(user.getPlayer(), shop, this.module::openShopOptions);
            })
        );
    }
}
