package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.module.ModuleCommand;

import java.util.List;

public class ListCommand extends ModuleCommand<ChestShopModule> {

    public ListCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"list"}, ChestPerms.COMMAND_LIST);
        this.setDescription(plugin.getMessage(ChestLang.COMMAND_LIST_DESC));
        this.setPlayerOnly(true);
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
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        Player player = (Player) sender;
        if (result.length() >= 2) {
            this.plugin.getUserManager().getUserDataAsync(result.getArg(1)).thenAccept(user -> {
                if (user == null) {
                    this.errorPlayer(sender);
                    return;
                }

                this.plugin.runTask(task -> {
                    this.module.listShops(player, user.getId());
                });
            });
            return;
        }

        this.module.listShops(player);
    }
}
