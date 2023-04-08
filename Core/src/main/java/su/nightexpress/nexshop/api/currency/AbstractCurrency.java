package su.nightexpress.nexshop.api.currency;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.nexshop.Placeholders;

public abstract class AbstractCurrency implements ICurrency {

    protected final ICurrencyConfig config;
    protected final PlaceholderMap placeholderMap;

    public AbstractCurrency(@NotNull ICurrencyConfig config) {
        this.config = config;
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.CURRENCY_NAME, () -> this.getConfig().getName())
            .add(Placeholders.CURRENCY_ID, () -> this.getConfig().getId())
        ;
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @NotNull
    @Override
    public ICurrencyConfig getConfig() {
        return config;
    }
}
