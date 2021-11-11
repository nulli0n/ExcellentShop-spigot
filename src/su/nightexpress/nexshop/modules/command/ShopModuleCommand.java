package su.nightexpress.nexshop.modules.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.module.AbstractModuleCommand;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.modules.ShopModule;

public class ShopModuleCommand<M extends ShopModule> extends AbstractModuleCommand<ExcellentShop, M> {

    public ShopModuleCommand(@NotNull M module, @NotNull String[] aliases) {
        super(module, aliases, null);
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
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

    }
}
