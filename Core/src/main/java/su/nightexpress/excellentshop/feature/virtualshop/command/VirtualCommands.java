package su.nightexpress.excellentshop.feature.virtualshop.command;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualConfig;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualPerms;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.argument.ArgumentType;
import su.nightexpress.nightcore.commands.builder.ArgumentNodeBuilder;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;

import java.util.*;

public class VirtualCommands {

    public static final String DEF_SHOP = "shop";

    private static final String ARG_PLAYER = "player";
    private static final String ARG_SHOP   = "shop";

    private static final String FLAG_FORCE  = "f";
    private static final String FLAG_SILENT = "s";

    private final ShopPlugin        plugin;
    private final VirtualShopModule module;

    private final Set<NightCommand>         standalones;
    private final Map<String, NightCommand> shopAliases;

    private final ArgumentType<VirtualShop> shopArgumentType;

    public VirtualCommands(@NonNull ShopPlugin plugin, @NonNull VirtualShopModule module) {
        this.plugin = plugin;
        this.module = module;

        this.standalones = new HashSet<>();
        this.shopAliases = new HashMap<>();

        this.shopArgumentType = (context, string) -> Optional.ofNullable(this.module.getShopById(string))
            .orElseThrow(() -> CommandSyntaxException.custom(VirtualLang.ERROR_COMMAND_INVALID_SHOP_ARGUMENT));
    }

    public void load(@NonNull HubNodeBuilder builder) {
        builder.branch(Commands.literal("editor")
            .permission(VirtualPerms.COMMAND_EDITOR)
            .description(VirtualLang.COMMAND_EDITOR_DESC.text())
            .playerOnly()
            .executes((context, arguments) -> this.openEditor(context))
        );

        builder.branch(Commands.literal("open")
            .permission(VirtualPerms.COMMAND_OPEN)
            .description(VirtualLang.COMMAND_OPEN_DESC.text())
            .withArguments(
                this.shopArg().localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP.text()),
                Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_OPEN_OTHERS).optional()
            )
            .withFlags(FLAG_FORCE, FLAG_SILENT)
            .executes(this::openShop)
        );

        builder.branch(Commands.literal("rotate")
            .permission(VirtualPerms.COMMAND_ROTATE)
            .description(VirtualLang.COMMAND_ROTATE_DESC.text())
            .withArguments(this.shopArg().localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP.text()))
            .executes(this::rotateShop)
        );

        if (VirtualConfig.isCentralMenuEnabled()) {
            builder.branch(Commands.literal("menu")
                .permission(VirtualPerms.COMMAND_MENU)
                .description(VirtualLang.COMMAND_MENU_DESC.text())
                .withArguments(Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_MENU_OTHERS).optional())
                .withFlags(FLAG_FORCE)
                .executes(this::openCentralGUI)
            );
        }

        if (VirtualConfig.SHOP_SHORTCUTS_ENABLED.get()) {
            this.registerStandalone(NightCommand.literal(plugin, VirtualConfig.SHOP_SHORTCUTS_COMMANDS.get(), child -> child
                .playerOnly()
                .permission(VirtualPerms.COMMAND_SHOP)
                .description(VirtualLang.COMMAND_SHOP_DESC.text())
                .withArguments(this.shopArg().optional(VirtualConfig.isCentralMenuEnabled()))
                .executes(this::openDirectShop)
            ));
        }

        if (VirtualConfig.SELL_MENU_ENABLED.get()) {
            this.registerStandalone(NightCommand.literal(plugin, VirtualConfig.SELL_MENU_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_MENU)
                .description(VirtualLang.COMMAND_SELL_MENU_DESC.text())
                .withArguments(Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_SELL_MENU_OTHERS).optional())
                .executes(this::openSellMenu)
            ));
        }

        if (VirtualConfig.SELL_ALL_ENABLED.get()) {
            this.registerStandalone(NightCommand.literal(plugin, VirtualConfig.SELL_ALL_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_ALL)
                .description(VirtualLang.COMMAND_SELL_ALL_DESC.text())
                .withArguments(Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_SELL_ALL_OTHERS).optional())
                .withFlags(FLAG_SILENT)
                .executes(this::sellAll)
            ));
        }

        if (VirtualConfig.SELL_HAND_ENABLED.get()) {
            this.registerStandalone(NightCommand.literal(plugin, VirtualConfig.SELL_HAND_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_HAND)
                .description(VirtualLang.COMMAND_SELL_HAND_DESC.text())
                .withArguments(Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_SELL_HAND_OTHERS).optional())
                .executes(this::sellHand)
            ));
        }

        if (VirtualConfig.SELL_HAND_ALL_ENABLED.get()) {
            this.registerStandalone(NightCommand.literal(plugin, VirtualConfig.SELL_HAND_ALL_COMMANDS.get(), child -> child
                .permission(VirtualPerms.COMMAND_SELL_HAND_ALL)
                .description(VirtualLang.COMMAND_SELL_HAND_ALL_DESC.text())
                .withArguments(Arguments.player(ARG_PLAYER).permission(VirtualPerms.COMMAND_SELL_HAND_ALL_OTHERS).optional())
                .executes(this::sellHandAll)
            ));
        }
    }

    public void unload() {
        this.standalones.forEach(NightCommand::unregister);
        this.standalones.clear();

        this.shopAliases.values().forEach(NightCommand::unregister);
        this.shopAliases.clear();
    }

    private void registerStandalone(@NonNull NightCommand command) {
        if (command.register()) {
            this.standalones.add(command);
        }
    }

    public void registerAliases(@NonNull VirtualShop shop) {
        if (!shop.hasAliases()) return;

        PlaceholderContext placeholderContext = PlaceholderContext.builder().with(shop.placeholders()).build();

        NightCommand command = NightCommand.literal(this.plugin, shop.getAliases().toArray(new String[0]), builder -> builder
            .playerOnly()
            .permission(VirtualPerms.COMMAND_SHOP)
            .description(placeholderContext.apply(VirtualLang.COMMAND_SHOP_ALIAS_DESC.text()))
            .executes((context, arguments) -> openExplicitShop(shop, context))
        );

        if (command.register()) {
            this.shopAliases.put(shop.getId(), command);
        }
    }

    public void unregisterAliases(@NonNull VirtualShop shop) {
        NightCommand command = this.shopAliases.remove(shop.getId());
        if (command == null) return;

        command.unregister();
    }

    @NonNull
    private ArgumentNodeBuilder<VirtualShop> shopArg() {
        return Commands.argument(ARG_SHOP, this.shopArgumentType)
            .localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP.text())
            .suggestions((reader, context) -> {
                Collection<VirtualShop> shops = !context.isPlayer() ? this.module.getShops() : this.module.getShops(context.getPlayerOrThrow());
                return shops.stream().map(VirtualShop::getId).toList();
            });
    }

    private boolean openDirectShop(@NonNull CommandContext context, @NonNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();

        if (arguments.contains(ARG_SHOP)) {
            VirtualShop shop = arguments.get(ARG_SHOP, VirtualShop.class);
            shop.open(player);
        }
        else {
            this.module.openMainMenu(player);
        }
        return true;
    }

    private boolean openExplicitShop(@NonNull VirtualShop shop, @NonNull CommandContext context) {
        Player player = context.getPlayerOrThrow();
        shop.open(player);
        return true;
    }

    private boolean openSellMenu(@NonNull CommandContext context, @NonNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player target = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();

        if (!this.module.isAvailable(target, true)) return false;

        this.module.openSellMenu(target, false);

        if (target != context.getSender()) {
            this.module.sendPrefixed(VirtualLang.COMMAND_SELL_MENU_DONE_OTHERS, context.getSender(), builder -> builder
                .with(CommonPlaceholders.PLAYER.resolver(target))
            );
        }
        return true;
    }

    private boolean sellAll(@NonNull CommandContext context, @NonNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player target = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();

        if (!this.module.isAvailable(target, true)) return false;

        this.module.sellAll(target, target.getInventory(), context.hasFlag(FLAG_SILENT), completed -> {});

        if (target != context.getSender()) {
            this.module.sendPrefixed(VirtualLang.COMMAND_SELL_ALL_DONE_OTHERS, context.getSender(), builder -> builder
                .with(CommonPlaceholders.PLAYER.resolver(target))
            );
        }
        return true;
    }

    private boolean sellHand(@NonNull CommandContext context, @NonNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player target = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();

        if (!this.module.isAvailable(target, true)) return false;

        this.module.sellSlots(target, target.getInventory().getHeldItemSlot());

        if (target != context.getSender()) {
            this.module.sendPrefixed(VirtualLang.COMMAND_SELL_HAND_DONE_OTHERS, context.getSender(), builder -> builder
                .with(CommonPlaceholders.PLAYER.resolver(target))
            );
        }
        return true;
    }

    private boolean sellHandAll(@NonNull CommandContext context, @NonNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player target = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();

        if (!this.module.isAvailable(target, true)) return false;

        PlayerInventory inventory = target.getInventory();

        ItemStack itemStack = inventory.getItemInMainHand();
        if (itemStack.getType().isAir()) return false;

        Set<Integer> slots = new HashSet<>();
        slots.add(inventory.getHeldItemSlot());

        for (int slot = 0; slot < inventory.getStorageContents().length; slot++) {
            ItemStack content = inventory.getItem(slot);
            if (content == null || !content.isSimilar(itemStack)) continue;

            slots.add(slot);
        }

        this.module.sellSlots(target, slots.stream().mapToInt(i -> i).toArray());

        if (target != context.getSender()) {
            this.module.sendPrefixed(VirtualLang.COMMAND_SELL_HAND_DONE_OTHERS, context.getSender(), builder -> builder
                .with(CommonPlaceholders.PLAYER.resolver(target))
            );
        }
        return true;
    }

    private boolean openCentralGUI(@NonNull CommandContext context, @NonNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player target = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();
        if (target != context.getSender()) {
            this.module.sendPrefixed(VirtualLang.COMMAND_MENU_DONE_OTHERS, context.getSender(), builder -> builder
                .with(CommonPlaceholders.PLAYER.resolver(target))
            );
        }

        boolean force = context.hasFlag(FLAG_FORCE);

        return this.module.openMainMenu(target, force);
    }

    private boolean openEditor(@NonNull CommandContext context) {
        Player player = context.getPlayerOrThrow();
        this.module.openShopsEditor(player);
        return true;
    }

    private boolean openShop(@NonNull CommandContext context, @NonNull ParsedArguments arguments) {
        if (!arguments.contains(ARG_PLAYER) && !context.isPlayer()) {
            context.printUsage();
            return false;
        }

        Player target = arguments.contains(ARG_PLAYER) ? arguments.getPlayer(ARG_PLAYER) : context.getPlayerOrThrow();
        VirtualShop shop = arguments.get(ARG_SHOP, VirtualShop.class);

        boolean force = context.hasFlag(FLAG_FORCE);
        // TODO Page

        if (target != context.getSender() && !context.hasFlag(FLAG_SILENT)) {
            this.module.sendPrefixed(VirtualLang.COMMAND_OPEN_DONE_OTHERS, context.getSender(), builder -> builder
                .with(CommonPlaceholders.PLAYER.resolver(target))
                .with(shop.placeholders())
            );
        }

        return module.openShop(target, shop, 1, force);
    }

    private boolean rotateShop(@NonNull CommandContext context, @NonNull ParsedArguments arguments) {
        VirtualShop shop = arguments.get(ARG_SHOP, VirtualShop.class);

        shop.performRotation();
        this.module.sendPrefixed(VirtualLang.COMMAND_ROTATE_DONE, context.getSender(), builder -> builder.with(shop.placeholders()));
        return true;
    }
}
