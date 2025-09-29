package su.nightexpress.nexshop.shop.virtual.dialog.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.dialog.VirtualDialogProvider;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.bridge.dialog.wrap.input.text.WrappedMultilineOptions;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.util.Players;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static su.nightexpress.nexshop.Placeholders.GENERIC_PERMISSION;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ShopAliasesDialog extends VirtualDialogProvider<VirtualShop> {

    private static final String JSON_RELOAD  = "reload";
    private static final String JSON_ALIASES = "aliases";


    public static final TextLocale          TITLE = VirtualLang.builder("Dialog.ShopAliases.Title").text(TITLE_PREFIX + "Shop Aliases");
    public static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.ShopAliases.Body").dialogElement(
        400,
        "Here you can add custom aliases for the shop.",
        "These aliases will be registered as " + SOFT_YELLOW.wrap("server commands") + ", and running one of them will open this shop.",
        "",
        SOFT_ORANGE.wrap("⚠") + " Players must have the " + SOFT_ORANGE.wrap(GENERIC_PERMISSION) + " permission to access these commands.",
        "",
        SOFT_RED.wrap("→") + " Enter " + SOFT_RED.wrap("one") + " alias " + SOFT_RED.wrap("per line") + "."
    );

    public static final DialogElementLocale INPUT_ALIASES = VirtualLang.builder("Dialog.ShopAliases.Input.Text").dialogElement("Aliases");
    public static final TextLocale          INPUT_RELOAD  = VirtualLang.builder("Dialog.ShopAliases.Input.Reload").text("Reload commands");

    public ShopAliasesDialog(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, module);
    }

    @Override
    public void show(@NotNull Player player, @NotNull VirtualShop shop) {
        List<WrappedActionButton> actions = new ArrayList<>();
        actions.add(DialogButtons.action(VirtualLang.DIALOG_BUTTON_APPLY).action(DialogActions.customClick(ACTION_APPLY)).build());
        if (shop.hasAliases()) {
            actions.add(DialogButtons.action(VirtualLang.DIALOG_BUTTON_RESET).action(DialogActions.customClick(ACTION_RESET)).build());
        }

        Dialogs.createAndShow(player, builder -> builder
                .base(DialogBases.builder(TITLE)
                    .body(DialogBodies.plainMessage(BODY))
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
                .type(DialogTypes.multiAction(actions)
                    .exitAction(DialogButtons.action(VirtualLang.DIALOG_BUTTON_BACK).build())
                    .columns(2)
                    .build()
                )
                .handleResponse(ACTION_RESET, (user, identifier, nbtHolder) -> {
                    boolean reload = nbtHolder != null && nbtHolder.getBoolean(JSON_RELOAD, false);
                    this.setShopAliases(player, shop, new String[]{}, reload);
                })
                .handleResponse(ACTION_APPLY, (user, identifier, nbtHolder) -> {
                    if (nbtHolder == null) return;

                    boolean reload = nbtHolder.getBoolean(JSON_RELOAD, false);
                    String aliases = nbtHolder.getText(JSON_ALIASES).orElse(null);
                    if (aliases == null) return;

                    this.setShopAliases(player, shop, aliases.split("\n"), reload);
                })
            , replacer -> replacer.replace(Placeholders.GENERIC_PERMISSION, VirtualPerms.COMMAND_SHOP::getName));
    }

    private void setShopAliases(@NotNull Player user, @NotNull VirtualShop shop, @NotNull String[] aliases, boolean reload) {
        shop.setAliases(Arrays.stream(aliases).map(s -> s.startsWith("/") && s.length() >= 2 ? s.substring(1) : s).collect(Collectors.toSet()));
        shop.setSaveRequired(true);

        if (reload) {
            this.module.reloadShopAliases(shop);
            Players.getOnline().forEach(Player::updateCommands);
        }

        this.closeAndThen(user, shop, this.module::openShopOptions);
    }
}
