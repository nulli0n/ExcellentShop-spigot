package su.nightexpress.nexshop.shop.virtual.command.child;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.command.CommandFlags;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.util.CommandUtil;

public class MenuCommand {

    private static final String ARG_PLAYER = "player";

    public static void build(@NotNull VirtualShopModule module, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("menu", builder -> builder
            .permission(VirtualPerms.COMMAND_MENU)
            .description(VirtualLang.COMMAND_MENU_DESC)
            .withArgument(ArgumentTypes.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_MENU_OTHERS))
            .withFlag(CommandFlags.force().permission(Perms.COMMAND_FLAGS))
            .executes((context, arguments) -> execute(module, context, arguments))
        );
    }

    public static boolean execute(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, ARG_PLAYER);
        if (player == null) return false;

        if (player != context.getSender()) {
            context.send(VirtualLang.COMMAND_MENU_DONE_OTHERS.getMessage().replace(Placeholders.forPlayer(player)));
        }

        boolean force = arguments.hasFlag(CommandFlags.FORCE);

        return module.openMainMenu(player, force);
    }
}
