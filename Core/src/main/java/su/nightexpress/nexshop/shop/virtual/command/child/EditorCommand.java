package su.nightexpress.nexshop.shop.virtual.command.child;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.module.ModuleCommand;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;

import java.util.Map;

public class EditorCommand extends ModuleCommand<VirtualShopModule> {

    public EditorCommand(@NotNull VirtualShopModule module) {
        super(module, new String[]{"editor"}, VirtualPerms.COMMAND_EDITOR);
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
