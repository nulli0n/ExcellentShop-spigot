package su.nightexpress.excellentshop.feature.virtualshop.dialog.product;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.product.content.CommandContent;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.integration.placeholder.PAPI;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;

import java.util.Arrays;

import static su.nightexpress.excellentshop.ShopPlaceholders.GENERIC_AMOUNT;
import static su.nightexpress.nightcore.util.Placeholders.PLAYER_NAME;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ProductCommandsDialog extends Dialog<VirtualProduct> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.ProductCommands.Title").text(title("Produc", "Commands"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ProductCommands.Body").dialogElement(
        400,
        "Enter the commands that will be executed when a player purchases this item.",
        SOFT_RED.wrap("(one command per line)"),
        "",
        SOFT_YELLOW.and(BOLD).wrap("PLACEHOLDERS:"),
        SOFT_YELLOW.wrap("→") + " The " + SOFT_YELLOW.wrap(GENERIC_AMOUNT) + " placeholder for the " + SOFT_YELLOW.wrap("selected quantity") + ".",
        SOFT_YELLOW.wrap("→") + " The " + SOFT_YELLOW.wrap(PLAYER_NAME) + " placeholder for the " + SOFT_YELLOW.wrap("player name") + ".",
        "",
        GREEN.wrap("✔") + " Internal " + GREEN.wrap("Shop") + " and " + GREEN.wrap("Product") + " placeholders are available.",
        GREEN.wrap("✔") + " External " + GREEN.wrap(PAPI.NAME) + " placeholders are available."
    );

    private static final DialogElementLocale INPUT_COMMANDS = VirtualLang.builder("Dialog.ProductCommands.Input.Text").dialogElement(300, "Commands");

    private static final String JSON_COMMANDS = "commands";

    @Override
    @NonNull
    public WrappedDialog create(@NonNull Player player, @NonNull VirtualProduct product) {
        CommandContent content = (CommandContent) product.getContent();

        return Dialogs.create(builder -> builder
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
            .type(DialogTypes.multiAction(DialogButtons.apply(), DialogButtons.reset())
                .exitAction(DialogButtons.back())
                .columns(2)
                .build()
            )
            .handleResponse(DialogActions.RESET, (viewer, identifier, nbtHolder) -> {
                this.setShopAliases(product, content, new String[0]);
                this.show(viewer.getPlayer(), product, viewer.getCallback());
            })
            .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                String aliases = nbtHolder.getText(JSON_COMMANDS).orElse(null);
                if (aliases == null) return;

                this.setShopAliases(product, content, aliases.split("\n"));
                viewer.closeFully();
            })
        );
    }

    private void setShopAliases(@NonNull VirtualProduct product, @NonNull CommandContent content, @NonNull String[] aliases) {
        content.setCommands(Arrays.stream(aliases).map(s -> s.startsWith("/") && s.length() >= 2 ? s.substring(1) : s).toList());
        product.getShop().markDirty();
    }
}
