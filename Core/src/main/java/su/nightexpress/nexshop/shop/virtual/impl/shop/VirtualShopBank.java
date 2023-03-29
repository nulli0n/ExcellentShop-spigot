package su.nightexpress.nexshop.shop.virtual.impl.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.ShopBank;

public class VirtualShopBank extends ShopBank<VirtualShop> {

    public VirtualShopBank(@NotNull VirtualShop shop) {
        super(shop);
    }

    @Override
    public boolean deposit(@NotNull ICurrency currency, double amount) {
        return true;
    }

    @Override
    public boolean withdraw(@NotNull ICurrency currency, double amount) {
        return true;
    }

    @Override
    public double getBalance(@NotNull ICurrency currency) {
        return -1;
    }

    @Override
    public boolean hasEnough(@NotNull ICurrency currency, double amount) {
        return true;
    }
}
