package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.nexshop.module.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.chest.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;

import java.util.List;
import java.util.Map;

public class ListCommand extends ShopModuleCommand<ChestShopModule> {

    public ListCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"list", "browse"}, ChestPerms.COMMAND_LIST);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(ChestLang.COMMAND_LIST_DESC).getLocalized();
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
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return CollectionsUtil.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        Player player = (Player) sender;
        if (args.length >= 2) {
            this.module.getListMenu().open(player, args[1], 1);
        }
        else {
            this.module.getListMenu().open(player, 1);
        }
    }
}
