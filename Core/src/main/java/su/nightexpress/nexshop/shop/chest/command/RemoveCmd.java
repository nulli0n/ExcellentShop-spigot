package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.chest.ChestShop;

public class RemoveCmd extends ShopModuleCommand<ChestShop> {

    public RemoveCmd(@NotNull ChestShop module) {
        super(module, new String[]{"remove"}, Perms.CHEST_REMOVE);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.Command_Remove_Desc).getLocalized();
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
