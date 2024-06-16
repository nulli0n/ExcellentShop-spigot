package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.dialog.Dialog;
import su.nightexpress.nightcore.dialog.DialogHandler;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.util.text.TextRoot;

public abstract class ShopEditorMenu extends ConfigMenu<ShopPlugin> {

    public ShopEditorMenu(@NotNull ShopPlugin plugin, @NotNull FileConfig cfg) {
        super(plugin, cfg);
    }

    protected Dialog handleInput(@NotNull MenuViewer viewer, @NotNull LangString prompt, @NotNull DialogHandler handler) {
        return this.handleInput(viewer.getPlayer(), prompt, handler);
    }

    protected Dialog handleInput(@NotNull Player player, @NotNull LangString prompt, @NotNull DialogHandler handler) {
        return this.handleInput(player, prompt.getMessage(), handler);
    }

    protected Dialog handleInput(@NotNull Player player, @NotNull TextRoot prompt, @NotNull DialogHandler handler) {
        Dialog dialog = Dialog.create(player, handler);
        dialog.prompt(prompt);
        this.runNextTick(player::closeInventory);
        return dialog;
    }
}
