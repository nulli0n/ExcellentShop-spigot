package su.nightexpress.nexshop.currency.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.data.impl.CoinsUser;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;
import su.nightexpress.nexshop.api.currency.CurrencyOfflineHandler;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CoinsEngineCurrency implements Currency, CurrencyHandler, CurrencyOfflineHandler {

    private final su.nightexpress.coinsengine.api.currency.Currency currency;

    public CoinsEngineCurrency(@NotNull su.nightexpress.coinsengine.api.currency.Currency currency) {
        this.currency = currency;
    }

    @NotNull
    public static Set<CoinsEngineCurrency> getCurrencies() {
        Set<CoinsEngineCurrency> currencies = new HashSet<>();
        CoinsEngineAPI.getCurrencyManager().getCurrencies().forEach(cura -> {
            if (!cura.isVaultEconomy()) {
                currencies.add(new CoinsEngineCurrency(cura));
            }
        });
        return currencies;
    }

    @Override
    @NotNull
    public String getDefaultName() {
        return this.getName();
    }

    @Override
    @NotNull
    public ItemStack getDefaultIcon() {
        return this.currency.getIcon();
    }

    @Override
    @NotNull
    public String formatValue(double price) {
        return this.currency.formatValue(price);
    }

    @Override
    @NotNull
    public CurrencyHandler getHandler() {
        return this;
    }

    @Override
    @NotNull
    public String getId() {
        return "coinsengine_" + this.currency.getId();
    }

    @Override
    @NotNull
    public String getName() {
        return this.currency.getName();
    }

    @Override
    @NotNull
    public String getFormat() {
        return this.currency.getFormat();
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return this.currency.getIcon();
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        PlaceholderMap map = new PlaceholderMap();
        this.currency.getPlaceholders().getKeys().forEach(pair -> {
            map.add(pair.getFirst(), pair.getSecond());
        });
        return map;
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return CoinsEngineAPI.getBalance(player, this.currency);
    }

    @Override
    public double getBalance(@NotNull UUID playerId) {
        CoinsUser user = CoinsEngineAPI.getUserData(playerId);
        if (user == null) return 0D;

        return user.getBalance(this.currency);
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        CoinsEngineAPI.addBalance(player, this.currency, amount);
    }

    @Override
    public void give(@NotNull UUID playerId, double amount) {
        CoinsEngineAPI.getUserDataAsync(playerId).thenAccept(user -> {
            if (user == null) return;

            user.addBalance(this.currency, amount);
            CoinsEngineAPI.getUserManager().saveAsync(user);
        });
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        CoinsEngineAPI.removeBalance(player, this.currency, amount);
    }

    @Override
    public void take(@NotNull UUID playerId, double amount) {
        CoinsEngineAPI.getUserDataAsync(playerId).thenAccept(user -> {
            if (user == null) return;

            user.removeBalance(this.currency, amount);
            CoinsEngineAPI.getUserManager().saveAsync(user);
        });
    }
}
