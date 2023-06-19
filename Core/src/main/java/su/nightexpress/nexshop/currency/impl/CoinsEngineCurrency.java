package su.nightexpress.nexshop.currency.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

import java.util.HashSet;
import java.util.Set;

public class CoinsEngineCurrency implements Currency, CurrencyHandler {

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
    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.RAW_GOLD);
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.currency.getPlaceholders();
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return CoinsEngineAPI.getBalance(player, this.currency);
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        CoinsEngineAPI.addBalance(player, this.currency, amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        CoinsEngineAPI.removeBalance(player, this.currency, amount);
    }
}
