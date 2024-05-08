package su.nightexpress.nexshop.module;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;

public class ReloadCommand {

    public static void build(@NotNull AbstractShopModule module, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("reload", builder -> builder
            .permission(Perms.COMMAND_RELOAD)
            .description(Lang.MODULE_COMMAND_RELOAD_DESC)
            .executes((context, arguments) -> execute(module, context, arguments))
        );
    }

    public static boolean execute(@NotNull AbstractShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        module.reload();
        return context.sendSuccess(Lang.MODULE_COMMAND_RELOAD.getMessage().replace(Placeholders.GENERIC_NAME, module.getName()));
    }
}
