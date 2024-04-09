package su.nightexpress.nexshop.currency.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ManageCommand extends AbstractCommand<ExcellentShop> {

    public ManageCommand(@NotNull ExcellentShop plugin, @NotNull String[] aliases, @NotNull Permission permission) {
        super(plugin, aliases, permission);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 2) {
            return new ArrayList<>(this.plugin.getCurrencyManager().getCurrencyIds());
        }
        if (arg == 3) {
            return CollectionsUtil.playerNames(player);
        }
        if (arg == 4) {
            return Arrays.asList("1", "10", "100");
        }
        return super.getTab(player, arg, args);
    }

    protected abstract void manage(@NotNull CommandSender sender, @NotNull Player player, @NotNull Currency currency, double amount);

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 5) {
            this.printUsage(sender);
            return;
        }

        Currency currency = this.plugin.getCurrencyManager().getCurrency(result.getArg(2));
        if (currency == null) {
            plugin.getMessage(Lang.ERROR_CURRENCY_INVALID).send(sender);
            return;
        }

        Player player = plugin.getServer().getPlayer(result.getArg(3));
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        double amount = result.getDouble(4, 0);
        if (amount <= 0D) {
            this.errorNumber(sender, result.getArg(4));
            return;
        }

        this.manage(sender, player, currency, amount);
    }
}
