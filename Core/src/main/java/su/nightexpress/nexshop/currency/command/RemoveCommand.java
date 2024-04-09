package su.nightexpress.nexshop.currency.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Lang;

public class RemoveCommand extends ManageCommand {

    public RemoveCommand(@NotNull ExcellentShop plugin) {
        super(plugin, new String[]{"remove", "take"}, Perms.COMMAND_CURRENCY_TAKE);
        this.setDescription(plugin.getMessage(Lang.COMMAND_CURRENCY_TAKE_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_CURRENCY_TAKE_USAGE));
    }

    @Override
    protected void manage(@NotNull CommandSender sender, @NotNull Player player, @NotNull Currency currency, double amount) {
        currency.getHandler().take(player, amount);

        plugin.getMessage(Lang.COMMAND_CURRENCY_TAKE_DONE)
            .replace(currency.replacePlaceholders())
            .replace(Placeholders.forPlayer(player))
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(amount))
            .send(sender);
    }
}
