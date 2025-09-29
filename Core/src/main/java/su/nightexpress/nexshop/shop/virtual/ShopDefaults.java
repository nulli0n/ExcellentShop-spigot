package su.nightexpress.nexshop.shop.virtual;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.product.content.ContentType;
import su.nightexpress.nexshop.product.price.impl.FlatPricing;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class ShopDefaults {

    public static void createDefaultShops(@NotNull VirtualShopModule module) {
        createShop(module, "building_blocks",
            "#15A2DD",
            "BUILDING BLOCKS",
            Lists.newList("Search your favorite items for", "build and make your best builds."),
            NightItem.asCustomHead("99ddfda73dd88c669b62f6a0baea91c3c688df55d65e812fec8782ab87497c4d"),
            12,
            11,
            ShopDefaults::addBuildingBlocksItems);

        createShop(module, "colored_blocks",
            "#FF7222",
            "COLORED BLOCKS",
            Lists.newList("Beautify your builds with", "different block colors."),
            NightItem.asCustomHead("7a7d6c0accd2e9ccaefa96b9438d6e202fec795acf7072bcfd9cd9d4b5a32b0e"),
            11,
            12,
            ShopDefaults::addColoredBlocksItems);

        createShop(module, "food",
            "#ABE5A7",
            "FOOD",
            Lists.newList("Don't be hungry! Buy some", "yummy food and feed yourself."),
            NightItem.asCustomHead("ca9c8753780ebc39c351da8efd91bce90bd8cca7b511f93e78df75f6615c79a6"),
            2,
            13,
            ShopDefaults::addFoodItems);

        createShop(module, "mob_drops",
            "#D7BB9A",
            "MOB DROPS",
            Lists.newList("Make your money by killing or", "farming mobs and sell their drops."),
            NightItem.asCustomHead("f26bde45049c7b7d34605d806a06829b6f955b856a5991fd33e7eabce44c0834"),
            2,
            14,
            ShopDefaults::addMobDropsItems);

        createShop(module, "miscellaneous",
            "#B240CE",
            "MISCELLANEOUS",
            Lists.newList("There are some rare", "and random items."),
            NightItem.asCustomHead("e02eace744e0c913778fe8b35e13e3b0549d459ae713dfc480a2b41073cd7492"),
            2,
            15,
            ShopDefaults::addMiscellaneousItems);

        createShop(module, "minerals",
            "#91F0FF",
            "MINERALS",
            Lists.newList("Precious minerals are on sale."),
            NightItem.asCustomHead("2c21799fdb23ea44432d6a449c747d3bbd566cc29392c0fe3153a9ddca5e5d5"),
            2,
            20,
            ShopDefaults::addMineralsItems);

        createShop(module, "potions",
            "#56FF5C",
            "POTIONS & ARROWS",
            Lists.newList("Be stronger by using potions", "and arrows with effects!"),
            NightItem.asCustomHead("42f57c9eed9f90b7c33b0a447568150cb7b5ec62afddf280b4f981ffd480a766"),
            4,
            21,
            ShopDefaults::addPotionsItems);

        createShop(module, "combat_tools",
            "#7FEFD0",
            "COMBAT & TOOLS",
            Lists.newList("Do you need PvP equipment", "or some tools?"),
            NightItem.asCustomHead("f20cca630a71638f646ab5ab7ff053951e796d8559f816152e11560588fd501c"),
            3,
            23,
            ShopDefaults::addCombatToolsItems);

        createShop(module, "redstone",
            "#D93636",
            "REDSTONE",
            Lists.newList("You love to play with redstone?"),
            NightItem.asCustomHead("c168291abac4a5f86fe8b360338986aee7abcb7f4b8169eb55dfec928561258"),
            1,
            24,
            ShopDefaults::addRedstoneItems);

        createShop(module, "farming",
            "#FFD05A",
            "FARMING",
            Lists.newList("Make your own garden and", "start earning your money."),
            NightItem.asCustomHead("26459be09998e50abd2ccf4cd383e6b38ab5bc905facb66dce0e14e038ba1968"),
            2,
            30,
            ShopDefaults::addFarmingItems);

        createShop(module, "decoration",
            "#E78A8A",
            "DECORATION",
            Lists.newList("Decorate your builds with", "flowers, leaves and more!"),
            NightItem.asCustomHead("3052520ee99b6fcdbe70ed1f7cfbc3fb7175f04a2ceb007c9b11d8d727bca044"),
            4,
            32,
            ShopDefaults::addDecorationItems);
    }

    private static void addBuildingBlocksItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.GRASS_BLOCK, 50, 2, 1, 1);
        addShopProduct(shop, Material.DIRT, 20, 1, 1, 2);
        addShopProduct(shop, Material.COARSE_DIRT, 20, 1, 1, 3);
        addShopProduct(shop, Material.FARMLAND, 25, 1, 1, 4);
        addShopProduct(shop, Material.ROOTED_DIRT, 25, 1, 1, 5);
        addShopProduct(shop, Material.PODZOL, 20, 1, 1, 6);
        addShopProduct(shop, Material.MYCELIUM, 50, -1, 1, 7);
        addShopProduct(shop, Material.CRIMSON_NYLIUM, 75, 1, 1, 10);
        addShopProduct(shop, Material.WARPED_NYLIUM, 150, 2, 1, 11);
        addShopProduct(shop, Material.WARPED_WART_BLOCK, -1, -1, 1, 12);
        addShopProduct(shop, Material.GRAVEL, 50, 2, 1, 13);
        addShopProduct(shop, Material.GLASS, 4, 0.5, 1, 14);
        addShopProduct(shop, Material.GLASS_PANE, 2.5, 0.1, 1, 15);
        addShopProduct(shop, Material.ICE, 20, 2.5, 1, 16);
        addShopProduct(shop, Material.BLUE_ICE, 20, 2.5, 1, 19);
        addShopProduct(shop, Material.PACKED_ICE, 40, 3, 1, 20);
        addShopProduct(shop, Material.SNOW_BLOCK, 2, 0.2, 1, 21);
        addShopProduct(shop, Material.OBSIDIAN, 500, 25, 1, 22);
        addShopProduct(shop, Material.CRYING_OBSIDIAN, 1_500, 50, 1, 23);
        addShopProduct(shop, Material.RESPAWN_ANCHOR, 185_000, 500, 1, 24);
        addShopProduct(shop, Material.BOOKSHELF, 100, -1, 1, 25);
        addShopProduct(shop, Material.SOUL_SAND, 2_000, 15, 1, 28);
        addShopProduct(shop, Material.SOUL_SOIL, 2_000, 25, 1, 29);
        addShopProduct(shop, Material.ANCIENT_DEBRIS, 1250, 45, 1, 30);
        addShopProduct(shop, Material.GLOWSTONE, 20, 10, 1, 31);
        addShopProduct(shop, Material.HAY_BLOCK, 250, -1, 1, 32);
        addShopProduct(shop, Material.MAGMA_BLOCK, 175, 10, 1, 33);
        addShopProduct(shop, Material.BONE_BLOCK, 200, 15, 1, 34);
        addShopProduct(shop, Material.MUD, 20, 1, 2, 3);
        addShopProduct(shop, Material.SCULK, 35, 2.5, 2, 5);
        addShopProduct(shop, Material.DRIED_KELP_BLOCK, 150, 15, 2, 11);
        addShopProduct(shop, Material.BEE_NEST, 200, 4, 2, 12);
        addShopProduct(shop, Material.BEEHIVE, 130, 2, 2, 13);
        addShopProduct(shop, Material.HONEY_BLOCK, 100, 1, 2, 14);
        addShopProduct(shop, Material.HONEYCOMB_BLOCK, 160, 2, 2, 15);
        addShopProduct(shop, Material.BASALT, 250, 3, 2, 19);
        addShopProduct(shop, Material.POLISHED_BASALT, 275, 3, 2, 20);
        addShopProduct(shop, Material.SMOOTH_BASALT, 275, 3, 2, 21);
        addShopProduct(shop, Material.TUFF, 40, 1.5, 2, 22);
        addShopProduct(shop, Material.DRIPSTONE_BLOCK, 125, 1, 2, 23);
        addShopProduct(shop, Material.CALCITE, 150, 1.5, 2, 24);
        addShopProduct(shop, Material.MOSS_BLOCK, 100, 1, 2, 25);
        addShopProduct(shop, Material.AMETHYST_BLOCK, 750, 50, 2, 29);
        addShopProduct(shop, Material.BUDDING_AMETHYST, 350, 20, 2, 30);
        addShopProduct(shop, Material.PEARLESCENT_FROGLIGHT, 40, 5, 2, 31);
        addShopProduct(shop, Material.OCHRE_FROGLIGHT, 40, 5, 2, 32);
        addShopProduct(shop, Material.VERDANT_FROGLIGHT, 40, 5, 2, 33);
        addShopProduct(shop, Material.OAK_WOOD, 20, 1, 3, 0);
        addShopProduct(shop, Material.OAK_LOG, 15, 1, 3, 1);
        addShopProduct(shop, Material.OAK_PLANKS, 4, 0.2, 3, 2);
        addShopProduct(shop, Material.OAK_STAIRS, 8, 1.1, 3, 3);
        addShopProduct(shop, Material.OAK_SLAB, 3, 0.1, 3, 4);
        addShopProduct(shop, Material.OAK_FENCE, 8, 1, 3, 5);
        addShopProduct(shop, Material.OAK_FENCE_GATE, 12, 0.5, 3, 6);
        addShopProduct(shop, Material.OAK_DOOR, 20, 1, 3, 7);
        addShopProduct(shop, Material.OAK_TRAPDOOR, 25, 1, 3, 8);
        addShopProduct(shop, Material.SPRUCE_WOOD, 20, 1, 3, 9);
        addShopProduct(shop, Material.SPRUCE_LOG, 15, 1, 3, 10);
        addShopProduct(shop, Material.SPRUCE_PLANKS, 4, 0.2, 3, 11);
        addShopProduct(shop, Material.SPRUCE_STAIRS, 8, 1.1, 3, 12);
        addShopProduct(shop, Material.SPRUCE_SLAB, 3, 0.1, 3, 13);
        addShopProduct(shop, Material.SPRUCE_FENCE, 8, 1, 3, 14);
        addShopProduct(shop, Material.SPRUCE_FENCE_GATE, 12, 0.5, 3, 15);
        addShopProduct(shop, Material.SPRUCE_DOOR, 20, 1, 3, 16);
        addShopProduct(shop, Material.SPRUCE_TRAPDOOR, 25, 1, 3, 17);
        addShopProduct(shop, Material.BIRCH_WOOD, 20, 1, 3, 18);
        addShopProduct(shop, Material.BIRCH_LOG, 15, 1, 3, 19);
        addShopProduct(shop, Material.BIRCH_PLANKS, 4, 0.2, 3, 20);
        addShopProduct(shop, Material.BIRCH_STAIRS, 8, 1.1, 3, 21);
        addShopProduct(shop, Material.BIRCH_SLAB, 3, 0.1, 3, 22);
        addShopProduct(shop, Material.BIRCH_FENCE, 8, 1, 3, 23);
        addShopProduct(shop, Material.BIRCH_FENCE_GATE, 12, 0.5, 3, 24);
        addShopProduct(shop, Material.BIRCH_DOOR, 20, 1, 3, 25);
        addShopProduct(shop, Material.BIRCH_TRAPDOOR, 25, 1, 3, 26);
        addShopProduct(shop, Material.JUNGLE_WOOD, 20, 1, 3, 27);
        addShopProduct(shop, Material.JUNGLE_LOG, 15, 1, 3, 28);
        addShopProduct(shop, Material.JUNGLE_PLANKS, 4, 0.2, 3, 29);
        addShopProduct(shop, Material.JUNGLE_STAIRS, 8, 1.1, 3, 30);
        addShopProduct(shop, Material.JUNGLE_SLAB, 3, 0.1, 3, 31);
        addShopProduct(shop, Material.JUNGLE_FENCE, 8, 1, 3, 32);
        addShopProduct(shop, Material.JUNGLE_FENCE_GATE, 12, 0.5, 3, 33);
        addShopProduct(shop, Material.JUNGLE_DOOR, 20, 1, 3, 34);
        addShopProduct(shop, Material.JUNGLE_TRAPDOOR, 25, 1, 3, 35);
        addShopProduct(shop, Material.ACACIA_WOOD, 20, 1, 4, 0);
        addShopProduct(shop, Material.ACACIA_LOG, 15, 1, 4, 1);
        addShopProduct(shop, Material.ACACIA_PLANKS, 4, 0.2, 4, 2);
        addShopProduct(shop, Material.ACACIA_STAIRS, 8, 1.1, 4, 3);
        addShopProduct(shop, Material.ACACIA_SLAB, 3, 0.1, 4, 4);
        addShopProduct(shop, Material.ACACIA_FENCE, 8, 1, 4, 5);
        addShopProduct(shop, Material.ACACIA_FENCE_GATE, 12, 0.5, 4, 6);
        addShopProduct(shop, Material.ACACIA_DOOR, 20, 1, 4, 7);
        addShopProduct(shop, Material.ACACIA_TRAPDOOR, 25, 1, 4, 8);
        addShopProduct(shop, Material.DARK_OAK_WOOD, 20, 1, 4, 9);
        addShopProduct(shop, Material.DARK_OAK_LOG, 15, 1, 4, 10);
        addShopProduct(shop, Material.DARK_OAK_PLANKS, 4, 0.2, 4, 11);
        addShopProduct(shop, Material.DARK_OAK_STAIRS, 8, 1.1, 4, 12);
        addShopProduct(shop, Material.DARK_OAK_SLAB, 3, 0.1, 4, 13);
        addShopProduct(shop, Material.DARK_OAK_FENCE, 8, 1, 4, 14);
        addShopProduct(shop, Material.DARK_OAK_FENCE_GATE, 12, 0.5, 4, 15);
        addShopProduct(shop, Material.DARK_OAK_DOOR, 20, 1, 4, 16);
        addShopProduct(shop, Material.DARK_OAK_TRAPDOOR, 25, 1, 4, 17);
        addShopProduct(shop, Material.COBBLESTONE, 3.5, 0.1, 4, 18);
        addShopProduct(shop, Material.COBBLESTONE_STAIRS, 6, 0.15, 4, 19);
        addShopProduct(shop, Material.COBBLESTONE_SLAB, 3.5, 0.08, 4, 20);
        addShopProduct(shop, Material.COBBLESTONE_WALL, 4, 0.1, 4, 21);
        addShopProduct(shop, Material.STONE, 5, 0.2, 4, 22);
        addShopProduct(shop, Material.STONE_STAIRS, 10, 0.3, 4, 23);
        addShopProduct(shop, Material.STONE_SLAB, 35, 0.1, 4, 24);
        addShopProduct(shop, Material.SMOOTH_STONE, 30, 1, 4, 25);
        addShopProduct(shop, Material.SMOOTH_STONE_SLAB, 25, 0.5, 4, 26);
        addShopProduct(shop, Material.STONE_BRICKS, 25, 0.5, 4, 28);
        addShopProduct(shop, Material.STONE_BRICK_STAIRS, 45, 1, 4, 29);
        addShopProduct(shop, Material.STONE_BRICK_SLAB, 15, 0.2, 4, 30);
        addShopProduct(shop, Material.STONE_BRICK_WALL, 30, 0.5, 4, 32);
        addShopProduct(shop, Material.CRACKED_STONE_BRICKS, 25, 0.5, 4, 33);
        addShopProduct(shop, Material.CHISELED_STONE_BRICKS, 35, 0.5, 4, 34);
        addShopProduct(shop, Material.MOSSY_COBBLESTONE, 5, 0.1, 5, 0);
        addShopProduct(shop, Material.MOSSY_COBBLESTONE_STAIRS, 10, 0.2, 5, 1);
        addShopProduct(shop, Material.MOSSY_COBBLESTONE_SLAB, 5, 0.1, 5, 2);
        addShopProduct(shop, Material.MOSSY_COBBLESTONE_WALL, 4, 0.1, 5, 3);
        addShopProduct(shop, Material.MOSSY_STONE_BRICKS, 30, 0.5, 5, 5);
        addShopProduct(shop, Material.MOSSY_STONE_BRICK_STAIRS, 60, 1, 5, 6);
        addShopProduct(shop, Material.MOSSY_STONE_BRICK_SLAB, 30, 0.1, 5, 7);
        addShopProduct(shop, Material.MOSSY_STONE_BRICK_WALL, 40, 0.5, 5, 8);
        addShopProduct(shop, Material.ANDESITE, 4, 0.5, 5, 10);
        addShopProduct(shop, Material.ANDESITE_STAIRS, 8, 0.5, 5, 11);
        addShopProduct(shop, Material.ANDESITE_SLAB, 4, 0.5, 5, 12);
        addShopProduct(shop, Material.ANDESITE_WALL, 10, 0.5, 5, 13);
        addShopProduct(shop, Material.POLISHED_ANDESITE, 8, 0.6, 5, 14);
        addShopProduct(shop, Material.POLISHED_ANDESITE_STAIRS, 16, 1, 5, 15);
        addShopProduct(shop, Material.POLISHED_ANDESITE_SLAB, 8, 0.6, 5, 16);
        addShopProduct(shop, Material.DIORITE, 4, 0.5, 5, 19);
        addShopProduct(shop, Material.DIORITE_STAIRS, 8, 0.5, 5, 20);
        addShopProduct(shop, Material.DIORITE_SLAB, 4, 0.5, 5, 21);
        addShopProduct(shop, Material.DIORITE_WALL, 10, 0.5, 5, 22);
        addShopProduct(shop, Material.POLISHED_DIORITE, 8, 0.6, 5, 23);
        addShopProduct(shop, Material.POLISHED_DIORITE_STAIRS, 16, 1, 5, 24);
        addShopProduct(shop, Material.POLISHED_DIORITE_SLAB, 8, 0.6, 5, 25);
        addShopProduct(shop, Material.GRANITE, 4, 0.5, 5, 28);
        addShopProduct(shop, Material.GRANITE_STAIRS, 8, 0.5, 5, 29);
        addShopProduct(shop, Material.GRANITE_SLAB, 4, 0.5, 5, 30);
        addShopProduct(shop, Material.GRANITE_WALL, 10, 0.5, 5, 31);
        addShopProduct(shop, Material.POLISHED_GRANITE, 8, 0.6, 5, 32);
        addShopProduct(shop, Material.POLISHED_GRANITE_STAIRS, 16, 1, 5, 33);
        addShopProduct(shop, Material.POLISHED_GRANITE_SLAB, 8, 0.6, 5, 34);
        addShopProduct(shop, Material.SAND, 10, 0.5, 6, 0);
        addShopProduct(shop, Material.CHISELED_SANDSTONE, 35, 0.3, 6, 1);
        addShopProduct(shop, Material.SANDSTONE, 25, 0.5, 6, 2);
        addShopProduct(shop, Material.CUT_SANDSTONE, 35, 0.3, 6, 3);
        addShopProduct(shop, Material.DARK_PRISMARINE, 30, 2.5, 6, 4);
        addShopProduct(shop, Material.SMOOTH_SANDSTONE, 35, 0.3, 6, 5);
        addShopProduct(shop, Material.SANDSTONE_STAIRS, 45, 1, 6, 6);
        addShopProduct(shop, Material.SANDSTONE_SLAB, 15, 0.2, 6, 7);
        addShopProduct(shop, Material.SANDSTONE_WALL, 50, 1, 6, 8);
        addShopProduct(shop, Material.RED_SAND, 15, 1, 6, 9);
        addShopProduct(shop, Material.CHISELED_RED_SANDSTONE, 85, 1.5, 6, 10);
        addShopProduct(shop, Material.RED_SANDSTONE, 50, 0.2, 6, 11);
        addShopProduct(shop, Material.CUT_RED_SANDSTONE, 35, 0.3, 6, 12);
        addShopProduct(shop, Material.DARK_PRISMARINE_STAIRS, 75, 4, 6, 13);
        addShopProduct(shop, Material.SMOOTH_RED_SANDSTONE, 35, 0.3, 6, 14);
        addShopProduct(shop, Material.RED_SANDSTONE_STAIRS, 80, 2, 6, 15);
        addShopProduct(shop, Material.RED_SANDSTONE_SLAB, 40, 1, 6, 16);
        addShopProduct(shop, Material.RED_SANDSTONE_WALL, 60, 1.1, 6, 17);
        addShopProduct(shop, Material.PRISMARINE, 15, 1, 6, 18);
        addShopProduct(shop, Material.PRISMARINE_STAIRS, 60, 2.5, 6, 19);
        addShopProduct(shop, Material.PRISMARINE_SLAB, 8, 0.5, 6, 20);
        addShopProduct(shop, Material.PRISMARINE_WALL, 45, 1, 6, 21);
        addShopProduct(shop, Material.DARK_PRISMARINE_SLAB, 25, 1, 6, 22);
        addShopProduct(shop, Material.SEA_LANTERN, 150, 5, 6, 23);
        addShopProduct(shop, Material.PRISMARINE_BRICKS, 30, 2.5, 6, 24);
        addShopProduct(shop, Material.PRISMARINE_BRICK_STAIRS, 75, 4, 6, 25);
        addShopProduct(shop, Material.PRISMARINE_BRICK_SLAB, 25, 1, 6, 26);
        addShopProduct(shop, Material.PURPUR_BLOCK, 25, 0.5, 6, 27);
        addShopProduct(shop, Material.PURPUR_PILLAR, 25, 0.5, 6, 28);
        addShopProduct(shop, Material.PURPUR_STAIRS, 45, 1, 6, 29);
        addShopProduct(shop, Material.PURPUR_SLAB, 15, 0.2, 6, 30);
        addShopProduct(shop, Material.BRICKS, 30, 1.5, 6, 32);
        addShopProduct(shop, Material.BRICK_STAIRS, 50, 2, 6, 33);
        addShopProduct(shop, Material.BRICK_SLAB, 25, 1, 6, 34);
        addShopProduct(shop, Material.BRICK_WALL, 70, 2, 6, 35);
        addShopProduct(shop, Material.PACKED_MUD, 45, 5, 7, 2);
        addShopProduct(shop, Material.MUD_BRICKS, 45, 5, 7, 3);
        addShopProduct(shop, Material.MUD_BRICK_STAIRS, 70, 15, 7, 4);
        addShopProduct(shop, Material.MUD_BRICK_SLAB, 25, 1.5, 7, 5);
        addShopProduct(shop, Material.MUD_BRICK_WALL, 275, 20, 7, 6);
        addShopProduct(shop, Material.NETHERRACK, 5, 0.5, 7, 10);
        addShopProduct(shop, Material.NETHER_BRICKS, 100, 3, 7, 11);
        addShopProduct(shop, Material.CRACKED_NETHER_BRICKS, 100, 2, 7, 12);
        addShopProduct(shop, Material.NETHER_BRICK_STAIRS, 175, 15, 7, 13);
        addShopProduct(shop, Material.NETHER_BRICK_SLAB, 30, 2.5, 7, 14);
        addShopProduct(shop, Material.NETHER_BRICK_FENCE, 110, 10, 7, 15);
        addShopProduct(shop, Material.NETHER_BRICK_WALL, 110, 10, 7, 16);
        addShopProduct(shop, Material.QUARTZ_BLOCK, 80, 1, 7, 18);
        addShopProduct(shop, Material.NETHER_WART_BLOCK, 500, 5, 7, 20);
        addShopProduct(shop, Material.RED_NETHER_BRICKS, 450, 10, 7, 21);
        addShopProduct(shop, Material.RED_NETHER_BRICK_STAIRS, 450, 10, 7, 22);
        addShopProduct(shop, Material.RED_NETHER_BRICK_SLAB, 450, 10, 7, 23);
        addShopProduct(shop, Material.RED_NETHER_BRICK_WALL, 475, 10, 7, 24);
        addShopProduct(shop, Material.SMOOTH_QUARTZ_SLAB, 105, 2, 7, 26);
        addShopProduct(shop, Material.QUARTZ_STAIRS, 140, 3, 7, 28);
        addShopProduct(shop, Material.QUARTZ_SLAB, 50, 2, 7, 29);
        addShopProduct(shop, Material.CHISELED_QUARTZ_BLOCK, 105, 2, 7, 30);
        addShopProduct(shop, Material.QUARTZ_PILLAR, 80, 1, 7, 32);
        addShopProduct(shop, Material.SMOOTH_QUARTZ, 105, 2, 7, 33);
        addShopProduct(shop, Material.SMOOTH_QUARTZ_STAIRS, 185, 3, 7, 34);
        addShopProduct(shop, Material.MANGROVE_WOOD, 20, 1, 8, 0);
        addShopProduct(shop, Material.MANGROVE_LOG, 15, 1, 8, 1);
        addShopProduct(shop, Material.MANGROVE_PLANKS, 4, 0.2, 8, 2);
        addShopProduct(shop, Material.MANGROVE_STAIRS, 8, 1, 8, 3);
        addShopProduct(shop, Material.MANGROVE_SLAB, 3, 0.1, 8, 4);
        addShopProduct(shop, Material.MANGROVE_FENCE, 8, 1, 8, 5);
        addShopProduct(shop, Material.MANGROVE_FENCE_GATE, 12, 1, 8, 6);
        addShopProduct(shop, Material.MANGROVE_DOOR, 20, 1, 8, 7);
        addShopProduct(shop, Material.MANGROVE_TRAPDOOR, 25, 1, 8, 8);
        addShopProduct(shop, Material.CHERRY_WOOD, 20, 1, 8, 9);
        addShopProduct(shop, Material.CHERRY_LOG, 15, 1, 8, 10);
        addShopProduct(shop, Material.CHERRY_PLANKS, 4, 0.5, 8, 11);
        addShopProduct(shop, Material.CHERRY_STAIRS, 8, 1, 8, 12);
        addShopProduct(shop, Material.CHERRY_SLAB, 3, 0.1, 8, 13);
        addShopProduct(shop, Material.CHERRY_FENCE, 8, 1, 8, 14);
        addShopProduct(shop, Material.CHERRY_FENCE_GATE, 12, 1, 8, 15);
        addShopProduct(shop, Material.CHERRY_DOOR, 20, 1, 8, 16);
        addShopProduct(shop, Material.CHERRY_TRAPDOOR, 25, 1, 8, 17);
        addShopProduct(shop, Material.CRIMSON_HYPHAE, 100, 2, 8, 18);
        addShopProduct(shop, Material.CRIMSON_STEM, 100, 2, 8, 19);
        addShopProduct(shop, Material.CRIMSON_PLANKS, 30, 1, 8, 20);
        addShopProduct(shop, Material.CRIMSON_STAIRS, 75, 2, 8, 21);
        addShopProduct(shop, Material.CRIMSON_SLAB, 30, 1, 8, 22);
        addShopProduct(shop, Material.CRIMSON_FENCE, 65, 2, 8, 23);
        addShopProduct(shop, Material.CRIMSON_FENCE_GATE, 75, 2, 8, 24);
        addShopProduct(shop, Material.CRIMSON_DOOR, 90, 2, 8, 25);
        addShopProduct(shop, Material.CRIMSON_TRAPDOOR, 100, 1.5, 8, 26);
        addShopProduct(shop, Material.WARPED_HYPHAE, 125, 3, 8, 27);
        addShopProduct(shop, Material.WARPED_STEM, 125, 3, 8, 28);
        addShopProduct(shop, Material.WARPED_PLANKS, 40, 1, 8, 29);
        addShopProduct(shop, Material.WARPED_STAIRS, 85, 2, 8, 30);
        addShopProduct(shop, Material.WARPED_SLAB, 30, 1, 8, 31);
        addShopProduct(shop, Material.WARPED_FENCE, 75, 2, 8, 32);
        addShopProduct(shop, Material.WARPED_FENCE_GATE, 85, 2, 8, 33);
        addShopProduct(shop, Material.WARPED_DOOR, 115, 2.5, 8, 34);
        addShopProduct(shop, Material.WARPED_TRAPDOOR, 125, 2.5, 8, 35);
        addShopProduct(shop, Material.COPPER_BLOCK, 165, 15, 9, 0);
        addShopProduct(shop, Material.CUT_COPPER, 170, 15, 9, 1);
        addShopProduct(shop, Material.CUT_COPPER_STAIRS, 260, 18, 9, 2);
        addShopProduct(shop, Material.CUT_COPPER_SLAB, 60, 1.5, 9, 3);
        addShopProduct(shop, Material.WAXED_COPPER_BLOCK, 230, 18, 9, 5);
        addShopProduct(shop, Material.WAXED_CUT_COPPER, 235, 18, 9, 6);
        addShopProduct(shop, Material.WAXED_CUT_COPPER_STAIRS, 355, 20, 9, 7);
        addShopProduct(shop, Material.WAXED_CUT_COPPER_SLAB, 120, 3, 9, 8);
        addShopProduct(shop, Material.EXPOSED_COPPER, 165, 15, 9, 9);
        addShopProduct(shop, Material.EXPOSED_CUT_COPPER, 170, 15, 9, 10);
        addShopProduct(shop, Material.EXPOSED_CUT_COPPER_STAIRS, 260, 18, 9, 11);
        addShopProduct(shop, Material.EXPOSED_CUT_COPPER_SLAB, 60, 1.5, 9, 12);
        addShopProduct(shop, Material.WAXED_EXPOSED_COPPER, 230, 18, 9, 14);
        addShopProduct(shop, Material.WAXED_EXPOSED_CUT_COPPER, 235, 18, 9, 15);
        addShopProduct(shop, Material.WAXED_EXPOSED_CUT_COPPER_STAIRS, 355, 20, 9, 16);
        addShopProduct(shop, Material.WAXED_EXPOSED_CUT_COPPER_SLAB, 120, 3, 9, 17);
        addShopProduct(shop, Material.WEATHERED_COPPER, 165, 15, 9, 18);
        addShopProduct(shop, Material.WEATHERED_CUT_COPPER, 170, 15, 9, 19);
        addShopProduct(shop, Material.WEATHERED_CUT_COPPER_STAIRS, 260, 18, 9, 20);
        addShopProduct(shop, Material.WEATHERED_CUT_COPPER_SLAB, 60, 1.5, 9, 21);
        addShopProduct(shop, Material.WAXED_WEATHERED_COPPER, 230, 18, 9, 23);
        addShopProduct(shop, Material.WAXED_WEATHERED_CUT_COPPER, 235, 18, 9, 24);
        addShopProduct(shop, Material.WAXED_WEATHERED_CUT_COPPER_STAIRS, 355, 20, 9, 25);
        addShopProduct(shop, Material.WAXED_WEATHERED_CUT_COPPER_SLAB, 120, 3, 9, 26);
        addShopProduct(shop, Material.OXIDIZED_COPPER, 165, 15, 9, 27);
        addShopProduct(shop, Material.OXIDIZED_CUT_COPPER, 170, 15, 9, 28);
        addShopProduct(shop, Material.OXIDIZED_CUT_COPPER_STAIRS, 260, 18, 9, 29);
        addShopProduct(shop, Material.OXIDIZED_CUT_COPPER_SLAB, 60, 1.5, 9, 30);
        addShopProduct(shop, Material.WAXED_OXIDIZED_COPPER, 230, 18, 9, 32);
        addShopProduct(shop, Material.WAXED_OXIDIZED_CUT_COPPER, 235, 18, 9, 33);
        addShopProduct(shop, Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS, 355, 20, 9, 34);
        addShopProduct(shop, Material.WAXED_OXIDIZED_CUT_COPPER_SLAB, 120, 3, 9, 35);
        addShopProduct(shop, Material.GILDED_BLACKSTONE, 100, 10, 10, 11);
        addShopProduct(shop, Material.BLACKSTONE, 50, 2, 10, 12);
        addShopProduct(shop, Material.BLACKSTONE_STAIRS, 90, 3, 10, 13);
        addShopProduct(shop, Material.BLACKSTONE_SLAB, 50, 1.5, 10, 14);
        addShopProduct(shop, Material.BLACKSTONE_WALL, 50, 1.5, 10, 15);
        addShopProduct(shop, Material.POLISHED_BLACKSTONE, 65, 2, 10, 20);
        addShopProduct(shop, Material.CHISELED_POLISHED_BLACKSTONE, 125, 3, 10, 21);
        addShopProduct(shop, Material.POLISHED_BLACKSTONE_STAIRS, 125, 3, 10, 22);
        addShopProduct(shop, Material.POLISHED_BLACKSTONE_SLAB, 125, 3, 10, 23);
        addShopProduct(shop, Material.POLISHED_BLACKSTONE_WALL, 65, 2, 10, 24);
        addShopProduct(shop, Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, 125, 3, 10, 29);
        addShopProduct(shop, Material.POLISHED_BLACKSTONE_BRICKS, 70, 2, 10, 30);
        addShopProduct(shop, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, 135, 3, 10, 31);
        addShopProduct(shop, Material.POLISHED_BLACKSTONE_BRICK_SLAB, 70, 1.5, 10, 32);
        addShopProduct(shop, Material.POLISHED_BLACKSTONE_BRICK_WALL, 70, 2, 10, 33);
        addShopProduct(shop, Material.DEEPSLATE, 30, 0.5, 11, 9);
        addShopProduct(shop, Material.INFESTED_DEEPSLATE, 35, -1, 11, 10);
        addShopProduct(shop, Material.DEEPSLATE_BRICKS, 40, -1, 11, 11);
        addShopProduct(shop, Material.CHISELED_DEEPSLATE, 50, -1, 11, 12);
        addShopProduct(shop, Material.DEEPSLATE_BRICK_WALL, 40, -1, 11, 13);
        addShopProduct(shop, Material.CRACKED_DEEPSLATE_BRICKS, 35, -1, 11, 14);
        addShopProduct(shop, Material.DEEPSLATE_BRICK_STAIRS, 90, -1, 11, 15);
        addShopProduct(shop, Material.DEEPSLATE_BRICK_SLAB, 40, -1, 11, 16);
        addShopProduct(shop, Material.POLISHED_DEEPSLATE, 65, -1, 11, 18);
        addShopProduct(shop, Material.POLISHED_DEEPSLATE_STAIRS, 125, -1, 11, 19);
        addShopProduct(shop, Material.POLISHED_DEEPSLATE_SLAB, 65, -1, 11, 20);
        addShopProduct(shop, Material.POLISHED_DEEPSLATE_WALL, 65, -1, 11, 21);
        addShopProduct(shop, Material.COBBLED_DEEPSLATE, 35, 0.5, 11, 23);
        addShopProduct(shop, Material.COBBLED_DEEPSLATE_STAIRS, 65, -1, 11, 24);
        addShopProduct(shop, Material.COBBLED_DEEPSLATE_SLAB, 35, -1, 11, 25);
        addShopProduct(shop, Material.COBBLED_DEEPSLATE_WALL, 35, -1, 11, 26);
        addShopProduct(shop, Material.DEEPSLATE_TILES, 60, -1, 11, 29);
        addShopProduct(shop, Material.CRACKED_DEEPSLATE_TILES, 50, -1, 11, 30);
        addShopProduct(shop, Material.DEEPSLATE_TILE_STAIRS, 115, -1, 11, 31);
        addShopProduct(shop, Material.DEEPSLATE_TILE_SLAB, 50, -1, 11, 32);
        addShopProduct(shop, Material.DEEPSLATE_TILE_WALL, 50, -1, 11, 33);
        if (Version.isAtLeast(Version.MC_1_21_2)) {
            addShopProduct(shop, Material.PALE_OAK_WOOD, 20, 1, 12, 0);
            addShopProduct(shop, Material.PALE_OAK_LOG, 15, 1, 12, 1);
            addShopProduct(shop, Material.PALE_OAK_PLANKS, 4, 0.2, 12, 2);
            addShopProduct(shop, Material.PALE_OAK_STAIRS, 8, 1.1, 12, 3);
            addShopProduct(shop, Material.PALE_OAK_SLAB, 3, 0.1, 12, 4);
            addShopProduct(shop, Material.PALE_OAK_FENCE, 8, 1, 12, 5);
            addShopProduct(shop, Material.PALE_OAK_FENCE_GATE, 12, 0.5, 12, 6);
            addShopProduct(shop, Material.PALE_OAK_DOOR, 20, 1, 12, 7);
            addShopProduct(shop, Material.PALE_OAK_TRAPDOOR, 25, 1, 12, 8);
        }
        addShopProduct(shop, Material.BAMBOO_BLOCK, 20, 1, 12, 9);
        addShopProduct(shop, Material.STRIPPED_BAMBOO_BLOCK, 15, 1, 12, 10);
        addShopProduct(shop, Material.BAMBOO_PLANKS, 4, 0.2, 12, 11);
        addShopProduct(shop, Material.BAMBOO_STAIRS, 8, 1.1, 12, 12);
        addShopProduct(shop, Material.BAMBOO_SLAB, 3, 0.1, 12, 13);
        addShopProduct(shop, Material.BAMBOO_FENCE, 8, 1, 12, 14);
        addShopProduct(shop, Material.BAMBOO_FENCE_GATE, 12, 0.5, 12, 15);
        addShopProduct(shop, Material.BAMBOO_DOOR, 20, 1, 12, 16);
        addShopProduct(shop, Material.BAMBOO_TRAPDOOR, 25, 1, 12, 17);
    }

    private static void addFarmingItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.WHEAT_SEEDS, 25, 1, 1, 11);
        addShopProduct(shop, Material.PUMPKIN_SEEDS, 30, 1, 1, 12);
        addShopProduct(shop, Material.MELON_SEEDS, 25, 1, 1, 13);
        addShopProduct(shop, Material.BEETROOT_SEEDS, 25, 1, 1, 14);
        addShopProduct(shop, Material.BEETROOT, 20, 2.5, 1, 15);
        addShopProduct(shop, Material.COCOA_BEANS, 25, 2.5, 1, 19);
        addShopProduct(shop, Material.NETHER_WART, 200, 4, 1, 20);
        addShopProduct(shop, Material.SUGAR_CANE, 30, 3, 1, 21);
        addShopProduct(shop, Material.WHEAT, 20, 2.5, 1, 22);
        addShopProduct(shop, Material.PUMPKIN, 50, 2, 1, 23);
        addShopProduct(shop, Material.MELON, 25, 1.5, 1, 24);
        addShopProduct(shop, Material.CACTUS, 300, 2.5, 1, 25);
        addShopProduct(shop, Material.OAK_SAPLING, 30, 1, 1, 29);
        addShopProduct(shop, Material.SPRUCE_SAPLING, 30, 1, 1, 30);
        addShopProduct(shop, Material.BIRCH_SAPLING, 30, 1, 1, 31);
        addShopProduct(shop, Material.JUNGLE_SAPLING, 30, 1, 1, 32);
        addShopProduct(shop, Material.ACACIA_SAPLING, 30, 1, 1, 33);
        addShopProduct(shop, Material.DARK_OAK_SAPLING, 30, 1, 2, 11);
        addShopProduct(shop, Material.MANGROVE_PROPAGULE, 30, 1, 2, 12);
        addShopProduct(shop, Material.CARROT, 25, 3, 2, 14);
        addShopProduct(shop, Material.POTATO, 25, 3, 2, 15);
        addShopProduct(shop, Material.BROWN_MUSHROOM, 25, 2.5, 2, 19);
        addShopProduct(shop, Material.RED_MUSHROOM, 25, 2.5, 2, 20);
        addShopProduct(shop, Material.CRIMSON_FUNGUS, 40, 3, 2, 21);
        addShopProduct(shop, Material.WARPED_FUNGUS, 55, 3.5, 2, 22);
        addShopProduct(shop, Material.WEEPING_VINES, 30, 1.2, 2, 23);
        addShopProduct(shop, Material.WARPED_ROOTS, 45, 1.4, 2, 24);
        addShopProduct(shop, Material.TWISTING_VINES, 50, 1.5, 2, 25);
        addShopProduct(shop, Material.POPPY, 10, 0.5, 2, 29);
        addShopProduct(shop, Material.CHORUS_PLANT, 15, -1, 2, 30);
        addShopProduct(shop, Material.CHORUS_FRUIT, 6, 2, 2, 31);
        addShopProduct(shop, Material.KELP, 5, 0.5, 2, 32);
        addShopProduct(shop, Material.SHROOMLIGHT, 50, 3.5, 2, 33);
    }

    private static void addRedstoneItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.REDSTONE, 15, 3, 1, 3);
        addShopProduct(shop, Material.LECTERN, 300, 1, 1, 5);
        addShopProduct(shop, Material.REPEATER, 350, -1, 1, 11);
        addShopProduct(shop, Material.COMPARATOR, 400, -1, 1, 12);
        addShopProduct(shop, Material.HOPPER, 400, -1, 1, 13);
        addShopProduct(shop, Material.PISTON, 500, -1, 1, 14);
        addShopProduct(shop, Material.STICKY_PISTON, 800, -1, 1, 15);
        addShopProduct(shop, Material.DAYLIGHT_DETECTOR, 800, -1, 1, 19);
        addShopProduct(shop, Material.TARGET, 100, 3, 1, 20);
        addShopProduct(shop, Material.NOTE_BLOCK, 700, -1, 1, 21);
        addShopProduct(shop, Material.DROPPER, 500, -1, 1, 22);
        addShopProduct(shop, Material.DISPENSER, 500, -1, 1, 23);
        addShopProduct(shop, Material.OBSERVER, 500, -1, 1, 24);
        addShopProduct(shop, Material.CRAFTER, 500, -1, 1, 25);
        addShopProduct(shop, Material.TRAPPED_CHEST, 250, -1, 1, 29);
        addShopProduct(shop, Material.REDSTONE_TORCH, 35, -1, 1, 30);
        addShopProduct(shop, Material.REDSTONE_LAMP, 800, -1, 1, 31);
        addShopProduct(shop, Material.LEVER, 50, -1, 1, 32);
        addShopProduct(shop, Material.TRIPWIRE_HOOK, 85, -1, 1, 33);
    }

    private static void addFoodItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.APPLE, 20, 4, 1, 3);
        addShopProduct(shop, Material.GOLDEN_APPLE, 100, 10, 1, 4);
        addShopProduct(shop, Material.ENCHANTED_GOLDEN_APPLE, 2_000, 80, 1, 5);
        addShopProduct(shop, Material.BREAD, 25, 5, 1, 11);
        addShopProduct(shop, Material.MUSHROOM_STEW, 20, 4, 1, 12);
        addShopProduct(shop, Material.RABBIT_STEW, 40, 3, 1, 13);
        addShopProduct(shop, Material.BEETROOT_SOUP, 20, 4, 1, 14);
        addShopProduct(shop, Material.PUMPKIN_PIE, 20, 5, 1, 15);
        addShopProduct(shop, Material.COOKED_CHICKEN, 25, 2, 1, 19);
        addShopProduct(shop, Material.BAKED_POTATO, 30, 6, 1, 20);
        addShopProduct(shop, Material.COOKED_COD, 20, 4, 1, 21);
        addShopProduct(shop, Material.COOKED_SALMON, 25, 5, 1, 22);
        addShopProduct(shop, Material.COOKED_RABBIT, 20, 3, 1, 23);
        addShopProduct(shop, Material.COOKED_PORKCHOP, 30, 3, 1, 24);
        addShopProduct(shop, Material.COOKED_BEEF, 30, 3, 1, 25);
        addShopProduct(shop, Material.COOKED_MUTTON, 25, 3, 1, 28);
        addShopProduct(shop, Material.COOKIE, 30, 3, 1, 29);
        addShopProduct(shop, Material.CAKE, 50, 10, 1, 30);
        addShopProduct(shop, Material.SWEET_BERRIES, 35, 1, 1, 32);
        addShopProduct(shop, Material.GLOW_BERRIES, 40, 1.1, 1, 33);
        addShopProduct(shop, Material.DRIED_KELP, 15, 1, 1, 34);
        addShopProduct(shop, Material.COD, 10, 1.5, 2, 12);
        addShopProduct(shop, Material.TROPICAL_FISH, 12, 2, 2, 14);
        addShopProduct(shop, Material.SALMON, 12, 2, 2, 20);
        addShopProduct(shop, Material.RABBIT, 10, 1.5, 2, 21);
        addShopProduct(shop, Material.PORKCHOP, 15, 1.5, 2, 22);
        addShopProduct(shop, Material.MUTTON, 12, 1.5, 2, 23);
        addShopProduct(shop, Material.PUFFERFISH, 50, 3, 2, 24);
        addShopProduct(shop, Material.CHICKEN, 10, 0.5, 2, 29);
        addShopProduct(shop, Material.BEEF, 15, 1.5, 2, 33);
    }

    private static void addMobDropsItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.ROTTEN_FLESH, 12, 5, 1, 11);
        addShopProduct(shop, Material.BONE, 30, 5, 1, 12);
        addShopProduct(shop, Material.GUNPOWDER, 30, 5, 1, 13);
        addShopProduct(shop, Material.STRING, 30, 3, 1, 14);
        addShopProduct(shop, Material.SPIDER_EYE, 40, 3, 1, 15);
        addShopProduct(shop, Material.ENDER_PEARL, 50, 8, 1, 20);
        addShopProduct(shop, Material.SLIME_BALL, 40, 4, 1, 21);
        addShopProduct(shop, Material.BLAZE_ROD, 50, 5, 1, 22);
        addShopProduct(shop, Material.MAGMA_CREAM, 40, 4, 1, 23);
        addShopProduct(shop, Material.GHAST_TEAR, 200, 12, 1, 24);
        addShopProduct(shop, Material.LEATHER, 50, 6, 1, 29);
        addShopProduct(shop, Material.RABBIT_HIDE, 20, 3, 1, 30);
        addShopProduct(shop, Material.RABBIT_FOOT, 50, 8, 1, 31);
        addShopProduct(shop, Material.INK_SAC, 30, 6, 1, 32);
        addShopProduct(shop, Material.GLOW_INK_SAC, 30, 7, 1, 33);
        addShopProduct(shop, Material.FEATHER, 20, 3, 2, 11);
        addShopProduct(shop, Material.EGG, 20, 3, 2, 12);
        addShopProduct(shop, Material.ARROW, 50, 3, 2, 13);
        addShopProduct(shop, Material.PRISMARINE_SHARD, 10, 2, 2, 14);
        addShopProduct(shop, Material.PRISMARINE_CRYSTALS, 10, 2, 2, 15);
        addShopProduct(shop, Material.TOTEM_OF_UNDYING, 100_000, 3, 2, 20);
        addShopProduct(shop, Material.TURTLE_SCUTE, 150, 10, 2, 21);
        addShopProduct(shop, Material.PHANTOM_MEMBRANE, 250, 12, 2, 22);
        addShopProduct(shop, Material.NAUTILUS_SHELL, 1_000, 10, 2, 23);
        addShopProduct(shop, Material.ARMADILLO_SCUTE, 150, 10, 2, 24);
        addShopProduct(shop, Material.SCULK_CATALYST, 1_250, 12, 2, 31);
    }

    private static void addMiscellaneousItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.COMPOSTER, 50, -1, 1, 3);
        addShopProduct(shop, Material.HONEYCOMB, 60, 1, 1, 4);
        addShopProduct(shop, Material.SPYGLASS, 350, -1, 1, 5);
        addShopProduct(shop, Material.BREWING_STAND, 1_000, -1, 1, 11);
        addShopProduct(shop, Material.NETHER_STAR, 500_000, -1, 1, 12);
        addShopProduct(shop, Material.ENDER_EYE, 30_000, -1, 1, 13);
        addShopProduct(shop, Material.CAULDRON, 400, -1, 1, 14);
        addShopProduct(shop, Material.CLOCK, 350, -1, 1, 15);
        addShopProduct(shop, Material.COMPASS, 300, -1, 1, 19);
        addShopProduct(shop, Material.SADDLE, 8_500, -1, 1, 20);
        addShopProduct(shop, Material.ANVIL, 3_000, -1, 1, 21);
        addShopProduct(shop, Material.BEACON, 550_000, -1, 1, 22);
        addShopProduct(shop, Material.ENDER_CHEST, 50_000, -1, 1, 23);
        addShopProduct(shop, Material.ELYTRA, 1_000_000, -1, 1, 24);
        addShopProduct(shop, Material.HEART_OF_THE_SEA, 100_000, -1, 1, 25);
        addShopProduct(shop, Material.CONDUIT, 110_000, -1, 1, 29);
        addShopProduct(shop, Material.AMETHYST_SHARD, 190, -1, 1, 30);
        addShopProduct(shop, Material.ECHO_SHARD, 3_000, 50, 1, 31);
        addShopProduct(shop, Material.SCULK_SHRIEKER, 5_000, 50, 1, 32);
        addShopProduct(shop, Material.SCULK_SENSOR, 6_500, 100, 1, 33);
        addShopProduct(shop, Material.LEATHER_HORSE_ARMOR, 1_500, -1, 2, 2);
        addShopProduct(shop, Material.IRON_HORSE_ARMOR, 3_500, -1, 2, 3);
        addShopProduct(shop, Material.GOLDEN_HORSE_ARMOR, 4_000, -1, 2, 5);
        addShopProduct(shop, Material.DIAMOND_HORSE_ARMOR, 5_000, -1, 2, 6);
        addShopProduct(shop, Material.BUCKET, 100, -1, 2, 11);
        addShopProduct(shop, Material.MILK_BUCKET, 120, -1, 2, 12);
        addShopProduct(shop, Material.WATER_BUCKET, 200, -1, 2, 14);
        addShopProduct(shop, Material.LAVA_BUCKET, 3_000, -1, 2, 15);
        addShopProduct(shop, Material.POWDER_SNOW_BUCKET, 200, -1, 2, 19);
        addShopProduct(shop, Material.PUFFERFISH_BUCKET, 1_250, -1, 2, 20);
        addShopProduct(shop, Material.SALMON_BUCKET, 1_250, -1, 2, 21);
        addShopProduct(shop, Material.COD_BUCKET, 1_250, -1, 2, 22);
        addShopProduct(shop, Material.TROPICAL_FISH_BUCKET, 1_250, -1, 2, 23);
        addShopProduct(shop, Material.AXOLOTL_BUCKET, 1_250, -1, 2, 24);
        addShopProduct(shop, Material.TADPOLE_BUCKET, 1_250, -1, 2, 25);
        addShopProduct(shop, Material.OAK_BOAT, 125, -1, 2, 28);
        addShopProduct(shop, Material.SPRUCE_BOAT, 125, -1, 2, 29);
        addShopProduct(shop, Material.BIRCH_BOAT, 125, -1, 2, 30);
        addShopProduct(shop, Material.JUNGLE_BOAT, 125, -1, 2, 31);
        addShopProduct(shop, Material.ACACIA_BOAT, 125, -1, 2, 32);
        addShopProduct(shop, Material.DARK_OAK_BOAT, 125, -1, 2, 33);
        addShopProduct(shop, Material.MANGROVE_BOAT, 125, -1, 2, 34);
        addShopProduct(shop, Material.CHERRY_BOAT, 125, -1, 2, 39);
        addShopProduct(shop, Material.BAMBOO_RAFT, 125, -1, 2, 40);
        if (Version.isAtLeast(Version.MC_1_21_2)) {
            addShopProduct(shop, Material.PALE_OAK_BOAT, 125, -1, 2, 41);
        }
    }

    private static void addPotionsItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.POTION, PotionType.LONG_REGENERATION, 300, -1, 1, 1);
        addShopProduct(shop, Material.POTION, PotionType.LONG_SWIFTNESS, 350, -1, 1, 2);
        addShopProduct(shop, Material.POTION, PotionType.LONG_STRENGTH, 450, -1, 1, 3);
        addShopProduct(shop, Material.POTION, PotionType.HEALING, 250, -1, 1, 4);
        addShopProduct(shop, Material.POTION, PotionType.FIRE_RESISTANCE, 400, -1, 1, 5);
        addShopProduct(shop, Material.POTION, PotionType.WATER_BREATHING, 200, -1, 1, 6);
        addShopProduct(shop, Material.POTION, PotionType.LONG_LEAPING, 250, -1, 1, 7);
        addShopProduct(shop, Material.POTION, PotionType.STRONG_REGENERATION, 400, -1, 1, 10);
        addShopProduct(shop, Material.POTION, PotionType.STRONG_SWIFTNESS, 500, -1, 1, 11);
        addShopProduct(shop, Material.POTION, PotionType.STRONG_STRENGTH, 600, -1, 1, 12);
        addShopProduct(shop, Material.POTION, PotionType.STRONG_HEALING, 350, -1, 1, 13);
        addShopProduct(shop, Material.POTION, PotionType.LONG_FIRE_RESISTANCE, 500, -1, 1, 14);
        addShopProduct(shop, Material.POTION, PotionType.LONG_WATER_BREATHING, 300, -1, 1, 15);
        addShopProduct(shop, Material.POTION, PotionType.STRONG_LEAPING, 400, -1, 1, 16);
        addShopProduct(shop, Material.POTION, PotionType.NIGHT_VISION, 250, -1, 1, 19);
        addShopProduct(shop, Material.POTION, PotionType.INVISIBILITY, 250, -1, 1, 20);
        addShopProduct(shop, Material.POTION, PotionType.HARMING, 300, -1, 1, 21);
        addShopProduct(shop, Material.POTION, PotionType.SLOW_FALLING, 250, -1, 1, 22);
        addShopProduct(shop, Material.POTION, PotionType.LONG_POISON, 250, -1, 1, 23);
        addShopProduct(shop, Material.POTION, PotionType.SLOWNESS, 150, -1, 1, 24);
        addShopProduct(shop, Material.POTION, PotionType.WEAKNESS, 150, -1, 1, 25);
        addShopProduct(shop, Material.POTION, PotionType.LONG_NIGHT_VISION, 400, -1, 1, 28);
        addShopProduct(shop, Material.POTION, PotionType.LONG_INVISIBILITY, 400, -1, 1, 29);
        addShopProduct(shop, Material.POTION, PotionType.STRONG_HARMING, 400, -1, 1, 30);
        addShopProduct(shop, Material.POTION, PotionType.LONG_SLOW_FALLING, 350, -1, 1, 31);
        addShopProduct(shop, Material.POTION, PotionType.STRONG_POISON, 400, -1, 1, 32);
        addShopProduct(shop, Material.POTION, PotionType.LONG_SLOWNESS, 250, -1, 1, 33);
        addShopProduct(shop, Material.POTION, PotionType.LONG_WEAKNESS, 250, -1, 1, 34);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_REGENERATION, 300, -1, 2, 1);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_SWIFTNESS, 350, -1, 2, 2);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_STRENGTH, 450, -1, 2, 3);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.HEALING, 250, -1, 2, 4);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.FIRE_RESISTANCE, 400, -1, 2, 5);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.WATER_BREATHING, 200, -1, 2, 6);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_LEAPING, 250, -1, 2, 7);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.STRONG_REGENERATION, 400, -1, 2, 10);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.STRONG_SWIFTNESS, 500, -1, 2, 11);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.STRONG_STRENGTH, 650, -1, 2, 12);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.STRONG_HEALING, 350, -1, 2, 13);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_FIRE_RESISTANCE, 500, -1, 2, 14);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_WATER_BREATHING, 300, -1, 2, 15);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.STRONG_LEAPING, 400, -1, 2, 16);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.NIGHT_VISION, 250, -1, 2, 19);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.INVISIBILITY, 250, -1, 2, 20);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.HARMING, 300, -1, 2, 21);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.SLOW_FALLING, 250, -1, 2, 22);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_POISON, 250, -1, 2, 23);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.SLOWNESS, 150, -1, 2, 24);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.WEAKNESS, 150, -1, 2, 25);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_NIGHT_VISION, 400, -1, 2, 28);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_INVISIBILITY, 400, -1, 2, 29);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.STRONG_HARMING, 400, -1, 2, 30);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_SLOW_FALLING, 350, -1, 2, 31);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.STRONG_POISON, 400, -1, 2, 32);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_SLOWNESS, 250, -1, 2, 33);
        addShopProduct(shop, Material.SPLASH_POTION, PotionType.LONG_WEAKNESS, 250, -1, 2, 34);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_REGENERATION, 200, -1, 3, 1);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_SWIFTNESS, 200, -1, 3, 2);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_STRENGTH, 250, -1, 3, 3);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.HEALING, 200, -1, 3, 4);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.FIRE_RESISTANCE, 200, -1, 3, 5);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.WATER_BREATHING, 100, -1, 3, 6);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_LEAPING, 200, -1, 3, 7);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.STRONG_REGENERATION, 300, -1, 3, 10);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.STRONG_SWIFTNESS, 300, -1, 3, 11);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.STRONG_STRENGTH, 300, -1, 3, 12);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.STRONG_HEALING, 300, -1, 3, 13);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_FIRE_RESISTANCE, 300, -1, 3, 14);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_WATER_BREATHING, 150, -1, 3, 15);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.STRONG_LEAPING, 300, -1, 3, 16);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.NIGHT_VISION, 150, -1, 3, 19);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.INVISIBILITY, 150, -1, 3, 20);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.HARMING, 200, -1, 3, 21);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.SLOW_FALLING, 200, -1, 3, 22);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_POISON, 150, -1, 3, 23);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.SLOWNESS, 100, -1, 3, 24);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.WEAKNESS, 100, -1, 3, 25);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_NIGHT_VISION, 200, -1, 3, 28);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_INVISIBILITY, 200, -1, 3, 29);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.STRONG_HARMING, 300, -1, 3, 30);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_SLOW_FALLING, 300, -1, 3, 31);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.STRONG_POISON, 300, -1, 3, 32);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_SLOWNESS, 250, -1, 3, 33);
        addShopProduct(shop, Material.LINGERING_POTION, PotionType.LONG_WEAKNESS, 250, -1, 3, 34);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_REGENERATION, 350, -1, 4, 1);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_SWIFTNESS, 400, -1, 4, 2);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_STRENGTH, 500, -1, 4, 3);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.HEALING, 350, -1, 4, 4);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.FIRE_RESISTANCE, 350, -1, 4, 5);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.WATER_BREATHING, 250, -1, 4, 6);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_LEAPING, 250, -1, 4, 7);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.STRONG_REGENERATION, 450, -1, 4, 10);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.STRONG_SWIFTNESS, 600, -1, 4, 11);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.STRONG_STRENGTH, 700, -1, 4, 12);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.STRONG_HEALING, 500, -1, 4, 13);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_FIRE_RESISTANCE, 500, -1, 4, 14);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_WATER_BREATHING, 400, -1, 4, 15);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.STRONG_LEAPING, 350, -1, 4, 16);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.NIGHT_VISION, 250, -1, 4, 19);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.INVISIBILITY, 400, -1, 4, 20);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.HARMING, 500, -1, 4, 21);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.SLOW_FALLING, 350, -1, 4, 22);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_POISON, 400, -1, 4, 23);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.SLOWNESS, 250, -1, 4, 24);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.WEAKNESS, 250, -1, 4, 25);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_NIGHT_VISION, 350, -1, 4, 28);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_INVISIBILITY, 650, -1, 4, 29);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.STRONG_HARMING, 700, -1, 4, 30);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_SLOW_FALLING, 500, -1, 4, 31);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.STRONG_POISON, 500, -1, 4, 32);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_SLOWNESS, 400, -1, 4, 33);
        addShopProduct(shop, Material.TIPPED_ARROW, PotionType.LONG_WEAKNESS, 400, -1, 4, 34);
    }

    private static void addDecorationItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.OAK_LEAVES, 3, -1, 1, 1);
        addShopProduct(shop, Material.SPRUCE_LEAVES, 3, -1, 1, 2);
        addShopProduct(shop, Material.BIRCH_LEAVES, 3, -1, 1, 3);
        addShopProduct(shop, Material.JUNGLE_LEAVES, 3, -1, 1, 4);
        addShopProduct(shop, Material.ACACIA_LEAVES, 3, -1, 1, 5);
        addShopProduct(shop, Material.DARK_OAK_LEAVES, 3, -1, 1, 6);
        addShopProduct(shop, Material.AZALEA_LEAVES, 3, -1, 1, 7);
        addShopProduct(shop, Material.FLOWERING_AZALEA_LEAVES, 3, -1, 1, 9);
        addShopProduct(shop, Material.MANGROVE_LEAVES, 3, -1, 1, 10);
        addShopProduct(shop, Material.SMALL_DRIPLEAF, 3, -1, 1, 11);
        addShopProduct(shop, Material.BIG_DRIPLEAF, 5, -1, 1, 12);
        addShopProduct(shop, Material.VINE, 1, -1, 1, 13);
        addShopProduct(shop, Material.AZALEA, 3, -1, 1, 14);
        addShopProduct(shop, Material.FLOWERING_AZALEA, 3, -1, 1, 15);
        addShopProduct(shop, Material.MANGROVE_ROOTS, 3, -1, 1, 16);
        addShopProduct(shop, Material.MUDDY_MANGROVE_ROOTS, 3, -1, 1, 17);
        addShopProduct(shop, Material.SHORT_GRASS, 1, -1, 1, 19);
        addShopProduct(shop, Material.SEAGRASS, 5, 0.1, 1, 20);
        addShopProduct(shop, Material.FERN, 1, -1, 1, 21);
        addShopProduct(shop, Material.TALL_GRASS, 1, -1, 1, 22);
        addShopProduct(shop, Material.LARGE_FERN, 1, -1, 1, 23);
        addShopProduct(shop, Material.LILY_PAD, 1, -1, 1, 24);
        addShopProduct(shop, Material.BAMBOO, 20, 0.5, 1, 25);
        addShopProduct(shop, Material.SEA_PICKLE, 20, 2, 1, 30);
        addShopProduct(shop, Material.CRIMSON_ROOTS, 2.5, -1, 1, 31);
        addShopProduct(shop, Material.NETHER_SPROUTS, 3, -1, 1, 32);
        addShopProduct(shop, Material.DANDELION, 4, 0.5, 2, 10);
        addShopProduct(shop, Material.POPPY, 10, 0.5, 2, 11);
        addShopProduct(shop, Material.ALLIUM, 4, 0.5, 2, 12);
        addShopProduct(shop, Material.AZURE_BLUET, 4, 0.5, 2, 13);
        addShopProduct(shop, Material.BLUE_ORCHID, 4, 0.5, 2, 14);
        addShopProduct(shop, Material.OXEYE_DAISY, 4, 0.5, 2, 15);
        addShopProduct(shop, Material.SUNFLOWER, 4, 0.5, 2, 16);
        addShopProduct(shop, Material.LILY_OF_THE_VALLEY, 4, 0.5, 2, 19);
        addShopProduct(shop, Material.CORNFLOWER, 4, 0.5, 2, 20);
        addShopProduct(shop, Material.WITHER_ROSE, 5_000, 100, 2, 21);
        addShopProduct(shop, Material.RED_TULIP, 4, 0.5, 2, 22);
        addShopProduct(shop, Material.ORANGE_TULIP, 4, 0.5, 2, 23);
        addShopProduct(shop, Material.PINK_TULIP, 4, 0.5, 2, 24);
        addShopProduct(shop, Material.WHITE_TULIP, 4, 0.5, 2, 25);
        addShopProduct(shop, Material.ROSE_BUSH, 4, 0.5, 2, 29);
        addShopProduct(shop, Material.PEONY, 4, 0.5, 2, 30);
        addShopProduct(shop, Material.LILAC, 4, 0.5, 2, 31);
        addShopProduct(shop, Material.CHORUS_FLOWER, 50, 1, 2, 32);
        addShopProduct(shop, Material.DEAD_BUSH, 3, 0.5, 2, 33);
        addShopProduct(shop, Material.HANGING_ROOTS, 3, 0.1, 3, 11);
        addShopProduct(shop, Material.POINTED_DRIPSTONE, 4, 0.1, 3, 12);
        addShopProduct(shop, Material.SPORE_BLOSSOM, 20, 0.2, 3, 13);
        addShopProduct(shop, Material.END_ROD, 25, 0.5, 3, 14);
        addShopProduct(shop, Material.LIGHTNING_ROD, 25, 0.5, 3, 15);
        addShopProduct(shop, Material.COBWEB, 35, -1, 3, 19);
        addShopProduct(shop, Material.ARMOR_STAND, 125, -1, 3, 20);
        addShopProduct(shop, Material.RED_BED, 50, -1, 3, 21);
        addShopProduct(shop, Material.FLOWER_POT, 4, 0.5, 3, 22);
        addShopProduct(shop, Material.PAINTING, 80, -1, 3, 23);
        addShopProduct(shop, Material.ITEM_FRAME, 75, -1, 3, 24);
        addShopProduct(shop, Material.GLOW_ITEM_FRAME, 80, -1, 3, 25);
        addShopProduct(shop, Material.SMALL_AMETHYST_BUD, 100, -1, 3, 29);
        addShopProduct(shop, Material.MEDIUM_AMETHYST_BUD, 125, -1, 3, 30);
        addShopProduct(shop, Material.MOSS_CARPET, 5, -1, 3, 31);
        addShopProduct(shop, Material.LARGE_AMETHYST_BUD, 150, -1, 3, 32);
        addShopProduct(shop, Material.AMETHYST_CLUSTER, 175, -1, 3, 33);
        addShopProduct(shop, Material.GLOW_LICHEN, 3, -1, 4, 3);
        addShopProduct(shop, Material.SCULK_VEIN, 20, 1, 4, 4);
        addShopProduct(shop, Material.FROGSPAWN, 5, -1, 4, 5);
        addShopProduct(shop, Material.CRAFTING_TABLE, 75, -1, 4, 10);
        addShopProduct(shop, Material.CARTOGRAPHY_TABLE, 85, -1, 4, 11);
        addShopProduct(shop, Material.FLETCHING_TABLE, 100, -1, 4, 12);
        addShopProduct(shop, Material.SMITHING_TABLE, 250, -1, 4, 13);
        addShopProduct(shop, Material.LOOM, 50, -1, 4, 14);
        addShopProduct(shop, Material.STONECUTTER, 150, -1, 4, 15);
        addShopProduct(shop, Material.GRINDSTONE, 125, -1, 4, 16);
        addShopProduct(shop, Material.LODESTONE, 5_500, 750, 4, 18);
        addShopProduct(shop, Material.BELL, 1_000, -1, 4, 19);
        addShopProduct(shop, Material.SCAFFOLDING, 100, -1, 4, 20);
        addShopProduct(shop, Material.FURNACE, 50, -1, 4, 21);
        addShopProduct(shop, Material.BLAST_FURNACE, 600, -1, 4, 22);
        addShopProduct(shop, Material.SMOKER, 125, -1, 4, 23);
        addShopProduct(shop, Material.CHEST, 75, -1, 4, 24);
        addShopProduct(shop, Material.BARREL, 50, -1, 4, 25);
        addShopProduct(shop, Material.CHAIN, 85, 1, 4, 26);
        addShopProduct(shop, Material.TORCH, 35, -1, 4, 28);
        addShopProduct(shop, Material.LANTERN, 105, -1, 4, 29);
        addShopProduct(shop, Material.CAMPFIRE, 110, -1, 4, 30);
        addShopProduct(shop, Material.SOUL_CAMPFIRE, 130, -1, 4, 32);
        addShopProduct(shop, Material.SOUL_LANTERN, 120, -1, 4, 33);
        addShopProduct(shop, Material.SOUL_TORCH, 45, -1, 4, 34);
    }

    private static void addMineralsItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.DIAMOND_ORE, 225, 30, 1, 1);
        addShopProduct(shop, Material.EMERALD_ORE, 175, 20, 1, 2);
        addShopProduct(shop, Material.GOLD_ORE, 12, 1.5, 1, 3);
        addShopProduct(shop, Material.IRON_ORE, 9.5, 1, 1, 4);
        addShopProduct(shop, Material.LAPIS_ORE, 150, 15, 1, 5);
        addShopProduct(shop, Material.REDSTONE_ORE, 125, 25, 1, 6);
        addShopProduct(shop, Material.COAL_ORE, 40, 2, 1, 7);
        addShopProduct(shop, Material.NETHERITE_SCRAP, 1_250, 80, 1, 9);
        addShopProduct(shop, Material.DEEPSLATE_DIAMOND_ORE, 225, 30, 1, 10);
        addShopProduct(shop, Material.DEEPSLATE_EMERALD_ORE, 175, 20, 1, 11);
        addShopProduct(shop, Material.DEEPSLATE_GOLD_ORE, 12, 1.5, 1, 12);
        addShopProduct(shop, Material.DEEPSLATE_IRON_ORE, 9.5, 1, 1, 13);
        addShopProduct(shop, Material.DEEPSLATE_LAPIS_ORE, 150, 15, 1, 14);
        addShopProduct(shop, Material.DEEPSLATE_REDSTONE_ORE, 125, 25, 1, 15);
        addShopProduct(shop, Material.DEEPSLATE_COAL_ORE, 40, 2, 1, 16);
        addShopProduct(shop, Material.NETHER_QUARTZ_ORE, 30, 0.5, 1, 17);
        addShopProduct(shop, Material.NETHERITE_INGOT, 5_500, 400, 1, 18);
        addShopProduct(shop, Material.DIAMOND, 200, 30, 1, 19);
        addShopProduct(shop, Material.EMERALD, 150, 20, 1, 20);
        addShopProduct(shop, Material.GOLD_INGOT, 80, 15, 1, 21);
        addShopProduct(shop, Material.IRON_INGOT, 70, 10, 1, 22);
        addShopProduct(shop, Material.LAPIS_LAZULI, 15, 1, 1, 23);
        addShopProduct(shop, Material.REDSTONE, 15, 3, 1, 24);
        addShopProduct(shop, Material.COAL, 30, 2, 1, 25);
        addShopProduct(shop, Material.QUARTZ, 25, 0.3, 1, 26);
        addShopProduct(shop, Material.NETHERITE_BLOCK, 49_500, 3_600, 1, 27);
        addShopProduct(shop, Material.DIAMOND_BLOCK, 2_000, 270, 1, 28);
        addShopProduct(shop, Material.EMERALD_BLOCK, 1_350, 180, 1, 29);
        addShopProduct(shop, Material.GOLD_BLOCK, 720, 135, 1, 30);
        addShopProduct(shop, Material.IRON_BLOCK, 630, 90, 1, 31);
        addShopProduct(shop, Material.LAPIS_BLOCK, 135, 9, 1, 32);
        addShopProduct(shop, Material.REDSTONE_BLOCK, 135, 27, 1, 33);
        addShopProduct(shop, Material.COAL_BLOCK, 270, 18, 1, 34);
        addShopProduct(shop, Material.QUARTZ_BLOCK, 80, 1, 1, 35);
        addShopProduct(shop, Material.GOLD_NUGGET, 9, 1.5, 2, 2);
        addShopProduct(shop, Material.IRON_NUGGET, 8, 1, 2, 3);
        addShopProduct(shop, Material.COPPER_ORE, 35, 1.5, 2, 6);
        addShopProduct(shop, Material.RAW_GOLD, 12, 1.5, 2, 11);
        addShopProduct(shop, Material.RAW_IRON, 9.5, 1, 2, 12);
        addShopProduct(shop, Material.CHARCOAL, 32, 2, 2, 13);
        addShopProduct(shop, Material.RAW_COPPER, 35, 1.5, 2, 14);
        addShopProduct(shop, Material.DEEPSLATE_COPPER_ORE, 35, 1.5, 2, 15);
        addShopProduct(shop, Material.NETHER_GOLD_ORE, 10, 1.5, 2, 20);
        addShopProduct(shop, Material.RAW_IRON_BLOCK, 80.5, 9, 2, 21);
        addShopProduct(shop, Material.RAW_COPPER_BLOCK, 31.5, 13.5, 2, 23);
        addShopProduct(shop, Material.COPPER_INGOT, 41.5, 3, 2, 24);
        addShopProduct(shop, Material.RAW_GOLD_BLOCK, 108, 13.5, 2, 29);
        addShopProduct(shop, Material.COPPER_BLOCK, 165, 15, 2, 33);
    }

    private static void addCombatToolsItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.TURTLE_HELMET, 500, -1, 1, 4);
        addShopProduct(shop, Material.IRON_HELMET, 500, -1, 1, 11);
        addShopProduct(shop, Material.IRON_CHESTPLATE, 700, -1, 1, 12);
        addShopProduct(shop, Material.IRON_LEGGINGS, 650, -1, 1, 13);
        addShopProduct(shop, Material.IRON_BOOTS, 450, -1, 1, 14);
        addShopProduct(shop, Material.IRON_SWORD, 300, -1, 1, 15);
        addShopProduct(shop, Material.DIAMOND_HELMET, 1_200, -1, 1, 20);
        addShopProduct(shop, Material.DIAMOND_CHESTPLATE, 1_800, -1, 1, 21);
        addShopProduct(shop, Material.DIAMOND_LEGGINGS, 1_600, -1, 1, 22);
        addShopProduct(shop, Material.DIAMOND_BOOTS, 1_000, -1, 1, 23);
        addShopProduct(shop, Material.DIAMOND_SWORD, 800, -1, 1, 24);
        addShopProduct(shop, Material.IRON_PICKAXE, 300, -1, 2, 2);
        addShopProduct(shop, Material.DIAMOND_PICKAXE, 700, -1, 2, 6);
        addShopProduct(shop, Material.IRON_AXE, 250, -1, 2, 11);
        addShopProduct(shop, Material.DIAMOND_AXE, 700, -1, 2, 15);
        addShopProduct(shop, Material.IRON_SHOVEL, 100, -1, 2, 20);
        addShopProduct(shop, Material.DIAMOND_SHOVEL, 250, -1, 2, 24);
        addShopProduct(shop, Material.IRON_HOE, 200, -1, 2, 29);
        addShopProduct(shop, Material.DIAMOND_HOE, 450, -1, 2, 33);
        addShopProduct(shop, Material.FISHING_ROD, 150, -1, 3, 12);
        addShopProduct(shop, Material.CARROT_ON_A_STICK, 175, -1, 3, 13);
        addShopProduct(shop, Material.WARPED_FUNGUS_ON_A_STICK, 200, -1, 3, 14);
        addShopProduct(shop, Material.BOW, 500, -1, 3, 19);
        addShopProduct(shop, Material.ARROW, 50, 3, 3, 20);
        addShopProduct(shop, Material.SPECTRAL_ARROW, 65, -1, 3, 21);
        addShopProduct(shop, Material.CROSSBOW, 500, -1, 3, 22);
        addShopProduct(shop, Material.TRIDENT, 50_000, 1_000, 3, 23);
        addShopProduct(shop, Material.SHIELD, 120, -1, 3, 24);
        addShopProduct(shop, Material.FLINT_AND_STEEL, 500, -1, 3, 25);
        addShopProduct(shop, Material.NAME_TAG, 3_000, -1, 3, 30);
        addShopProduct(shop, Material.LEAD, 1_000, -1, 3, 31);
        addShopProduct(shop, Material.SHEARS, 200, -1, 3, 32);
    }

    private static void addColoredBlocksItems(@NotNull VirtualShop shop) {
        addShopProduct(shop, Material.BLACK_WOOL, 5, 1, 1, 11);
        addShopProduct(shop, Material.GRAY_WOOL, 5, 1, 1, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_WOOL, 5, 1, 1, 14);
        addShopProduct(shop, Material.WHITE_WOOL, 5, 1, 1, 15);
        addShopProduct(shop, Material.BLUE_WOOL, 5, 1, 1, 19);
        addShopProduct(shop, Material.CYAN_WOOL, 5, 1, 1, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_WOOL, 5, 1, 1, 21);
        addShopProduct(shop, Material.PURPLE_WOOL, 5, 1, 1, 22);
        addShopProduct(shop, Material.MAGENTA_WOOL, 5, 1, 1, 23);
        addShopProduct(shop, Material.PINK_WOOL, 5, 1, 1, 24);
        addShopProduct(shop, Material.RED_WOOL, 5, 1, 1, 25);
        addShopProduct(shop, Material.BROWN_WOOL, 5, 1, 1, 29);
        addShopProduct(shop, Material.ORANGE_WOOL, 5, 1, 1, 30);
        addShopProduct(shop, Material.YELLOW_WOOL, 5, 1, 1, 31);
        addShopProduct(shop, Material.LIME_WOOL, 5, 1, 1, 32);
        addShopProduct(shop, Material.GREEN_WOOL, 5, 1, 1, 33);
        addShopProduct(shop, Material.BLACK_CARPET, 4, 0.5, 2, 11);
        addShopProduct(shop, Material.GRAY_CARPET, 4, 0.5, 2, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_CARPET, 4, 0.5, 2, 14);
        addShopProduct(shop, Material.WHITE_CARPET, 4, 0.5, 2, 15);
        addShopProduct(shop, Material.BLUE_CARPET, 4, 0.5, 2, 19);
        addShopProduct(shop, Material.CYAN_CARPET, 4, 0.5, 2, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_CARPET, 4, 0.5, 2, 21);
        addShopProduct(shop, Material.PURPLE_CARPET, 4, 0.5, 2, 22);
        addShopProduct(shop, Material.MAGENTA_CARPET, 4, 0.5, 2, 23);
        addShopProduct(shop, Material.PINK_CARPET, 4, 0.5, 2, 24);
        addShopProduct(shop, Material.RED_CARPET, 4, 0.5, 2, 25);
        addShopProduct(shop, Material.BROWN_CARPET, 4, 0.5, 2, 29);
        addShopProduct(shop, Material.ORANGE_CARPET, 4, 0.5, 2, 30);
        addShopProduct(shop, Material.YELLOW_CARPET, 4, 0.5, 2, 31);
        addShopProduct(shop, Material.LIME_CARPET, 4, 0.5, 2, 32);
        addShopProduct(shop, Material.GREEN_CARPET, 4, 0.5, 2, 33);
        addShopProduct(shop, Material.BLACK_STAINED_GLASS, 5, 1, 3, 11);
        addShopProduct(shop, Material.GRAY_STAINED_GLASS, 5, 1, 3, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_STAINED_GLASS, 5, 1, 3, 14);
        addShopProduct(shop, Material.WHITE_STAINED_GLASS, 5, 1, 3, 15);
        addShopProduct(shop, Material.BLUE_STAINED_GLASS, 5, 1, 3, 19);
        addShopProduct(shop, Material.CYAN_STAINED_GLASS, 5, 1, 3, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_STAINED_GLASS, 5, 1, 3, 21);
        addShopProduct(shop, Material.PURPLE_STAINED_GLASS, 5, 1, 3, 22);
        addShopProduct(shop, Material.MAGENTA_STAINED_GLASS, 5, 1, 3, 23);
        addShopProduct(shop, Material.PINK_STAINED_GLASS, 5, 1, 3, 24);
        addShopProduct(shop, Material.RED_STAINED_GLASS, 5, 1, 3, 25);
        addShopProduct(shop, Material.BROWN_STAINED_GLASS, 5, 1, 3, 29);
        addShopProduct(shop, Material.ORANGE_STAINED_GLASS, 5, 1, 3, 30);
        addShopProduct(shop, Material.YELLOW_STAINED_GLASS, 5, 1, 3, 31);
        addShopProduct(shop, Material.LIME_STAINED_GLASS, 5, 1, 3, 32);
        addShopProduct(shop, Material.GREEN_STAINED_GLASS, 5, 1, 3, 33);
        addShopProduct(shop, Material.BLACK_STAINED_GLASS_PANE, 3.5, 0.3, 4, 11);
        addShopProduct(shop, Material.GRAY_STAINED_GLASS_PANE, 3.5, 0.3, 4, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_STAINED_GLASS_PANE, 3.5, 0.3, 4, 14);
        addShopProduct(shop, Material.WHITE_STAINED_GLASS_PANE, 3.5, 0.3, 4, 15);
        addShopProduct(shop, Material.BLUE_STAINED_GLASS_PANE, 3.5, 0.3, 4, 19);
        addShopProduct(shop, Material.CYAN_STAINED_GLASS_PANE, 3.5, 0.3, 4, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_STAINED_GLASS_PANE, 3.5, 0.3, 4, 21);
        addShopProduct(shop, Material.PURPLE_STAINED_GLASS_PANE, 3.5, 0.3, 4, 22);
        addShopProduct(shop, Material.MAGENTA_STAINED_GLASS_PANE, 3.5, 0.3, 4, 23);
        addShopProduct(shop, Material.PINK_STAINED_GLASS_PANE, 3.5, 0.3, 4, 24);
        addShopProduct(shop, Material.RED_STAINED_GLASS_PANE, 3.5, 0.3, 4, 25);
        addShopProduct(shop, Material.BROWN_STAINED_GLASS_PANE, 3.5, 0.3, 4, 29);
        addShopProduct(shop, Material.ORANGE_STAINED_GLASS_PANE, 3.5, 0.3, 4, 30);
        addShopProduct(shop, Material.YELLOW_STAINED_GLASS_PANE, 3.5, 0.3, 4, 31);
        addShopProduct(shop, Material.LIME_STAINED_GLASS_PANE, 3.5, 0.3, 4, 32);
        addShopProduct(shop, Material.GREEN_STAINED_GLASS_PANE, 3.5, 0.3, 4, 33);
        addShopProduct(shop, Material.BLACK_TERRACOTTA, 5, 1, 5, 11);
        addShopProduct(shop, Material.GRAY_TERRACOTTA, 5, 1, 5, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_TERRACOTTA, 5, 1, 5, 14);
        addShopProduct(shop, Material.WHITE_TERRACOTTA, 5, 1, 5, 15);
        addShopProduct(shop, Material.BLUE_TERRACOTTA, 5, 1, 5, 19);
        addShopProduct(shop, Material.CYAN_TERRACOTTA, 5, 1, 5, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_TERRACOTTA, 5, 1, 5, 21);
        addShopProduct(shop, Material.PURPLE_TERRACOTTA, 5, 1, 5, 22);
        addShopProduct(shop, Material.MAGENTA_TERRACOTTA, 5, 1, 5, 23);
        addShopProduct(shop, Material.PINK_TERRACOTTA, 5, 1, 5, 24);
        addShopProduct(shop, Material.RED_TERRACOTTA, 5, 1, 5, 25);
        addShopProduct(shop, Material.BROWN_TERRACOTTA, 5, 1, 5, 29);
        addShopProduct(shop, Material.ORANGE_TERRACOTTA, 5, 1, 5, 30);
        addShopProduct(shop, Material.YELLOW_TERRACOTTA, 5, 1, 5, 31);
        addShopProduct(shop, Material.LIME_TERRACOTTA, 5, 1, 5, 32);
        addShopProduct(shop, Material.GREEN_TERRACOTTA, 5, 1, 5, 33);
        addShopProduct(shop, Material.BLACK_BANNER, 35, 3, 6, 11);
        addShopProduct(shop, Material.GRAY_BANNER, 35, 3, 6, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_BANNER, 35, 3, 6, 14);
        addShopProduct(shop, Material.WHITE_BANNER, 35, 3, 6, 15);
        addShopProduct(shop, Material.BLUE_BANNER, 35, 3, 6, 19);
        addShopProduct(shop, Material.CYAN_BANNER, 35, 3, 6, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_BANNER, 35, 3, 6, 21);
        addShopProduct(shop, Material.PURPLE_BANNER, 35, 3, 6, 22);
        addShopProduct(shop, Material.MAGENTA_BANNER, 35, 3, 6, 23);
        addShopProduct(shop, Material.PINK_BANNER, 35, 3, 6, 24);
        addShopProduct(shop, Material.RED_BANNER, 35, 3, 6, 25);
        addShopProduct(shop, Material.BROWN_BANNER, 35, 3, 6, 29);
        addShopProduct(shop, Material.ORANGE_BANNER, 35, 3, 6, 30);
        addShopProduct(shop, Material.YELLOW_BANNER, 35, 3, 6, 31);
        addShopProduct(shop, Material.LIME_BANNER, 35, 3, 6, 32);
        addShopProduct(shop, Material.GREEN_BANNER, 35, 3, 6, 33);
        addShopProduct(shop, Material.BLACK_CONCRETE_POWDER, 100, 5, 7, 11);
        addShopProduct(shop, Material.GRAY_CONCRETE_POWDER, 100, 5, 7, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_CONCRETE_POWDER, 100, 5, 7, 14);
        addShopProduct(shop, Material.WHITE_CONCRETE_POWDER, 100, 5, 7, 15);
        addShopProduct(shop, Material.BLUE_CONCRETE_POWDER, 100, 5, 7, 19);
        addShopProduct(shop, Material.CYAN_CONCRETE_POWDER, 100, 5, 7, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_CONCRETE_POWDER, 100, 5, 7, 21);
        addShopProduct(shop, Material.PURPLE_CONCRETE_POWDER, 100, 5, 7, 22);
        addShopProduct(shop, Material.MAGENTA_CONCRETE_POWDER, 100, 5, 7, 23);
        addShopProduct(shop, Material.PINK_CONCRETE_POWDER, 100, 5, 7, 24);
        addShopProduct(shop, Material.RED_CONCRETE_POWDER, 100, 5, 7, 25);
        addShopProduct(shop, Material.BROWN_CONCRETE_POWDER, 100, 5, 7, 29);
        addShopProduct(shop, Material.ORANGE_CONCRETE_POWDER, 100, 5, 7, 30);
        addShopProduct(shop, Material.YELLOW_CONCRETE_POWDER, 100, 5, 7, 31);
        addShopProduct(shop, Material.LIME_CONCRETE_POWDER, 100, 5, 7, 32);
        addShopProduct(shop, Material.GREEN_CONCRETE_POWDER, 100, 5, 7, 33);
        addShopProduct(shop, Material.BLACK_CONCRETE, 105, 5, 8, 11);
        addShopProduct(shop, Material.GRAY_CONCRETE, 105, 5, 8, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_CONCRETE, 105, 5, 8, 14);
        addShopProduct(shop, Material.WHITE_CONCRETE, 105, 5, 8, 15);
        addShopProduct(shop, Material.BLUE_CONCRETE, 105, 5, 8, 19);
        addShopProduct(shop, Material.CYAN_CONCRETE, 105, 5, 8, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_CONCRETE, 105, 5, 8, 21);
        addShopProduct(shop, Material.PURPLE_CONCRETE, 105, 5, 8, 22);
        addShopProduct(shop, Material.MAGENTA_CONCRETE, 105, 5, 8, 23);
        addShopProduct(shop, Material.PINK_CONCRETE, 105, 5, 8, 24);
        addShopProduct(shop, Material.RED_CONCRETE, 105, 5, 8, 25);
        addShopProduct(shop, Material.BROWN_CONCRETE, 105, 5, 8, 29);
        addShopProduct(shop, Material.ORANGE_CONCRETE, 105, 5, 8, 30);
        addShopProduct(shop, Material.YELLOW_CONCRETE, 105, 5, 8, 31);
        addShopProduct(shop, Material.LIME_CONCRETE, 105, 5, 8, 32);
        addShopProduct(shop, Material.GREEN_CONCRETE, 105, 5, 8, 33);
        addShopProduct(shop, Material.BLACK_GLAZED_TERRACOTTA, 110, 5, 9, 11);
        addShopProduct(shop, Material.GRAY_GLAZED_TERRACOTTA, 110, 5, 9, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_GLAZED_TERRACOTTA, 110, 5, 9, 14);
        addShopProduct(shop, Material.WHITE_GLAZED_TERRACOTTA, 110, 5, 9, 15);
        addShopProduct(shop, Material.BLUE_GLAZED_TERRACOTTA, 110, 5, 9, 19);
        addShopProduct(shop, Material.CYAN_GLAZED_TERRACOTTA, 110, 5, 9, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_GLAZED_TERRACOTTA, 110, 5, 9, 21);
        addShopProduct(shop, Material.PURPLE_GLAZED_TERRACOTTA, 110, 5, 9, 22);
        addShopProduct(shop, Material.MAGENTA_GLAZED_TERRACOTTA, 110, 5, 9, 23);
        addShopProduct(shop, Material.PINK_GLAZED_TERRACOTTA, 110, 5, 9, 24);
        addShopProduct(shop, Material.RED_GLAZED_TERRACOTTA, 110, 5, 9, 25);
        addShopProduct(shop, Material.BROWN_GLAZED_TERRACOTTA, 110, 5, 9, 29);
        addShopProduct(shop, Material.ORANGE_GLAZED_TERRACOTTA, 110, 5, 9, 30);
        addShopProduct(shop, Material.YELLOW_GLAZED_TERRACOTTA, 110, 5, 9, 31);
        addShopProduct(shop, Material.LIME_GLAZED_TERRACOTTA, 110, 5, 9, 32);
        addShopProduct(shop, Material.GREEN_GLAZED_TERRACOTTA, 110, 5, 9, 33);
        addShopProduct(shop, Material.BLACK_BED, 50, -1, 10, 11);
        addShopProduct(shop, Material.GRAY_BED, 50, -1, 10, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_BED, 50, -1, 10, 14);
        addShopProduct(shop, Material.WHITE_BED, 50, -1, 10, 15);
        addShopProduct(shop, Material.BLUE_BED, 50, -1, 10, 19);
        addShopProduct(shop, Material.CYAN_BED, 50, -1, 10, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_BED, 50, -1, 10, 21);
        addShopProduct(shop, Material.PURPLE_BED, 50, -1, 10, 22);
        addShopProduct(shop, Material.MAGENTA_BED, 50, -1, 10, 23);
        addShopProduct(shop, Material.PINK_BED, 50, -1, 10, 24);
        addShopProduct(shop, Material.RED_BED, 50, -1, 10, 25);
        addShopProduct(shop, Material.BROWN_BED, 50, -1, 10, 29);
        addShopProduct(shop, Material.ORANGE_BED, 50, -1, 10, 30);
        addShopProduct(shop, Material.YELLOW_BED, 50, -1, 10, 31);
        addShopProduct(shop, Material.LIME_BED, 50, -1, 10, 32);
        addShopProduct(shop, Material.GREEN_BED, 50, -1, 10, 33);
        addShopProduct(shop, Material.BLACK_SHULKER_BOX, 1_000, 150, 11, 11);
        addShopProduct(shop, Material.GRAY_SHULKER_BOX, 1_000, 150, 11, 12);
        addShopProduct(shop, Material.LIGHT_GRAY_SHULKER_BOX, 1_000, 150, 11, 14);
        addShopProduct(shop, Material.WHITE_SHULKER_BOX, 1_000, 150, 11, 15);
        addShopProduct(shop, Material.BLUE_SHULKER_BOX, 1_000, 150, 11, 19);
        addShopProduct(shop, Material.CYAN_SHULKER_BOX, 1_000, 150, 11, 20);
        addShopProduct(shop, Material.LIGHT_BLUE_SHULKER_BOX, 1_000, 150, 11, 21);
        addShopProduct(shop, Material.PURPLE_SHULKER_BOX, 1_000, 150, 11, 22);
        addShopProduct(shop, Material.MAGENTA_SHULKER_BOX, 1_000, 150, 11, 23);
        addShopProduct(shop, Material.PINK_SHULKER_BOX, 1_000, 150, 11, 24);
        addShopProduct(shop, Material.RED_SHULKER_BOX, 1_000, 150, 11, 25);
        addShopProduct(shop, Material.BROWN_SHULKER_BOX, 1_000, 150, 11, 29);
        addShopProduct(shop, Material.ORANGE_SHULKER_BOX, 1_000, 150, 11, 30);
        addShopProduct(shop, Material.YELLOW_SHULKER_BOX, 1_000, 150, 11, 31);
        addShopProduct(shop, Material.LIME_SHULKER_BOX, 1_000, 150, 11, 32);
        addShopProduct(shop, Material.GREEN_SHULKER_BOX, 1_000, 150, 11, 33);
    }

    private static void createShop(@NotNull VirtualShopModule module,
                                   @NotNull String id,
                                   @NotNull String hexColor,
                                   @NotNull String name,
                                   @NotNull List<String> description,
                                   @NotNull NightItem icon,
                                   int pages,
                                   int menuSlot,
                                   @NotNull Consumer<VirtualShop> consumer) {

        List<String> realDescription = new ArrayList<>();
        description.forEach(line -> realDescription.add(GRAY.wrap(line)));
        realDescription.add(" ");
        realDescription.add(COLOR.with(hexColor).wrap("→ " + BOLD.wrap(UNDERLINED.wrap("CLICK")) + " to browse"));

        module.createShop(id, shop -> {
            shop.setName(StringUtil.capitalizeUnderscored(id));
            shop.setDescription(realDescription);
            shop.setIcon(icon.setDisplayName(COLOR.with(hexColor).and(BOLD).wrap(name)));
            shop.setPermissionRequired(false);
            shop.setBuyingAllowed(true);
            shop.setSellingAllowed(true);
            shop.setPages(pages);
            shop.setMenuSlots(Lists.newSet(menuSlot));
            consumer.accept(shop);
        });
    }

    private static void addShopProduct(@NotNull VirtualShop shop, @NotNull Material material, double buyPrice, double sellPrice, int page, int slot) {
        addShopProduct(shop, new ItemStack(material), buyPrice, sellPrice, page, slot);
    }

    private static void addShopProduct(@NotNull VirtualShop shop, @NotNull Material material, @NotNull PotionType type, double buyPrice, double sellPrice, int page, int slot) {
        ItemStack itemStack = new ItemStack(material);
        ItemUtil.editMeta(itemStack, PotionMeta.class, potionMeta -> {
            potionMeta.setBasePotionType(type);
        });

        addShopProduct(shop, itemStack, buyPrice, sellPrice, page, slot);
    }

    private static void addShopProduct(@NotNull VirtualShop shop, @NotNull ItemStack itemStack, double buyPrice, double sellPrice, int page, int slot) {
        addShopProduct(shop, itemStack, product -> {
            product.setPricing(FlatPricing.of(buyPrice, sellPrice));
            product.setPage(page);
            product.setSlot(slot);
        });
    }

    private static void addShopProduct(@NotNull VirtualShop shop, @NotNull ItemStack itemStack, @NotNull Consumer<VirtualProduct> consumer) {
        VirtualProduct product = shop.createProduct(ContentType.ITEM, itemStack);
        consumer.accept(product);
        shop.addProduct(product);
    }
}
