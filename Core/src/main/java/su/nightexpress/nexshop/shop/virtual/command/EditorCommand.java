package su.nightexpress.nexshop.shop.virtual.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

import java.util.Map;

public class EditorCommand extends ShopModuleCommand<VirtualShopModule> {

    public EditorCommand(@NotNull VirtualShopModule module) {
        super(module, new String[]{"editor"}, Perms.VIRTUAL_COMMAND_EDITOR);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.CORE_COMMAND_EDITOR_DESC).getLocalized();
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        Player player = (Player) sender;
        this.module.getEditor().open(player, 1);
    }
}
