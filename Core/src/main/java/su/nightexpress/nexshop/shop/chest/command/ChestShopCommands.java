package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.impl.ShopBlock;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChestShopCommands {

    private static final Set<Material> TRANSPARENT = new HashSet<>();

    static {
        TRANSPARENT.addAll(Tag.AIR.getValues());
        TRANSPARENT.addAll(Tag.SIGNS.getValues());
    }

    public static void build(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module, @NotNull HubNodeBuilder root) {
        root.branch(Commands.literal("browse")
            .playerOnly()
            .permission(ChestPerms.COMMAND_BROWSE)
            .description(ChestLang.COMMAND_BROWSE_DESC.text())
            .executes((context, arguments) -> browseShops(module, context, arguments))
        );

        root.branch(Commands.literal("list")
            .playerOnly()
            .permission(ChestPerms.COMMAND_LIST)
            .description(ChestLang.COMMAND_LIST_DESC.text())
            .executes((context, arguments) -> listShops(module, context, arguments))
        );

        root.branch(Commands.literal("listall")
            .playerOnly()
            .permission(ChestPerms.COMMAND_LIST)
            .description(ChestLang.COMMAND_LIST_DESC.text())
            .executes((context, arguments) -> listAllShops(module, context, arguments))
        );

        root.branch(Commands.literal("playersearch")
            .playerOnly()
            .permission(ChestPerms.COMMAND_PLAYER_SEARCH)
            .description(ChestLang.COMMAND_PLAYER_SEARCH_DESC.text())
            .withArguments(Arguments.playerName(CommandArguments.PLAYER))
            .executes((context, arguments) -> searchPlayerShops(module, context, arguments))
        );

        root.branch(Commands.literal("create")
            .playerOnly()
            .permission(ChestPerms.CREATE)
            .description(ChestLang.COMMAND_CREATE_DESC.text())
            .withArguments(
                Arguments.decimalCompact(CommandArguments.BUY_PRICE)
                    .optional()
                    .localized(ChestLang.COMMAND_ARGUMENT_NAME_BUY_PRICE.text())
                    .suggestions((reader, context) -> Lists.newList("100", "1000")),
                Arguments.decimalCompact(CommandArguments.SELL_PRICE)
                    .optional()
                    .localized(ChestLang.COMMAND_ARGUMENT_NAME_SELL_PRICE.text())
                    .suggestions((reader, context) -> Lists.newList("50", "500"))
            )
            .executes((context, arguments) -> createShop(module, context, arguments))
        );

        root.branch(Commands.literal("remove")
            .playerOnly()
            .permission(ChestPerms.REMOVE)
            .description(ChestLang.COMMAND_REMOVE_DESC.text())
            .executes((context, arguments) -> removeShop(module, context, arguments))
        );

        root.branch(Commands.literal("openinv")
            .playerOnly()
            .permission(ChestPerms.COMMAND_OPEN_INV)
            .description(ChestLang.COMMAND_OPEN_INV_DESC.text())
            .executes((context, arguments) -> openShopInventory(module, context, arguments))
        );

        if (ChestConfig.SHOP_ITEM_CREATION_ENABLED.get()) {
            root.branch(Commands.literal("giveitem")
                .description(ChestLang.COMMAND_GIVE_ITEM_DESC.text())
                .permission(ChestPerms.COMMAND_GIVE_ITEM)
                .withArguments(
                    Arguments.player(CommandArguments.PLAYER),
                    CommandArguments.forShopBlock(module),
                    Arguments.integer(CommandArguments.AMOUNT)
                        .optional()
                        .localized(CoreLang.COMMAND_ARGUMENT_NAME_AMOUNT.text())
                        .suggestions((reader, context) -> Lists.newList("1", "5", "10"))
                )
                .executes((context, arguments) -> giveShopItem(module, context, arguments))
            );
        }

        if (!ChestConfig.isAutoBankEnabled()) {
            root.branch(Commands.literal("bank")
                .playerOnly()
                .permission(ChestPerms.COMMAND_BANK)
                .description(ChestLang.COMMAND_BANK_DESC.text())
                .withArguments(Arguments.playerName(CommandArguments.PLAYER).optional().permission(ChestPerms.COMMAND_BANK_OTHERS))
                .executes((context, arguments) -> openBank(plugin, module, context, arguments))
            );
        }
    }

    public static boolean browseShops(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        module.browseShopOwners(player);
        return true;
    }

    public static boolean listShops(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        module.browsePlayerShops(player, player.getName());
        return true;
    }

    public static boolean listAllShops(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        module.browseAllShops(player);
        return true;
    }

    public static boolean searchItemShops(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        String itemName = arguments.getString(CommandArguments.ITEM_NAME);
        module.browseItemShops(player, itemName);
        return true;
    }

    public static boolean searchPlayerShops(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        String ownerName = arguments.getString(CommandArguments.PLAYER);
        module.browsePlayerShops(player, ownerName);
        return true;
    }

    public static boolean createShop(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        Block block = player.getTargetBlock(TRANSPARENT, 100);

        double buyPrice = arguments.getDouble(CommandArguments.BUY_PRICE, ChestConfig.SHOP_PRODUCT_INITIAL_BUY_PRICE.get());
        double sellPrice = arguments.getDouble(CommandArguments.SELL_PRICE, ChestConfig.SHOP_PRODUCT_INITIAL_SELL_PRICE.get());

        return module.createShopNaturally(player, block, buyPrice, sellPrice);
    }

    public static boolean removeShop(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        Block block = player.getTargetBlock(null, 100);
        return module.deleteShop(player, block);
    }

    public static boolean openShopInventory(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        Block block = player.getTargetBlock(null, 10);

        ChestShop shop = module.getShop(block);
        if (shop == null) {
            module.getPrefixed(ChestLang.ERROR_BLOCK_IS_NOT_SHOP).send(context.getSender());
            return false;
        }

        Inventory inventory = shop.inventory();
        if (inventory == null) return false;

        player.openInventory(inventory);
        return true;
    }

    public static boolean giveShopItem(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        ShopBlock shopBlock = arguments.get(CommandArguments.SHOP_BLOCK, ShopBlock.class);
        Player player = arguments.getPlayer(CommandArguments.PLAYER);
        int amount = arguments.getInt(CommandArguments.AMOUNT, 1);

        ItemStack itemStack = shopBlock.getItemStack();
        itemStack.setAmount(amount);
        Players.addItem(player, itemStack);

        module.getPrefixed(ChestLang.COMMAND_GIVE_ITEM_DONE).send(context.getSender(), replacer -> replacer
            .replace(Placeholders.GENERIC_NAME, ItemUtil.getNameSerialized(itemStack))
            .replace(Placeholders.forPlayer(player))
        );
        return true;
    }

    public static boolean openBank(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();

        String userName = arguments.getString(CommandArguments.PLAYER, context.getSender().getName());
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
