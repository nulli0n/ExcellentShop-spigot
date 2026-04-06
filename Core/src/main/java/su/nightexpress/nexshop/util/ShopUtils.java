package su.nightexpress.nexshop.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.core.Perms;
import su.nightexpress.excellentshop.product.ProductContent;
import su.nightexpress.excellentshop.feature.playershop.core.ChestPerms;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.NightMessage;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShopUtils {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    public static final DateTimeFormatter HOURS_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @NonNull
    public static ItemStack getInvalidProductPlaceholder() {
        return NightItem.fromType(Material.BARRIER).localized(Lang.EDITOR_GENERIC_BROKEN_ITEM).getItemStack();
    }

    @NonNull
    public static String formatOrInfinite(double value) {
        return value < 0 ? CoreLang.OTHER_INFINITY.text() : NumberUtil.format(value);
    }

    public static boolean hasCurrencyPermission(@NonNull Player player, @NonNull Currency currency) {
        boolean hasOldPerm = player.hasPermission(ChestPerms.PREFIX + "currency." + currency.getInternalId());

        return player.hasPermission(Perms.CURRENCY) || player.hasPermission(Perms.PREFIX_CURRENCY + currency.getInternalId()) || hasOldPerm;
    }

    @NonNull
    public static String generateProductId(@NonNull VirtualShop shop, @NonNull ProductContent content) {
        ItemStack itemStack = content.getPreview();

        String name = NightMessage.stripTags(ItemUtil.getNameSerialized(itemStack));
        String id = Strings.filterForVariable(name.isBlank() ? BukkitThing.getValue(itemStack.getType()) : name);

        int count = 0;
        while (shop.getProductById(addCount(id, count)) != null) {
            count++;
        }

        return addCount(id, count);
    }

    @NonNull
    public static String getProductLogName(@NonNull Product product) {
        ItemStack preview = product.getPreview();
        String name = ItemUtil.getCustomNameSerialized(preview);
        if (name == null) name = ItemUtil.getItemNameSerialized(preview);
        if (name == null) name = StringUtil.capitalizeUnderscored(BukkitThing.getValue(preview.getType()));

        return name;
    }

    @NonNull
    private static String addCount(@NonNull String id, int count) {
        return count == 0 ? id : id + "_" + count;
    }

    @Nullable
    public static <T extends Product> T getBestProduct(@NonNull Collection<T> products, @NonNull TradeType tradeType, int stackSize, @Nullable Player player) {
        Comparator<T> comparator = Comparator.comparingDouble(product -> product.getFinalPrice(tradeType, player) * UnitUtils.amountToUnits(product, stackSize));
        Stream<T> stream = products.stream();

        return (tradeType == TradeType.BUY ? stream.min(comparator) : stream.max(comparator)).orElse(null);
    }

    @Nullable
    public static <T extends Product> T getBestProduct(@NonNull Collection<T> products, @NonNull TradeType tradeType, @Nullable Player player) {
        Comparator<T> comparator = Comparator.comparingDouble(product -> product.getFinalPrice(tradeType, player));
        Stream<T> stream = products.stream();

        return (tradeType == TradeType.BUY ? stream.min(comparator) : stream.max(comparator)).orElse(null);
    }

    @NonNull
    public static Set<LocalTime> parseTimes(@NonNull List<String> list) {
        Set<LocalTime> result = Lists.modify(new HashSet<>(list), str -> {
            try {
                return LocalTime.parse(str, TIME_FORMATTER);
            }
            catch (DateTimeParseException exception) {
                return null;
            }
        });
        result.removeIf(Objects::isNull);
        return result;
    }

    @NonNull
    public static Set<String> serializeTimes(@NonNull Set<LocalTime> times) {
        return Lists.modify(times, HOURS_FORMATTER::format);
    }

    @NonNull
    public static Set<DayOfWeek> parseDays(@NonNull String str) {
        return Stream.of(str.split(",")).map(raw -> Enums.get(raw.trim(), DayOfWeek.class)).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static int countItemSpace(@NonNull Inventory inventory, @NonNull ItemStack item) {
        return countItemSpace(inventory, item::isSimilar, item.getMaxStackSize());
    }

    public static int countItemSpace(@NonNull Inventory inventory, @NonNull Predicate<ItemStack> predicate, int maxSize) {
        return Stream.of(inventory.getStorageContents()).mapToInt(itemHas -> {
            if (itemHas == null || itemHas.getType().isAir()) {
                return maxSize;
            }
            if (predicate.test(itemHas)) {
                return (maxSize - itemHas.getAmount());
            }
            return 0;
        }).sum();
    }

    public static int countItem(@NonNull Inventory inventory, @NonNull Predicate<ItemStack> predicate) {
        return Stream.of(inventory.getContents())
            .filter(item -> item != null && !item.getType().isAir() && predicate.test(item))
            .mapToInt(ItemStack::getAmount).sum();
    }

    public static int countItem(@NonNull Inventory inventory, @NonNull ItemStack item) {
        return countItem(inventory, item::isSimilar);
    }

    public static int countItem(@NonNull Inventory inventory, @NonNull Material material) {
        return countItem(inventory, itemHas -> itemHas.getType() == material);
    }

    public static boolean takeItem(@NonNull Inventory inventory, @NonNull ItemStack item) {
        return takeItem(inventory, itemHas -> itemHas.isSimilar(item), countItem(inventory, item));
    }

    public static boolean takeItem(@NonNull Inventory inventory, @NonNull ItemStack item, int amount) {
        return takeItem(inventory, itemHas -> itemHas.isSimilar(item), amount);
    }

    public static boolean takeItem(@NonNull Inventory inventory, @NonNull Material material) {
        return takeItem(inventory, itemHas -> itemHas.getType() == material, countItem(inventory, material));
    }

    public static boolean takeItem(@NonNull Inventory inventory, @NonNull Material material, int amount) {
        return takeItem(inventory, itemHas -> itemHas.getType() == material, amount);
    }

    public static boolean takeItem(@NonNull Inventory inventory, @NonNull Predicate<ItemStack> predicate) {
        return takeItem(inventory, predicate, countItem(inventory, predicate));
    }

    public static boolean takeItem(@NonNull Inventory inventory, @NonNull Predicate<ItemStack> predicate, int amount) {
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

    public static void addItem(@NonNull Inventory inventory, @NonNull ItemStack... items) {
        Arrays.asList(items).forEach(item -> addItem(inventory, item, item.getAmount()));
    }

    @NonNull
    public static Map<Integer, ItemStack> addItem(@NonNull Inventory inventory, @NonNull ItemStack origin, int amount) {
        if (amount <= 0 || origin.getType().isAir()) return Collections.emptyMap();

        ItemStack split = new ItemStack(origin);
        split.setAmount(amount);

        return inventory.addItem(split);
    }
}
