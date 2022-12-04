package su.nightexpress.nexshop.currency.external;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.gamepoints.api.GamePointsAPI;
import su.nightexpress.gamepoints.data.PointUser;
import su.nightexpress.nexshop.api.currency.AbstractCurrency;
import su.nightexpress.nexshop.api.currency.ICurrencyConfig;
import su.nightexpress.nexshop.api.currency.SingleCurrency;

public class GamePointsCurrency extends AbstractCurrency implements SingleCurrency {

    public GamePointsCurrency(@NotNull ICurrencyConfig config) {
        super(config);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        PointUser user = GamePointsAPI.getUserData(player.getUniqueId());
        return user == null ? 0 : user.getBalance();
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        PointUser user = GamePointsAPI.getUserData(player.getUniqueId());
        if (user != null) user.addPoints((int) amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        PointUser user = GamePointsAPI.getUserData(player.getUniqueId());
        if (user != null) user.takePoints((int) amount);
    }
}
