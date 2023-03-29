package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.WeakMenuItem;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Colorizer;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.ShopView;
import su.nightexpress.nexshop.api.type.ShopClickType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChestShopView extends ShopView<ChestShop> {

    private static int[]        PRODUCT_SLOTS;
    private static List<String> PRODUCT_FORMAT_LORE;

    public ChestShopView(@NotNull ChestShop shop) {
        super(shop, JYML.loadOrExtract(shop.plugin(), shop.getModule().getPath() + "view.yml"));

        PRODUCT_SLOTS = cfg.getIntArray("Product_Slots");
        PRODUCT_FORMAT_LORE = Colorizer.apply(cfg.getStringList("Product_Format.Lore.Text"));

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
        };

        for (String id : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + id, MenuItemType.class);
            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }

        this.setTitle(shop.getName());
    }

    @Override
    public void displayProducts(@NotNull Player player, @NotNull Inventory inventory, int page) {
        int length = PRODUCT_SLOTS.length;
        List<ChestProduct> list = new ArrayList<>(this.getShop().getProducts());
        List<List<ChestProduct>> split = CollectionsUtil.split(list, length);

        int pages = split.size();
        if (pages < 1) list = Collections.emptyList();
        else list = split.get(page - 1);

        int count = 0;
        for (ChestProduct product : list) {
            ItemStack preview = product.getPreview();
            ItemMeta meta = preview.getItemMeta();

            if (meta != null) {
                List<String> lore = new ArrayList<>();

                for (String lineFormat : PRODUCT_FORMAT_LORE) {
                    if (lineFormat.contains(Placeholders.GENERIC_LORE)) {
                        List<String> list2 = meta.getLore();
                        if (list2 != null) lore.addAll(list2);
                        continue;
                    }
                    lore.add(lineFormat);
                }

                lore.replaceAll(product.getPlaceholders(player).replacer());
                lore.replaceAll(product.getCurrency().replacePlaceholders());
                meta.setLore(lore);
                preview.setItemMeta(meta);
            }

            WeakMenuItem menuItem = new WeakMenuItem(player, preview, PRODUCT_SLOTS[count++]);
            menuItem.setPriority(100);
            menuItem.setClickHandler((player2, type, e) -> {
                ShopClickType clickType = ShopClickType.getByDefault(e.getClick());
                if (clickType == null) return;

                product.prepareTrade(player2, clickType);
            });
            this.addItem(menuItem);
        }
        this.setPage(player, page, pages);
    }
}
