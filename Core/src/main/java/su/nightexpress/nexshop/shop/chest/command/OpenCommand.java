package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;

public class OpenCommand {

    public static void build(@NotNull ChestShopModule module, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("open", builder -> builder
            .permission(ChestPerms.COMMAND_OPEN)
            .description(ChestLang.COMMAND_OPEN_DESC)
            .playerOnly()
            .executes((context, arguments) -> execute(module, context, arguments))
        );
    }

    public static boolean execute(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getExecutor();
        if (player == null) return false;

        Block block = player.getTargetBlock(null, 10);

        ChestShop shop = module.getShop(block);
        if (shop == null) {
            return context.sendFailure(ChestLang.ERROR_BLOCK_IS_NOT_SHOP.getMessage());
        }

        player.openInventory(shop.getInventory());
        return true;
    }
}
