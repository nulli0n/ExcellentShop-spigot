package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.modules.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.chest.ChestShop;

import java.util.Arrays;
import java.util.List;

public class CreateCmd extends ShopModuleCommand<ChestShop> {

    public CreateCmd(@NotNull ChestShop module) {
        super(module, new String[]{"create"}, Perms.CHEST_CREATE);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Chest_Shop_Command_Create_Desc.getMsg();
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.lang().Chest_Shop_Command_Create_Usage.getMsg();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    @NotNull
    public List<@NotNull String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
        if (i == 1) {
            return Arrays.asList("<admin[true/false]>");
        }
        return super.getTab(player, i, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 100);

        boolean admin = false;
        if (args.length >= 2) {
            admin = Boolean.parseBoolean(args[1]);
        }

        this.module.createShop(player, block, admin);
    }
}
