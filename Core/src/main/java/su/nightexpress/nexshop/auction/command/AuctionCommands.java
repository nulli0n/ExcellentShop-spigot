package su.nightexpress.nexshop.auction.command;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.config.AuctionLang;
import su.nightexpress.nexshop.auction.config.AuctionPerms;
import su.nightexpress.nexshop.shop.virtual.command.VirtualCommands;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.nightcore.util.Lists;

import java.util.UUID;

public class AuctionCommands {

    private static final String ARG_PLAYER = "player";
    private static final String ARG_TARGET = "target";
    private static final String ARG_PRICE = "price";

    public static final String FORCE = "f";

    public static void build(@NotNull ShopPlugin plugin, @NotNull AuctionManager manager, @NotNull HubNodeBuilder builder) {
        builder.branch(Commands.literal("open")
            .permission(AuctionPerms.COMMAND_OPEN)
            .description(AuctionLang.COMMAND_OPEN_DESC.text())
            .withArguments(Arguments.player(ARG_PLAYER).optional().permission(AuctionPerms.COMMAND_OPEN_OTHERS))
            .withFlags(FORCE)
            .executes((context, arguments) -> openAuction(plugin, manager, context, arguments))
        );

        builder.branch(Commands.literal("sell")
            .permission(AuctionPerms.COMMAND_SELL)
            .description(AuctionLang.COMMAND_SELL_DESC.text())
            .playerOnly()
            .withArguments(Arguments.decimalCompact(ARG_PRICE)
                .localized(AuctionLang.COMMAND_ARGUMENT_NAME_PRICE.text())
                .suggestions((reader, context) -> Lists.newList("10", "500", "10000"))
            )
            .executes((context, arguments) -> sellItem(plugin, manager, context, arguments))
        );
        
        builder.branch(Commands.literal("expired")
            .permission(AuctionPerms.COMMAND_EXPIRED)
            .description(AuctionLang.COMMAND_EXPIRED_DESC.text())
            .playerOnly()
            .withArguments(Arguments.playerName(ARG_TARGET).optional().permission(AuctionPerms.COMMAND_EXPIRED_OTHERS))
            .withFlags(FORCE)
            .executes((context, arguments) -> viewExpired(plugin, manager, context, arguments))
        );

        builder.branch(Commands.literal("history")
            .permission(AuctionPerms.COMMAND_HISTORY)
            .description(AuctionLang.COMMAND_HISTORY_DESC.text())
            .playerOnly()
            .withArguments(Arguments.playerName(ARG_TARGET).optional().permission(AuctionPerms.COMMAND_HISTORY_OTHERS))
            .withFlags(FORCE)
            .executes((context, arguments) -> viewHistory(plugin, manager, context, arguments))
        );

        builder.branch(Commands.literal("listings")
            .permission(AuctionPerms.COMMAND_SELLING)
            .description(AuctionLang.COMMAND_SELLING_DESC.text())
            .playerOnly()
            .withArguments(Arguments.playerName(ARG_TARGET).optional().permission(AuctionPerms.COMMAND_SELLING_OTHERS))
            .withFlags(FORCE)
            .executes((context, arguments) -> viewListings(plugin, manager, context, arguments))
        );

        builder.branch(Commands.literal("unclaimed")
            .permission(AuctionPerms.COMMAND_UNCLAIMED)
            .description(AuctionLang.COMMAND_UNCLAIMED_DESC.text())
            .playerOnly()
            .withArguments(Arguments.playerName(ARG_TARGET).optional().permission(AuctionPerms.COMMAND_UNCLAIMED_OTHERS))
            .withFlags(FORCE)
            .executes((context, arguments) -> viewUnclaimed(plugin, manager, context, arguments))
        );
        
        builder.branch(Commands.literal("filldumy")
            .permission(AuctionPerms.COMMAND)
            .description("Debug command.")
            .playerOnly()
            .executes((context, arguments) -> viewHistory(plugin, manager, context, arguments))
        );

        builder.executes((context, arguments) -> openAuction(plugin, manager, context, arguments));
    }

    public static boolean viewExpired(@NotNull ShopPlugin plugin, @NotNull AuctionManager manager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        String userName = arguments.getString(ARG_TARGET, player.getName());

        plugin.getUserManager().manageUser(userName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }
            UUID targetId = user.getId();
            boolean force = context.hasFlag(VirtualCommands.FLAG_FORCE);
            manager.openExpiedListings(player, targetId, force);
        });
        return true;
    }

    public static boolean fillDummy(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        AuctionUtils.fillDummy(auctionManager);
        return true;
    }

    public static boolean viewHistory(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        String userName = arguments.getString(ARG_TARGET, player.getName());

        plugin.getUserManager().manageUser(userName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }
            UUID targetId = user.getId();
            boolean force = context.hasFlag(VirtualCommands.FLAG_FORCE);
            auctionManager.openSalesHistory(player, targetId, force);
        });
        return true;
    }

    public static boolean viewListings(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        String userName = arguments.getString(ARG_TARGET, player.getName());

        plugin.getUserManager().manageUser(userName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }
            UUID targetId = user.getId();
            boolean force = context.hasFlag(VirtualCommands.FLAG_FORCE);
            auctionManager.openPlayerListings(player, targetId, force);
        });
        return true;
    }

    public static boolean openAuction(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (arguments.contains(ARG_PLAYER)) {
            Player target = arguments.getPlayer(ARG_PLAYER);
            boolean force = context.hasFlag(VirtualCommands.FLAG_FORCE);
            return auctionManager.openAuction(target, force);
        }

        if (!context.isPlayer()) {
            context.printUsage();
            return false;
        }

        return auctionManager.openAuction(context.getPlayerOrThrow());
    }

    /*public static boolean openAuction(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context) {
        Player player = context.getExecutor();
        if (player == null) {
            context.errorPlayerOnly();
            return false;
        }
        return auctionManager.openAuction(player);
    }*/

    public static boolean sellItem(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            context.send(AuctionLang.COMMAND_SELL_ERROR_NO_ITEM);
            return false;
        }

        double price = arguments.getDouble(ARG_PRICE);

        return auctionManager.sell(player, item, price);
    }

    public static boolean viewUnclaimed(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        String userName = arguments.getString(ARG_TARGET, player.getName());

        plugin.getUserManager().manageUser(userName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }
            UUID targetId = user.getId();
            boolean force = context.hasFlag(VirtualCommands.FLAG_FORCE);
            auctionManager.openUnclaimedListings(player, targetId, force);
        });
        return true;
    }
}
