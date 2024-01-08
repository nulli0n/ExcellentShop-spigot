package su.nightexpress.nexshop.currency.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class DummyCurrency implements Currency, CurrencyHandler {

    @Override
    @NotNull
    public CurrencyHandler getHandler() {
        return this;
    }

    @Override
    @NotNull
    public String getId() {
        return "dummy";
    }

    @Override
    @NotNull
    public String getName() {
        return "Dummy";
    }

    @Override
    @NotNull
    public String getFormat() {
        return Placeholders.GENERIC_AMOUNT + " Dummy";
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        return new ItemStack(Material.BRICK);
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return new PlaceholderMap();
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return 0;
    }

    @Override
    public void give(@NotNull Player player, double amount) {

    }

    @Override
    public void take(@NotNull Player player, double amount) {

    }
}
