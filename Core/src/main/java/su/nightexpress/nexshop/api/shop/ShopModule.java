package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nightcore.config.FileConfig;

public interface ShopModule {

    @NotNull String getId();

    @NotNull String getName();

    @NotNull FileConfig getConfig();

    @NotNull String getLocalPath();

    @NotNull String getAbsolutePath();

    @NotNull Currency getDefaultCurrency();

    void info(@NotNull String msg);

    void warn(@NotNull String msg);

    void error(@NotNull String msg);
}
