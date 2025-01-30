package su.nightexpress.nexshop.shop.virtual.command;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.command.experimental.argument.CommandArgument;
import su.nightexpress.nightcore.command.experimental.builder.ArgumentBuilder;

import java.util.Collection;

public class CommandArguments {

    public static final String PLAYER = "player";
    public static final String SHOP = "shop";

    @NotNull
    public static ArgumentBuilder<VirtualShop> forShop(@NotNull VirtualShopModule module) {
        return CommandArgument.builder(SHOP, (string, context) -> module.getShopById(string))
            .localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP)
            .customFailure(VirtualLang.ERROR_COMMAND_INVALID_SHOP_ARGUMENT)
            .withSamples(tabContext -> {
                Collection<VirtualShop> shops = tabContext.getPlayer() == null ? module.getShops() : module.getShops(tabContext.getPlayer());
                return shops.stream().map(VirtualShop::getId).toList();
            });
    }
}
