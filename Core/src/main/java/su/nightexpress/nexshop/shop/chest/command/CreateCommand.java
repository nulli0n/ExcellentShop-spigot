package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.nexshop.module.ModuleCommand;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.util.ShopType;

import java.util.List;

public class CreateCommand extends ModuleCommand<ChestShopModule> {

    public CreateCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"create"}, ChestPerms.CREATE);
        this.setDescription(plugin.getMessage(ChestLang.COMMAND_CREATE_DESC));
        this.setUsage(plugin.getMessage(ChestLang.COMMAND_CREATE_USAGE));
        this.setPlayerOnly(true);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return List.of("<buyPrice>");
        }
        if (arg == 2) {
            return List.of("<sellPrice>");
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 100);

        double buyPrice = result.length() >= 2 ? result.getDouble(1, -1) : -1;
        double sellPrice = result.length() >= 3 ? result.getDouble(2, -1) : -1;

        this.module.createShop(player, block, ShopType.PLAYER, buyPrice, sellPrice);
    }
}
