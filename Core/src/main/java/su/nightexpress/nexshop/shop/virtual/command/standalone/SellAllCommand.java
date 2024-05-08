package su.nightexpress.nexshop.shop.virtual.command.standalone;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.RootCommand;
import su.nightexpress.nightcore.command.experimental.ServerCommand;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.util.CommandUtil;

public class SellAllCommand {

    private static final String ARG_PLAYER = "player";

    @NotNull
    public static ServerCommand create(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull String[] aliases) {
        return RootCommand.direct(plugin, aliases, builder -> builder
            .permission(VirtualPerms.COMMAND_SELL_ALL)
            .description(VirtualLang.COMMAND_SELL_ALL_DESC)
            .withArgument(ArgumentTypes.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_SELL_ALL_OTHERS))
            .executes((context, arguments) -> execute(module, context, arguments))
        );
    }

    public static boolean execute(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, ARG_PLAYER);
        if (player == null) return false;

        module.sellAll(player);

        if (player != context.getSender()) {
            context.send(VirtualLang.COMMAND_SELL_ALL_DONE_OTHERS.getMessage().replace(Placeholders.forPlayer(player)));
        }
        return true;
    }
}
