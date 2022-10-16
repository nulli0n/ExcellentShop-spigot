package su.nightexpress.nexshop.currency.internal;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.AbstractCurrency;
import su.nightexpress.nexshop.api.currency.ICurrencyConfig;

public class ExpCurrency extends AbstractCurrency {

    public ExpCurrency(@NotNull ICurrencyConfig config) {
        super(config);
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return getExpTotal(player);
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        setExp(player, (int) (getExpTotal(player) + amount));
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        setExp(player, (int) (getExpTotal(player) - amount));
    }

    private void setExp(@NotNull Player player, int amount) {
        player.setExp(0F);
        player.setLevel(0);
        player.setTotalExperience(0);

        amount = Math.max(0, amount);
        while (amount > 0) {
            int expToLevel = getExpRequired(player);
            if (amount - expToLevel >= 0) {
                player.giveExp(expToLevel);
                amount -= expToLevel;
                continue;
            }
            player.giveExp(amount);
            break;
        }
    }

    private int getExpRequired(@NotNull Player player) {
        return getExpRequired(player.getLevel());
    }

    /*private static int getExpRequiredLeft(@NotNull Player player) {
        int exp = Math.round(getExpRequired(player) * player.getExp());
        int level = player.getLevel();
        return getExpRequired(level) - exp;
    }*/

    private int getExpRequired(int levelCurrent) {
        if (levelCurrent <= 15) return 2 * levelCurrent + 7;
        if (levelCurrent <= 30) return 5 * levelCurrent - 38;
        return 9 * levelCurrent - 158;
    }

    private int getExpTotal(@NotNull Player player) {
        int exp = Math.round(getExpRequired(player) * player.getExp());
        int currentLevel = player.getLevel();
        while (currentLevel > 0) {
            exp += getExpRequired(--currentLevel);
        }
        return Math.max(0, exp);
    }

    private int getExpRequiredTotal(int level) {
        int currentLevel = 0;
        int exp = 0;
        while (currentLevel < level) {
            exp += getExpRequired(currentLevel++);
        }
        return Math.max(0, exp);
    }
}
