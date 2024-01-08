package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.module.ModuleCommand;

public class BrowseCommand extends ModuleCommand<ChestShopModule> {

    public BrowseCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"browse"}, ChestPerms.COMMAND_BROWSE);
        this.setDescription(plugin.getMessage(ChestLang.COMMAND_BROWSE_DESC));
        this.setUsage(plugin.getMessage(ChestLang.COMMAND_BROWSE_USAGE));
        this.setPlayerOnly(true);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        this.module.getBrowseMenu().open(player, 1);
    }
}
