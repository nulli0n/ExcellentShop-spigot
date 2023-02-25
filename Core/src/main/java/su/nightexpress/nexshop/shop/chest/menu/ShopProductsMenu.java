package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class ShopProductsMenu extends AbstractMenu<ExcellentShop> {

    private final ChestShop shop;

    private final int[] productSlots;
    private final ItemStack productFree;
    private final ItemStack productLocked;
    private final String productName;
    private final List<String> productLore;

    public ShopProductsMenu(@NotNull ChestShop shop) {
        super(shop.plugin(), JYML.loadOrExtract(shop.plugin(), shop.getModule().getPath() + "menu/shop_products.yml"), "");
        this.shop = shop;

        this.productSlots = cfg.getIntArray("Products.Slots");
        this.productFree = cfg.getItem("Products.Free");
        this.productLocked = cfg.getItem("Products.Locked");
        this.productName = Colorizer.apply(cfg.getString("Products.Product.Name", Placeholders.PRODUCT_PREVIEW_NAME));
        this.productLore = Colorizer.apply(cfg.getStringList("Products.Product.Lore"));

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    this.shop.getEditor().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);
            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    public boolean onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        int page = this.getPage(player);

        int maxProducts = ChestConfig.getMaxShopProducts(player);
        if (maxProducts < 0) maxProducts = this.productSlots.length;

        PriorityQueue<ChestProduct> queue = new PriorityQueue<>(Comparator.comparing(Product::getId));
        queue.addAll(this.shop.getProducts());

        int productCount = 0;

        for (int productSlot : this.productSlots) {
            // If no products left to display in slots, then display free/locked slot items.
            if (queue.isEmpty()) {
                WeakMenuItem item;
                if (maxProducts - productCount > 0) {
                    productCount++;
                    item = new WeakMenuItem(player, this.productFree);
                    item.setClickHandler((player2, type, e) -> {
                        ItemStack cursor = e.getCursor();
                        if (cursor == null || !this.shop.createProduct(player2, cursor)) return;

                        e.getView().setCursor(null);
                        PlayerUtil.addItem(player2, cursor);
                        this.shop.save();
                        this.open(player2, page);
                    });
                }
                else {
                    item = new WeakMenuItem(player, this.productLocked);
                }
                item.setSlots(productSlot);
                this.addItem(item);
            }
            // Diplay current shop products
            else {
                productCount++;
                ChestProduct product = queue.poll();
                ItemStack productIcon = new ItemStack(product.getPreview());
                ItemUtil.mapMeta(productIcon, meta -> {
                    meta.setDisplayName(this.productName);
                    meta.setLore(this.productLore);
                    ItemUtil.replace(meta, product.replacePlaceholders());
                });

                WeakMenuItem item = new WeakMenuItem(player, productIcon);
                item.setSlots(productSlot);
                item.setClickHandler((p, type, e) -> {
                    if (e.isShiftClick()) {
                        if (e.isRightClick()) {
                            if (product.getStock().getLeftAmount(TradeType.BUY) > 0) {
                                plugin.getMessage(ChestLang.EDITOR_ERROR_PRODUCT_LEFT).send(p);
                                return;
                            }
                            this.shop.removeProduct(product.getId());
                            this.shop.save();
                            this.open(p, page);
                        }
                        return;
                    }
                    if (e.isRightClick()) {
                        product.getPriceEditor().open(p, 1);
                        return;
                    }
                    if (e.isLeftClick()) {
                        List<ICurrency> currencies = new ArrayList<>(ChestConfig.ALLOWED_CURRENCIES);
                        int index = currencies.indexOf(product.getCurrency()) + 1;
                        if (index >= currencies.size()) index = 0;
                        product.setCurrency(currencies.get(index));
                        this.shop.save();
                        this.open(p, page);
                    }
                });
                this.addItem(item);
            }
        }
        return true;
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        Player player = (Player) e.getWhoClicked();
        int maxProducts = ChestConfig.getMaxShopProducts(player);
        int hasProducts = this.shop.getProducts().size();
        boolean canAdd = maxProducts < 0 || hasProducts < maxProducts;
        if (!canAdd) return true;

        if (slotType == SlotType.PLAYER || slotType == SlotType.EMPTY_PLAYER) {
            if (PlayerUtil.isBedrockPlayer(player)) {
                ItemStack item = e.getCurrentItem();
                if (item == null || item.getType().isAir()) return true;

                this.shop.createProduct(player, item);
                return true;
            }
            return false;
        }

        return true;
    }
}
