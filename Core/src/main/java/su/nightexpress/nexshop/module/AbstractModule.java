package su.nightexpress.nexshop.module;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.module.Module;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.exception.ModuleLoadException;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.command.experimental.RootCommand;
import su.nightexpress.nightcore.command.experimental.ServerCommand;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.message.LangMessage;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.StringUtil;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractModule extends AbstractManager<ShopPlugin> implements Module {

    public static final String CONFIG_NAME = "settings.yml";

    private final String         id;
    private final String         name;
    private final ModuleSettings settings;

    private ServerCommand moduleCommand;

    public AbstractModule(@NotNull ShopPlugin plugin, @NotNull String id, @NotNull ModuleSettings config) {
        super(plugin);
        this.id = id;
        this.name = StringUtil.capitalizeUnderscored(id);
        this.settings = config;
    }

    // TODO Prefix usage

    @Override
    protected final void onLoad() throws ModuleLoadException {
        if (this.getDefaultCurrency().isDummy()) {
            throw new ModuleLoadException("Unknown default currency '" + this.settings.getDefaultCurrency() + "'!");
        }

        this.info("Enabled currencies: " + this.getEnabledCurrencies().stream().map(Currency::getInternalId).collect(Collectors.joining(", ")));

        FileConfig config = this.getConfig();

        this.loadModule(config);

        this.moduleCommand = RootCommand.chained(this.plugin, this.settings.getCommandAliases(), builder -> {
            builder.localized(this.getName());
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

    @NotNull
    public LangMessage getPrefixed(@NotNull MessageLocale text) {
        return text.withPrefix(this.settings.getPrefix());
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
    public ModuleSettings getSettings() {
        return this.settings;
    }

    @NotNull
    public FileConfig getConfig() {
        return FileConfig.loadOrExtract(this.plugin, this.getLocalPath(), CONFIG_NAME);
    }

    @NotNull
    public final String getLocalPath() {
        return this.id;
    }

    @NotNull
    public final String getLocalPathTo(@NotNull String dir) {
        return this.id + dir;
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
        return this.settings.getDefaultCurrency().equalsIgnoreCase(id) ||
            this.settings.getEnabledCurrencies().contains(id) ||
            this.settings.getEnabledCurrencies().contains(Placeholders.WILDCARD);
    }

    @Override
    @NotNull
    public Currency getDefaultCurrency() {
        return EconomyBridge.getCurrencyOrDummy(this.settings.getDefaultCurrency());
    }

    @Override
    @NotNull
    public Set<Currency> getEnabledCurrencies() {
        if (this.settings.getEnabledCurrencies().contains(Placeholders.WILDCARD)) return EconomyBridge.getCurrencies();

        Set<Currency> currencies = this.settings.getEnabledCurrencies().stream()
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
        return this.settings.getDefaultCurrency().equalsIgnoreCase(id);
    }

    public boolean isItemProvidersDisabled() {
        return this.isItemProviderDisabled(Placeholders.WILDCARD);
    }

    public boolean isItemProviderDisabled(@NotNull ItemAdapter<?> adapter) {
        return this.isItemProviderDisabled(adapter.getName()) || this.isItemProvidersDisabled();
    }

    public boolean isItemProviderDisabled(@NotNull String id) {
        return this.settings.getDisabledItemProviders().contains(id);
    }

    @Override
    public boolean isItemProviderAllowed(@NotNull ItemAdapter<?> adapter) {
        return !this.isItemProviderDisabled(adapter);
    }

    @Override
    public boolean isItemProviderAllowed(@NotNull String id) {
        return !this.isItemProviderDisabled(id);
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
