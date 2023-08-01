package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;

import java.util.Map;
import java.util.UUID;

public class ChestPlayerBank {

    private final UUID holder;
    private final Map<Currency, Double> balanceMap;

    public ChestPlayerBank(@NotNull UUID holder, @NotNull Map<Currency, Double> balanceMap) {
        this.holder = holder;
        this.balanceMap = balanceMap;
    }

    @Nullable
    public Player getOnlinePlayer() {
        return Bukkit.getPlayer(this.getHolder());
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

        Player player = this.getOnlinePlayer();
        if (player != null && ChestConfig.SHOP_AUTO_BANK.get()) {
            currency.getHandler().give(player, amount);
            return true;
        }

        double balance = this.getBalance(currency) + amount;
        this.getBalanceMap().put(currency, balance);
        return true;
    }

    public boolean withdraw(@NotNull Currency currency, double amount) {
        if (!this.hasEnough(currency, amount)) return false;

        Player player = this.getOnlinePlayer();
        if (player != null && ChestConfig.SHOP_AUTO_BANK.get()) {
            currency.getHandler().take(player, amount);
            return true;
        }

        double balance = this.getBalance(currency) - amount;
        this.getBalanceMap().put(currency, balance);
        return true;
    }

    public double getBalance(@NotNull Currency currency) {
        return this.getBalanceMap().getOrDefault(currency, 0D);
    }

    public boolean hasEnough(@NotNull Currency currency, double amount) {
        Player player = this.getOnlinePlayer();
        if (player != null && ChestConfig.SHOP_AUTO_BANK.get()) {
            return currency.getHandler().getBalance(player) >= amount;
        }

        return this.getBalance(currency) >= amount;
    }
}
