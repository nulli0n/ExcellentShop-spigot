package su.nightexpress.nexshop.api.module;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.module.ModuleSettings;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.Set;

public interface Module {

    void setup();

    void shutdown();

    @NotNull String getId();

    @NotNull String getName();

    @NotNull ModuleSettings getSettings();

    @NotNull FileConfig getConfig();

    @NotNull String getLocalPath();

    @NotNull String getAbsolutePath();

    @NotNull Currency getDefaultCurrency();

    @NotNull Set<Currency> getEnabledCurrencies();

    boolean isDefaultCurrency(@NotNull Currency currency);

    boolean isDefaultCurrency(@NotNull String id);

    boolean isEnabledCurrency(@NotNull String currencyId);

    boolean isEnabledCurrency(@NotNull Currency currency);

    boolean isAvailableCurrency(@NotNull Player player, @NotNull Currency currency);

    @NotNull Set<Currency> getAvailableCurrencies(@NotNull Player player);

    boolean isItemProvidersDisabled();

    boolean isItemProviderDisabled(@NotNull ItemAdapter<?> adapter);

    boolean isItemProviderDisabled(@NotNull String id);

    boolean isItemProviderAllowed(@NotNull ItemAdapter<?> adapter);

    boolean isItemProviderAllowed(@NotNull String id);

    void info(@NotNull String msg);

    void warn(@NotNull String msg);

    void error(@NotNull String msg);
}
