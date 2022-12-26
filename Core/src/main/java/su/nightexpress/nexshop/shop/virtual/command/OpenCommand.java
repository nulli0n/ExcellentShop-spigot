package su.nightexpress.nexshop.shop.virtual.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;

import java.util.List;
import java.util.Map;

public class OpenCommand extends ShopModuleCommand<VirtualShopModule> {

    public OpenCommand(@NotNull VirtualShopModule guiShop) {
        super(guiShop, new String[]{"open"}, Perms.VIRTUAL_COMMAND_OPEN);
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
        if (arg == 2 && player.hasPermission(Perms.VIRTUAL_COMMAND_OPEN_OTHERS)) {
            return PlayerUtil.getPlayerNames();
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        // /virtualshop open
        if (args.length < 2) {
            if (!(sender instanceof Player player)) {
                this.errorPlayer(sender);
                return;
            }
            this.module.openMainMenu(player);
            return;
        }

        VirtualShop virtualShop = this.module.getShopById(args[1]);
        Player player = plugin.getServer().getPlayer(args.length >= 3 && virtualShop != null ? args[2] : args[1]);

        if (virtualShop == null) {
            if (player == null) {
                if (sender instanceof Player || args.length >= 3) {
                    plugin.getMessage(VirtualLang.OPEN_ERROR_INVALID_SHOP).send(sender);
                }
                else {
                    this.errorPlayer(sender);
                }
                return;
            }

            this.module.openMainMenu(player);
            return;
        }

        if (player == null) {
            if (!(sender instanceof  Player player1)) {
                this.errorPlayer(sender);
                return;
            }
            player = player1;
        }

        if (!player.equals(sender) && !sender.hasPermission(Perms.VIRTUAL_COMMAND_OPEN_OTHERS)) {
            this.errorPermission(sender);
            return;
        }

        if (!virtualShop.hasPermission(player)) {
            this.errorPermission(player);
            return;
        }

        virtualShop.open(player, 1);
    }
}
