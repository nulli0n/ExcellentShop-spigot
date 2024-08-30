package su.nightexpress.nexshop.currency.handler;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class PlayerXPHandler implements CurrencyHandler {

    public static final String ID = "xp";

    @Override
    @NotNull
    public String getDefaultName() {
        return "XP";
    }

    @Override
    @NotNull
    public ItemStack getDefaultIcon() {
        return new ItemStack(Material.EXPERIENCE_BOTTLE);
    }

    private void modify(@NotNull Player player, double amount) {
        player.giveExp((int) amount);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return player.getTotalExperience();
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        this.modify(player, amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        this.modify(player, -amount);
    }
}
