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
import su.nightexpress.nexshop.api.shop.product.typing.CommandTyping;
import su.nightexpress.nexshop.api.shop.product.typing.PluginTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.product.typing.VanillaTyping;
import su.nightexpress.nexshop.api.shop.type.ShopClickAction;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShopUtils {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    private static DateTimeFormatter dateFormatter;

    public static void setDateFormatter(@NotNull String pattern) {
        ShopUtils.dateFormatter = DateTimeFormatter.ofPattern(pattern);
    }

    @NotNull
    public static DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    @Nullable
    public static ItemStack readItemTag(@NotNull String serialized) {
        return Version.isAtLeast(Version.MC_1_21) && serialized.contains("{") ? ItemNbt.fromTagString(serialized) : ItemNbt.decompress(serialized);
    }

    @Nullable
    public static String getItemTag(@NotNull ItemStack itemStack) {
        return Version.isAtLeast(Version.MC_1_21) ? ItemNbt.getTagString(itemStack) : ItemNbt.compress(itemStack);
    }

    public static boolean hasEconomyBridge() {
        return Plugins.isInstalled(HookId.ECONOMY_BRIDGE);
    }

    @NotNull
    public static String generateProductId(@NotNull VirtualShop shop, @NotNull ProductTyping typing) {
        String id = switch (typing) {
            case VanillaTyping vanilla -> {
                ItemStack item = vanilla.getItem();
                String name = StringUtil.transformForID(NightMessage.stripTags(ItemUtil.getSerializedName(item)).toLowerCase()); // Remove all non-latins from item display name.

                yield name.isBlank() ? BukkitThing.toString(item.getType()) : name;
            }
            case PluginTyping pluginTyping -> (pluginTyping.getHandler().getName() + "_" + pluginTyping.getItemId()).toLowerCase();
            case CommandTyping ignored -> "command_item";
            default -> UUID.randomUUID().toString().substring(0, 8);
        };

        int count = 0;
        while (shop.getProductById(addCount(id, count)) != null) {
            count++;
        }

        return addCount(id, count);
    }

    @NotNull
    private static String addCount(@NotNull String id, int count) {
        return count == 0 ? id : id + "_" + count;
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
        return list.stream().map(timeRaw -> LocalTime.parse(timeRaw, TIME_FORMATTER)).collect(Collectors.toSet());
    }

    @NotNull
    public static Set<DayOfWeek> parseDays(@NotNull String str) {
        return Stream.of(str.split(","))
            .map(raw -> StringUtil.getEnum(raw.trim(), DayOfWeek.class).orElse(null))
            .filter(Objects::nonNull).collect(Collectors.toSet());
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
