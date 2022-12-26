package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.Map;

@Deprecated
public class OpenCommand extends ShopModuleCommand<ChestShopModule> {

    public OpenCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"open"}, Perms.ADMIN);
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
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 10);
        ChestShop shopChest = this.module.getShop(block);
        if (shopChest == null) return;

        player.openInventory(shopChest.getInventory());
    }
}
