package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestKeys;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.impl.Showcase;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.integration.item.ItemBridge;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.impl.AdaptedCustomStack;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.NightMessage;
import su.nightexpress.nightcore.util.text.tag.Tags;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChestUtils {

    @NotNull
    public static String generateShopId(@NotNull Player player, @NotNull Location location) {
        return generateShopId(player.getName(), location);
    }

    @NotNull
    public static String generateShopId(@NotNull String playerName, @NotNull Location location) {
        return (playerName + "_" + LocationUtil.getWorldName(location) + "_" + location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ()).toLowerCase();
    }

    public static boolean canUseDisplayEntities() {
        return !ChestConfig.DISPLAY_USE_ARMOR_STANDS.get();
    }

    public static boolean hasRentPermission(@NotNull Player player) {
        return player.hasPermission(ChestPerms.RENT);
    }

    @Nullable
    public static Location getSafeLocation(@NotNull Location origin) {
        Location location = origin.clone();

        while (!isSafeLocation(location) && location.getBlockY() > 0) {
            location = location.add(0, -1, 0);
        }

        return location.getBlockY() == 0 ? null : location;
    }

    public static boolean isSafeLocation(@NotNull Location origin) {
        Block block = origin.getBlock();
        return !isDangerousBlock(block) && isSafeBlock(block.getRelative(BlockFace.DOWN));
    }

    public static boolean isSafeBlock(@NotNull Block block) {
        Material material = block.getType();
        return material.isSolid() && !isDangerousBlock(block);
    }

    public static boolean isDangerousBlock(@NotNull Block block) {
        Material material = block.getType();
        return material == Material.LAVA || material == Material.MAGMA_BLOCK;
    }

    public static boolean containsItem(@NotNull ChestShop shop, @NotNull String searchFor) {
        return shop.getValidProducts().stream().anyMatch(product -> matchesItem(product, searchFor));
    }

    public static boolean matchesItem(@NotNull ChestProduct product, @NotNull String searchFor) {
        if (!product.isValid()) return false;
        if (!(product.getContent() instanceof ItemContent itemContent)) return false;

        ItemStack item = itemContent.getItem();
        String itemType = BukkitThing.getValue(item.getType()).toLowerCase();
        //String itemTypeLocalized = LangAssets.get(item.getType()).toLowerCase();
        String itemCustomName = NightMessage.stripTags(ItemUtil.getNameSerialized(item)).toLowerCase();

        if (itemType.contains(searchFor) || /*itemTypeLocalized.contains(searchFor) ||*/ itemCustomName.contains(searchFor)) {
            return true;
        }

        AdaptedItem adaptedItem = itemContent.getAdaptedItem();

        if (adaptedItem instanceof AdaptedCustomStack customStack) {
            String itemId = customStack.getData().getItemId();
            return itemId.contains(searchFor);
        }

        return false;
    }

//    @Deprecated
//    public static boolean bypassHandlerDetection(@NotNull InventoryClickEvent event) {
//        if (!ChestConfig.SHOP_PRODUCT_BYPASS_DETECTION_ENABLED.get()) return false;
//
//        boolean needShift = ChestConfig.SHOP_PRODUCT_BYPASS_DETECTION_HOLD_SHIFT.get();
//        return !needShift || event.isShiftClick();
//    }

    public static boolean isDye(@NotNull Material material) {
        return BukkitThing.getValue(material).endsWith("_dye");
    }

    public static boolean isSignDecor(@NotNull ItemStack itemStack) {
        return isSignDecor(itemStack.getType());
    }

    public static boolean isSignDecor(@NotNull Material material) {
        return material == Material.GLOW_INK_SAC || ChestUtils.isDye(material);
    }

    public static boolean isShopDecor(@NotNull ItemStack itemStack) {
        return isShopDecor(itemStack.getType());
    }

    public static boolean isShopDecor(@NotNull Material material) {
        return Tag.SIGNS.isTagged(material) || material == Material.ITEM_FRAME || material == Material.GLOW_ITEM_FRAME || material == Material.HOPPER;
    }

    public static int getShopLimit(@NotNull Player player) {
        return ChestConfig.SHOP_CREATION_MAX_PER_RANK.get().getGreatestOrNegative(player);
    }

    public static int getProductLimit(@NotNull Player player) {
        return ChestConfig.SHOP_PRODUCTS_MAX_PER_RANK.get().getGreatestOrNegative(player);
    }

    public static int getRentDurationLimit() {
        return ChestConfig.RENT_MAX_DURATION.get();
    }

    public static double getRentPriceLimit(@NotNull Currency currency) {
        return ChestConfig.RENT_MAX_PRICE.get().getOrDefault(currency.getInternalId(), -1D);
    }

    public static boolean isAllowedItem(@NotNull ItemStack item) {
        Set<String> bannedItems = ChestConfig.SHOP_PRODUCT_BANNED_ITEMS.get();

        String material = item.getType().getKey().getKey();
        if (bannedItems.contains(material)) return false;

        for (ItemAdapter<?> handler : ItemBridge.getAdapters()) {
            if (!(handler instanceof IdentifiableItemAdapter identifiableAdapter)) continue;

            String id = identifiableAdapter.getItemId(item);
            if (id != null && bannedItems.contains(id.toLowerCase())) {
                return false;
            }
        }

        String name = NightMessage.stripTags(ItemUtil.getNameSerialized(item));
        if (ChestConfig.SHOP_PRODUCT_DENIED_NAMES.get().stream().anyMatch(name::contains)) {
            return false;
        }

        List<String> lore = Lists.modify(ItemUtil.getLoreSerialized(item), NightMessage::stripTags);
        return lore.stream().noneMatch(line -> ChestConfig.SHOP_PRODUCT_DENIED_LORES.get().stream().anyMatch(line::contains));
    }

    public static boolean isContainer(@NotNull Material material) {
        return material != Material.ENDER_CHEST && material.createBlockData().createBlockState() instanceof Container;
    }

    public static boolean isInfiniteStorage() {
        return ChestConfig.SHOP_INFINITE_STORAGE_ENABLED.get();
    }

    public static void setShopItemType(@NotNull ItemStack itemType, @NotNull Material material) {
        PDCUtil.set(itemType, ChestKeys.shopItemType, BukkitThing.getAsString(material));
    }

    public static boolean isShopItem(@NotNull ItemStack itemStack) {
        return getShopItemType(itemStack) != null;
    }

    @Nullable
    public static Material getShopItemType(@NotNull ItemStack itemStack) {
        String name = PDCUtil.getString(itemStack, ChestKeys.shopItemType).orElse(null);
        return name == null ? null : BukkitThing.getMaterial(name);
    }

    @NotNull
    public static Set<Material> getDefaultShopBlockTypes() {
        Set<Material> materials = Lists.newSet(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.BARREL
        );
        materials.addAll(Tag.SHULKER_BOXES.getValues());
        return materials;
    }

    @NotNull
    public static NightItem getDefaultShopItem(@NotNull Material material) {
        String texture = switch (material) {
            case CHEST -> "edc36c9cb50a527aa55607a0df7185ad20aabaa903e8d9abfc78260705540def";
            case BARREL -> "5193c89d1df679854f33c2215247b676a159d5395392d0c61b8476f813d9edb0";
            case WHITE_SHULKER_BOX -> "7e066c569d4b94e49b23770e46c9a0e1d736711becb702809375e2d5a32f2a99";
            case LIGHT_GRAY_SHULKER_BOX -> "56a48c4037343731bd5fd1510ca15c573788389f258677a17f014e08aeaa9560";
            case GRAY_SHULKER_BOX -> "a95bde13c45754468cfc8c3a00133d997362fa7e302b0b0fbc4bd0fca6890059";
            case BLACK_SHULKER_BOX -> "bf6174c01a67e1eada9db16bb551ddee32dfcbf37611382f88cdb1e62895bab2";
            case BROWN_SHULKER_BOX -> "b41956931d1f6d1d1b6f82f077b8a265b259f5d29f567869e9955cc6f9a82f12";
            case RED_SHULKER_BOX -> "324aa7bf056ccd3d4e63197b04d89f0e9ba79fa6049c503029521724e234054a";
            case ORANGE_SHULKER_BOX -> "a83cbb7b98e1954dd2c007ea45975fc7fe1f6ebea7c12d75a578d0960f34996";
            case YELLOW_SHULKER_BOX -> "e780feff71541bf6eb4368e726f868871cc549f2599d45cc0e1c729825fee9df";
            case LIME_SHULKER_BOX -> "f45cf042172c9e4e7c8eea570ac8bbd76dc7d8d561ae0f7b60937b3bd4d92e19";
            case GREEN_SHULKER_BOX -> "efd8881d02fe9cee859ac597731b2df46bee6d52c446205f9da9dfa7bd111694";
            case CYAN_SHULKER_BOX -> "101facfbdea4980bb0182dcf259341093e22b87375e70b849b88cfad17fc5b27";
            case LIGHT_BLUE_SHULKER_BOX -> "dbcf2e5a0ee72bbd63b08336918f11730120a49df3557cfa84ff6450fbc4c65c";
            case BLUE_SHULKER_BOX -> "47d0d86e1f1108468ac150dafa28e7d0619a6e3066cb42994bc6c1866bd267e3";
            case PURPLE_SHULKER_BOX -> "30eed20b95873611c2b60e54f8df6f4e6654fb9575fc47e63f63327f9f6c56cf";
            case MAGENTA_SHULKER_BOX -> "83524237183c2ba221ba61923a7130099e37166227cacb1f65cec93b6c33a6a8";
            case PINK_SHULKER_BOX -> "a374cf299874af600608acfb55169237fda244ccca0e23aa08c2e335bd73406b";
            default -> null;
        };

        NightItem item = NightItem.fromType(texture == null ? material : Material.PLAYER_HEAD)
            .setDisplayName(Tags.LIGHT_YELLOW.wrap(Tags.BOLD.wrap("Shop Block")) + " " + Tags.GRAY.wrap("(" + Tags.WHITE.wrap(Placeholders.GENERIC_TYPE) + ")"))
            .setLore(Lists.newList(
                Tags.GRAY.wrap("Place down to create a shop!")
            ))
            .hideAllComponents();

        if (texture != null) item.setProfileBySkinURL(texture);

        return item;
    }

    @NotNull
    public static Map<String, Showcase> getDefaultShowcaseCatalog() {
        Map<String, Showcase> map = new LinkedHashMap<>();

        List<Showcase> list = Lists.newList(
            Showcase.fromMaterial(Material.ICE),
            Showcase.fromMaterial(Material.GLASS),
            Showcase.fromMaterial(Material.TINTED_GLASS),
            Showcase.fromMaterial(Material.SPAWNER),

            Showcase.fromMaterial(Material.WHITE_STAINED_GLASS),
            Showcase.fromMaterial(Material.LIME_STAINED_GLASS),
            Showcase.fromMaterial(Material.GRAY_STAINED_GLASS),
            Showcase.fromMaterial(Material.BLACK_STAINED_GLASS),
            Showcase.fromMaterial(Material.BLUE_STAINED_GLASS),
            Showcase.fromMaterial(Material.BROWN_STAINED_GLASS),
            Showcase.fromMaterial(Material.CYAN_STAINED_GLASS),
            Showcase.fromMaterial(Material.GREEN_STAINED_GLASS),
            Showcase.fromMaterial(Material.LIGHT_BLUE_STAINED_GLASS),
            Showcase.fromMaterial(Material.LIGHT_GRAY_STAINED_GLASS),
            Showcase.fromMaterial(Material.MAGENTA_STAINED_GLASS),
            Showcase.fromMaterial(Material.ORANGE_STAINED_GLASS),
            Showcase.fromMaterial(Material.PINK_STAINED_GLASS),
            Showcase.fromMaterial(Material.PURPLE_STAINED_GLASS),
            Showcase.fromMaterial(Material.RED_STAINED_GLASS),
            Showcase.fromMaterial(Material.YELLOW_STAINED_GLASS),

            Showcase.fromMaterial(Material.COPPER_GRATE),
            Showcase.fromMaterial(Material.EXPOSED_COPPER_GRATE),
            Showcase.fromMaterial(Material.OXIDIZED_COPPER_GRATE),
            Showcase.fromMaterial(Material.WAXED_COPPER_GRATE),
            Showcase.fromMaterial(Material.WAXED_EXPOSED_COPPER_GRATE),
            Showcase.fromMaterial(Material.WAXED_OXIDIZED_COPPER_GRATE),
            Showcase.fromMaterial(Material.WAXED_WEATHERED_COPPER_GRATE)
        );
        list.forEach(showcase -> map.put(showcase.getId(), showcase));

        return map;
    }

    @Nullable
    public static Showcase getShowcaseFromCatalog(@NotNull String id) {
        return ChestConfig.SHOWCASE_CATALOG.get().get(id.toLowerCase());
    }
}
