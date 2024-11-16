package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.product.ProductHandlerRegistry;
import su.nightexpress.nexshop.product.handler.impl.BukkitCommandHandler;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingProduct;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.StaticProduct;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nexshop.shop.virtual.menu.ShopLayout;
import su.nightexpress.nexshop.shop.virtual.type.ShopType;
import su.nightexpress.nightcore.language.entry.LangItem;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.Menu;
import su.nightexpress.nightcore.menu.click.ClickResult;
import su.nightexpress.nightcore.menu.impl.AbstractMenu;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.ItemOptions;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.text.tag.Tags;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ProductListEditor extends EditorMenu<ShopPlugin, VirtualShop> implements ShopEditor {

    private final VirtualShopModule          module;
    private final Map<String, StaticProduct> productCache;
    private final NamespacedKey              keyProductCache;

    public ProductListEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, Tags.BLACK.enclose("Products Editor"), MenuSize.CHEST_54);
        this.module = module;
        this.productCache = new HashMap<>();
        this.keyProductCache = new NamespacedKey(plugin, "product_cache");
    }

    @NotNull
    private ItemStack cacheProduct(@NotNull StaticProduct product) {
        String pId = UUID.randomUUID().toString();
        this.productCache.put(pId, product);

        ItemStack stack = product.getPreview();
        PDCUtil.set(stack, this.keyProductCache, pId);
        return stack;
    }

    @Nullable
    private StaticProduct getCachedProduct(@NotNull ItemStack stack) {
        String pId = PDCUtil.getString(stack, this.keyProductCache).orElse(null);
        if (pId == null) return null;

        PDCUtil.remove(stack, this.keyProductCache);
        return productCache.remove(pId);
    }

    @Override
    public void clear() {
        super.clear();
        this.productCache.clear();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        VirtualShop virtualShop = this.getLink(viewer);
        AbstractVirtualShop<?> shop = (AbstractVirtualShop<?>) virtualShop;

        options.setTitle(virtualShop.replacePlaceholders().apply(options.getTitle()));

        // ====================================
        // Create Pages and Free Slots
        // ====================================
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

        // ====================================
        // Build Shop Layout
        // ====================================
        ShopLayout layout = this.module.getLayout(shop);
        if (layout != null) {
            options.setType(layout.getOptions().getType());
            options.setSize(layout.getOptions().getSize());

            for (MenuItem item : layout.getItems()) {
                if (item.getPriority() < 0) continue;

                MenuItem clone = new MenuItem(item.getItemStack());
                clone.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
                clone.setSlots(item.getSlots());
                clone.setPriority(item.getPriority());

                String handlerName = item.getHandler().getName();
                if (handlerName.equalsIgnoreCase(ItemHandler.RETURN)) {
                    clone.setHandler((viewer1, event) -> {
                        this.runNextTick(() -> this.module.openShopEditor(viewer1.getPlayer(), shop));
                    });
                }
                else if (handlerName.equalsIgnoreCase(ItemHandler.NEXT_PAGE)) {
                    clone.setHandler(ItemHandler.forNextPage(this));
                }
                else if (handlerName.equalsIgnoreCase(ItemHandler.PREVIOUS_PAGE)) {
                    clone.setHandler(ItemHandler.forPreviousPage(this));
                }

                this.addItem(clone);
                IntStream.of(clone.getSlots()).forEach(freeSlots::remove);
            }
        }

        Set<VirtualProduct> products = new HashSet<>();
        if (shop instanceof RotatingShop rotatingShop) {
            int limit = rotatingShop.getProductSlots().length;
            products.addAll(shop.getProducts().stream().skip((long) (page - 1) * limit).limit(limit).collect(Collectors.toSet()));
        }
        else {
            products.addAll(shop.getProducts());
        }

        int index = 0;
        for (VirtualProduct product : products) {
            if (!product.isValid()) {
                this.plugin.error("Invalid item id for '" + product.getId() + "' product in '" + shop.getId() + "' shop!");
                continue;
            }

            int slot;
            if (product instanceof StaticProduct staticProduct) {
                if (staticProduct.getPage() != page) continue;
                slot = staticProduct.getSlot();
            }
            else if (product instanceof RotatingProduct rotatingProduct) {
                slot = rotatingProduct.getShop().getProductSlots()[index++];
            }
            else continue;

            LangItem locale = shop.getType() == ShopType.STATIC ? VirtualLocales.PRODUCT_OBJECT : VirtualLocales.ROTATING_PRODUCT_OBJECT;
            ItemStack productIcon = new ItemStack(product.getPreview());
            ItemReplacer.create(productIcon).readMeta().readLocale(locale).hideFlags().trimmed()
                .replace(product.replacePlaceholders())
                .writeMeta();

            MenuItem productItem = new MenuItem(productIcon);
            productItem.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
            productItem.setSlots(slot);
            productItem.setPriority(Integer.MAX_VALUE);
            productItem.setHandler((viewer2, event) -> {
                if (event.getClick() == ClickType.DROP) {
                    shop.removeProduct(product);
                    shop.getConfigProducts().remove(shop.getProductSavePath(product));
                    shop.getConfigProducts().saveChanges();
                    //shop.saveProducts();
                    this.doFlush(viewer);
                    return;
                }

                if (event.isLeftClick()) {
                    this.runNextTick(() -> this.module.openProductEditor(viewer2.getPlayer(), product));
                    return;
                }

                if (!event.isRightClick()) return;

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
                        su.nightexpress.nexshop.api.shop.handler.ItemHandler handler;
                        if (event.isShiftClick()) {
                            handler = ProductHandlerRegistry.forBukkitItem();
                        }
                        else handler = ProductHandlerRegistry.getHandler(cursor);

                        ProductPacker packer = handler.createPacker(cursor);
                        if (packer == null) return;

                        cached = staticShop.createProduct(currency, handler, packer);

                        shop.getPricer().deleteData(cached);
                        shop.getStock().resetGlobalValues(cached);
                    }
                    cached.setSlot(event.getRawSlot());
                    cached.setPage(page);
                    staticShop.addProduct(cached);
                }

                // Set cached item to cursor so player can put it somewhere
                event.getView().setCursor(null);
                this.saveProductsAndFlush(viewer2, shop);
                viewer2.getPlayer().getOpenInventory().setCursor(saved);
            });
            this.addItem(productItem);
            freeSlots.remove(slot);
        }

        ItemStack freeIcon = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemReplacer.create(freeIcon).readLocale(VirtualLocales.PRODUCT_FREE_SLOT).trimmed().hideFlags().writeMeta();

        MenuItem freeItem = new MenuItem(freeIcon);
        freeItem.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
        freeItem.setSlots(freeSlots.stream().mapToInt(Number::intValue).toArray());
        freeItem.setHandler((viewer2, event) -> {
            ItemStack cursor = event.getCursor();
            boolean hasCursor = cursor != null && !cursor.getType().isAir();

            VirtualProduct product = hasCursor ? this.getCachedProduct(cursor) : null;
            if (product == null) {
                Currency currency = shop.getModule().getDefaultCurrency();
                ProductHandler handler;
                if (event.isShiftClick() && hasCursor) {
                    handler = ProductHandlerRegistry.forBukkitItem();
                }
                else {
                    handler = hasCursor ? ProductHandlerRegistry.getHandler(cursor) : ProductHandlerRegistry.forBukkitCommand();
                }

                ProductPacker packer;
                if (handler instanceof su.nightexpress.nexshop.api.shop.handler.ItemHandler itemHandler && cursor != null) {
                    packer = itemHandler.createPacker(cursor);
                }
                else {
                    BukkitCommandHandler commandHandler = (BukkitCommandHandler) handler;
                    packer = commandHandler.createPacker();
                }

                if (packer == null) {
                    return;
                }

                product = shop.createProduct(currency, handler, packer);

                // Delete product price & stock datas for new items in case there was product with similar ID.
                shop.getPricer().deleteData(product);
                shop.getStock().resetGlobalValues(product);
            }

            if (product instanceof StaticProduct staticProduct) {
                staticProduct.setSlot(event.getRawSlot());
                staticProduct.setPage(page);
            }

            shop.addProduct(product);
            event.getView().setCursor(null);
            this.saveProductAndFlush(viewer2, product);
        });

        this.addItem(freeItem);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);
        if (result.isInventory()) {
            event.setCancelled(false);
        }
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        VirtualShop shop = this.getLink(viewer);
        Player player = viewer.getPlayer();

        this.runNextTick(() -> {
            Menu menu = AbstractMenu.getMenu(player);
            if (menu != null) return;

            this.module.openShopEditor(player, shop);
        });

        super.onClose(viewer, event);
    }
}
