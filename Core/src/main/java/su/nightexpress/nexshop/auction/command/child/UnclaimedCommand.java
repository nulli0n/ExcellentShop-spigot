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

import java.util.UUID;

public class UnclaimedCommand {

    private static final String ARG_TARGET = "target";

    public static void build(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("unclaimed", builder -> builder
            .permission(AuctionPerms.COMMAND_UNCLAIMED)
            .description(AuctionLang.COMMAND_UNCLAIMED_DESC.text())
            .playerOnly()
            .withArgument(ArgumentTypes.playerName(ARG_TARGET).permission(AuctionPerms.COMMAND_UNCLAIMED_OTHERS))
            .withFlag(CommandFlags.force().permission(Perms.COMMAND_FLAGS))
            .executes((context, arguments) -> executes(plugin, auctionManager, context, arguments))
        );
    }

    public static boolean executes(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getExecutor();
        if (player == null) return false;

        String userName = arguments.getStringArgument(ARG_TARGET, player.getName());
        plugin.getUserManager().manageUser(userName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }
            UUID targetId = user.getId();
            boolean force = arguments.hasFlag(CommandFlags.FORCE);
            auctionManager.openUnclaimedListings(player, targetId, force);
        });
        return true;
    }
}
