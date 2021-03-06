package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.VirtualShopConfig;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorHandler;
import su.nightexpress.nexshop.shop.virtual.object.ShopVirtualProduct;

import java.util.*;
import java.util.stream.IntStream;

public class EditorShopProductList extends AbstractMenu<ExcellentShop> {

    private static final Map<String, IShopVirtualProduct> PRODUCT_CACHE = new HashMap<>();

    private static ItemStack    FREE_SLOT;
    private static String       PRODUCT_NAME;
    private static List<String> PRODUCT_LORE;

    private final IShopVirtual  shop;
    private final NamespacedKey keyProductCache;

    public EditorShopProductList(@NotNull ExcellentShop plugin, @NotNull IShopVirtual shop) {
        super(plugin, shop.getView().getTitle(), shop.getView().getSize());
        this.shop = shop;
        this.keyProductCache = new NamespacedKey(plugin, "product_cache");

        JYML cfg = VirtualEditorHandler.SHOP_PRODUCT_LIST_YML;

        FREE_SLOT = cfg.getItem("Free_Slot");
        PRODUCT_NAME = StringUT.color(cfg.getString("Product.Name", IShopProduct.PLACEHOLDER_PREVIEW_NAME));
        PRODUCT_LORE = StringUT.color(cfg.getStringList("Product.Lore"));
    }

    @NotNull
    private ItemStack cacheProduct(@NotNull IShopVirtualProduct product) {
        String pId = UUID.randomUUID().toString();
        PRODUCT_CACHE.put(pId, product);

        ItemStack stack = product.getPreview();
        DataUT.setData(stack, this.keyProductCache, pId);
        return stack;
    }

    @Nullable
    private IShopVirtualProduct getCachedProduct(@NotNull ItemStack stack) {
        String pId = DataUT.getStringData(stack, this.keyProductCache);
        if (pId == null) return null;

        DataUT.removeData(stack, this.keyProductCache);
        return PRODUCT_CACHE.remove(pId);
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

        for (IShopVirtualProduct shopProduct : this.shop.getProducts()) {
            if (shopProduct.getPage() != page) continue;

            ItemStack preview = new ItemStack(shopProduct.getPreview());
            ItemMeta meta = preview.getItemMeta();
            if (meta == null) continue;

            meta.setDisplayName(PRODUCT_NAME);
            meta.setLore(PRODUCT_LORE);
            preview.setItemMeta(meta);
            ItemUT.replace(preview, shopProduct.replacePlaceholders());

            IMenuItem item = new MenuItem(preview);
            item.setSlots(shopProduct.getSlot());
            item.setClick((p, type, e) -> {
                if (!e.isLeftClick() && !e.isRightClick()) return;

                if (e.isShiftClick()) {
                    if (e.isLeftClick()) {
                        shopProduct.getEditor().open(p, 1);
                    }
                    else if (e.isRightClick()) {
                        this.shop.deleteProduct(shopProduct);
                        this.shop.save();
                        this.open(p, page);
                    }
                    return;
                }

                // Cache clicked product to item stack
                // then remove it from the shop
                ItemStack saved = this.cacheProduct(shopProduct);
                this.shop.deleteProduct(shopProduct);

                // If user wants to replace a clicked product
                // then create or load cached product from an itemstack
                // and add it to the shop
                ItemStack cursor = e.getCursor();
                if (cursor != null && !ItemUT.isAir(cursor)) {
                    IShopVirtualProduct cached = this.getCachedProduct(cursor);
                    if (cached == null) {
                        IShopCurrency currency = VirtualShopConfig.DEFAULT_CURRENCY;
                        cached = new ShopVirtualProduct(this.shop, currency, cursor, e.getRawSlot(), page);
                    }
                    else {
                        cached.setSlot(e.getRawSlot());
                        cached.setPage(page);
                    }
                    this.shop.getProductMap().put(cached.getId(), cached);
                }

                this.shop.save();

                // Set cached item to cursor
                // so player can put it somewhere
                e.getView().setCursor(null);
                this.open(p, page);
                p.getOpenInventory().setCursor(saved);
            });
            this.addItem(player, item);
            contentSlots.add(shopProduct.getSlot());
        }


        IMenuItem free = new MenuItem(FREE_SLOT);
        int[] freeSlots = new int[this.getSize() - contentSlots.size()];
        int count = 0;
        for (int slot = 0; count < freeSlots.length; slot++) {
            if (contentSlots.contains(slot)) continue;
            freeSlots[count++] = slot;
        }
        free.setSlots(freeSlots);
        free.setClick((p, type, e) -> {
            ItemStack cursor = e.getCursor();
            if (cursor == null || ItemUT.isAir(cursor)) return;

            IShopVirtualProduct shopProduct = this.getCachedProduct(cursor);
            if (shopProduct == null) {
                IShopCurrency currency = VirtualShopConfig.DEFAULT_CURRENCY;
                shopProduct = new ShopVirtualProduct(this.shop, currency, cursor, e.getRawSlot(), page);
            }
            else {
                shopProduct.setSlot(e.getRawSlot());
                shopProduct.setPage(page);
            }
            e.getView().setCursor(null);
            this.shop.getProductMap().put(shopProduct.getId(), shopProduct);
            this.shop.save();
            this.open(p, page);
        });

        this.addItem(player, free);

        this.setPage(player, page, this.shop.getPages()); // Hack for page items display.
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        plugin.runTask((c) -> {
            IMenu menu = IMenu.getMenu(player);
            if (menu != null) return;

            shop.getEditor().open(player, 1);
        }, false);

        super.onClose(player, e);
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }
}
