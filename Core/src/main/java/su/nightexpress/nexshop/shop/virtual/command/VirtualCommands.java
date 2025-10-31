package su.nightexpress.nexshop.shop.virtual.command;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.builder.ArgumentNodeBuilder;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;

import java.util.*;

public class VirtualCommands {

    public static final String DEF_SHOP = "shop";

    public static final String ARG_PLAYER = "player";
    public static final String ARG_SHOP   = "shop";

    public static final String FLAG_FORCE  = "f";
    public static final String FLAG_SILENT = "s";

    private static      Set<NightCommand> commands;

    private static final Map<String, NightCommand> SHOP_ALIASES = new HashMap<>();

    public static void load(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull HubNodeBuilder builder) {
        commands = new HashSet<>();

        builder.branch(Commands.literal("editor")
            .permission(VirtualPerms.COMMAND_EDITOR)
            .description(VirtualLang.COMMAND_EDITOR_DESC.text())
            .playerOnly()
            .executes((context, arguments) -> openEditor(module, context))
        );

        builder.branch(Commands.literal("open")
            .permission(VirtualPerms.COMMAND_OPEN)
            .description(VirtualLang.COMMAND_OPEN_DESC.text())
            .withArguments(
                shopArg(module).localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP.text()),
                Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_OPEN_OTHERS).optional()
            )
            .withFlags(FLAG_FORCE, FLAG_SILENT)
            .executes((context, arguments) -> openShop(module, context, arguments))
        );

        builder.branch(Commands.literal("rotate")
            .permission(VirtualPerms.COMMAND_ROTATE)
            .description(VirtualLang.COMMAND_ROTATE_DESC.text())
            .withArguments(shopArg(module).localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP.text()))
            .executes(VirtualCommands::rotateShop)
        );

        if (VirtualConfig.isCentralMenuEnabled()) {
            builder.branch(Commands.literal("menu")
                .permission(VirtualPerms.COMMAND_MENU)
                .description(VirtualLang.COMMAND_MENU_DESC.text())
                .withArguments(Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_MENU_OTHERS).optional())
                .withFlags(FLAG_FORCE)
                .executes((context, arguments) -> openCentralGUI(module, context, arguments))
            );
        }

        if (VirtualConfig.SHOP_SHORTCUTS_ENABLED.get()) {
            register(NightCommand.literal(plugin, VirtualConfig.SHOP_SHORTCUTS_COMMANDS.get(), child -> child
                .playerOnly()
                .permission(VirtualPerms.COMMAND_SHOP)
                .description(VirtualLang.COMMAND_SHOP_DESC.text())
                .withArguments(shopArg(module).optional(VirtualConfig.isCentralMenuEnabled()))
                .executes((context, arguments) -> openDirectShop(module, context, arguments))
            ));
        }

        if (VirtualConfig.SELL_MENU_ENABLED.get()) {
            register(NightCommand.literal(plugin, VirtualConfig.SELL_MENU_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_MENU)
                .description(VirtualLang.COMMAND_SELL_MENU_DESC.text())
                .withArguments(Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_SELL_MENU_OTHERS).optional())
                .executes((context, arguments) -> openSellMenu(module, context, arguments))
            ));
        }

        if (VirtualConfig.SELL_ALL_ENABLED.get()) {
            register(NightCommand.literal(plugin, VirtualConfig.SELL_ALL_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_ALL)
                .description(VirtualLang.COMMAND_SELL_ALL_DESC.text())
                .withArguments(Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_SELL_ALL_OTHERS).optional())
                .withFlags(FLAG_SILENT)
                .executes((context, arguments) -> sellAll(module, context, arguments))
            ));
        }

        if (VirtualConfig.SELL_HAND_ENABLED.get()) {
            register(NightCommand.literal(plugin, VirtualConfig.SELL_HAND_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_HAND)
                .description(VirtualLang.COMMAND_SELL_HAND_DESC.text())
                .withArguments(Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_SELL_HAND_OTHERS).optional())
                .executes((context, arguments) -> sellHand(module, context, arguments))
            ));
        }

        if (VirtualConfig.SELL_HAND_ALL_ENABLED.get()) {
            register(NightCommand.literal(plugin, VirtualConfig.SELL_HAND_ALL_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_HAND_ALL)
                .description(VirtualLang.COMMAND_SELL_HAND_ALL_DESC.text())
                .withArguments(Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_SELL_HAND_ALL_OTHERS).optional())
                .executes((context, arguments) -> sellHandAll(module, context, arguments))
            ));
        }
    }

    public static void unload() {
        unregister();
    }

    private static void register(@NotNull NightCommand command) {
        if (command.register()) {
            commands.add(command);
        }
    }

    private static void unregister() {
        commands.forEach(NightCommand::unregister);
        commands.clear();
    }

    public static void registerAliases(@NotNull ShopPlugin plugin, @NotNull VirtualShop shop) {
        if (!shop.hasAliases()) return;

        NightCommand command = NightCommand.literal(plugin, shop.getAliases().toArray(new String[0]), builder -> builder
            .playerOnly()
            .permission(VirtualPerms.COMMAND_SHOP)
            .description(shop.replacePlaceholders().apply(VirtualLang.COMMAND_SHOP_ALIAS_DESC.text()))
            .executes((context, arguments) -> openExplicitShop(shop, context))
        );

        if (command.register()) {
            SHOP_ALIASES.put(shop.getId(), command);
        }
    }

    public static void unregisterAliases(@NotNull VirtualShop shop) {
        NightCommand command = SHOP_ALIASES.remove(shop.getId());
        if (command == null) return;

        command.unregister();
    }

    @NotNull
    private static ArgumentNodeBuilder<VirtualShop> shopArg(@NotNull VirtualShopModule module) {
        return Commands.argument(ARG_SHOP, (context, string) -> Optional.ofNullable(module.getShopById(string))
                .orElseThrow(() -> CommandSyntaxException.custom(VirtualLang.ERROR_COMMAND_INVALID_SHOP_ARGUMENT)))
            .localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP.text())
            .suggestions((reader, context) -> {
                Collection<VirtualShop> shops = !context.isPlayer() ? module.getShops() : module.getShops(context.getPlayerOrThrow());
                return shops.stream().map(VirtualShop::getId).toList();
            });
    }

    private static boolean openDirectShop(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();

        if (arguments.contains(ARG_SHOP)) {
            VirtualShop shop = arguments.get(ARG_SHOP, VirtualShop.class);
            shop.open(player);
        }
        else {
            module.openMainMenu(player);
        }
        return true;
    }

    private static boolean openExplicitShop(@NotNull VirtualShop shop, @NotNull su.nightexpress.nightcore.commands.context.CommandContext context) {
        Player player = context.getPlayerOrThrow();
        shop.open(player);
        return true;
    }

    private static boolean openSellMenu(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player player = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();

        module.openSellMenu(player, false);

        if (player != context.getSender()) {
            VirtualLang.COMMAND_SELL_MENU_DONE_OTHERS.message().send(context.getSender(), replacer -> replacer.replace(Placeholders.forPlayer(player)));
        }
        return true;
    }

    private static boolean sellAll(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player player = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();

        if (!module.isAvailable(player, true)) return false;

        module.sellAll(player, context.hasFlag(FLAG_SILENT));

        if (player != context.getSender()) {
            VirtualLang.COMMAND_SELL_ALL_DONE_OTHERS.message().send(context.getSender(), replacer -> replacer.replace(Placeholders.forPlayer(player)));
        }
        return true;
    }

    private static boolean sellHand(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player player = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();

        if (!module.isAvailable(player, true)) return false;

        module.sellSlots(player, player.getInventory().getHeldItemSlot());

        if (player != context.getSender()) {
            VirtualLang.COMMAND_SELL_HAND_DONE_OTHERS.message().send(context.getSender(), replacer -> replacer.replace(Placeholders.forPlayer(player)));
        }
        return true;
    }

    private static boolean sellHandAll(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player player = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();

        if (!module.isAvailable(player, true)) return false;

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
            VirtualLang.COMMAND_SELL_HAND_DONE_OTHERS.message().send(context.getSender(), replacer -> replacer.replace(Placeholders.forPlayer(player)));
        }
        return true;
    }

    private static boolean openCentralGUI(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player player = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();
        if (player != context.getSender()) {
            VirtualLang.COMMAND_MENU_DONE_OTHERS.message().send(context.getSender(), replacer -> replacer.replace(Placeholders.forPlayer(player)));
        }

        boolean force = context.hasFlag(FLAG_FORCE);

        return module.openMainMenu(player, force);
    }

    private static boolean openEditor(@NotNull VirtualShopModule module, @NotNull CommandContext context) {
        Player player = context.getPlayerOrThrow();
        module.openShopsEditor(player);
        return true;
    }

    private static boolean openShop(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player player = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();
        VirtualShop shop = arguments.get(ARG_SHOP, VirtualShop.class);

        boolean force = context.hasFlag(FLAG_FORCE);

        if (player != context.getSender() && !context.hasFlag(FLAG_SILENT)) {
            VirtualLang.COMMAND_OPEN_DONE_OTHERS.message().send(context.getSender(), replacer -> replacer
                .replace(Placeholders.forPlayer(player))
                .replace(shop.replacePlaceholders())
            );
        }

        return module.openShop(player, shop, force);
    }

    private static boolean rotateShop(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        VirtualShop shop = arguments.get(ARG_SHOP, VirtualShop.class);

        shop.performRotation();
        VirtualLang.COMMAND_ROTATE_DONE.message().send(context.getSender(), replacer -> replacer.replace(shop.replacePlaceholders()));
        return true;
    }
}
