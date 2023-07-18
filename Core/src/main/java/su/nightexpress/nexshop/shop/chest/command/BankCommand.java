package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.module.ModuleCommand;

import java.util.List;
import java.util.UUID;

public class BankCommand extends ModuleCommand<ChestShopModule> {

    public BankCommand(@NotNull ChestShopModule module) {
        super(module, new String[]{"bank"}, ChestPerms.COMMAND_BANK);
        this.setDescription(plugin.getMessage(ChestLang.COMMAND_BANK_DESC));
        this.setUsage(plugin.getMessage(ChestLang.COMMAND_BANK_USAGE));
        this.setPlayerOnly(true);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1 && player.hasPermission(ChestPerms.COMMAND_BANK_OTHERS)) {
            return CollectionsUtil.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() >= 2 && !sender.hasPermission(ChestPerms.COMMAND_BANK_OTHERS)) {
            this.errorPermission(sender);
            return;
        }

        Player player = (Player) sender;
        String target = result.getArg(1, player.getName());
        this.plugin.getUserManager().getUserDataAsync(target).thenAccept(user -> {
            if (user == null) {
                this.errorPlayer(sender);
                return;
            }

            UUID targetId = user.getId();
            if (targetId.equals(player.getUniqueId())) {
                this.module.getBankMenu().openNextTick(player, 1);
            }
            else {
                this.plugin.runTask(task -> this.module.getBankMenu().open(player, targetId));
            }
        });
    }
}
