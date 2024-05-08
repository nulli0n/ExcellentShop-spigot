package su.nightexpress.nexshop.currency.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.CommandArgument;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.DirectNodeBuilder;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;

import java.util.ArrayList;

public class CurrencyCommand {

    private static final String ARG_CURRENCY = "currency";
    private static final String ARG_PLAYER   = "player";
    private static final String ARG_AMOUNT   = "amount";

    public static void inject(@NotNull ShopPlugin plugin, @NotNull ChainedNode node) {
        node.addChildren(
            ChainedNode.builder(plugin, "currency")
                .permission(Perms.COMMAND_CURRENCY)
                .description(Lang.COMMAND_CURRENCY_DESC)
                .addDirect("create", builder -> {
                    CreateCommand.build(plugin, builder);
                })
                .addDirect("give", builder -> {
                    builder.aliases("add");
                    builder.permission(Perms.COMMAND_CURRENCY_GIVE);
                    builder.description(Lang.COMMAND_CURRENCY_GIVE_DESC);
                    addArguments(plugin, builder);
                    builder.executes(CurrencyCommand::executeGive);
                })
                .addDirect("take", builder -> {
                    builder.aliases("remove");
                    builder.permission(Perms.COMMAND_CURRENCY_TAKE);
                    builder.description(Lang.COMMAND_CURRENCY_TAKE_DESC);
                    addArguments(plugin, builder);
                    builder.executes(CurrencyCommand::executeRemove);
                })
        );
    }

    private static void addArguments(@NotNull ShopPlugin plugin, @NotNull DirectNodeBuilder builder) {
        builder
            .withArgument(
                CommandArgument.builder(ARG_CURRENCY, str -> plugin.getCurrencyManager().getCurrency(str))
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_CURRENCY)
                    .withSamples(tabContext -> new ArrayList<>(plugin.getCurrencyManager().getCurrencyIds()))
            )
            .withArgument(
                ArgumentTypes.player(ARG_PLAYER)
                    .required()
            )
            .withArgument(
                ArgumentTypes.decimalAbs(ARG_AMOUNT)
                    .required()
                    .localized(Lang.COMMAND_ARGUMENT_NAME_AMOUNT)
                    .withSamples(tabContext -> Lists.newList("1", "10", "100", "1000"))
            )
        ;
    }

    private static boolean executeGive(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Currency currency = arguments.getArgument(ARG_CURRENCY, Currency.class);
        Player player = arguments.getPlayerArgument(ARG_PLAYER);
        double amount = arguments.getDoubleArgument(ARG_AMOUNT);

        currency.getHandler().give(player, amount);

        return context.sendSuccess(Lang.COMMAND_CURRENCY_GIVE_DONE.getMessage()
            .replace(currency.replacePlaceholders())
            .replace(Placeholders.forPlayer(player))
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(amount)));
    }

    private static boolean executeRemove(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Currency currency = arguments.getArgument(ARG_CURRENCY, Currency.class);
        Player player = arguments.getPlayerArgument(ARG_PLAYER);
        double amount = arguments.getDoubleArgument(ARG_AMOUNT);

        currency.getHandler().take(player, amount);

        return context.sendSuccess(Lang.COMMAND_CURRENCY_TAKE_DONE.getMessage()
            .replace(currency.replacePlaceholders())
            .replace(Placeholders.forPlayer(player))
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(amount)));
    }
}
