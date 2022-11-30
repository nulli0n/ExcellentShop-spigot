package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.editor.GenericEditorType;
import su.nightexpress.nexshop.editor.menu.EditorProductList;
import su.nightexpress.nexshop.shop.FlatProductPricer;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProductStock;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;

import java.util.*;
import java.util.stream.IntStream;

public class EditorShopProductList extends EditorProductList<VirtualShop> {

    private static final Map<String, VirtualProduct> PRODUCT_CACHE = new HashMap<>();

    private final NamespacedKey keyProductCache;

    public EditorShopProductList(@NotNull ExcellentShop plugin, @NotNull VirtualShop shop) {
        super(shop);
        this.keyProductCache = new NamespacedKey(plugin, "product_cache");
    }

    @NotNull
    private ItemStack cacheProduct(@NotNull VirtualProduct product) {
        String pId = UUID.randomUUID().toString();
        PRODUCT_CACHE.put(pId, product);

        ItemStack stack = product.getPreview();
        PDCUtil.setData(stack, this.keyProductCache, pId);
        return stack;
    }

    @Nullable
    private VirtualProduct getCachedProduct(@NotNull ItemStack stack) {
        String pId = PDCUtil.getStringData(stack, this.keyProductCache);
        if (pId == null) return null;

        PDCUtil.removeData(stack, this.keyProductCache);
        return PRODUCT_CACHE.remove(pId);
    }

    @Override
    public void clear() {
        super.clear();
        PRODUCT_CACHE.clear();
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        int page = this.getPage(player);

        IMenuClick click = (player1, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    this.shop.getEditor().open(player1, 1);
                }
                else this.onItemClickDefault(player1, type2);
            }
        };

        Set<Integer> contentSlots = new HashSet<>();
        for (IMenuItem item : this.shop.getView().getItemsMap().values()) {
            IMenuItem clone = new MenuItem(item);
            clone.setClick(click);
            this.addItem(player, clone);
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

            IMenuItem productMenuItem = new MenuItem(productIcon);
            productMenuItem.setSlots(shopProduct.getSlot());
            productMenuItem.setClick((player2, type, e) -> {
                if (!e.isLeftClick() && !e.isRightClick()) return;

                if (e.isShiftClick()) {
                    if (e.isLeftClick()) {
                        shopProduct.getEditor().open(player2, 1);
                    }
                    else if (e.isRightClick()) {
                        this.shop.removeProduct(shopProduct);
                        this.shop.save();
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
                        cached = new VirtualProduct(VirtualShopModule.defaultCurrency, cursor);
                        cached.setItem(cursor);
                        cached.setPricer(new FlatProductPricer());
                        cached.setStock(new VirtualProductStock());
                    }
                    cached.setSlot(e.getRawSlot());
                    cached.setPage(page);
                    this.shop.addProduct(cached);
                }

                this.shop.save();

                // Set cached item to cursor
                // so player can put it somewhere
                e.getView().setCursor(null);
                this.open(player2, page);
                player2.getOpenInventory().setCursor(saved);
            });
            this.addItem(player, productMenuItem);
            contentSlots.add(shopProduct.getSlot());
        }


        IMenuItem free = new MenuItem(GenericEditorType.PRODUCT_FREE_SLOT.getItem());
        int[] freeSlots = new int[this.getSize() - contentSlots.size()];
        int count = 0;
        for (int slot = 0; count < freeSlots.length; slot++) {
            if (contentSlots.contains(slot)) continue;
            freeSlots[count++] = slot;
        }
        free.setSlots(freeSlots);
        free.setClick((player1, type, e) -> {
            ItemStack cursor = e.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            VirtualProduct shopProduct = this.getCachedProduct(cursor);
            if (shopProduct == null) {
                shopProduct = new VirtualProduct(VirtualShopModule.defaultCurrency, cursor);
                shopProduct.setItem(cursor);
                shopProduct.setPricer(new FlatProductPricer());
                shopProduct.setStock(new VirtualProductStock());
            }
            shopProduct.setSlot(e.getRawSlot());
            shopProduct.setPage(page);
            this.shop.addProduct(shopProduct);
            this.shop.save();
            e.getView().setCursor(null);
            this.open(player1, page);
        });

        this.addItem(player, free);

        this.setPage(player, page, this.shop.getPages()); // Hack for page items display.
    }
}
