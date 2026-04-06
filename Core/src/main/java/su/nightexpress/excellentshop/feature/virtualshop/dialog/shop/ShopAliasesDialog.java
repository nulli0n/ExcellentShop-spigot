package su.nightexpress.excellentshop.feature.virtualshop.dialog.shop;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualPerms;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.util.Players;

import java.util.Arrays;
import java.util.stream.Collectors;

import static su.nightexpress.excellentshop.ShopPlaceholders.GENERIC_PERMISSION;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ShopAliasesDialog extends Dialog<VirtualShop> {

    private static final String JSON_RELOAD  = "reload";
    private static final String JSON_ALIASES = "aliases";

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.ShopAliases.Title").text(title("Shop", "Aliases"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ShopAliases.Body").dialogElement(
        400,
        "Here you can add custom aliases for the shop.",
        "These aliases will be registered as " + SOFT_YELLOW.wrap("server commands") + ", and running one of them will open this shop.",
        "",
        SOFT_ORANGE.wrap("⚠") + " Players must have the " + SOFT_ORANGE.wrap(GENERIC_PERMISSION) + " permission to access these commands.",
        "",
        SOFT_RED.wrap("→") + " Enter " + SOFT_RED.wrap("one") + " alias " + SOFT_RED.wrap("per line") + "."
    );

    private static final DialogElementLocale INPUT_ALIASES = VirtualLang.builder("Dialog.ShopAliases.Input.Text").dialogElement("Aliases");
    private static final TextLocale          INPUT_RELOAD  = VirtualLang.builder("Dialog.ShopAliases.Input.Reload").text("Reload commands");

    private final VirtualShopModule module;
    
    public ShopAliasesDialog(@NonNull VirtualShopModule module) {
        this.module = module;
    }

    @Override
    @NonNull
    public WrappedDialog create(@NonNull Player player, @NonNull VirtualShop shop) {
        return Dialogs.create(builder -> builder
                .base(DialogBases.builder(TITLE)
                    .body(DialogBodies.plainMessage(BODY.replace(str -> str.replace(ShopPlaceholders.GENERIC_PERMISSION, VirtualPerms.COMMAND_SHOP.getName()))))
                    .inputs(
                        DialogInputs.text(JSON_ALIASES, INPUT_ALIASES)
                            .initial(String.join("\n", shop.getSlashedAliases()))
                            .maxLength(150)
                            .multiline(new WrappedMultilineOptions(10, 100))
                            .build(),
                        DialogInputs.bool(JSON_RELOAD, INPUT_RELOAD).initial(false).build()
                    )
                    .build()
                )
                .type(DialogTypes.multiAction(DialogButtons.apply(), DialogButtons.reset())
                    .exitAction(DialogButtons.back())
                    .columns(2)
                    .build()
                )
                .handleResponse(DialogActions.RESET, (viewer, identifier, nbtHolder) -> {
                    boolean reload = nbtHolder != null && nbtHolder.getBoolean(JSON_RELOAD, false);
                    this.setShopAliases(shop, new String[]{}, reload);
                    this.show(viewer.getPlayer(), shop, viewer.getCallback());
                })
                .handleResponse(DialogActions.APPLY, (viewer, identifier, nbtHolder) -> {
                    if (nbtHolder == null) return;

                    boolean reload = nbtHolder.getBoolean(JSON_RELOAD, false);
                    String aliases = nbtHolder.getText(JSON_ALIASES).orElse(null);
                    if (aliases == null) return;

                    this.setShopAliases(shop, aliases.split("\n"), reload);
                    viewer.closeFully();
                })
            );
    }

    private void setShopAliases(@NonNull VirtualShop shop, @NonNull String[] aliases, boolean reload) {
        shop.setAliases(Arrays.stream(aliases).map(s -> s.startsWith("/") && s.length() >= 2 ? s.substring(1) : s).collect(Collectors.toSet()));
        shop.markDirty();

        if (reload) {
            this.module.reloadShopAliases(shop);
            Players.getOnline().forEach(Player::updateCommands);
        }
    }
}
