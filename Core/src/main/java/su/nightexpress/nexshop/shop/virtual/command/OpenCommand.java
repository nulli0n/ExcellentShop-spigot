package su.nightexpress.nexshop.shop.virtual.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

import java.util.List;

public class OpenCommand extends ShopModuleCommand<VirtualShop> {

    public OpenCommand(@NotNull VirtualShop guiShop) {
        super(guiShop, new String[]{"open"}, Perms.VIRTUAL_CMD_OPEN);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.Virtual_Shop_Command_Open_Desc).getLocalized();
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.Virtual_Shop_Command_Open_Usage).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    @NotNull
    public List<@NotNull String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return module.getShops(player);
        }
        if (arg == 2 && player.hasPermission(Perms.VIRTUAL_CMD_OPEN_OTHERS)) {
            return PlayerUtil.getPlayerNames();
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        // /virtualshop open
        if (args.length < 2) {
            if (!(sender instanceof Player player)) {
                this.errorPlayer(sender);
                return;
            }
            this.module.openMainMenu(player);
            return;
        }

        IShopVirtual shopVirtual = this.module.getShopById(args[1]);
        Player player = plugin.getServer().getPlayer(args.length >= 3 && shopVirtual != null ? args[2] : args[1]);

        if (shopVirtual == null) {
            if (player == null) {
                if (sender instanceof Player || args.length >= 3) {
                    plugin.getMessage(Lang.Virtual_Shop_Open_Error_InvalidShop).send(sender);
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

        if (!player.equals(sender) && !sender.hasPermission(Perms.VIRTUAL_CMD_OPEN_OTHERS)) {
            this.errorPermission(sender);
            return;
        }

        if (!shopVirtual.hasPermission(player)) {
            this.errorPermission(player);
            return;
        }

        shopVirtual.open(player, 1);
    }
}
