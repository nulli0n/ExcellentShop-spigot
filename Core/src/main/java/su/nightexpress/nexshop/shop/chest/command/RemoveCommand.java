package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.chest.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;

import java.util.Map;

public class RemoveCommand extends ShopModuleCommand<ChestShopModule> {

    public RemoveCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"remove"}, ChestPerms.REMOVE);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(ChestLang.COMMAND_REMOVE_DESC).getLocalized();
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
        Block block = player.getTargetBlock(null, 100);
        this.module.deleteShop(player, block);
    }

}
