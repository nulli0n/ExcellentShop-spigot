package su.nightexpress.nexshop.currency.command;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nexshop.currency.handler.ItemStackHandler;
import su.nightexpress.nexshop.currency.impl.ConfigCurrency;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.DirectNodeBuilder;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.StringUtil;

public class CreateCommand {

    private static final String ARG_NAME = "name";

    public static void build(@NotNull ShopPlugin plugin, @NotNull DirectNodeBuilder builder) {
        builder
            .permission(Perms.COMMAND_CURRENCY_CREATE)
            .description(Lang.COMMAND_CURRENCY_CREATE_DESC)
            .playerOnly()
            .withArgument(ArgumentTypes.string(ARG_NAME).localized(Lang.COMMAND_ARGUMENT_NAME_NAME).required())
            .executes((context, arguments) -> execute(plugin, context, arguments));

    }

    public static boolean execute(@NotNull ShopPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getExecutor();
        if (player == null) return false;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            return context.sendFailure(Lang.COMMAND_CURRENCY_ERROR_NO_ITEM.getMessage());
        }

        String id = arguments.getStringArgument(ARG_NAME);
        Currency currency = plugin.getCurrencyManager().getCurrency(id);
        if (currency != null) {
            if (!(currency instanceof ConfigCurrency) || !(currency.getHandler() instanceof ItemStackHandler)) {
                return context.sendFailure(Lang.COMMAND_CURRENCY_CREATE_ERROR_EXIST.getMessage().replace(currency.replacePlaceholders()));
            }
        }

        plugin.getCurrencyManager().createItemCurrency(id, item);

        return context.sendSuccess(Lang.COMMAND_CURRENCY_CREATE_DONE_NEW.getMessage()
            .replace(Placeholders.GENERIC_NAME, StringUtil.capitalizeFully(id))
            .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item)));
    }
}
