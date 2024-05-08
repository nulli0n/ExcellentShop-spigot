package su.nightexpress.nexshop.shop.virtual.command.standalone;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.command.CommandArguments;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.RootCommand;
import su.nightexpress.nightcore.command.experimental.ServerCommand;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;

public class ShopCommand {

    private static final String ARG_SHOP = "shop";

    @NotNull
    public static ServerCommand create(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module, @NotNull String[] aliases) {
        return RootCommand.direct(plugin, aliases, builder -> builder
            .permission(VirtualPerms.COMMAND_SHOP)
            .description(VirtualLang.COMMAND_SHOP_DESC)
            .playerOnly()
            .withArgument(CommandArguments.forShop(ARG_SHOP, module))
            .executes((context, arguments) -> execute(module, context, arguments))
        );
    }

    public static boolean execute(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getExecutor();
        if (player == null) return false;

        if (arguments.hasArgument(ARG_SHOP)) {
            VirtualShop shop = arguments.getArgument(ARG_SHOP, VirtualShop.class);
            shop.open(player);
            return true;
        }

        module.openMainMenu(player);
        return true;
    }
}
