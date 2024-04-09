package su.nightexpress.nexshop.api.currency;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;

public interface CurrencyHandler {

    @NotNull String getDefaultName();

    @NotNull
    default String getDefaultFormat() {
        return Placeholders.GENERIC_PRICE + " " + Placeholders.GENERIC_NAME;
    }

    @NotNull ItemStack getDefaultIcon();

    double getBalance(@NotNull Player player);

    void give(@NotNull Player player, double amount);

    void take(@NotNull Player player, double amount);
}
