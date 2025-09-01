package su.nightexpress.nexshop.auction.command.child;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.command.CommandFlags;
import su.nightexpress.nexshop.auction.config.AuctionLang;
import su.nightexpress.nexshop.auction.config.AuctionPerms;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.util.CommandUtil;

public class OpenCommand {

    private static final String ARG_PLAYER = "player";

    public static void build(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("open", builder -> builder
            .permission(AuctionPerms.COMMAND_OPEN)
            .description(AuctionLang.COMMAND_OPEN_DESC.text())
            .withArgument(ArgumentTypes.player(ARG_PLAYER).permission(AuctionPerms.COMMAND_OPEN_OTHERS))
            .withFlag(CommandFlags.force().permission(Perms.COMMAND_FLAGS))
            .executes((context, arguments) -> executes(plugin, auctionManager, context, arguments))
        );
    }

    public static boolean executes(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, ARG_PLAYER);
        if (player == null) return false;

        boolean force = arguments.hasFlag(CommandFlags.FORCE);
        return auctionManager.openAuction(player, force);
    }

    public static boolean executes(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context) {
        Player player = context.getExecutor();
        if (player == null) {
            context.errorPlayerOnly();
            return false;
        }
        return auctionManager.openAuction(player);
    }
}
