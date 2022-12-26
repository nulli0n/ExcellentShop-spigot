package su.nightexpress.nexshop.module.command;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.module.AbstractModuleCommand;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.module.ShopModule;

import java.util.Map;

public class ShopModuleCommand<M extends ShopModule> extends AbstractModuleCommand<ExcellentShop, M> {

    public ShopModuleCommand(@NotNull M module, @NotNull String[] aliases) {
        this(module, aliases, (String) null);
    }

    public ShopModuleCommand(@NotNull M module, @NotNull String[] aliases, @Nullable Permission permission) {
        this(module, aliases, permission == null ? null : permission.getName());
    }

    public ShopModuleCommand(@NotNull M module, @NotNull String[] aliases, @Nullable String permission) {
        super(module, aliases, permission);
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return "";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {

    }
}
