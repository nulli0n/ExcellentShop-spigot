package su.nightexpress.nexshop.shop.virtual.util;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;

public class ShopUtils {

    @NotNull
    public static Currency getDefaultCurrency() {
        Currency currency = ShopAPI.getCurrencyManager().getCurrency(VirtualConfig.DEFAULT_CURRENCY.get());
        if (currency != null) return currency;

        return ShopAPI.getCurrencyManager().getAny();
    }
}
