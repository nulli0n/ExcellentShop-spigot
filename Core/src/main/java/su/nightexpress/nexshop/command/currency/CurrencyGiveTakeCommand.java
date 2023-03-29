package su.nightexpress.nexshop.command.currency;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.config.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CurrencyGiveTakeCommand extends AbstractCommand<ExcellentShop> {

    enum Mode {
        GIVE, TAKE
    }

    private final Mode mode;

    public CurrencyGiveTakeCommand(@NotNull ExcellentShop plugin, @NotNull Mode mode) {
        super(plugin, new String[]{mode == Mode.GIVE ? "give" : "take"}, mode == Mode.GIVE ? Perms.COMMAND_CURRENCY_GIVE : Perms.COMMAND_CURRENCY_TAKE);
        this.mode = mode;
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(this.mode == Mode.GIVE ? Lang.COMMAND_CURRENCY_GIVE_USAGE : Lang.COMMAND_CURRENCY_TAKE_USAGE).getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(this.mode == Mode.GIVE ? Lang.COMMAND_CURRENCY_GIVE_DESC : Lang.COMMAND_CURRENCY_TAKE_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
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

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        if (args.length < 5) {
            this.printUsage(sender);
            return;
        }

        ICurrency currency = this.plugin.getCurrencyManager().getCurrency(args[2]);
        if (currency == null) {
            plugin.getMessage(Lang.ERROR_CURRENCY_INVALID).send(sender);
            return;
        }

        Player player = plugin.getServer().getPlayer(args[3]);
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        double amount = StringUtil.getDouble(args[4], 0D);
        if (amount <= 0D) {
            this.errorNumber(sender, args[4]);
            return;
        }

        if (this.mode == Mode.GIVE) {
            currency.give(player, amount);
        }
        else {
            currency.take(player, amount);
        }

        plugin.getMessage(this.mode == Mode.GIVE ? Lang.COMMAND_CURRENCY_GIVE_DONE : Lang.COMMAND_CURRENCY_TAKE_DONE)
            .replace(currency.replacePlaceholders())
            .replace(Placeholders.Player.replacer(player))
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(amount))
            .send(sender);
    }
}
