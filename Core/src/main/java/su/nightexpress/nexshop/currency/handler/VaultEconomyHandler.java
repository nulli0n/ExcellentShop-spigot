package su.nightexpress.nexshop.currency.handler;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;
import su.nightexpress.nexshop.api.currency.CurrencyOfflineHandler;
import su.nightexpress.nightcore.integration.VaultHook;

import java.util.UUID;

public class VaultEconomyHandler implements CurrencyHandler, CurrencyOfflineHandler {

    public static final String ID = "vault";

    @Override
    @NotNull
    public String getDefaultName() {
        return "Money";
    }

    @Override
    @NotNull
    public String getDefaultFormat() {
        return "$" + Placeholders.GENERIC_PRICE;
    }

    @Override
    @NotNull
    public ItemStack getDefaultIcon() {
        return new ItemStack(Material.GOLD_NUGGET);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return VaultHook.getBalance(player);
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        VaultHook.addMoney(player, amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        VaultHook.takeMoney(player, amount);
    }

    @Override
    public double getBalance(@NotNull UUID playerId) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);

        return VaultHook.getBalance(offlinePlayer);
    }

    @Override
    public void give(@NotNull UUID playerId, double amount) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);

        VaultHook.addMoney(offlinePlayer, amount);
    }

    @Override
    public void take(@NotNull UUID playerId, double amount) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);

        VaultHook.takeMoney(offlinePlayer, amount);
    }
}
