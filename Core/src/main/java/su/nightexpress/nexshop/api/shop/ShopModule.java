package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.api.currency.Currency;

public interface ShopModule {

    @NotNull String getId();

    @NotNull String getName();

    @NotNull JYML getConfig();

    @NotNull String getLocalPath();

    @NotNull String getAbsolutePath();

    @NotNull Currency getDefaultCurrency();

    @NotNull TransactionLogger getLogger();

    void info(@NotNull String msg);

    void warn(@NotNull String msg);

    void error(@NotNull String msg);
}
