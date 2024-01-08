package su.nightexpress.nexshop.currency.handler;

import com.magmaguy.elitemobs.economy.EconomyHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class EliteMobsHandler implements CurrencyHandler {

    @Override
    public double getBalance(@NotNull Player player) {
        return EconomyHandler.checkCurrency(player.getUniqueId());
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        EconomyHandler.addCurrency(player.getUniqueId(), amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        EconomyHandler.subtractCurrency(player.getUniqueId(), amount);
    }
}
