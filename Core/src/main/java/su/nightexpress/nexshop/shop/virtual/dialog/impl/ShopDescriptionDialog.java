package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static su.nightexpress.nexshop.Placeholders.VIRTUAL_SHOP_DESCRIPTION;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.SOFT_YELLOW;

public class ShopDescriptionDialog extends VirtualDialogProvider<VirtualShop> {

    private static final String JSON_DESCRIPTION = "description";

    public static final TextLocale          TITLE             = VirtualLang.builder("Dialog.ShopDescription.Title").text(TITLE_PREFIX + "Shop Description");
    public static final DialogElementLocale BODY              = VirtualLang.builder("Dialog.ShopDescription.Body").dialogElement(
        300,
        "Sets custom shop description.",
        "",
        "Feel free to use the " + SOFT_YELLOW.wrap(VIRTUAL_SHOP_DESCRIPTION) + " placeholder in configuration files to \"insert\" shop description in GUIs and messages.",
        ""
    );
    public static final DialogElementLocale INPUT_DESCRIPTION = VirtualLang.builder("Dialog.ShopDescription.Input.Text").dialogElement("Description");

    public ShopDescriptionDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @NotNull VirtualShop shop) {
        List<WrappedActionButton> actions = new ArrayList<>();
        actions.add(DialogButtons.action(VirtualLang.DIALOG_BUTTON_APPLY).action(DialogActions.customClick(ACTION_APPLY)).build());
        if (shop.hasDescription()) {
            actions.add(DialogButtons.action(VirtualLang.DIALOG_BUTTON_RESET).action(DialogActions.customClick(ACTION_RESET)).build());
        }

        Dialogs.createAndShow(player, builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(DialogInputs.text(JSON_DESCRIPTION, INPUT_DESCRIPTION)
                    .initial(String.join("\n", shop.getDescription()))
                    .maxLength(500)
                    .multiline(new WrappedMultilineOptions(10, 100))
                    .build())
                .build()
            )
            .type(DialogTypes.multiAction(actions)
                .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).build())
                .columns(2)
                .build()
            )
            .handleResponse(ACTION_RESET, (user, identifier, nbtHolder) -> {
                this.setDescription(user, shop, null);
            })
            .handleResponse(ACTION_APPLY, (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String description = nbtHolder.getText(JSON_DESCRIPTION).orElse(null);
                if (description == null) return;

                this.setDescription(user, shop, description);
            })
        );
    }

    private void setDescription(@NotNull Player user, @NotNull VirtualShop shop, @Nullable String desc) {
        shop.setDescription(desc == null ? Collections.emptyList() : Lists.newList(desc.split("\n")));
        shop.setSaveRequired(true);
        this.closeAndThen(user, shop, this.module::openShopOptions);
    }
}
