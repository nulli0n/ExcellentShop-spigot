package su.nightexpress.nexshop.shop.module;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.config.Lang;

public class ModuleReloadCommand<S extends ShopModule> extends ModuleCommand<S> {

    public ModuleReloadCommand(@NotNull S module) {
        super(module, new String[]{"reload"}, Perms.COMMAND_RELOAD);

        this.setDescription("");
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        this.module.reload();
        this.plugin.getMessage(Lang.Module_Cmd_Reload).replace("%module%", module.getName()).send(sender);
    }
}