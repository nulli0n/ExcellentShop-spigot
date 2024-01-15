package su.nightexpress.nexshop.currency.handler;

import me.mraxetv.beasttokens.api.BeastTokensAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class BeastTokensHandler implements CurrencyHandler {

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
