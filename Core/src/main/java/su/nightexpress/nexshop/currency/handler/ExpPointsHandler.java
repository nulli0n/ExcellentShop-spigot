package su.nightexpress.nexshop.currency.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

import java.util.function.BiFunction;

public class ExpPointsHandler implements CurrencyHandler {

    private int getExpRequired(int level) {
        if (level <= 15) return 2 * level + 7;
        if (level <= 30) return 5 * level - 38;
        return 9 * level - 158;
    }

    private void modify(@NotNull Player player, double amount1, @NotNull BiFunction<Integer, Integer, Integer> f) {
        int amount = (int) amount1;
        int levelHas = player.getLevel();
        //int expHas = player.getTotalExperience();

        //if (result.hasFlag(FLAG_LEVEL)) {
        int levelFinal = Math.max(0, f.apply(levelHas, amount));
        int expBasic = 0;
        int expLeft = (int) (this.getExpRequired(levelFinal) * player.getExp());
        for (int level = 0; level < levelFinal; level++) {
            expBasic += (this.getExpRequired(level));
        }
        int expHas = expBasic + expLeft;
        /*}
        else {
            expHas = Math.max(0, mode.modify(expHas, amount));
        }*/
        player.setExp(0F);
        player.setTotalExperience(0);
        player.setLevel(0);
        player.giveExp(expHas);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return player.getTotalExperience();
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        this.modify(player, amount, Integer::sum);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        this.modify(player, amount, (levelHas, levelTake) -> levelHas - levelTake);
    }
}
