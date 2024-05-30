package su.nightexpress.nexshop.shop.virtual.command.child;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.command.CommandArguments;
import su.nightexpress.nexshop.shop.virtual.command.CommandFlags;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.util.CommandUtil;

public class OpenCommand {

    private static final String ARG_SHOP = "shop";
    private static final String ARG_PLAYER = "player";

    public static void build(@NotNull VirtualShopModule module, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("open", builder -> builder
            .permission(VirtualPerms.COMMAND_OPEN)
            .description(VirtualLang.COMMAND_OPEN_DESC)
            .withArgument(CommandArguments.forShop(ARG_SHOP, module).localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP.getString()).required())
            .withArgument(ArgumentTypes.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_OPEN_OTHERS))
            .withFlag(CommandFlags.force().permission(Perms.COMMAND_FLAGS))
            .withFlag(CommandFlags.silent()).permission(Perms.COMMAND_FLAGS)
            .executes((context, arguments) -> execute(module, context, arguments))
        );
    }

    public static boolean execute(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        VirtualShop shop = arguments.getArgument(ARG_SHOP, VirtualShop.class);
        Player player = CommandUtil.getPlayerOrSender(context, arguments, ARG_PLAYER);
        if (player == null) return false;

        if (player != context.getSender() && !arguments.hasFlag(CommandFlags.SILENT)) {
            context.send(VirtualLang.COMMAND_OPEN_DONE_OTHERS.getMessage()
                .replace(Placeholders.forPlayer(player))
                .replace(Placeholders.SHOP_NAME, shop.getName())
            );
        }

        boolean force = arguments.hasFlag(CommandFlags.FORCE);
        return module.openShop(player, shop, force);
    }
}
