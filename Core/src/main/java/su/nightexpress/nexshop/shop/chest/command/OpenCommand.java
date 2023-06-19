package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.nexshop.shop.chest.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.module.ModuleCommand;

@Deprecated
public class OpenCommand extends ModuleCommand<ChestShopModule> {

    public OpenCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"open"}, ChestPerms.COMMAND);
    }

    @Override
    @NotNull
    public String getDescription() {
        return "Open shop inventory"; // TODO
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
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 10);
        ChestShop shopChest = this.module.getShop(block);
        if (shopChest == null) return;

        player.openInventory(shopChest.getInventory());
    }
}
