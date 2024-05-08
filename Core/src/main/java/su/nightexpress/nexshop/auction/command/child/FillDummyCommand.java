package su.nightexpress.nexshop.auction.command.child;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.config.AuctionPerms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;

public class FillDummyCommand {

    public static void build(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("filldummy", builder -> builder
            .permission(AuctionPerms.COMMAND)
            .description("Debug command.")
            .playerOnly()
            .executes((context, arguments) -> executes(plugin, auctionManager, context, arguments))
        );
    }

    public static boolean executes(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        AuctionUtils.fillDummy(auctionManager);
        return true;
    }
}
