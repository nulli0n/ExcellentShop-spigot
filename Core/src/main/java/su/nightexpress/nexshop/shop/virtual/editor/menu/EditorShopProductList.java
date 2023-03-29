package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.FlatProductPricer;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualCommandProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualItemProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProductStock;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.util.*;
import java.util.stream.IntStream;

public class EditorShopProductList extends AbstractMenu<ExcellentShop> {

    private static final Map<String, VirtualProduct> PRODUCT_CACHE = new HashMap<>();

    private final VirtualShop   shop;
    private final NamespacedKey keyProductCache;

    public EditorShopProductList(@NotNull ExcellentShop plugin, @NotNull VirtualShop shop) {
        super(shop.plugin(), shop.getView().getTitle(), shop.getView().getSize());
        this.shop = shop;
        this.keyProductCache = new NamespacedKey(plugin, "product_cache");
    }

    @NotNull
    private ItemStack cacheProduct(@NotNull VirtualProduct product) {
        String pId = UUID.randomUUID().toString();
        PRODUCT_CACHE.put(pId, product);

        ItemStack stack = product.getPreview();
        PDCUtil.set(stack, this.keyProductCache, pId);
        return stack;
    }

    @Nullable
    private VirtualProduct getCachedProduct(@NotNull ItemStack stack) {
        String pId = PDCUtil.getString(stack, this.keyProductCache).orElse(null);
        if (pId == null) return null;

        PDCUtil.remove(stack, this.keyProductCache);
        return PRODUCT_CACHE.remove(pId);
    }

    @Override
    public void clear() {
        super.clear();
        PRODUCT_CACHE.clear();
    }

    @Override
    public boolean onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        int page = this.getPage(player);

        MenuClick click = (player1, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    this.shop.getEditor().open(player1, 1);
                }
                else this.onItemClickDefault(player1, type2);
            }
        };

        Set<Integer> contentSlots = new HashSet<>();
        for (MenuItem item : this.shop.getView().getItemsMap().values()) {
            MenuItem clone = new WeakMenuItem(player, item.getItem());
            clone.setSlots(item.getSlots());
            clone.setPriority(100);
            clone.setType(item.getType());
            clone.setClickHandler(click);
            this.addItem(clone);
            contentSlots.addAll(IntStream.of(clone.getSlots()).boxed().toList());
        }

        for (VirtualProduct shopProduct : this.shop.getProducts()) {
            if (shopProduct.getPage() != page) continue;

            ItemStack productIcon = new ItemStack(shopProduct.getPreview());
            ItemMeta productMeta = productIcon.getItemMeta();
            if (productMeta == null) continue;

            ItemStack editorIcon = VirtualEditorType.PRODUCT_OBJECT.getItem();
            productMeta.setDisplayName(ItemUtil.getItemName(editorIcon));
            productMeta.setLore(ItemUtil.getLore(editorIcon));
            productIcon.setItemMeta(productMeta);
            ItemUtil.replace(productIcon, shopProduct.replacePlaceholders());

            MenuItem productMenuItem = new WeakMenuItem(player, productIcon);
            productMenuItem.setSlots(shopProduct.getSlot());
            productMenuItem.setClickHandler((player2, type, e) -> {
                if (!e.isLeftClick() && !e.isRightClick()) return;

                if (e.isShiftClick()) {
                    if (e.isLeftClick()) {
                        shopProduct.getEditor().open(player2, 1);
                    }
                    else if (e.isRightClick()) {
                        this.shop.removeProduct(shopProduct);
                        this.shop.saveProducts();
                        this.open(player2, page);
                    }
                    return;
                }

                // Cache clicked product to item stack
                // then remove it from the shop
                ItemStack saved = this.cacheProduct(shopProduct);
                this.shop.removeProduct(shopProduct);

                // If user wants to replace a clicked product
                // then create or load cached product from an itemstack
                // and add it to the shop
                ItemStack cursor = e.getCursor();
                if (cursor != null && !cursor.getType().isAir()) {
                    VirtualProduct cached = this.getCachedProduct(cursor);
                    if (cached == null) {
                        cached = new VirtualItemProduct(cursor, VirtualShopModule.defaultCurrency);
                        //cached.setItem(cursor);
                        cached.setPricer(new FlatProductPricer());
                        cached.setStock(new VirtualProductStock());
                        cached.getStock().unlock();
                    }
                    cached.setSlot(e.getRawSlot());
                    cached.setPage(page);
                    this.shop.addProduct(cached);
                }

                this.shop.saveProducts();

                // Set cached item to cursor
                // so player can put it somewhere
                e.getView().setCursor(null);
                this.open(player2, page);
                player2.getOpenInventory().setCursor(saved);
            });
            this.addItem(productMenuItem);
            contentSlots.add(shopProduct.getSlot());
        }


        MenuItem free = new WeakMenuItem(player, VirtualEditorType.PRODUCT_FREE_SLOT.getItem());
        int[] freeSlots = new int[this.getSize() - contentSlots.size()];
        int count = 0;
        for (int slot = 0; count < freeSlots.length; slot++) {
            if (contentSlots.contains(slot)) continue;
            freeSlots[count++] = slot;
        }
        free.setSlots(freeSlots);
        free.setClickHandler((player1, type, e) -> {
            ItemStack cursor = e.getCursor();
            boolean hasCursor = cursor != null && !cursor.getType().isAir();
            //if (cursor == null || cursor.getType().isAir()) return;

            VirtualProduct shopProduct = hasCursor ? this.getCachedProduct(cursor) : null;
            if (shopProduct == null) {
                if (hasCursor) {
                    shopProduct = new VirtualItemProduct(cursor, VirtualShopModule.defaultCurrency);
                }
                else if (e.isRightClick()) {
                    shopProduct = new VirtualCommandProduct(new ItemStack(Material.COMMAND_BLOCK), VirtualShopModule.defaultCurrency);
                }
                else return;
                shopProduct.setPricer(new FlatProductPricer());
                shopProduct.setStock(new VirtualProductStock());
                shopProduct.getStock().unlock();
            }
            shopProduct.setSlot(e.getRawSlot());
            shopProduct.setPage(page);
            this.shop.addProduct(shopProduct);
            this.shop.saveProducts();
            e.getView().setCursor(null);
            this.open(player1, page);
        });

        this.addItem(free);

        this.setPage(player, page, this.shop.getPages()); // Hack for page items display.
        return true;
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        this.plugin.runTask(task -> {
            AbstractMenu<?> menu = getMenu(player);
            if (menu != null) return;

            this.shop.getEditor().open(player, 1);
        });

        super.onClose(player, e);
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }

    @Override
    public boolean cancelClick(@NotNull InventoryDragEvent e) {
        return e.getRawSlots().stream().anyMatch(slot -> slot < e.getInventory().getSize());
    }
}
