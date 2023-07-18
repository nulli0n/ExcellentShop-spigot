package su.nightexpress.nexshop.shop.chest.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Colorizer;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ShopUtils {

    @NotNull
    public static Currency getDefaultCurrency() {
        Currency currency = ShopAPI.getCurrencyManager().getCurrency(ChestConfig.DEFAULT_CURRENCY.get());
        if (currency != null) return currency;

        return ShopAPI.getCurrencyManager().getCurrencies()
            .stream().filter(cur -> ChestConfig.ALLOWED_CURRENCIES.get().contains(cur.getId())).findFirst().orElseThrow();
    }

    @NotNull
    public static Set<Currency> getAllowedCurrencies() {
        return ChestConfig.ALLOWED_CURRENCIES.get().stream()
            .map(id -> ShopAPI.getCurrencyManager().getCurrency(id)).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static int getShopLimit(@NotNull Player player) {
        return ChestConfig.SHOP_CREATION_MAX_PER_RANK.get().getBestValue(player, 0);
    }

    public static int getProductLimit(@NotNull Player player) {
        return ChestConfig.SHOP_PRODUCTS_MAX_PER_RANK.get().getBestValue(player, 0);
    }

    @NotNull
    public static List<String> getHologramLines(@NotNull ShopType chestType) {
        return ChestConfig.DISPLAY_TEXT.get().getOrDefault(chestType, Collections.emptyList());
    }

    public static boolean isAllowedCurrency(@NotNull Currency currency) {
        return ChestConfig.ALLOWED_CURRENCIES.get().contains(currency.getId());
    }

    public static boolean isAllowedItem(@NotNull ItemStack item) {
        if (ChestConfig.SHOP_PRODUCT_DENIED_MATERIALS.get().contains(item.getType())) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                String name = Colorizer.strip(meta.getDisplayName());
                if (ChestConfig.SHOP_PRODUCT_DENIED_NAMES.get().stream().anyMatch(name::contains)) {
                    return false;
                }
            }
            List<String> lore = meta.getLore();
            if (lore != null) {
                return lore.stream().noneMatch(line -> ChestConfig.SHOP_PRODUCT_DENIED_LORES.get().stream().anyMatch(line::contains));
            }
        }
        return true;
    }

    public static boolean isValidContainer(@NotNull Block block) {
        if (block.getType() == Material.ENDER_CHEST) return false;
        if (!(block.getState() instanceof Container container)) return false;
        return ChestConfig.ALLOWED_CONTAINERS.get().contains(block.getType());
    }
}
