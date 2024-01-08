package su.nightexpress.nexshop.shop.virtual.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.menu.MainMenu;

import java.util.List;

public class ShopCommand extends GeneralCommand<ExcellentShop> {

    private final VirtualShopModule module;

    public ShopCommand(@NotNull VirtualShopModule module) {
        super(module.plugin(), VirtualConfig.SHOP_SHORTCUTS.get().split(","), VirtualPerms.COMMAND_SHOP);
        this.setDescription(plugin.getMessage(VirtualLang.COMMAND_SHOP_DESC));
        this.setUsage(plugin.getMessage(VirtualLang.COMMAND_SHOP_USAGE));
        this.setPlayerOnly(true);
        this.module = module;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return this.module.getShops(player).stream().map(Shop::getId).toList();
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        if (result.length() < 1) {
            MainMenu mainMenu = this.module.getMainMenu();
            if (mainMenu != null) {
                if (this.module.isAvailable(player, true)) {
                    mainMenu.open(player, 1);
                }
            }
            else this.printUsage(sender);
            return;
        }

        VirtualShop shop = this.module.getShopById(result.getArg(0));
        if (shop == null) {
            plugin.getMessage(VirtualLang.SHOP_ERROR_INVALID).send(sender);
            return;
        }

        if (shop.canAccess(player, true)) {
            shop.open(player, 1);
        }
    }
}
