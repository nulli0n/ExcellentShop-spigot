package su.nightexpress.nexshop.currency.impl;

import me.TechsCode.UltraEconomy.UltraEconomy;
import me.TechsCode.UltraEconomy.objects.Account;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

import java.util.Optional;

public class UltraEconomyCurrency implements Currency, CurrencyHandler {

    private final me.TechsCode.UltraEconomy.objects.Currency currency;
    private final PlaceholderMap placeholderMap;

    public UltraEconomyCurrency(me.TechsCode.UltraEconomy.objects.Currency currency) {
        this.currency = currency;
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.CURRENCY_ID, this::getId)
            .add(Placeholders.CURRENCY_NAME, this::getName);
    }


    @Override
    @NotNull
    public CurrencyHandler getHandler() {
        return this;
    }

    @Override
    @NotNull
    public String getId() {
        return ("ultraeconomy_" + currency.getName()).toLowerCase();
    }

    @Override
    @NotNull
    public String getName() {
        return currency.getName().toLowerCase();
    }

    @Override
    @NotNull
    public String getFormat() {
        return currency.getFormat().format(0.0);
    }

    @Override
    @NotNull
    public String format(double price) {
        return currency.getFormat().format(price);
    }

    @Override
    @NotNull
    public String formatValue(double price) {
        return currency.getFormat().format(price);
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return currency.getIcon().getAsItemStack().orElseGet(() -> new ItemStack(Material.GOLD_INGOT));
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
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
