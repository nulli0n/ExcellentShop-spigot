package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.chest.ChestShop;

public class ListCmd extends ShopModuleCommand<ChestShop> {

    public ListCmd(@NotNull ChestShop module) {
        super(module, new String[]{"list"}, Perms.CHEST_CMD_LIST);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.Command_List_Desc).getLocalized();
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
        this.module.getListOwnMenu().open(player, 1);
    }
}
