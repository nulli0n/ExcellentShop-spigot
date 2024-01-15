package su.nightexpress.nexshop.currency.handler;

import com.bencodez.votingplugin.VotingPluginHooks;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class VotingPluginHandler implements CurrencyHandler {

    @Override
    public double getBalance(@NotNull Player player) {
        return VotingPluginHooks.getInstance().getUserManager().getVotingPluginUser(player).getPoints();
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        VotingPluginHooks.getInstance().getUserManager().getVotingPluginUser(player).addPoints((int) amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        VotingPluginHooks.getInstance().getUserManager().getVotingPluginUser(player).removePoints((int) amount);
    }
}
