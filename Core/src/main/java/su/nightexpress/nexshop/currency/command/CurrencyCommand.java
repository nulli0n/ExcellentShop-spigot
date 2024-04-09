package su.nightexpress.nexshop.currency.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.config.Lang;

public class CurrencyCommand extends GeneralCommand<ExcellentShop> {

    public CurrencyCommand(@NotNull ExcellentShop plugin) {
        super(plugin, new String[]{"currency"}, Perms.COMMAND_CURRENCY);
        this.setDescription(plugin.getMessage(Lang.COMMAND_CURRENCY_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_CURRENCY_USAGE));

        this.addDefaultCommand(new HelpSubCommand<>(this.plugin));
        this.addChildren(new CreateCommand(this.plugin));
        this.addChildren(new GiveCommand(this.plugin));
        this.addChildren(new RemoveCommand(this.plugin));
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {

    }
}
