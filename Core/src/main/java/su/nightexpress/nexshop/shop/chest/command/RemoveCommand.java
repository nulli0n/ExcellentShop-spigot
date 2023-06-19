package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.nexshop.shop.chest.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.module.ModuleCommand;

public class RemoveCommand extends ModuleCommand<ChestShopModule> {

    public RemoveCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"remove"}, ChestPerms.REMOVE);
        this.setDescription(plugin.getMessage(ChestLang.COMMAND_REMOVE_DESC));
        this.setPlayerOnly(true);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 100);
        this.module.deleteShop(player, block);
    }
}
