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

public class ProductPermissionsDialog extends Dialog<VirtualProduct> {

    private static final TextLocale TITLE = VirtualLang.builder("Dialog.Product.PermissionRequirements.Title")
        .text(title("Product", "Permission Requirements"));

    private static final DialogElementLocale BODY = VirtualLang.builder("Dialog.Product.PermissionRequirements.Body")
        .dialogElement(
            400,
            "Configure product accessibility based on player permissions.",
            "Each permission in the list must be entered on a " + TagWrappers.SOFT_YELLOW.wrap("separate line") + ".",
            "",
            TagWrappers.GREEN.wrap("✔ Allowed Perms:") +
                " Grants access to players who has any of the listed permissions.",
            TagWrappers.RED.wrap("✘ Forbidden Perms:") +
                " Denies access to players who has any of the listed permissions.",
            "",
            TagWrappers.SOFT_YELLOW.wrap("→ Note:") +
                " Forbidden Perms takes priority. If a player matches both lists, they will be denied access."
        );

    private static final DialogElementLocale INPUT_ALLOWED_PERMS = VirtualLang.builder(
        "Dialog.Product.PermissionRequirements.Input.AllowedPerms")
        .dialogElement(200, TagWrappers.GREEN.wrap("✔") + " Allowed Perms");

    private static final DialogElementLocale INPUT_FORBIDDEN_PERMS = VirtualLang.builder(
        "Dialog.Product.PermissionRequirements.Input.ForbiddenPerms")
        .dialogElement(200, TagWrappers.RED.wrap("✘") + " Forbidden Perms");

    private static final String JSON_ALLOWED_PERMS   = "allowed_perms";
    private static final String JSON_FORBIDDEN_PERMS = "forbidden_perms";

    @Override
    @NonNull
    public WrappedDialog create(@NotNull Player player, @NotNull VirtualProduct product) {
        return Dialogs.create(builder -> builder
            .base(DialogBases.builder(TITLE)
                .body(DialogBodies.plainMessage(BODY))
                .inputs(
                    DialogInputs.text(JSON_ALLOWED_PERMS, INPUT_ALLOWED_PERMS)
                        .initial(String.join("\n", product.getRequiredPermissions()))
                        .maxLength(200)
                        .multiline(new WrappedMultilineOptions(10, 100))
                        .build(),
                    DialogInputs.text(JSON_FORBIDDEN_PERMS, INPUT_FORBIDDEN_PERMS)
                        .initial(String.join("\n", product.getForbiddenPermissions()))
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
                product.setRequiredPermissions(new HashSet<>());
                product.setForbiddenPermissions(new HashSet<>());
                product.getShop().markDirty();
                this.show(player, product, viewer.getCallback());
            })
            .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                if (nbtHolder == null) return;

                nbtHolder.getText(JSON_ALLOWED_PERMS)
                    .ifPresent(allowedRanks -> product.setRequiredPermissions(Arrays.asList(allowedRanks.split("\n"))));

                nbtHolder.getText(JSON_FORBIDDEN_PERMS)
                    .ifPresent(forbiddenRanks -> product.setForbiddenPermissions(Arrays.asList(forbiddenRanks.split(
                        "\n"))));

                product.getShop().markDirty();
                viewer.callback();
            })
        );
    }
}
