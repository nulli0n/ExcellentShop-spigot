package su.nightexpress.nexshop.shop.virtual.util;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.impl.product.specific.CommandSpecific;
import su.nightexpress.nexshop.shop.virtual.impl.product.specific.ItemSpecific;
import su.nightexpress.nexshop.shop.virtual.impl.product.specific.ProductSpecific;

import java.util.UUID;

public class ShopUtils {

    @NotNull
    public static Currency getDefaultCurrency() {
        Currency currency = ShopAPI.getCurrencyManager().getCurrency(VirtualConfig.DEFAULT_CURRENCY.get());
        if (currency != null) return currency;

        return ShopAPI.getCurrencyManager().getAny();
    }

    @NotNull
    public static String generateProductId(@NotNull ProductSpecific specific, @NotNull Shop<?, ?> shop) {
        String id;
        if (specific instanceof ItemSpecific itemSpecific) {
            id = ItemUtil.getItemName(itemSpecific.getItem());
        }
        else if (specific instanceof CommandSpecific commandSpecific) {
            id = "command_item";
        }
        else id = UUID.randomUUID().toString();

        id = StringUtil.lowerCaseUnderscore(Colorizer.restrip(id));

        int count = 0;
        while (shop.getProductById(id) != null) {
            id = id + "_" + (++count);
        }

        return id;
    }
}
