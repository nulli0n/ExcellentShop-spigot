package su.nightexpress.nexshop.shop.virtual.command.child;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.module.ModuleCommand;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.menu.MainMenu;

import java.util.List;

public class MenuCommand extends ModuleCommand<VirtualShopModule> {

    public MenuCommand(@NotNull VirtualShopModule module) {
        super(module, new String[]{"menu"}, VirtualPerms.COMMAND_MENU);
        this.setDescription(plugin.getMessage(VirtualLang.COMMAND_MENU_DESC));
        this.setUsage(plugin.getMessage(VirtualLang.COMMAND_MENU_USAGE));
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return CollectionsUtil.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        String pName = result.length() >= 2 ? result.getArg(1) : sender.getName();
        Player player = PlayerUtil.getPlayer(pName);
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        MainMenu mainMenu = this.module.getMainMenu();
        if (mainMenu == null) return;

        mainMenu.open(player, 1);
    }
}
