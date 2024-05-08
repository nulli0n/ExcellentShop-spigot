package su.nightexpress.nexshop.shop.virtual.command.child;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;

public class EditorCommand {

    public static void build(@NotNull VirtualShopModule module, @NotNull ChainedNodeBuilder nodeBuilder) {
        nodeBuilder.addDirect("editor", builder -> builder
            .permission(VirtualPerms.COMMAND_EDITOR)
            .description(VirtualLang.COMMAND_EDITOR_DESC)
            .playerOnly()
            .executes((context, arguments) -> execute(module, context, arguments)));
    }

    public static boolean execute(@NotNull VirtualShopModule module, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getExecutor();
        if (player == null) return false;

        module.openShopsEditor(player);
        return true;
    }
}
