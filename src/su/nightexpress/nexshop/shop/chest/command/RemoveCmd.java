package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.modules.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.chest.ChestShop;

public class RemoveCmd extends ShopModuleCommand<ChestShop> {

    public RemoveCmd(@NotNull ChestShop module) {
        super(module, new String[]{"remove"}, Perms.CHEST_REMOVE);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Chest_Shop_Command_Remove_Desc.getMsg();
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
        Block block = player.getTargetBlock(null, 100);

        this.module.deleteShop(player, block);
    }

}
