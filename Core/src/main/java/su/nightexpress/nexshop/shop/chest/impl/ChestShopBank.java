package su.nightexpress.nexshop.shop.chest.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.ShopBank;

public class ChestShopBank extends ShopBank<ChestShop> {

    public ChestShopBank(@NotNull ChestShop shop) {
        super(shop);
    }

    @Override
    public boolean deposit(@NotNull ICurrency currency, double amount) {
        if (!this.getShop().isAdminShop()) {
            this.getBalance().put(currency.getId(), this.getBalance(currency) + Math.abs(amount));
        }
        return true;
    }

    @Override
    public boolean withdraw(@NotNull ICurrency currency, double amount) {
        if (!this.hasEnough(currency, amount)) return false;
        if (!this.getShop().isAdminShop()) {
            this.getBalance().put(currency.getId(), this.getBalance(currency) - Math.abs(amount));
        }
        return true;
    }

    @Override
    public double getBalance(@NotNull ICurrency currency) {
        return this.getShop().isAdminShop() ? -1D : this.getBalance().getOrDefault(currency.getId(), 0D);
    }

    @Override
    public boolean hasEnough(@NotNull ICurrency currency, double amount) {
        return this.getBalance(currency) < 0D || this.getBalance(currency) >= amount;
    }
}
