package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.module.ModuleCommand;

public class OpenCommand extends ModuleCommand<ChestShopModule> {

    public OpenCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"open"}, ChestPerms.COMMAND_OPEN);
        this.setDescription(plugin.getMessage(ChestLang.COMMAND_OPEN_DESC));
        this.setPlayerOnly(true);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 10);
        ChestShop shop = this.module.getShop(block);
        if (shop == null) return;

        player.openInventory(shop.getInventory());
    }
}
