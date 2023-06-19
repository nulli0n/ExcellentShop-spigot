package su.nightexpress.nexshop.currency.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class VaultEconomyHandler implements CurrencyHandler {

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
}
