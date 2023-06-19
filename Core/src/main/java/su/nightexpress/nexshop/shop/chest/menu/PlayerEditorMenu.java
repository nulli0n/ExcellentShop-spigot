package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.editor.InputHandler;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.editor.EditorManager;
import su.nightexpress.nexshop.ExcellentShop;

public class PlayerEditorMenu extends ConfigMenu<ExcellentShop> {

    public PlayerEditorMenu(@NotNull ExcellentShop plugin, @NotNull JYML cfg) {
        super(plugin, cfg);
    }

    protected void handleInput(@NotNull MenuViewer viewer, @NotNull LangKey prompt, @NotNull InputHandler handler) {
        this.handleInput(viewer.getPlayer(), prompt, handler);
    }

    protected void handleInput(@NotNull Player player, @NotNull LangKey prompt, @NotNull InputHandler handler) {
        this.handleInput(player, this.plugin.getMessage(prompt), handler);
    }

    protected void handleInput(@NotNull Player player, @NotNull LangMessage prompt, @NotNull InputHandler handler) {
        EditorManager.prompt(player, prompt.getLocalized());
        EditorManager.startEdit(player, handler);
        this.plugin.runTask((task) -> {
            player.closeInventory();
        });
    }
}
