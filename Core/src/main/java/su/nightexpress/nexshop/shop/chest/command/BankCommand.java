package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;

import java.util.UUID;

public class BankCommand {

    private static final String ARG_USER = "user";

    public static void build(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("bank", builder -> builder
            .permission(ChestPerms.COMMAND_BANK)
            .description(ChestLang.COMMAND_BANK_DESC)
            .playerOnly()
            .withArgument(ArgumentTypes.playerName(ARG_USER).permission(ChestPerms.COMMAND_BANK_OTHERS))
            .executes((context, arguments) -> execute(plugin, module, context, arguments))
        );
    }

    public static boolean execute(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getExecutor();
        if (player == null) return false;

        String userName = arguments.getStringArgument(ARG_USER, context.getSender().getName());
        plugin.getUserManager().getUserDataAsync(userName).thenAccept(user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }

            UUID targetId = user.getId();
            plugin.runTask(task -> module.openBank(player, targetId));
        });
        return true;
    }
}
