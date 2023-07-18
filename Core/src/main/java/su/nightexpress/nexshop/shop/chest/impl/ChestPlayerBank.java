package su.nightexpress.nexshop.shop.chest.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.Currency;

import java.util.Map;
import java.util.UUID;

public class ChestPlayerBank {

    private final UUID holder;
    private final Map<Currency, Double> balanceMap;

    public ChestPlayerBank(@NotNull UUID holder, @NotNull Map<Currency, Double> balanceMap) {
        this.holder = holder;
        this.balanceMap = balanceMap;
    }

    @NotNull
    public UUID getHolder() {
        return holder;
    }

    @NotNull
    public Map<Currency, Double> getBalanceMap() {
        return balanceMap;
    }

    public boolean deposit(@NotNull Currency currency, double amount) {
        if (amount <= 0) return false;

        double balance = this.getBalance(currency) + amount;
        this.getBalanceMap().put(currency, balance);
        return true;
    }

    public boolean withdraw(@NotNull Currency currency, double amount) {
        if (!this.hasEnough(currency, amount)) return false;

        double balance = this.getBalance(currency) - amount;
        this.getBalanceMap().put(currency, balance);
        return true;
    }

    public double getBalance(@NotNull Currency currency) {
        return this.getBalanceMap().getOrDefault(currency, 0D);
    }

    public boolean hasEnough(@NotNull Currency currency, double amount) {
        return this.getBalance(currency) >= amount;
    }
}
