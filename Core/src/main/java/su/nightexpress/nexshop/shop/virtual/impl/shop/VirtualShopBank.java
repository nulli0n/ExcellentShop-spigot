package su.nightexpress.nexshop.shop.virtual.impl.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.ShopBank;

public class VirtualShopBank extends ShopBank<VirtualShop> {

    public VirtualShopBank(@NotNull VirtualShop shop) {
        super(shop);
    }

    @Override
    public boolean deposit(@NotNull Currency currency, double amount) {
        return true;
    }

    @Override
    public boolean withdraw(@NotNull Currency currency, double amount) {
        return true;
    }

    @Override
    public double getBalance(@NotNull Currency currency) {
        return -1;
    }

    @Override
    public boolean hasEnough(@NotNull Currency currency, double amount) {
        return true;
    }
}
