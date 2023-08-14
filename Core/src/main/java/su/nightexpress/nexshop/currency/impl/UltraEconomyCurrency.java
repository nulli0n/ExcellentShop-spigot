package su.nightexpress.nexshop.currency.impl;

import me.TechsCode.UltraEconomy.UltraEconomy;
import me.TechsCode.UltraEconomy.objects.Account;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

import java.util.Optional;

public class UltraEconomyCurrency implements Currency, CurrencyHandler {

    private final me.TechsCode.UltraEconomy.objects.Currency currency;

    public UltraEconomyCurrency(me.TechsCode.UltraEconomy.objects.Currency currency) {
        this.currency = currency;
    }


    @Override
    public @NotNull CurrencyHandler getHandler() {
        return this;
    }

    @Override
    public @NotNull String getId() {
        return ("ultraeconomy_" + currency.getName()).toLowerCase();
    }

    @Override
    public @NotNull String getName() {
        return currency.getName().toLowerCase();
    }

    @Override
    public @NotNull String getFormat() {
        return currency.getFormat().format(0.0);
    }

    @Override
    public @NotNull String format(double price) {
        return currency.getFormat().format(price);
    }

    @Override
    public @NotNull String formatValue(double price) {
        return currency.getFormat().format(price);
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return currency.getIcon().getAsItemStack().orElseGet(() -> new ItemStack(Material.GOLD_INGOT));
    }

    @Override
    public @NotNull PlaceholderMap getPlaceholders() {
        return new PlaceholderMap();
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return getAccount(player).map(value -> value.getBalance(currency).getOnHand()).orElse(0.0);
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        getAccount(player).ifPresent(value -> value.getBalance(currency).addHand(amount));
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        getAccount(player).ifPresent(value -> value.getBalance(currency).removeHand(amount));
    }

    private Optional<Account> getAccount(@NotNull Player player) {
        return UltraEconomy.getInstance().getAccounts().name(player.getName());
    }
}
