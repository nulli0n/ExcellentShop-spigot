package su.nightexpress.nexshop.modules.command;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.modules.ShopModule;

public abstract class ShopModuleCommand<M extends ShopModule> extends AbstractCommand<ExcellentShop> {

    protected M module;

    public ShopModuleCommand(@NotNull M module, @NotNull String[] aliases, @NotNull String permission) {
        super(module.plugin(), aliases, permission);
        this.module = module;
    }
}
