package su.nightexpress.nexshop.shop.chest;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.util.PlaceholderRelMap;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

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

    public static final String SHOP_HOLOGRAM_ENABLED = "%shop_hologram_enabled%";
    public static final String SHOP_SHOWCASE_ENABLED = "%shop_showcase_enabled%";
    //public static final String SHOP_SHOWCASE_TYPE    = "%shop_showcase_type%";

    public static final Function<Integer, String>              SHOP_PRODUCT_NAME  = (slot) -> "%shop_product_name_" + slot + "%";
    public static final BiFunction<TradeType, Integer, String> SHOP_PRODUCT_PRICE = (tradeType, slot) -> "%shop_product_price_" + tradeType.getLowerCase() + "_" + slot + "%";

    @NotNull
    public static PlaceholderMap forShop(@NotNull ChestShop shop) {
        PlaceholderMap placeholderMap = new PlaceholderMap()
            .add(SHOP_BANK_BALANCE, () -> shop.getOwnerBank().getBalanceMap().keySet().stream()
                .map(currency -> currency.format(shop.getOwnerBank().getBalance(currency))).collect(Collectors.joining(", ")))
            .add(SHOP_OWNER, () -> shop.isAdminShop() ? ChestConfig.ADMIN_SHOP_NAME.get() : shop.getOwnerName())
            .add(SHOP_LOCATION_X, () -> NumberUtil.format(shop.getBlockPos().getX()))
            .add(SHOP_LOCATION_Y, () -> NumberUtil.format(shop.getBlockPos().getY()))
            .add(SHOP_LOCATION_Z, () -> NumberUtil.format(shop.getBlockPos().getZ()))
            .add(SHOP_LOCATION_WORLD, () -> shop.isActive() ? LangAssets.get(shop.getWorld()) : shop.getWorldName())
            .add(SHOP_IS_ADMIN, () -> ChestLang.getYesOrNo(shop.isAdminShop()))
            .add(SHOP_TYPE, () -> ChestLang.SHOP_TYPES.getLocalized(shop.getType()));

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

        return placeholderMap;
    }

    @NotNull
    public static PlaceholderMap forShopDisplay(@NotNull ChestShop shop) {
        return new PlaceholderMap()
            .add(SHOP_HOLOGRAM_ENABLED, () -> ChestLang.getYesOrNo(shop.isHologramEnabled()))
            .add(SHOP_SHOWCASE_ENABLED, () -> ChestLang.getYesOrNo(shop.isShowcaseEnabled()));
    }

    public static PlaceholderRelMap<Player> forProductStock(@NotNull ChestProduct product) {
        PlaceholderRelMap<Player> map = new PlaceholderRelMap<>();

        for (TradeType tradeType : TradeType.values()) {
            map
                .add(PRODUCT_STOCK_AMOUNT_LEFT.apply(tradeType), player -> {
                    int leftAmount = product.getShop().getStock().countItem(product, tradeType, player);
                    return leftAmount < 0 ? ChestLang.OTHER_INFINITY.getString() : NumberUtil.format(leftAmount);
                });
        }

        return map;
    }
}
