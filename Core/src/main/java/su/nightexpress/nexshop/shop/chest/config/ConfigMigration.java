package su.nightexpress.nexshop.shop.chest.config;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.impl.ShopBlock;
import su.nightexpress.nexshop.shop.chest.impl.Showcase;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.Replacer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigMigration {

    public static void migrateHologramSettings(@NotNull FileConfig config) {
        if (config.contains("Display.Hologram.Force_ArmorStands")) {
            boolean oldVal = config.getBoolean("Display.Hologram.Force_ArmorStands");
            config.set("Display.UseArmorStands", oldVal);
            config.remove("Display.Hologram.Force_ArmorStands");
        }

        String path = "Display.Title";
        if (!config.contains(path)) return;

        boolean enabled = config.getBoolean(path + ".Enabled");
        List<String> textAdmin = config.getStringList(path + ".Values.ADMIN");
        List<String> textPlayer = config.getStringList(path + ".Values.PLAYER");
        List<String> textRent = config.getStringList(path + ".Rent");
        String buyValuePlayer = config.getString(path + ".BuyValue.PLAYER", "");
        String sellValuePlayer = config.getString(path + ".SellValue.PLAYER", "");

        textRent.replaceAll(str -> str
            .replace(Placeholders.GENERIC_TIME, Placeholders.CHEST_SHOP_RENT_EXPIRES_IN)
            .replace(Placeholders.GENERIC_PRICE, Placeholders.CHEST_SHOP_RENT_PRICE)
        );

        Replacer replacer = Replacer.create()
            .replace("%product_price_buy%", Placeholders.PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY))
            .replace("%product_price_sell%", Placeholders.PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL))
            .replace("%product_name%", Placeholders.PRODUCT_PREVIEW_NAME)
            .replace("%product_stock%", () -> Placeholders.PRODUCT_AMOUNT);

        String holoPath = "Display.Hologram";
        config.set(holoPath + ".Enabled", enabled);
        config.set(holoPath + ".Text.AdminShop", replacer.apply(textAdmin));
        config.set(holoPath + ".Text.PlayerShop", replacer.apply(textPlayer));
        config.set(holoPath + ".Text.Rent", textRent);
        config.set(holoPath + ".Text.BuyPrice", replacer.apply(buyValuePlayer));
        config.set(holoPath + ".Text.SellPrice", replacer.apply(sellValuePlayer));

        config.remove(path);
    }

    public static void migrateShowcaseCatalog(@NotNull FileConfig config) {
        if (config.contains("Display.PlayerCustomization.Showcases")) {
            config.getSection("Display.PlayerCustomization.Showcases").forEach(sId -> {
                NightItem item = config.getCosmeticItem("Display.PlayerCustomization.Showcases." + sId);
                config.set("Display.Showcase.Catalog." + sId, new Showcase(sId, Placeholders.GENERIC_TYPE, item));
            });
            config.remove("Display.PlayerCustomization");
        }
    }

    public static void migrateShopBlocks(@NotNull FileConfig from, @NotNull FileConfig target) {
        if (!target.getSection("Blocks").isEmpty()) return;

        Set<String> oldContainers = Lists.modify(from.getStringSet("Shops.Allowed_Containers"), String::toLowerCase);
        if (oldContainers.isEmpty()) return;

        if (oldContainers.contains(BukkitThing.getValue(Material.SHULKER_BOX))) {
            oldContainers.addAll(Lists.modify(Tag.SHULKER_BOXES.getValues(), BukkitThing::getValue));
        }

        Map<String, Showcase> oldContainerShowcases = new HashMap<>();
        Map<String, NightItem> oldContainerItems = new HashMap<>();

        if (from.contains("Display.Showcase") && !from.contains("Display.Showcase.Catalog")) {
            from.getSection("Display.Showcase").forEach(sId -> {
                NightItem item = from.getCosmeticItem("Display.Showcase." + sId);
                oldContainerShowcases.put(sId.toLowerCase(), new Showcase(sId, Placeholders.GENERIC_TYPE, item));
            });
            from.remove("Display.Showcase");
        }

        if (from.contains("Shops.ItemCreation.Items")) {
            from.getSection("Shops.ItemCreation.Items").forEach(sId -> {
                NightItem item = from.getCosmeticItem("Shops.ItemCreation.Items." + sId);
                oldContainerItems.put(sId.toLowerCase(), item);
            });
            from.remove("Shops.ItemCreation.Items");
        }

        oldContainers.forEach(type -> {
            Material material = BukkitThing.getMaterial(type);
            if (material == null || !material.isBlock()) return;

            NightItem item = oldContainerItems.getOrDefault(type.toLowerCase(), ChestUtils.getDefaultShopItem(material));
            Showcase showcase = oldContainerShowcases.getOrDefault(type.toLowerCase(),
                oldContainerShowcases.getOrDefault(Placeholders.DEFAULT, new Showcase(BukkitThing.getValue(material), Placeholders.GENERIC_TYPE, NightItem.fromType(Material.GLASS))));
            ShopBlock shopBlock = new ShopBlock(material, item, showcase);

            target.set("Blocks." + BukkitThing.getValue(material), shopBlock);
        });

        from.remove("Shops.Allowed_Containers");
    }
}
