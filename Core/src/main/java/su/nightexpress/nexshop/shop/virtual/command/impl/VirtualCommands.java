package su.nightexpress.nexshop.shop.virtual.command.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.command.CommandArguments;
import su.nightexpress.nexshop.shop.virtual.command.CommandFlags;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.RootCommand;
import su.nightexpress.nightcore.command.experimental.ServerCommand;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.util.CommandUtil;

import java.util.HashSet;
import java.util.Set;

public class VirtualCommands {

    private static Set<ServerCommand> commands;

    public static void load(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull ChainedNodeBuilder builder) {
        commands = new HashSet<>();

        builder.addDirect("editor", child -> child
            .permission(VirtualPerms.COMMAND_EDITOR)
            .description(VirtualLang.COMMAND_EDITOR_DESC)
            .playerOnly()
            .executes((context, arguments) -> openEditor(module, context))
        );

        builder.addDirect("open", child -> child
            .permission(VirtualPerms.COMMAND_OPEN)
            .description(VirtualLang.COMMAND_OPEN_DESC)
            .withArgument(CommandArguments.forShop(module).localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP.getString()).required())
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER).permission(VirtualPerms.COMMAND_OPEN_OTHERS))
            .withFlag(CommandFlags.force().permission(VirtualPerms.COMMAND_OPEN_OTHERS))
            .withFlag(CommandFlags.silent().permission(VirtualPerms.COMMAND_OPEN_OTHERS))
            .executes((context, arguments) -> openShop(module, context, arguments))
        );

        builder.addDirect("rotate", child -> child
            .permission(VirtualPerms.COMMAND_ROTATE)
            .description(VirtualLang.COMMAND_ROTATE_DESC)
            .withArgument(CommandArguments.forShop(module).localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP.getString()).required())
            .executes(VirtualCommands::rotateShop)
        );

        if (VirtualConfig.isCentralMenuEnabled()) {
            builder.addDirect("menu", child -> child
                .permission(VirtualPerms.COMMAND_MENU)
                .description(VirtualLang.COMMAND_MENU_DESC)
                .withArgument(ArgumentTypes.player(CommandArguments.PLAYER).permission(VirtualPerms.COMMAND_MENU_OTHERS))
                .withFlag(CommandFlags.force().permission(VirtualPerms.COMMAND_MENU_OTHERS))
                .executes((context, arguments) -> openCentralGUI(module, context, arguments))
            );
        }

        if (VirtualConfig.SHOP_SHORTCUTS_ENABLED.get()) {
            register(plugin, RootCommand.direct(plugin, VirtualConfig.SHOP_SHORTCUTS_COMMANDS.get(), child -> child
                .playerOnly()
                .permission(VirtualPerms.COMMAND_SHOP)
                .description(VirtualLang.COMMAND_SHOP_DESC)
                .withArgument(CommandArguments.forShop(module).required(!VirtualConfig.isCentralMenuEnabled()))
                .executes((context, arguments) -> openDirectShop(module, context, arguments))
            ));

            module.getShops().forEach(shop -> {
                if (shop.getAliases().isEmpty()) return;

                register(plugin, RootCommand.direct(plugin, shop.getAliases().toArray(new String[0]), child -> child
                    .playerOnly()
                    .permission(VirtualPerms.COMMAND_SHOP)
                    .description(shop.replacePlaceholders().apply(VirtualLang.COMMAND_SHOP_ALIAS_DESC.getString()))
                    .executes((context, arguments) -> openExplicitShop(shop, context))
                ));
            });
        }

        if (VirtualConfig.SELL_MENU_ENABLED.get()) {
            register(plugin, RootCommand.direct(plugin, VirtualConfig.SELL_MENU_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_MENU)
                .description(VirtualLang.COMMAND_SELL_MENU_DESC)
                .withArgument(ArgumentTypes.player(CommandArguments.PLAYER).permission(VirtualPerms.COMMAND_SELL_MENU_OTHERS))
                .executes((context, arguments) -> openSellMenu(module, context, arguments))
            ));
        }

        if (VirtualConfig.SELL_ALL_ENABLED.get()) {
            register(plugin, RootCommand.direct(plugin, VirtualConfig.SELL_ALL_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_ALL)
                .description(VirtualLang.COMMAND_SELL_ALL_DESC)
                .withArgument(ArgumentTypes.player(CommandArguments.PLAYER).permission(VirtualPerms.COMMAND_SELL_ALL_OTHERS))
                .withFlag(CommandFlags.silent().permission(VirtualPerms.COMMAND_SELL_ALL_OTHERS))
                .executes((context, arguments) -> sellAll(module, context, arguments))
            ));
        }

        if (VirtualConfig.SELL_HAND_ENABLED.get()) {
            register(plugin, RootCommand.direct(plugin, VirtualConfig.SELL_HAND_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_HAND)
                .description(VirtualLang.COMMAND_SELL_HAND_DESC)
                .withArgument(ArgumentTypes.player(CommandArguments.PLAYER).permission(VirtualPerms.COMMAND_SELL_HAND_OTHERS))
                .executes((context, arguments) -> sellHand(module, context, arguments))
            ));
        }

        if (VirtualConfig.SELL_HAND_ALL_ENABLED.get()) {
            register(plugin, RootCommand.direct(plugin, VirtualConfig.SELL_HAND_ALL_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_HAND_ALL)
                .description(VirtualLang.COMMAND_SELL_HAND_ALL_DESC)
                .withArgument(ArgumentTypes.player(CommandArguments.PLAYER).permission(VirtualPerms.COMMAND_SELL_HAND_ALL_OTHERS))
                .executes((context, arguments) -> sellHandAll(module, context, arguments))
            ));
        }
    }

    public static void unload(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        unregister(plugin);
    }

    private static void register(@NotNull ShopPlugin plugin, @NotNull ServerCommand command) {
        plugin.getCommandManager().registerCommand(command);
        commands.add(command);
    }

    private static void unregister(@NotNull ShopPlugin plugin) {
        commands.forEach(command -> plugin.getCommandManager().unregisterCommand(command));
        commands.clear();
    }

    private static boolean openDirectShop(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();

        if (arguments.hasArgument(CommandArguments.SHOP)) {
            VirtualShop shop = arguments.getArgument(CommandArguments.SHOP, VirtualShop.class);
            shop.open(player);
        }
        else {
            module.openMainMenu(player);
        }
        return true;
    }

    private static boolean openExplicitShop(@NotNull VirtualShop shop, @NotNull CommandContext context) {
        Player player = context.getPlayerOrThrow();
        shop.open(player);
        return true;
    }

    private static boolean openSellMenu(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        module.openSellMenu(player, false);

        if (player != context.getSender()) {
            VirtualLang.COMMAND_SELL_MENU_DONE_OTHERS.getMessage().send(context.getSender(), replacer -> replacer.replace(Placeholders.forPlayer(player)));
        }
        return true;
    }

    private static boolean sellAll(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        module.sellAll(player, arguments.hasFlag(CommandFlags.SILENT));

        if (player != context.getSender()) {
            VirtualLang.COMMAND_SELL_ALL_DONE_OTHERS.getMessage().send(context.getSender(), replacer -> replacer.replace(Placeholders.forPlayer(player)));
        }
        return true;
    }

    private static boolean sellHand(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        module.sellSlots(player, player.getInventory().getHeldItemSlot());

        if (player != context.getSender()) {
            VirtualLang.COMMAND_SELL_HAND_DONE_OTHERS.getMessage().send(context.getSender(), replacer -> replacer.replace(Placeholders.forPlayer(player)));
        }
        return true;
    }

    private static boolean sellHandAll(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        PlayerInventory inventory = player.getInventory();

        ItemStack itemStack = inventory.getItemInMainHand();
        if (itemStack.getType().isAir()) return false;

        Set<Integer> slots = new HashSet<>();
        slots.add(inventory.getHeldItemSlot());

        for (int slot = 0; slot < inventory.getStorageContents().length; slot++) {
            ItemStack content = inventory.getItem(slot);
            if (content == null || !content.isSimilar(itemStack)) continue;

            slots.add(slot);
        }

        module.sellSlots(player, slots.stream().mapToInt(i -> i).toArray());

        if (player != context.getSender()) {
            VirtualLang.COMMAND_SELL_HAND_DONE_OTHERS.getMessage().send(context.getSender(), replacer -> replacer.replace(Placeholders.forPlayer(player)));
        }
        return true;
    }

    private static boolean openCentralGUI(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        if (player != context.getSender()) {
            VirtualLang.COMMAND_MENU_DONE_OTHERS.getMessage().send(context.getSender(), replacer -> replacer.replace(Placeholders.forPlayer(player)));
        }

        boolean force = arguments.hasFlag(CommandFlags.FORCE);

        return module.openMainMenu(player, force);
    }

    private static boolean openEditor(@NotNull VirtualShopModule module, @NotNull CommandContext context) {
        Player player = context.getExecutor();
        if (player == null) return false;

        module.openShopsEditor(player);
        return true;
    }

    private static boolean openShop(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        VirtualShop shop = arguments.getArgument(CommandArguments.SHOP, VirtualShop.class);
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        if (player != context.getSender() && !arguments.hasFlag(CommandFlags.SILENT)) {
            VirtualLang.COMMAND_OPEN_DONE_OTHERS.getMessage().send(context.getSender(), replacer -> replacer
                .replace(Placeholders.forPlayer(player))
                .replace(shop.replacePlaceholders())
            );
        }

        boolean force = arguments.hasFlag(CommandFlags.FORCE);
        return module.openShop(player, shop, force);
    }

    private static boolean rotateShop(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        VirtualShop shop = arguments.getArgument(CommandArguments.SHOP, VirtualShop.class);

        shop.performRotation();
        VirtualLang.COMMAND_ROTATE_DONE.getMessage().send(context.getSender(), replacer -> replacer.replace(shop.replacePlaceholders()));
        return true;
    }
}
