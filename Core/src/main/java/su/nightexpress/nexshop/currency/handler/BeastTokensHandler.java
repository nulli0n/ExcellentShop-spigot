package su.nightexpress.nexshop.currency.handler;

import me.mraxetv.beasttokens.api.BeastTokensAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class BeastTokensHandler implements CurrencyHandler {

    public static final String ID = "beasttokens";

    @Override
    @NotNull
    public String getDefaultName() {
        return "Tokens";
    }

    @Override
    @NotNull
    public ItemStack getDefaultIcon() {
        return new ItemStack(Material.SUNFLOWER);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return BeastTokensAPI.getTokensManager().getTokens(player);
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        BeastTokensAPI.getTokensManager().addTokens(player, amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        BeastTokensAPI.getTokensManager().removeTokens(player, amount);
    }
}
