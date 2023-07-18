package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.Menu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.ItemOptions;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.price.FlatProductPricer;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualCommandProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualItemProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProductStock;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.util.ShopUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProductListEditor extends EditorMenu<ExcellentShop, VirtualShop> {

    private static final Map<String, VirtualProduct> PRODUCT_CACHE = new HashMap<>();

    private final NamespacedKey keyProductCache;

    public ProductListEditor(@NotNull ExcellentShop plugin, @NotNull VirtualShop shop) {
        super(shop.plugin(), shop, shop.getName(), 54);
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
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);

        options.setTitle(this.object.getView().getOptions().getTitle());
        options.setSize(this.object.getView().getOptions().getSize());

        VirtualShop shop = this.object;
        Set<Integer> freeSlots = IntStream.range(0, options.getSize()).boxed().collect(Collectors.toSet());

        int page = viewer.getPage();
        viewer.setPages(shop.getPages());

        for (MenuItem item : shop.getView().getItems()) {
            if (item.getPriority() < 0) continue;

            MenuItem clone = new MenuItem(item.getItem());
            clone.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
            clone.setSlots(item.getSlots());
            clone.setPriority(100);
            clone.setType(item.getType());
            clone.setClick((viewer2, event) -> {
                if (clone.getType() == MenuItemType.RETURN) {
                    shop.getEditor().open(viewer2.getPlayer(), 1);
                }
                else if (clone.getType() == MenuItemType.PAGE_NEXT) {
                    this.open(viewer2.getPlayer(), page + 1);
                }
                else if (clone.getType() == MenuItemType.PAGE_PREVIOUS) {
                    this.open(viewer2.getPlayer(), page - 1);
                }
            });
            this.addItem(clone);
            IntStream.of(clone.getSlots()).forEach(freeSlots::remove);
        }

        for (VirtualProduct product : shop.getProducts()) {
            if (product.getPage() != page) continue;

            ItemStack productIcon = new ItemStack(product.getPreview());
            ItemUtil.mapMeta(productIcon, meta -> {
                meta.setDisplayName(VirtualLocales.PRODUCT_OBJECT.getLocalizedName());
                meta.setLore(VirtualLocales.PRODUCT_OBJECT.getLocalizedLore());
                meta.addItemFlags(ItemFlag.values());
                ItemUtil.replace(meta, product.replacePlaceholders());
            });

            MenuItem productItem = new MenuItem(productIcon);
            productItem.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
            productItem.setSlots(product.getSlot());
            productItem.setPriority(200);
            productItem.setClick((viewer2, event) -> {
                if (!event.isLeftClick() && !event.isRightClick()) return;

                if (event.isShiftClick()) {
                    if (event.isLeftClick()) {
                        product.getEditor().open(viewer2.getPlayer(), 1);
                    }
                    else if (event.isRightClick()) {
                        shop.removeProduct(product);
                        shop.saveProducts();
                        this.open(viewer2.getPlayer(), page);
                    }
                    return;
                }

                // Cache clicked product to item stack
                // then remove it from the shop
                ItemStack saved = this.cacheProduct(product);
                shop.removeProduct(product);

                // If user wants to replace a clicked product
                // then create or load cached product from an itemstack
                // and add it to the shop
                ItemStack cursor = event.getCursor();
                if (cursor != null && !cursor.getType().isAir()) {
                    VirtualProduct cached = this.getCachedProduct(cursor);
                    if (cached == null) {
                        cached = new VirtualItemProduct(cursor, ShopUtils.getDefaultCurrency());
                        //cached.setItem(cursor);
                        cached.setPricer(new FlatProductPricer());
                        cached.setStock(new VirtualProductStock());
                        cached.getStock().unlock();
                    }
                    cached.setSlot(event.getRawSlot());
                    cached.setPage(page);
                    shop.addProduct(cached);
                }

                shop.saveProducts();

                // Set cached item to cursor so player can put it somewhere
                event.getView().setCursor(null);
                this.open(viewer2.getPlayer(), page);
                viewer2.getPlayer().getOpenInventory().setCursor(saved);
            });
            this.addItem(productItem);
            freeSlots.remove(product.getSlot());
        }


        MenuItem freeItem = this.addItem(Material.LIME_STAINED_GLASS_PANE, VirtualLocales.PRODUCT_FREE_SLOT);
        freeItem.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
        freeItem.setSlots(freeSlots.stream().mapToInt(Number::intValue).toArray());
        freeItem.setClick((viewer2, event) -> {
            Player player = viewer2.getPlayer();
            ItemStack cursor = event.getCursor();
            boolean hasCursor = cursor != null && !cursor.getType().isAir();
            //if (cursor == null || cursor.getType().isAir()) return;

            VirtualProduct product = hasCursor ? this.getCachedProduct(cursor) : null;
            if (product == null) {
                if (hasCursor) {
                    product = new VirtualItemProduct(cursor, ShopUtils.getDefaultCurrency());
                }
                else if (event.isRightClick()) {
                    product = new VirtualCommandProduct(new ItemStack(Material.COMMAND_BLOCK), ShopUtils.getDefaultCurrency());
                }
                else return;
                product.setPricer(new FlatProductPricer());
                product.setStock(new VirtualProductStock());
                product.getStock().unlock();
            }
            product.setSlot(event.getRawSlot());
            product.setPage(page);
            shop.addProduct(product);
            shop.saveProducts();
            event.getView().setCursor(null);
            this.open(player, page);
        });

        this.addItem(freeItem);
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent e) {
        this.plugin.runTask(task -> {
            Player player = viewer.getPlayer();

            Menu<?> menu = Menu.getMenu(player);
            if (menu != null) return;

            this.object.getEditor().open(player, 1);
        });

        super.onClose(viewer, e);
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @Nullable ItemStack item, @NotNull SlotType slotType, int slot, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, item, slotType, slot, event);
        if (slotType == SlotType.PLAYER || slotType == SlotType.PLAYER_EMPTY) {
            event.setCancelled(false);
        }
    }
}
