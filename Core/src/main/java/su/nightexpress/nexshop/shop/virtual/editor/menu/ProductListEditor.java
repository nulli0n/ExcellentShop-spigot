package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorLocale;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.Menu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.ItemOptions;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.shop.ProductHandlerRegistry;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingProduct;
import su.nightexpress.nexshop.shop.virtual.impl.StaticProduct;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualShop;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;
import su.nightexpress.nexshop.shop.virtual.type.ShopType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProductListEditor extends EditorMenu<ExcellentShop, AbstractVirtualShop<?>> {

    private static final Map<String, StaticProduct> PRODUCT_CACHE = new HashMap<>();

    private final NamespacedKey keyProductCache;

    public ProductListEditor(@NotNull ExcellentShop plugin, @NotNull AbstractVirtualShop<?> shop) {
        super(shop.plugin(), shop, shop.getName() + ": Products Editor", 54);
        this.keyProductCache = new NamespacedKey(plugin, "product_cache");
    }

    @NotNull
    private ItemStack cacheProduct(@NotNull StaticProduct product) {
        String pId = UUID.randomUUID().toString();
        PRODUCT_CACHE.put(pId, product);

        ItemStack stack = product.getPreview();
        PDCUtil.set(stack, this.keyProductCache, pId);
        return stack;
    }

    @Nullable
    private StaticProduct getCachedProduct(@NotNull ItemStack stack) {
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

        AbstractVirtualShop<?> shop = this.object;
        Set<Integer> freeSlots;

        int page = viewer.getPage();
        if (shop instanceof StaticShop staticShop) {
            viewer.setPages(staticShop.getPages());
            freeSlots = IntStream.range(0, options.getSize()).boxed().collect(Collectors.toSet());
        }
        else if (shop instanceof RotatingShop rotatingShop) {
            viewer.setPages(100);
            freeSlots = IntStream.of(rotatingShop.getProductSlots()).boxed().collect(Collectors.toSet());
        }
        else return;

        for (MenuItem item : shop.getView().getItems()) {
            if (item.getPriority() < 0) continue;

            MenuItem clone = new MenuItem(item.getItem());
            clone.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
            clone.setSlots(item.getSlots());
            clone.setPriority(item.getPriority());
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

        Collection<? extends AbstractVirtualProduct<?>> products = shop.getProducts();
        if (shop instanceof RotatingShop rotatingShop) {
            int limit = rotatingShop.getProductSlots().length;
            products = products.stream().skip((long) (page - 1) * limit).limit(limit).collect(Collectors.toSet());
        }

        int index = 0;
        for (AbstractVirtualProduct<?> product : products) {
            int slot;
            if (product instanceof StaticProduct staticProduct) {
                if (staticProduct.getPage() != page) continue;
                slot = staticProduct.getSlot();
            }
            else if (product instanceof RotatingProduct rotatingProduct) {
                slot = rotatingProduct.getShop().getProductSlots()[index++];
            }
            else continue;

            EditorLocale locale = shop.getType() == ShopType.STATIC ? VirtualLocales.PRODUCT_OBJECT : VirtualLocales.ROTATING_PRODUCT_OBJECT;
            ItemStack productIcon = new ItemStack(product.getPreview());
            ItemReplacer.create(productIcon).readLocale(locale).hideFlags().trimmed()
                .replace(product.replacePlaceholders())
                .writeMeta();

            MenuItem productItem = new MenuItem(productIcon);
            productItem.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
            productItem.setSlots(slot);
            productItem.setPriority(Integer.MAX_VALUE);
            productItem.setClick((viewer2, event) -> {
                if (!event.isLeftClick() && !event.isRightClick()) return;

                if (event.isShiftClick()) {
                    if (event.isLeftClick()) {
                        product.getEditor().open(viewer2.getPlayer(), 1);
                    }
                    else if (event.isRightClick()) {
                        shop.removeProduct(product.getId());
                        shop.saveProducts();
                        this.open(viewer2.getPlayer(), page);
                    }
                    return;
                }

                // Only Static Shops should support free product movement.
                if (!(shop instanceof StaticShop staticShop)) return;
                if (!(product instanceof StaticProduct staticProduct)) return;

                // Cache clicked product to item stack then remove it from the shop
                ItemStack saved = this.cacheProduct(staticProduct);
                staticShop.removeProduct(staticProduct);

                // If user wants to replace a clicked product
                // then create or load cached product from an itemstack
                // and add it to the shop
                ItemStack cursor = event.getCursor();
                if (cursor != null && !cursor.getType().isAir()) {
                    StaticProduct cached = this.getCachedProduct(cursor);
                    if (cached == null) {
                        Currency currency = shop.getModule().getDefaultCurrency();
                        ProductHandler handler = ProductHandlerRegistry.getHandler(cursor);
                        ProductPacker packer = handler.createPacker();
                        if (packer instanceof ItemPacker itemPacker) {
                            itemPacker.load(cursor);
                        }
                        cached = staticShop.createProduct(currency, handler, packer);

                        shop.getPricer().deleteData(cached);
                        shop.getStock().deleteData(cached);
                    }
                    cached.setSlot(event.getRawSlot());
                    cached.setPage(page);
                    staticShop.addProduct(cached);
                }

                staticShop.saveProducts();

                // Set cached item to cursor so player can put it somewhere
                event.getView().setCursor(null);
                this.open(viewer2.getPlayer(), page);
                viewer2.getPlayer().getOpenInventory().setCursor(saved);
            });
            this.addItem(productItem);
            freeSlots.remove(slot);
        }


        MenuItem freeItem = this.addItem(Material.LIME_STAINED_GLASS_PANE, VirtualLocales.PRODUCT_FREE_SLOT);
        freeItem.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
        freeItem.setSlots(freeSlots.stream().mapToInt(Number::intValue).toArray());
        freeItem.setClick((viewer2, event) -> {
            Player player = viewer2.getPlayer();
            ItemStack cursor = event.getCursor();
            boolean hasCursor = cursor != null && !cursor.getType().isAir();

            VirtualProduct product = hasCursor ? this.getCachedProduct(cursor) : null;
            if (product == null) {
                Currency currency = shop.getModule().getDefaultCurrency();
                ProductHandler handler = hasCursor ? ProductHandlerRegistry.getHandler(cursor) : ProductHandlerRegistry.forBukkitCommand();
                ProductPacker packer = handler.createPacker();
                if (packer instanceof ItemPacker itemPacker && cursor != null) {
                    itemPacker.load(cursor);
                }
                product = shop.createProduct(currency, handler, packer);

                // Delete product price & stock datas for new items in case there was product with similar ID.
                shop.getPricer().deleteData(product);
                shop.getStock().deleteData(product);
            }

            if (product instanceof StaticProduct staticProduct) {
                staticProduct.setSlot(event.getRawSlot());
                staticProduct.setPage(page);
            }
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
