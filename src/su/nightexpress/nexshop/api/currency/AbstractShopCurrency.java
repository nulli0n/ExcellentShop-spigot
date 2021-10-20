package su.nightexpress.nexshop.api.currency;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;

public abstract class AbstractShopCurrency implements IShopCurrency {

    private final String id;
    private final String name;
    private final String format;

    public AbstractShopCurrency(@NotNull String id, @NotNull String name, @NotNull String format) {
        this.id = id.toLowerCase();
        this.name = StringUT.color(name);
        this.format = StringUT.color(format);
    }

    @Override
    @NotNull
    public final String getId() {
        return this.id;
    }

    @Override
    @NotNull
    public final String getName() {
        return this.name;
    }

    @NotNull
    public String getFormat() {
        return this.replacePlaceholders().apply(this.format);
    }

    @Override
    @NotNull
    public String format(double price) {
        return this.getFormat().replace(PLACEHOLDER_PRICE, NumberUT.formatGroup(price));
    }
}
