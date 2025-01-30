package su.nightexpress.nexshop.shop.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Module;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.RootCommand;
import su.nightexpress.nightcore.command.experimental.ServerCommand;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.StringUtil;

public abstract class AbstractModule extends AbstractManager<ShopPlugin> implements Module {

    public static final String CONFIG_NAME = "settings.yml";

    private final String   id;
    private final String   name;
    private final String[] aliases;

    private ServerCommand moduleCommand;

    public AbstractModule(@NotNull ShopPlugin plugin, @NotNull String id, @NotNull String[] aliases) {
        super(plugin);
        this.id = id;
        this.name = StringUtil.capitalizeUnderscored(id);
        this.aliases = aliases;
    }

    @Override
    protected final void onLoad() {
        FileConfig config = this.getConfig();

        this.loadModule(config);

        this.moduleCommand = RootCommand.chained(this.plugin, this.aliases, builder -> {
            builder.localized(this.getName());
            builder.addDirect("reload", child -> child
                .permission(Perms.COMMAND_RELOAD)
                .description(Lang.MODULE_COMMAND_RELOAD_DESC)
                .executes(this::executeReload)
            );
            this.loadCommands(builder);
        });
        this.plugin.getCommandManager().registerCommand(this.moduleCommand);

        config.saveChanges();
    }

    @Override
    protected final void onShutdown() {
        this.disableModule();

        this.plugin.getCommandManager().unregisterCommand(this.moduleCommand);
    }

    protected abstract void loadModule(@NotNull FileConfig config);

    protected abstract void loadCommands(@NotNull ChainedNodeBuilder builder);

    protected abstract void disableModule();

    private boolean executeReload(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        this.reload();
        return context.sendSuccess(Lang.MODULE_COMMAND_RELOAD.getMessage().replace(Placeholders.GENERIC_NAME, this.getName()));
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public FileConfig getConfig() {
        return FileConfig.loadOrExtract(plugin, this.getLocalPath(), CONFIG_NAME);
    }

    @NotNull
    public final String getLocalPath() {
        return this.getId();
    }

    @NotNull
    public final String getMenusPath() {
        return this.getLocalPath() + Config.DIR_MENU;
    }

    @NotNull
    public final String getAbsolutePath() {
        return this.plugin.getDataFolder() + "/" + this.getLocalPath();
    }

    @NotNull
    private String buildLog(@NotNull String msg) {
        return "[" + this.getName() + "] " + msg;
    }

    public final void info(@NotNull String msg) {
        this.plugin.info(this.buildLog(msg));
    }

    public final void warn(@NotNull String msg) {
        this.plugin.warn(this.buildLog(msg));
    }

    public final void error(@NotNull String msg) {
        this.plugin.error(this.buildLog(msg));
    }
}
