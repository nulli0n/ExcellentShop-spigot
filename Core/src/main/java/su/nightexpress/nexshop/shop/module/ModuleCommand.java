package su.nightexpress.nexshop.shop.module;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nightexpress.nexshop.ExcellentShop;

public class ModuleCommand<S extends ShopModule> extends GeneralCommand<ExcellentShop> {

    protected final S module;

    public ModuleCommand(@NotNull S module, @NotNull String[] aliases, @Nullable Permission permission) {
        this(module, aliases, permission == null ? null : permission.getName());
    }

    public ModuleCommand(@NotNull S module, @NotNull String[] aliases, @Nullable String permission) {
        super(module.plugin(), aliases, permission);
        this.module = module;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        super.onExecute(sender, result);
    }
}
