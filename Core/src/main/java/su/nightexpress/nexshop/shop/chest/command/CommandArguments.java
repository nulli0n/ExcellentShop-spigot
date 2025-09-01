package su.nightexpress.nexshop.shop.chest.command;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ShopBlock;
import su.nightexpress.nightcore.command.experimental.argument.CommandArgument;
import su.nightexpress.nightcore.command.experimental.builder.ArgumentBuilder;
import su.nightexpress.nightcore.util.BukkitThing;

import java.util.Optional;

public class CommandArguments {

    public static final String PLAYER     = "player";
    public static final String BUY_PRICE  = "buy";
    public static final String SELL_PRICE = "sell";
    public static final String SHOP_BLOCK = "shopblock";
    public static final String AMOUNT     = "amount";
    public static final String ITEM_NAME = "itemname";

    @NotNull
    public static ArgumentBuilder<ShopBlock> forShopBlock(@NotNull ChestShopModule module) {
        return CommandArgument.builder(SHOP_BLOCK, (string, context) -> Optional.ofNullable(BukkitThing.getMaterial(string)).map(module::getShopBlock).orElse(null))
            // TODO .customFailure(ChestLang.ERROR_COMMAND_INVALID_SHOP_BLOCK_ARGUMENT)
            .localized(ChestLang.COMMAND_ARGUMENT_NAME_SHOP_BLOCK.text())
            .withSamples(tabContext -> module.getShopBlockMap().keySet().stream().map(BukkitThing::getValue).toList());
    }
}
