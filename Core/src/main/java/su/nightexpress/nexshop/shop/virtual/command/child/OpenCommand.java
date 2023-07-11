package su.nightexpress.nexshop.shop.virtual.command.child;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.nexshop.shop.module.ModuleCommand;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.util.List;

public class OpenCommand extends ModuleCommand<VirtualShopModule> {

    public OpenCommand(@NotNull VirtualShopModule module) {
        super(module, new String[]{"open"}, VirtualPerms.COMMAND_OPEN);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(VirtualLang.COMMAND_OPEN_DESC).getLocalized();
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(VirtualLang.COMMAND_OPEN_USAGE).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return module.getShops(player).stream().map(VirtualShop::getId).toList();
        }
        if (arg == 2) {
            return CollectionsUtil.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 2) {
            this.printUsage(sender);
            return;
        }

        VirtualShop shop = this.module.getShopById(result.getArg(1));
        if (shop == null) {
            plugin.getMessage(VirtualLang.SHOP_ERROR_INVALID).send(sender);
            return;
        }

        String pName = result.length() >= 3 ? result.getArg(2) : sender.getName();
        Player player = plugin.getServer().getPlayer(pName);
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        shop.open(player, 1);
    }
}
