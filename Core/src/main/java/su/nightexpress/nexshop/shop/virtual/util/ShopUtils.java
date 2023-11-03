package su.nightexpress.nexshop.shop.virtual.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    public static int countItemSpace(@NotNull Inventory inventory, @NotNull ItemStack item) {
        int stackSize = item.getType().getMaxStackSize();
        return Stream.of(inventory.getContents()).mapToInt(itemHas -> {
            if (itemHas == null || itemHas.getType().isAir()) {
                return stackSize;
            }
            if (itemHas.isSimilar(item)) {
                return (stackSize - itemHas.getAmount());
            }
            return 0;
        }).sum();
    }

    public static int countItem(@NotNull Inventory inventory, @NotNull Predicate<ItemStack> predicate) {
        return Stream.of(inventory.getContents())
            .filter(item -> item != null && !item.getType().isAir() && predicate.test(item))
            .mapToInt(ItemStack::getAmount).sum();
    }

    public static int countItem(@NotNull Inventory inventory, @NotNull ItemStack item) {
        return countItem(inventory, item::isSimilar);
    }

    public static int countItem(@NotNull Inventory inventory, @NotNull Material material) {
        return countItem(inventory, itemHas -> itemHas.getType() == material);
    }

    public static boolean takeItem(@NotNull Inventory inventory, @NotNull ItemStack item) {
        return takeItem(inventory, itemHas -> itemHas.isSimilar(item), countItem(inventory, item));
    }

    public static boolean takeItem(@NotNull Inventory inventory, @NotNull ItemStack item, int amount) {
        return takeItem(inventory, itemHas -> itemHas.isSimilar(item), amount);
    }

    public static boolean takeItem(@NotNull Inventory inventory, @NotNull Material material) {
        return takeItem(inventory, itemHas -> itemHas.getType() == material, countItem(inventory, material));
    }

    public static boolean takeItem(@NotNull Inventory inventory, @NotNull Material material, int amount) {
        return takeItem(inventory, itemHas -> itemHas.getType() == material, amount);
    }

    public static boolean takeItem(@NotNull Inventory inventory, @NotNull Predicate<ItemStack> predicate) {
        return takeItem(inventory, predicate, countItem(inventory, predicate));
    }

    public static boolean takeItem(@NotNull Inventory inventory, @NotNull Predicate<ItemStack> predicate, int amount) {
        int takenAmount = 0;

        for (ItemStack itemHas : inventory.getContents()) {
            if (itemHas == null || !predicate.test(itemHas)) continue;

            int hasAmount = itemHas.getAmount();
            if (takenAmount + hasAmount > amount) {
                int diff = (takenAmount + hasAmount) - amount;
                itemHas.setAmount(diff);
                break;
            }

            itemHas.setAmount(0);
            if ((takenAmount += hasAmount) == amount) {
                break;
            }
        }
        return true;
    }

    public static void addItem(@NotNull Inventory inventory, @NotNull ItemStack... items) {
        Arrays.asList(items).forEach(item -> addItem(inventory, item, item.getAmount()));
    }

    public static void addItem(@NotNull Inventory inventory, @NotNull ItemStack item2, int amount) {
        if (amount <= 0 || item2.getType().isAir()) return;

        Location location = inventory.getLocation();
        World world = location == null ? null : location.getWorld();
        ItemStack item = new ItemStack(item2);

        int realAmount = Math.min(item.getMaxStackSize(), amount);
        item.setAmount(realAmount);
        inventory.addItem(item).values().forEach(left -> {
            if (world != null) {
                world.dropItem(location, left);
            }
        });

        amount -= realAmount;
        if (amount > 0) addItem(inventory, item2, amount);
    }
}
