package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.shop.ProductHandlerRegistry;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Colorizer;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.Version;

import java.util.List;
import java.util.Set;

public class ChestUtils {

    @NotNull
    public static String generateShopId(@NotNull Player player, @NotNull Location location) {
        return (player.getName() + "_" + LocationUtil.getWorldName(location) + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ()).toLowerCase();
    }

    public static boolean canUseDisplayEntities() {
        return Version.isAtLeast(Version.V1_19_R3) && !ChestConfig.DISPLAY_HOLOGRAM_FORCE_ARMOR_STAND.get();
    }

    public static int getShopLimit(@NotNull Player player) {
        return ChestConfig.SHOP_CREATION_MAX_PER_RANK.get().getGreatestOrNegative(player);
    }

    public static int getProductLimit(@NotNull Player player) {
        return ChestConfig.SHOP_PRODUCTS_MAX_PER_RANK.get().getGreatestOrNegative(player);
    }

    public static boolean isAllowedItem(@NotNull ItemStack item) {
        Set<String> bannedItems = ChestConfig.SHOP_PRODUCT_BANNED_ITEMS.get();

        String material = item.getType().getKey().getKey();
        if (bannedItems.contains(material)) return false;

        for (PluginItemPacker pluginItem : ProductHandlerRegistry.getPluginItemPackers()) {
            String id = pluginItem.getItemId(item);
            if (id != null && bannedItems.contains(id.toLowerCase())) {
                return false;
            }
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
        if (!(block.getState() instanceof Container)) return false;

        return ChestConfig.ALLOWED_CONTAINERS.get().contains(block.getType());
    }

    @Nullable
    public static ItemStack getDefaultShowcase(@NotNull Material blockType) {
        var map = ChestConfig.DISPLAY_DEFAULT_SHOWCASE.get();

        ItemStack showcase = map.getOrDefault(BukkitThing.toString(blockType), map.get(Placeholders.DEFAULT));
        if (showcase == null || showcase.getType().isAir()) return null;

        return new ItemStack(showcase);
    }

    @Nullable
    public static ItemStack getCustomShowcase(@Nullable String type) {
        if (type == null) return null;

        var map = ChestConfig.DISPLAY_PLAYER_CUSTOMIZATION_SHOWCASE_LIST.get();

        ItemStack showcase = map.get(type.toLowerCase());
        if (showcase == null || showcase.getType().isAir()) return null;

        return new ItemStack(showcase);
    }

    @Nullable
    public static ItemStack getCustomShowcaseOrDefault(@NotNull ChestShop shop) {
        ItemStack showcase = getCustomShowcase(shop.getShowcaseType());
        return showcase == null ? getDefaultShowcase(shop.getBlockType()) : showcase;
    }
}
