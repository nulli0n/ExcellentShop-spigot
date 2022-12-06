package su.nightexpress.nexshop.currency.external;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.currency.AbstractCurrency;
import su.nightexpress.nexshop.api.currency.MultiCurrency;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.currency.config.CurrencyConfig;

import java.util.Locale;

/**
 * This class should be instantiated with a currency identifier in GemsEconomy.
 */
public class GemsEconomyCurrency extends AbstractCurrency implements MultiCurrency {

    private final String identifier;

    public GemsEconomyCurrency(@NotNull String identifier) {
        super(loadOrCreateConfig(identifier));
        this.identifier = identifier;
    }

    private static CurrencyConfig loadOrCreateConfig(String identifier) {
        JYML jyml = JYML.loadOrExtract(ShopAPI.PLUGIN, CurrencyManager.DIR_DEFAULT
                                                       + "gemseconomy:"
                                                       + identifier.toLowerCase(Locale.ROOT)
                                                       + ".yml");
        if (!jyml.isSet("Name")) jyml.set("Name", "GemsEconomy : " + identifier.toUpperCase(Locale.ROOT));
        CurrencyConfig config = new CurrencyConfig(ShopAPI.PLUGIN, jyml);
        config.save();
        return config;
    }

    @Override
    public double getBalance(@NotNull Player player) {
        Currency gemsCurrency = GemsEconomy.getInstance().getCurrencyManager().getCurrency(this.identifier);
        Account account = GemsEconomy.getInstance().getAccountManager().getAccount(player);
        validateCurrency(gemsCurrency);
        validateAccount(account, player);
        return account.getBalance(gemsCurrency);
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        Currency gemsCurrency = GemsEconomy.getInstance().getCurrencyManager().getCurrency(this.identifier);
        Account account = GemsEconomy.getInstance().getAccountManager().getAccount(player);
        validateCurrency(gemsCurrency);
        validateAccount(account, player);
        account.deposit(gemsCurrency, amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        Currency gemsCurrency = GemsEconomy.getInstance().getCurrencyManager().getCurrency(this.identifier);
        Account account = GemsEconomy.getInstance().getAccountManager().getAccount(player);
        validateCurrency(gemsCurrency);
        validateAccount(account, player);
        account.withdraw(gemsCurrency, amount);
    }

    private <T> void validateAccount(T account, Player player) {
        if (account == null) {
            throw new IllegalStateException("Cannot find GemsEconomy account for player: " + player.getName() + ". "
                                            + "Please report this error to the author of GemsEconomy at: "
                                            + "https://github.com/Ghost-chu/GemsEconomy/issues");
        }
    }

    private <T> void validateCurrency(T currency) {
        if (currency == null) {
            throw new IllegalStateException("Cannot find GemsEconomy currency: " + this.identifier + ". "
                                            + "You may have deleted a currency from the GemsEconomy database? "
                                            + "Try to run command `/excellentshop reload` to load the "
                                            + "currencies from GemsEconomy again.");
        }
    }

}
