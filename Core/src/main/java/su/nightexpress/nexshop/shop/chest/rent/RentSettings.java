package su.nightexpress.nexshop.shop.chest.rent;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.integration.currency.CurrencyId;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;

import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

public class RentSettings implements Writeable {

    private boolean enabled;
    private int     duration;
    private String  currencyId;
    private double  price;

    public RentSettings(boolean enabled, int duration, @NotNull String currencyId, double price) {
        this.enabled = enabled;
        this.duration = duration;
        this.currencyId = currencyId;
        this.price = price;
    }

    @NotNull
    public static RentSettings read(@NotNull FileConfig config, @NotNull String path) {
        boolean enabled = config.getBoolean(path + ".Enabled", false);
        int duration = config.getInt(path + ".Duration", 7);
        String currencyId = config.getString(path + ".Currency", CurrencyId.VAULT);
        double price = config.getDouble(path + ".Price", 1000D);

        return new RentSettings(enabled, duration, currencyId, price);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Enabled", this.enabled);
        config.set(path + ".Duration", this.duration);
        config.set(path + ".Currency", this.currencyId);
        config.set(path + ".Price", this.price);
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholder() {
        return Placeholders.RENT_SETTINGS.replacer(this);
    }

    public long getDurationMillis() {
        return TimeUnit.DAYS.toMillis(this.duration);
    }

    public boolean isValid() {
        return !this.getCurrency().isDummy();
    }

    @NotNull
    public Currency getCurrency() {
        return EconomyBridge.getCurrencyOrDummy(this.currencyId);
    }

    @NotNull
    public String getCurrencyName() {
        return this.getCurrency().getName();
    }

    @NotNull
    public String getPriceFormatted() {
        return this.getCurrency().format(this.price);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration, boolean limited) {
        if (limited) {
            int limit = ChestUtils.getRentDurationLimit();
            if (limit > 0 && duration > limit) {
                duration = limit;
            }
        }

        this.duration = Math.max(1, duration);
    }

    @NotNull
    public String getCurrencyId() {
        return this.currencyId;
    }

    public void setCurrencyId(@NotNull String currencyId) {
        this.currencyId = currencyId;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price, boolean limited) {
        Currency currency = this.getCurrency();

        if (limited) {
            double limit = ChestUtils.getRentPriceLimit(currency);
            if (limit > 0 && price > 0) {
                price = limit;
            }
        }

        price = currency.floorIfNeeded(price);

        this.price = Math.max(0, price);
    }
}
