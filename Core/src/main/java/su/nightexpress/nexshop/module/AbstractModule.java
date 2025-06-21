package su.nightexpress.nexshop.module;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.module.Module;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.RootCommand;
import su.nightexpress.nightcore.command.experimental.ServerCommand;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.entry.LangText;
import su.nightexpress.nightcore.language.message.LangMessage;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.StringUtil;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractModule extends AbstractManager<ShopPlugin> implements Module {

    public static final String CONFIG_NAME = "settings.yml";

    private final String   id;
    private final String   name;
    private final ModuleConfig moduleConfig;

    private ServerCommand moduleCommand;

    public AbstractModule(@NotNull ShopPlugin plugin, @NotNull String id, @NotNull ModuleConfig config) {
        super(plugin);
        this.id = id;
        this.name = StringUtil.capitalizeUnderscored(id);
        this.moduleConfig = config;
    }

    // TODO Prefix usage

    @Override
    public boolean validateConfig() {
        if (this.getDefaultCurrency().isDummy()) {
            this.plugin.error("Invalid/Unknown default currency '" + this.moduleConfig.getDefaultCurrency() + "' set for the '" + this.id + "' module!");
            return false;
        }

        this.info("Enabled currencies: " + this.getEnabledCurrencies().stream().map(Currency::getInternalId).collect(Collectors.joining(", ")));
        return true;
    }

    @Override
    protected final void onLoad() {
        FileConfig config = this.getConfig();

        this.loadModule(config);

        this.moduleCommand = RootCommand.chained(this.plugin, this.moduleConfig.getCommandAliases(), builder -> {
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
        context.send(this.getPrefixed(Lang.MODULE_COMMAND_RELOAD), replacer -> replacer.replace(Placeholders.GENERIC_NAME, this.name));
        return true;
    }

    @NotNull
    public LangMessage getPrefixed(@NotNull LangText text) {
        return text.getMessage().setPrefix(this.moduleConfig.getPrefix());
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
    public ModuleConfig getModuleConfig() {
        return this.moduleConfig;
    }

    @NotNull
    public FileConfig getConfig() {
        return FileConfig.loadOrExtract(this.plugin, this.getLocalPath(), CONFIG_NAME);
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

    @Override
    public boolean isEnabledCurrency(@NotNull Currency currency) {
        return this.isEnabledCurrency(currency.getInternalId());
    }

    @Override
    public boolean isEnabledCurrency(@NotNull String id) {
        return this.moduleConfig.getDefaultCurrency().equalsIgnoreCase(id) ||
            this.moduleConfig.getEnabledCurrencies().contains(id) ||
            this.moduleConfig.getEnabledCurrencies().contains(Placeholders.WILDCARD);
    }

    @Override
    @NotNull
    public Currency getDefaultCurrency() {
        return EconomyBridge.getCurrencyOrDummy(this.moduleConfig.getDefaultCurrency());
    }

    @Override
    @NotNull
    public Set<Currency> getEnabledCurrencies() {
        if (this.moduleConfig.getEnabledCurrencies().contains(Placeholders.WILDCARD)) return EconomyBridge.getCurrencies();

        Set<Currency> currencies = this.moduleConfig.getEnabledCurrencies().stream()
            .map(EconomyBridge::getCurrency)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(HashSet::new));

        currencies.add(this.getDefaultCurrency());
        currencies.removeIf(currency -> currency == null || currency.isDummy());

        return currencies;
    }

    @Override
    public boolean isAvailableCurrency(@NotNull Player player, @NotNull Currency currency) {
        if (!this.isEnabledCurrency(currency)) return false;
        if (!Config.CURRENCY_NEED_PERMISSION.get()) return true;

        return this.canUseCurrency(player, currency);
    }

    @Override
    @NotNull
    public Set<Currency> getAvailableCurrencies(@NotNull Player player) {
        Set<Currency> currencies = getEnabledCurrencies();
        if (!Config.CURRENCY_NEED_PERMISSION.get()) return currencies;

        return currencies.stream().filter(currency -> this.canUseCurrency(player, currency)).collect(Collectors.toSet());
    }

    private boolean canUseCurrency(@NotNull Player player, @NotNull Currency currency) {
        return this.isDefaultCurrency(currency) || ShopUtils.hasCurrencyPermission(player, currency);
    }

    @Override
    public boolean isDefaultCurrency(@NotNull Currency currency) {
        return this.isDefaultCurrency(currency.getInternalId());
    }

    @Override
    public boolean isDefaultCurrency(@NotNull String id) {
        return this.moduleConfig.getDefaultCurrency().equalsIgnoreCase(id);
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
