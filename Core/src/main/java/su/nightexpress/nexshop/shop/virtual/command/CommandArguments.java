package su.nightexpress.nexshop.shop.virtual.command;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nightcore.command.experimental.argument.CommandArgument;
import su.nightexpress.nightcore.command.experimental.builder.ArgumentBuilder;

import java.util.Collection;

public class CommandArguments {

    @NotNull
    public static ArgumentBuilder<VirtualShop> forShop(@NotNull String name, @NotNull VirtualShopModule module) {
        return CommandArgument.builder(name, module::getShopById)
            .localized(VirtualLang.COMMAND_ARGUMENT_NAME_SHOP)
            .customFailure(VirtualLang.ERROR_INVALID_SHOP)
            .withSamples(tabContext -> {
                Collection<VirtualShop> shops = tabContext.getPlayer() == null ? module.getShops() : module.getShops(tabContext.getPlayer());
                return shops.stream().map(VirtualShop::getId).toList();
            });
    }
}
