package su.nightexpress.nexshop.command.currency;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.currency.impl.ItemCurrency;

import java.util.Collections;
import java.util.List;

public class CurrencyCreateCommand extends AbstractCommand<ExcellentShop> {

    public CurrencyCreateCommand(@NotNull ExcellentShop plugin) {
        super(plugin, new String[]{"create"}, Perms.COMMAND_CURRENCY_CREATE);
        this.setDescription(plugin.getMessage(Lang.COMMAND_CURRENCY_CREATE_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_CURRENCY_CREATE_USAGE));
        this.setPlayerOnly(true);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 2) {
            return Collections.singletonList("<name>");
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 3) {
            this.printUsage(sender);
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            plugin.getMessage(Lang.ERROR_ITEM_INVALID).send(sender);
            return;
        }

        String id = result.getArg(2).toLowerCase();
        Currency currency = plugin.getCurrencyManager().getCurrency(id);
        if (currency == null) {
            ItemCurrency itemCurrency = new ItemCurrency(plugin, id);
            itemCurrency.getHandler().setItem(item);
            itemCurrency.save();
            plugin.getCurrencyManager().registerCurrency(itemCurrency);
            plugin.getMessage(Lang.COMMAND_CURRENCY_CREATE_DONE_NEW)
                .replace(itemCurrency.replacePlaceholders())
                .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item))
                .send(sender);
        }
        else {
            if (!(currency instanceof ItemCurrency itemCurrency)) {
                plugin.getMessage(Lang.COMMAND_CURRENCY_CREATE_ERROR_EXIST)
                    .replace(currency.replacePlaceholders())
                    .send(sender);
                return;
            }

            itemCurrency.getHandler().setItem(item);
            itemCurrency.save();

            plugin.getMessage(Lang.COMMAND_CURRENCY_CREATE_DONE_REPLACE)
                .replace(itemCurrency.replacePlaceholders())
                .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item))
                .send(sender);
        }
    }
}
