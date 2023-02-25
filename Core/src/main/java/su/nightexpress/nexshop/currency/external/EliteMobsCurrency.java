package su.nightexpress.nexshop.currency.external;


import com.magmaguy.elitemobs.economy.EconomyHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.AbstractCurrency;
import su.nightexpress.nexshop.api.currency.ICurrencyConfig;
import su.nightexpress.nexshop.api.currency.SingleCurrency;

public class EliteMobsCurrency extends AbstractCurrency implements SingleCurrency {

    public EliteMobsCurrency(@NotNull ICurrencyConfig config) {
        super(config);
    }

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
