package su.nightexpress.nexshop.currency.handler;

import com.magmaguy.elitemobs.economy.EconomyHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class EliteMobsHandler implements CurrencyHandler {

    public static final String ID = "elitemobs";

    @Override
    @NotNull
    public String getDefaultName() {
        return "Elite Currency";
    }

    @Override
    @NotNull
    public ItemStack getDefaultIcon() {
        return new ItemStack(Material.EMERALD);
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
