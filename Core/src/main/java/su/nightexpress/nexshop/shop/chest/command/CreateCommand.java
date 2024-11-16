package su.nightexpress.nexshop.shop.chest.command;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.util.ShopType;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.util.Lists;

public class CreateCommand {

    private static final String ARG_BUY_PRICE  = "buyprice";
    private static final String ARG_SELL_PRICE = "sellprice";

    public static void build(@NotNull ChestShopModule module, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("create", builder -> builder
            .permission(ChestPerms.CREATE)
            .description(ChestLang.COMMAND_CREATE_DESC)
            .playerOnly()
            .withArgument(ArgumentTypes.decimalCompactAbs(ARG_BUY_PRICE)
                .localized(ChestLang.COMMAND_ARGUMENT_NAME_BUY_PRICE)
                .withSamples(tabContext -> Lists.newList("100", "1000"))
            )
            .withArgument(ArgumentTypes.decimalCompactAbs(ARG_SELL_PRICE)
                .localized(ChestLang.COMMAND_ARGUMENT_NAME_SELL_PRICE)
                .withSamples(tabContext -> Lists.newList("50", "500"))
            )
            .executes((context, arguments) -> execute(module, context, arguments))
        );
    }

    public static boolean execute(@NotNull ChestShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getExecutor();
        if (player == null) return false;

        Block block = player.getTargetBlock(null, 100);

        double buyPrice = arguments.getDoubleArgument(ARG_BUY_PRICE, ChestConfig.SHOP_PRODUCT_INITIAL_BUY_PRICE.get());
        double sellPrice = arguments.getDoubleArgument(ARG_SELL_PRICE, ChestConfig.SHOP_PRODUCT_INITIAL_SELL_PRICE.get());

        return module.createShopNaturally(player, block, ShopType.PLAYER, buyPrice, sellPrice);
    }
}
