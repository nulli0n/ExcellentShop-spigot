package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.Currency;

import java.util.HashMap;
import java.util.Map;

public abstract class ShopBank<S extends Shop<S, ?>> {

    protected final Map<String, Double> balance;
    protected       S                   shop;

    public ShopBank(@NotNull S shop) {
        this.shop = shop;
        this.balance = new HashMap<>();
    }

    @NotNull
    public S getShop() {
        return shop;
    }

    @NotNull
    public Map<String, Double> getBalance() {
        return balance;
    }

    public abstract boolean deposit(@NotNull Currency currency, double amount);

    public abstract boolean withdraw(@NotNull Currency currency, double amount);

    public abstract double getBalance(@NotNull Currency currency);

    public abstract boolean hasEnough(@NotNull Currency currency, double amount);
}
