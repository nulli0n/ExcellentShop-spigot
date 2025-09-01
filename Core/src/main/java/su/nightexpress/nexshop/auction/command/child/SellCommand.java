package su.nightexpress.nexshop.auction.command.child;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.config.AuctionLang;
import su.nightexpress.nexshop.auction.config.AuctionPerms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.util.Lists;

public class SellCommand {

    private static final String ARG_PRICE = "price";

    public static void build(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("sell", builder -> builder
            .permission(AuctionPerms.COMMAND_SELL)
            .description(AuctionLang.COMMAND_SELL_DESC.text())
            .playerOnly()
            .withArgument(ArgumentTypes.decimalCompactAbs(ARG_PRICE)
                .required()
                .localized(AuctionLang.COMMAND_ARGUMENT_NAME_PRICE.text())
                .withSamples(tabContext -> Lists.newList("10", "500", "10000"))
            )
            .executes((context, arguments) -> executes(plugin, auctionManager, context, arguments))
        );
    }

    public static boolean executes(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getExecutor();
        if (player == null) return false;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            // TODO return context.sendFailure(AuctionLang.COMMAND_SELL_ERROR_NO_ITEM.message());
        }

        double price = arguments.getDoubleArgument(ARG_PRICE);

        return auctionManager.sell(player, item, price);
    }
}
