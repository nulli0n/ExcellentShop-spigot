package su.nightexpress.nexshop.shop.virtual.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.modules.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

import java.util.List;

public class OpenCommand extends ShopModuleCommand<VirtualShop> {

    public OpenCommand(@NotNull VirtualShop guiShop) {
        super(guiShop, new String[]{"open"}, Perms.VIRTUAL_CMD_OPEN);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Virtual_Shop_Command_Open_Desc.getMsg();
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.lang().Virtual_Shop_Command_Open_Usage.getMsg();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    @NotNull
    public List<@NotNull String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
        if (i == 1) {
            return module.getShops(player);
        }
        if (i == 2) {
            return PlayerUT.getPlayerNames();
        }
        return super.getTab(player, i, args);
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
                    plugin.lang().Virtual_Shop_Open_Error_InvalidShop.send(sender);
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

        if (!shopVirtual.hasPermission(player)) {
            this.errorPermission(player);
            return;
        }

        shopVirtual.open(player, 1);
    }
}
