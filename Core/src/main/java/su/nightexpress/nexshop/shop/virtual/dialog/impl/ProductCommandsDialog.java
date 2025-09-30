package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.product.content.impl.CommandContent;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.Plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static su.nightexpress.nexshop.Placeholders.GENERIC_AMOUNT;
import static su.nightexpress.nightcore.util.Placeholders.PLAYER_NAME;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ProductCommandsDialog extends VirtualDialogProvider<VirtualProduct> {

    private static final String JSON_COMMANDS = "commands";

    public static final TextLocale          TITLE = VirtualLang.builder("Dialog.ProductCommands.Title").text(TITLE_PREFIX + "Product Commands");
    public static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ProductCommands.Body").dialogElement(
        400,
        "Enter the commands that will be executed when a player purchases this item.",
        SOFT_RED.wrap("(one command per line)"),
        "",
        SOFT_YELLOW.and(BOLD).wrap("PLACEHOLDERS:"),
        SOFT_YELLOW.wrap("→") + " The " + SOFT_YELLOW.wrap(GENERIC_AMOUNT) + " placeholder for the " + SOFT_YELLOW.wrap("selected quantity") + ".",
        SOFT_YELLOW.wrap("→") + " The " + SOFT_YELLOW.wrap(PLAYER_NAME) + " placeholder for the " + SOFT_YELLOW.wrap("player name") + ".",
        "",
        GREEN.wrap("✔") + " Internal " + GREEN.wrap("Shop") + " and " + GREEN.wrap("Product") + " placeholders are available.",
        GREEN.wrap("✔") + " External " + GREEN.wrap(Plugins.PLACEHOLDER_API) + " placeholders are available."
    );

    public static final DialogElementLocale INPUT_COMMANDS = VirtualLang.builder("Dialog.ProductCommands.Input.Text").dialogElement(300, "Commands");

    public ProductCommandsDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @NotNull VirtualProduct product) {
        CommandContent content = (CommandContent) product.getContent();

        List<WrappedActionButton> actions = new ArrayList<>();
        actions.add(DialogButtons.action(VirtualLang.DIALOG_BUTTON_APPLY).action(DialogActions.customClick(ACTION_APPLY)).build());
        if (content.hasCommands()) {
            actions.add(DialogButtons.action(VirtualLang.DIALOG_BUTTON_RESET).action(DialogActions.customClick(ACTION_RESET)).build());
        }

        Dialogs.createAndShow(player, builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.text(JSON_COMMANDS, INPUT_COMMANDS)
                        .initial(String.join("\n", content.getCommands()))
                        .maxLength(300)
                        .multiline(new WrappedMultilineOptions(10, 100))
                        .build()
                )
                .build()
            )
            .type(DialogTypes.multiAction(actions)
                .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).build())
                .columns(2)
                .build()
            )
            .handleResponse(ACTION_RESET, (user, identifier, nbtHolder) -> {
                this.setShopAliases(player, product, content, new String[0]);
            })
            .handleResponse(ACTION_APPLY, (user, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String aliases = nbtHolder.getText(JSON_COMMANDS).orElse(null);
                if (aliases == null) return;

                this.setShopAliases(player, product, content, aliases.split("\n"));
            })
        );
    }

    private void setShopAliases(@NotNull Player user, @NotNull VirtualProduct product, @NotNull CommandContent content, @NotNull String[] aliases) {
        content.setCommands(Arrays.stream(aliases).map(s -> s.startsWith("/") && s.length() >= 2 ? s.substring(1) : s).toList());
        product.setSaveRequired(true);

        this.closeAndThen(user, product, this.module::openProductOptions);
    }
}
