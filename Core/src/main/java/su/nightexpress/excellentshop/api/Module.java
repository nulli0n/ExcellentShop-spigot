package su.nightexpress.excellentshop.api;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nexshop.module.ModuleDefinition;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;
import java.util.Set;

public interface Module {

    void setup();

    void shutdown();

    void onDataLoadFinished();

    @NonNull String getId();

    @NonNull String getName();

    @NonNull ModuleDefinition getDefinition();

    @NonNull FileConfig getConfig();

    /**
     * 
     * @return Full path to the module directory.
     */
    @NonNull Path getPath();

    @NonNull Path getUIPath();
    
    @Deprecated
    @NonNull String getLocalPath();

    @Deprecated
    @NonNull String getAbsolutePath();

    @NonNull Currency getDefaultCurrency();

    @NonNull Set<Currency> getEnabledCurrencies();

    boolean isDefaultCurrency(@NonNull Currency currency);

    boolean isDefaultCurrency(@NonNull String id);

    boolean isEnabledCurrency(@NonNull String currencyId);

    boolean isEnabledCurrency(@NonNull Currency currency);

    boolean isAvailableCurrency(@NonNull Player player, @NonNull Currency currency);

    @NonNull Set<Currency> getAvailableCurrencies(@NonNull Player player);

    boolean isItemProvidersDisabled();

    boolean isItemProviderDisabled(@NonNull ItemAdapter<?> adapter);

    boolean isItemProviderDisabled(@NonNull String id);

    boolean isItemProviderAllowed(@NonNull ItemAdapter<?> adapter);

    boolean isItemProviderAllowed(@NonNull String id);

    void info(@NonNull String msg);

    void warn(@NonNull String msg);

    void error(@NonNull String msg);
}
