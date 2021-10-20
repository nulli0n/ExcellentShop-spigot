package su.nightexpress.nexshop.currency.object;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.hooks.external.VaultHK;
import su.nightexpress.nexshop.api.currency.AbstractShopCurrency;
import su.nightexpress.nexshop.currency.CurrencyType;

public class VaultCurrency extends AbstractShopCurrency {

    private final VaultHK vault;

    public VaultCurrency(@NotNull String name, @NotNull String format, @NotNull VaultHK vaultHook) {
        super(CurrencyType.VAULT, name, format);
        this.vault = vaultHook;
    }

    @Override
    public boolean hasOfflineSupport() {
        return true;
    }

    @Override
    public double getBalance(@NotNull OfflinePlayer player) {
        return this.vault.getBalance(player);
    }

    @Override
    public void give(@NotNull OfflinePlayer player, double amount) {
        this.vault.give(player, amount);
    }

    @Override
    public void take(@NotNull OfflinePlayer player, double amount) {
        this.vault.take(player, amount);
    }
}
