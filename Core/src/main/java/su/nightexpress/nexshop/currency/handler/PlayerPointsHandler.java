package su.nightexpress.nexshop.currency.handler;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class PlayerPointsHandler implements CurrencyHandler {

    public static final String ID = "playerpoints";

    private final PlayerPointsAPI api;

    public PlayerPointsHandler() {
        this.api = PlayerPoints.getInstance().getAPI();
    }

    @Override
    @NotNull
    public String getDefaultName() {
        return "Points";
    }

    @Override
    @NotNull
    public ItemStack getDefaultIcon() {
        return new ItemStack(Material.SUNFLOWER);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return api.look(player.getUniqueId());
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        api.give(player.getUniqueId(), (int) amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        api.take(player.getUniqueId(), (int) amount);
    }
}
