package su.nightexpress.nexshop.currency.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import su.nightexpress.nexshop.api.currency.AbstractCurrency;
import su.nightexpress.nexshop.api.currency.ICurrencyConfig;
import su.nightexpress.nexshop.api.currency.SingleCurrency;

public class EliteMobsCurrency extends AbstractCurrency implements SingleCurrency {

    public EliteMobsCurrency(@NotNull ICurrencyConfig config) {
        super(config);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        double points = EconomyHandler.checkCurrency(player.getUniqueId());
        return points;
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
