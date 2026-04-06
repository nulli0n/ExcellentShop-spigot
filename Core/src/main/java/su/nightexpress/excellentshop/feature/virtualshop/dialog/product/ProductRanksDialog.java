package su.nightexpress.excellentshop.feature.virtualshop.dialog.product;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.Arrays;
import java.util.HashSet;

public class ProductRanksDialog extends Dialog<VirtualProduct> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.Product.RankRequirements.Title").text(title("Product", "Rank Requirements"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.Product.RankRequirements.Body").dialogElement(
        400,
        "Configure product accessibility based on player groups (ranks).",
        "Each group in the list must be entered on a " + TagWrappers.SOFT_YELLOW.wrap("separate line") + ".",
        "",
        TagWrappers.GREEN.wrap("✔ Allowed Ranks:") + " Grants access to players who belong to any of the listed groups.",
        TagWrappers.RED.wrap("✘ Forbidden Ranks:") + " Denies access to players who belong to any of the listed groups.",
        "",
        TagWrappers.SOFT_YELLOW.wrap("→ Note:") + " Forbidden Ranks takes priority. If a player matches both lists, they will be denied access."
    );

    private static final DialogElementLocale INPUT_ALLOWED_RANKS = VirtualLang.builder("Dialog.Product.RankRequirements.Input.AllowedRanks")
        .dialogElement(200, TagWrappers.GREEN.wrap("✔") + " Allowed Ranks");

    private static final DialogElementLocale INPUT_FORBIDDEN_RANKS = VirtualLang.builder("Dialog.Product.RankRequirements.Input.ForbiddenRanks")
        .dialogElement(200, TagWrappers.RED.wrap("✘") + " Forbidden Ranks");

    private static final String JSON_ALLOWED_RANKS   = "allowed_ranks";
    private static final String JSON_FORBIDDEN_RANKS = "forbidden_ranks";

    @Override
    @NonNull
    public WrappedDialog create(@NotNull Player player, @NotNull VirtualProduct product) {
        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.text(JSON_ALLOWED_RANKS, INPUT_ALLOWED_RANKS)
                        .initial(String.join("\n", product.getAllowedRanks()))
                        .maxLength(200)
                        .multiline(new WrappedMultilineOptions(10, 100))
                        .build(),
                    DialogInputs.text(JSON_FORBIDDEN_RANKS, INPUT_FORBIDDEN_RANKS)
                        .initial(String.join("\n", product.getForbiddenRanks()))
                        .maxLength(200)
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
            .handleResponse(DialogActions.CANCEL, (viewer, identifier, nbtHolder) -> {
                product.setAllowedRanks(new HashSet<>());
                product.setForbiddenRanks(new HashSet<>());
                product.getShop().markDirty();
                this.show(player, product, viewer.getCallback());
            })
            .handleResponse(DialogActions.OK, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                nbtHolder.getText(JSON_ALLOWED_RANKS).ifPresent(allowedRanks -> product.setAllowedRanks(Arrays.asList(allowedRanks.split("\n"))));
                nbtHolder.getText(JSON_FORBIDDEN_RANKS).ifPresent(forbiddenRanks -> product.setForbiddenRanks(Arrays.asList(forbiddenRanks.split("\n"))));

                product.getShop().markDirty();
                viewer.callback();
            })
        );
    }
}
