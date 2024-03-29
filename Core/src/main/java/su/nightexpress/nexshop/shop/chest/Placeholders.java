package su.nightexpress.nexshop.shop.chest;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Placeholders extends su.nightexpress.nexshop.Placeholders {

    public static final String                      GENERIC_PRODUCT_NAME  = "%product_name%";
    public static final Function<TradeType, String> GENERIC_PRODUCT_PRICE = tradeType -> "%product_price_" + tradeType.getLowerCase() + "%";

    public static final String SHOP_OWNER          = "%shop_owner%";
    public static final String SHOP_LOCATION_X     = "%shop_location_x%";
    public static final String SHOP_LOCATION_Y     = "%shop_location_y%";
    public static final String SHOP_LOCATION_Z     = "%shop_location_z%";
    public static final String SHOP_LOCATION_WORLD = "%shop_location_world%";
    public static final String SHOP_IS_ADMIN       = "%shop_is_admin%";
    public static final String SHOP_TYPE           = "%shop_type%";
    public static final String SHOP_BANK_BALANCE   = "%shop_bank_balance%";

    public static final Function<Integer, String>              SHOP_PRODUCT_NAME  = (slot) -> "%shop_product_name_" + slot + "%";
    public static final BiFunction<TradeType, Integer, String> SHOP_PRODUCT_PRICE = (tradeType, slot) -> "%shop_product_price_" + tradeType.getLowerCase() + "_" + slot + "%";

    public static PlaceholderMap forShop(@NotNull ChestShop shop) {
        PlaceholderMap placeholderMap = new PlaceholderMap()
            .add(SHOP_BANK_BALANCE, () -> ChestUtils.getAllowedCurrencies().stream()
                .map(currency -> currency.format(shop.getOwnerBank().getBalance(currency))).collect(Collectors.joining(", ")))
            .add(SHOP_OWNER, () -> shop.isAdminShop() ? ChestConfig.ADMIN_SHOP_NAME.get() : shop.getOwnerName())
            .add(SHOP_LOCATION_X, () -> NumberUtil.format(shop.getLocation().getX()))
            .add(SHOP_LOCATION_Y, () -> NumberUtil.format(shop.getLocation().getY()))
            .add(SHOP_LOCATION_Z, () -> NumberUtil.format(shop.getLocation().getZ()))
            .add(SHOP_LOCATION_WORLD, () -> LocationUtil.getWorldName(shop.getLocation()))
            .add(SHOP_IS_ADMIN, () -> LangManager.getBoolean(shop.isAdminShop()))
            .add(SHOP_TYPE, () -> ShopAPI.PLUGIN.getLangManager().getEnum(shop.getType()));

        for (int slot = 0; slot < 27; slot++) {
            int index = slot;
            for (TradeType tradeType : TradeType.values()) {
                placeholderMap.add(SHOP_PRODUCT_PRICE.apply(tradeType, slot), () -> {
                    ChestProduct product = shop.getProductAtSlot(index);
                    return product == null ? "-" : product.getCurrency().format(product.getPricer().getPrice(tradeType));
                });
            }
            placeholderMap.add(SHOP_PRODUCT_NAME.apply(slot), () -> {
                ChestProduct product = shop.getProductAtSlot(index);
                return product == null ? "-" : ItemUtil.getItemName(product.getPreview());
            });
        }

        /*for (TradeType tradeType : TradeType.values()) {
            for (int slot = 0; slot < 27; slot++) {
                int index = slot;
                placeholderMap.add(SHOP_PRODUCT_PRICE.apply(tradeType, slot), () -> {
                    if (products.size() <= index) return "-";

                    ChestProduct product = products.get(index);
                    return product.getCurrency().format(product.getPricer().getPrice(tradeType));
                });
            }
        }*/

        return placeholderMap;
    }
}
