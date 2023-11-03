package su.nightexpress.nexshop.currency.handler;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;
import su.nightexpress.nexshop.api.currency.CurrencyOfflineHandler;

import java.util.UUID;

public class GemsEconomyHandler implements CurrencyHandler, CurrencyOfflineHandler {

    private final me.xanium.gemseconomy.currency.Currency currency;

    public GemsEconomyHandler(@NotNull me.xanium.gemseconomy.currency.Currency currency) {
        this.currency = currency;
    }

    @Override
    public double getBalance(@NotNull Player player) {
        Account account = GemsEconomy.getInstance().getAccountManager().getAccount(player);
        validateAccount(account, player);
        return account.getBalance(this.currency);
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        Account account = GemsEconomy.getInstance().getAccountManager().getAccount(player);
        validateAccount(account, player);
        account.deposit(this.currency, amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        Account account = GemsEconomy.getInstance().getAccountManager().getAccount(player);
        validateAccount(account, player);
        account.withdraw(this.currency, amount);
    }

    @Override
    public double getBalance(@NotNull UUID playerId) {
        Account account = GemsEconomy.getInstance().getAccountManager().getAccount(playerId);
        validateAccount(account, playerId);
        return account.getBalance(this.currency);
    }

    @Override
    public void give(@NotNull UUID playerId, double amount) {
        Account account = GemsEconomy.getInstance().getAccountManager().getAccount(playerId);
        validateAccount(account, playerId);
        account.deposit(this.currency, amount);
    }

    @Override
    public void take(@NotNull UUID playerId, double amount) {
        Account account = GemsEconomy.getInstance().getAccountManager().getAccount(playerId);
        validateAccount(account, playerId);
        account.withdraw(this.currency, amount);
    }

    private <T> void validateAccount(T account, Player player) {
        if (account == null) {
            throw new IllegalStateException("Cannot find GemsEconomy account for player: " + player.getName() + ". "
                + "Please report this error to the author of GemsEconomy at: "
                + "https://github.com/Ghost-chu/GemsEconomy/issues");
        }
    }

    private <T> void validateAccount(T account, UUID player) {
        if (account == null) {
            throw new IllegalStateException("Cannot find GemsEconomy account for player: " + player + ". "
                + "Please report this error to the author of GemsEconomy at: "
                + "https://github.com/Ghost-chu/GemsEconomy/issues");
        }
    }
}
