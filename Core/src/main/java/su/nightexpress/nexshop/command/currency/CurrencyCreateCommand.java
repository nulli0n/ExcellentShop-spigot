package su.nightexpress.nexshop.command.currency;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.currency.config.CurrencyItemConfig;
import su.nightexpress.nexshop.currency.internal.ItemCurrency;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CurrencyCreateCommand extends AbstractCommand<ExcellentShop> {

    public CurrencyCreateCommand(@NotNull ExcellentShop plugin) {
        super(plugin, new String[]{"create"}, Perms.COMMAND_CURRENCY_CREATE);
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.COMMAND_CURRENCY_CREATE_USAGE).getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_CURRENCY_CREATE_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
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
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        if (args.length < 3) {
            this.printUsage(sender);
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            plugin.getMessage(Lang.ERROR_ITEM_INVALID).send(sender);
            return;
        }

        String id = args[2].toLowerCase();
        ICurrency currency = plugin.getCurrencyManager().getCurrency(id);
        if (currency == null) {
            CurrencyItemConfig config = new CurrencyItemConfig(plugin, id);
            config.setItem(item);
            currency = new ItemCurrency(config);
            config.save();
            plugin.getCurrencyManager().registerCurrency(currency);
            plugin.getMessage(Lang.COMMAND_CURRENCY_CREATE_DONE_NEW)
                .replace(currency.replacePlaceholders())
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

            itemCurrency.getConfig().setItem(item);
            itemCurrency.getConfig().save();

            plugin.getMessage(Lang.COMMAND_CURRENCY_CREATE_DONE_REPLACE)
                .replace(itemCurrency.replacePlaceholders())
                .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item))
                .send(sender);
        }
    }
}
