package su.nightexpress.nexshop.api.currency;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractCurrency implements ICurrency {

    protected final ICurrencyConfig config;

    public AbstractCurrency(@NotNull ICurrencyConfig config) {
        this.config = config;
    }

    @NotNull
    @Override
    public ICurrencyConfig getConfig() {
        return config;
    }
}
