package su.nightexpress.nexshop.module;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopFiles;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.Module;
import su.nightexpress.excellentshop.core.Config;
import su.nightexpress.excellentshop.data.DataHandler;
import su.nightexpress.excellentshop.data.DataManager;
import su.nightexpress.nexshop.exception.ModuleLoadException;
import su.nightexpress.nexshop.user.UserManager;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.locale.message.LangMessage;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.ui.dialog.wrap.DialogRegistry;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractModule extends AbstractManager<ShopPlugin> implements Module {

    protected final DataHandler    dataHandler;
    protected final DataManager    dataManager;
    protected final UserManager    userManager;
    protected final DialogRegistry dialogRegistry;

    private final String           id;
    private final String           name;
    private final ModuleDefinition definition;

    private NightCommand moduleCommand;

    public AbstractModule(@NonNull ModuleContext context) {
        super(context.plugin());
        this.dataHandler = context.dataHandler();
        this.dataManager = context.dataManager();
        this.userManager = context.userManager();
        this.dialogRegistry = context.dialogRegistry();

        this.id = context.id();
        this.name = StringUtil.capitalizeUnderscored(this.id);
        this.definition = context.definition();
    }

    @Override
    protected final void onLoad() throws ModuleLoadException {
        if (this.getDefaultCurrency().isDummy()) {
            throw new ModuleLoadException("Unknown default currency '" + this.definition.getDefaultCurrency() + "'!");
        }

        this.info("Enabled currencies: " + this.getEnabledCurrencies().stream().map(Currency::getInternalId).collect(Collectors.joining(", ")));

        FileConfig config = this.getConfig();

        this.loadModule(config);

        this.moduleCommand = NightCommand.hub(this.plugin, this.definition.getCommandAliases(), builder -> {
            builder.localized(this.getName());
            this.loadCommands(builder);
        });
        this.moduleCommand.register();

        config.saveChanges();
    }

    @Override
    protected final void onShutdown() {
        this.disableModule();

        if (this.moduleCommand != null) {
            this.moduleCommand.unregister();
            this.moduleCommand = null;
        }
    }

    protected abstract void loadModule(@NonNull FileConfig config);

    protected abstract void loadCommands(@NonNull HubNodeBuilder builder);

    protected abstract void disableModule();

    @NonNull
    public String getId() {
        return this.id;
    }

    @NonNull
    public String getName() {
        return this.name;
    }

    @NonNull
    public ModuleDefinition getDefinition() {
        return this.definition;
    }

    @NonNull
    public FileConfig getConfig() {
        return FileConfig.loadOrExtract(this.plugin, this.getLocalPath(), ShopFiles.FILE_MODULE_SETTINGS);
    }

    @Override
    @NonNull
    public Path getPath() {
        return this.plugin.dataPath().resolve(this.id);
    }

    @Override
    @NonNull
    public Path getUIPath() {
        return this.getPath().resolve(ShopFiles.DIR_MENU);
    }

    @NonNull
    @Deprecated
    public final String getLocalPath() {
        return this.id;
    }

    @NonNull
    @Deprecated
    public final String getLocalPathTo(@NonNull String dir) {
        return this.id + dir;
    }

    @NonNull
    @Deprecated
    public final String getMenusPath() {
        return this.getLocalPath() + Config.DIR_MENU;
    }

    @NonNull
    @Deprecated
    public final String getAbsolutePath() {
        return this.plugin.getDataFolder() + "/" + this.getLocalPath();
    }

    @Override
    public boolean isEnabledCurrency(@NonNull Currency currency) {
        return this.isEnabledCurrency(currency.getInternalId());
    }

    @Override
    public boolean isEnabledCurrency(@NonNull String id) {
        return this.definition.getDefaultCurrency().equalsIgnoreCase(id) ||
            this.definition.getEnabledCurrencies().contains(id) ||
            this.definition.getEnabledCurrencies().contains(ShopPlaceholders.WILDCARD);
    }

    @Override
    @NonNull
    public Currency getDefaultCurrency() {
        return EconomyBridge.api().getCurrencyOrDummy(this.definition.getDefaultCurrency());
    }

    @Override
    @NonNull
    public Set<Currency> getEnabledCurrencies() {
        if (this.definition.getEnabledCurrencies().contains(ShopPlaceholders.WILDCARD)) return EconomyBridge.api().getCurrencies();

        Set<Currency> currencies = this.definition.getEnabledCurrencies().stream()
            .map(EconomyBridge.api()::getCurrency)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(HashSet::new));

        currencies.add(this.getDefaultCurrency());
        currencies.removeIf(currency -> currency == null || currency.isDummy());

        return currencies;
    }

    @Override
    public boolean isAvailableCurrency(@NonNull Player player, @NonNull Currency currency) {
        if (!this.isEnabledCurrency(currency)) return false;
        if (!Config.CURRENCY_NEED_PERMISSION.get()) return true;

        return this.canUseCurrency(player, currency);
    }

    @Override
    @NonNull
    public Set<Currency> getAvailableCurrencies(@NonNull Player player) {
        Set<Currency> currencies = getEnabledCurrencies();
        if (!Config.CURRENCY_NEED_PERMISSION.get()) return currencies;

        return currencies.stream().filter(currency -> this.canUseCurrency(player, currency)).collect(Collectors.toSet());
    }

    public boolean canUseCurrency(@NonNull Player player, @NonNull Currency currency) {
        return this.isDefaultCurrency(currency) || ShopUtils.hasCurrencyPermission(player, currency);
    }

    @Override
    public boolean isDefaultCurrency(@NonNull Currency currency) {
        return this.isDefaultCurrency(currency.getInternalId());
    }

    @Override
    public boolean isDefaultCurrency(@NonNull String id) {
        return this.definition.getDefaultCurrency().equalsIgnoreCase(id);
    }

    public boolean isItemProvidersDisabled() {
        return this.isItemProviderDisabled(ShopPlaceholders.WILDCARD);
    }

    public boolean isItemProviderDisabled(@NonNull ItemAdapter<?> adapter) {
        return this.isItemProviderDisabled(adapter.getName()) || this.isItemProvidersDisabled();
    }

    public boolean isItemProviderDisabled(@NonNull String id) {
        return this.definition.getDisabledItemProviders().contains(id);
    }

    @Override
    public boolean isItemProviderAllowed(@NonNull ItemAdapter<?> adapter) {
        return !this.isItemProviderDisabled(adapter);
    }

    @Override
    public boolean isItemProviderAllowed(@NonNull String id) {
        return !this.isItemProviderDisabled(id);
    }

    @NonNull
    private String buildLog(@NonNull String msg) {
        return "[" + this.getName() + "] " + msg;
    }

    public final void info(@NonNull String msg) {
        this.plugin.info(this.buildLog(msg));
    }

    public final void warn(@NonNull String msg) {
        this.plugin.warn(this.buildLog(msg));
    }

    public final void error(@NonNull String msg) {
        this.plugin.error(this.buildLog(msg));
    }

    @NonNull
    public LangMessage getPrefixed(@NonNull MessageLocale locale) {
        return locale.withPrefix(this.definition.getPrefix());
    }

    public void sendPrefixed(@NonNull MessageLocale locale, @NonNull CommandSender sender) {
        this.getPrefixed(locale).send(sender);
    }

    public void sendPrefixed(@NonNull MessageLocale locale, @NonNull CommandSender sender, @Nullable Consumer<PlaceholderContext.Builder> consumer) {
        this.getPrefixed(locale).sendWith(sender, consumer);
    }

    public void sendPrefixed(@NonNull MessageLocale locale, @NonNull CommandSender sender, @Nullable PlaceholderContext context) {
        this.getPrefixed(locale).sendWith(sender, context);
    }

    public void sendPrefixed(@NonNull MessageLocale locale, @NonNull Collection<? extends CommandSender> receivers) {
        this.getPrefixed(locale).send(receivers);
    }

    public void sendPrefixed(@NonNull MessageLocale locale, @NonNull Collection<? extends CommandSender> receivers, @Nullable Consumer<PlaceholderContext.Builder> consumer) {
        this.getPrefixed(locale).sendWith(receivers, consumer);
    }

    public void sendPrefixed(@NonNull MessageLocale locale, @NonNull Collection<? extends CommandSender> receivers, @Nullable PlaceholderContext context) {
        this.getPrefixed(locale).sendWith(receivers, context);
    }

    public void broadcastPrefixed(@NonNull MessageLocale locale) {
        this.getPrefixed(locale).broadcast();
    }

    public void broadcastPrefixed(@NonNull MessageLocale locale, @Nullable Consumer<PlaceholderContext.Builder> consumer) {
        this.getPrefixed(locale).broadcastWith(consumer);
    }

    public void broadcastPrefixed(@NonNull MessageLocale locale, @Nullable PlaceholderContext context) {
        this.getPrefixed(locale).broadcastWith(context);
    }
}
