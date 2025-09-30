package su.nightexpress.nexshop.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.ShopClickAction;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nexshop.product.content.ProductContent;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
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

    public static boolean canUseDialogs() {
        return Version.isAtLeast(Version.MC_1_21_7);
    }

    @NotNull
    public static ItemStack getInvalidProductPlaceholder() {
        return NightItem.fromType(Material.BARRIER).localized(Lang.EDITOR_GENERIC_BROKEN_ITEM).getItemStack();
    }

    @NotNull
    public static String formatOrInfinite(double value) {
        return value < 0 ? CoreLang.OTHER_INFINITY.text() : NumberUtil.format(value);
    }

    public static boolean hasCurrencyPermission(@NotNull Player player, @NotNull Currency currency) {
        boolean hasOldPerm = player.hasPermission(ChestPerms.PREFIX + "currency." + currency.getInternalId());

        return player.hasPermission(Perms.CURRENCY) || player.hasPermission(Perms.PREFIX_CURRENCY + currency.getInternalId()) || hasOldPerm;
    }

    @NotNull
    public static String generateProductId(@NotNull VirtualShop shop, @NotNull ProductContent content) {
        ItemStack itemStack = content.getPreview();

        String name = NightMessage.stripTags(ItemUtil.getNameSerialized(itemStack));
        String id = Strings.filterForVariable(name.isBlank() ? BukkitThing.getValue(itemStack.getType()) : name);

        int count = 0;
        while (shop.getProductById(addCount(id, count)) != null) {
            count++;
        }

        return addCount(id, count);
    }

    @NotNull
    public static String getProductLogName(@NotNull Product product) {
        ItemStack preview = product.getPreview();
        String name = ItemUtil.getCustomNameSerialized(preview);
        if (name == null) name = ItemUtil.getItemNameSerialized(preview);
        if (name == null) name = StringUtil.capitalizeUnderscored(BukkitThing.getValue(preview.getType()));

        return name;
    }

    @NotNull
    private static String addCount(@NotNull String id, int count) {
        return count == 0 ? id : id + "_" + count;
    }

    @Nullable
    public static VirtualProduct getBestProduct(@NotNull Collection<VirtualProduct> products, @NotNull TradeType tradeType, int stackSize, @Nullable Player player) {
        Comparator<VirtualProduct> comparator = Comparator.comparingDouble(product -> product.getFinalPrice(tradeType, player) * UnitUtils.amountToUnits(product, stackSize));
        Stream<VirtualProduct> stream = products.stream();

        return (tradeType == TradeType.BUY ? stream.min(comparator) : stream.max(comparator)).orElse(null);
    }

    @NotNull
    public static ShopClickAction getClickAction(@NotNull Player player, @NotNull ClickType click, @NotNull Shop shop, @NotNull Product product) {
        boolean isBuyable = product.isBuyable();
        boolean isSellable = product.isSellable();
        if (!isBuyable && !isSellable) return ShopClickAction.UNDEFINED;

        if (Players.isBedrock(player)) {
            if (isBuyable && isSellable) return ShopClickAction.PURCHASE_OPTION;

            return !isSellable ? ShopClickAction.BUY_SELECTION : ShopClickAction.SELL_SELECTION;
        }

        ShopClickAction action = Config.GUI_CLICK_ACTIONS.get().get(click);
        return action == null ? ShopClickAction.UNDEFINED : action;
    }

    @NotNull
    public static Set<LocalTime> parseTimes(@NotNull List<String> list) {
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

    @NotNull
    public static Set<String> serializeTimes(@NotNull Set<LocalTime> times) {
        return Lists.modify(times, HOURS_FORMATTER::format);
    }

    @NotNull
    public static Set<DayOfWeek> parseDays(@NotNull String str) {
        return Stream.of(str.split(",")).map(raw -> Enums.get(raw.trim(), DayOfWeek.class)).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static int countItemSpace(@NotNull Inventory inventory, @NotNull ItemStack item) {
        return countItemSpace(inventory, item::isSimilar, item.getMaxStackSize());
    }

    public static int countItemSpace(@NotNull Inventory inventory, @NotNull Predicate<ItemStack> predicate, int maxSize) {
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

    public static boolean addItem(@NotNull Inventory inventory, @NotNull ItemStack origin, int amount) {
        if (amount <= 0 || origin.getType().isAir()) return false;
        if (countItemSpace(inventory, origin) < amount) return false;

        Location location = inventory.getLocation();
        World world = location == null ? null : location.getWorld();

        ItemStack split = new ItemStack(origin);

        int splitAmount = Math.min(split.getMaxStackSize(), amount);
        split.setAmount(splitAmount);
        inventory.addItem(split).values().forEach(left -> {
            if (world != null) {
                world.dropItem(location, left);
            }
        });

        amount -= splitAmount;
        if (amount > 0) addItem(inventory, origin, amount);

        return true;
    }
}
