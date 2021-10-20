package su.nightexpress.nexshop.shop.virtual.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.modules.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

public class EditorCommand extends ShopModuleCommand<VirtualShop> {

    public EditorCommand(@NotNull VirtualShop module) {
        super(module, new String[]{"editor"}, Perms.ADMIN);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Core_Command_Editor_Desc.getMsg();
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
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        this.module.getEditor().open(player, 1);
    }
}
