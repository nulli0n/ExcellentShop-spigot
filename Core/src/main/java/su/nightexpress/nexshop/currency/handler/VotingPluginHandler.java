package su.nightexpress.nexshop.currency.handler;

import com.bencodez.votingplugin.VotingPluginHooks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class VotingPluginHandler implements CurrencyHandler {

    public static final String ID = "votingplugin";

    @Override
    @NotNull
    public String getDefaultName() {
        return "Voting Points";
    }

    @Override
    @NotNull
    public ItemStack getDefaultIcon() {
        return new ItemStack(Material.SUNFLOWER);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return VotingPluginHooks.getInstance().getUserManager().getVotingPluginUser(player).getPoints();
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        ShopAPI.PLUGIN.runTaskAsync(task -> {
            VotingPluginHooks.getInstance().getUserManager().getVotingPluginUser(player).addPoints((int) amount);
        });
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        VotingPluginHooks.getInstance().getUserManager().getVotingPluginUser(player).removePoints((int) amount);
    }
}
