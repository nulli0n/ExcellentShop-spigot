package su.nightexpress.nexshop.shop.virtual.editor.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Keys;
import su.nightexpress.nexshop.product.content.ContentType;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.menu.ShopLayout;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NormalProductsMenu extends LinkedMenu<ShopPlugin, VirtualShop> {

    private final VirtualShopModule           module;
    private final Map<String, VirtualProduct> productCache;

    public NormalProductsMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, VirtualLang.EDITOR_TITLE_PRODUCTS_NORMAL.text());
        this.module = module;
        this.productCache = new HashMap<>();
    }

    @NotNull
    private ItemStack cacheProduct(@NotNull VirtualProduct product) {
        String pId = UUID.randomUUID().toString();
        this.productCache.put(pId, product);

        ItemStack stack = product.getPreviewOrPlaceholder();
        PDCUtil.set(stack, Keys.keyProductCache, pId);
        return stack;
    }

    @Nullable
    private VirtualProduct getCachedProduct(@NotNull ItemStack stack) {
        String pId = PDCUtil.getString(stack, Keys.keyProductCache).orElse(null);
        if (pId == null) return null;

        PDCUtil.remove(stack, Keys.keyProductCache);
        return productCache.remove(pId);
    }

    @Override
    public void clear() {
        super.clear();
        this.productCache.clear();
    }

    public boolean open(@NotNull Player player, @NotNull VirtualShop shop, int page) {
        return this.open(player, shop, viewer -> {
            viewer.setPages(shop.getPages());
            viewer.setPage(page);
        });
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        VirtualShop shop = this.getLink(viewer);
        Player player = viewer.getPlayer();

        int size = view.getTopInventory().getSize();
        int page = viewer.getPage();
        Set<Integer> freeSlots = IntStream.range(0, size).boxed().collect(Collectors.toSet());

        // ====================================
        // Build Shop Layout
        // ====================================
        ShopLayout layout = this.module.getLayout(shop, page);
        if (layout != null) {
            for (MenuItem layoutItem : layout.getItems()) {
                if (layoutItem.getPriority() < 0) continue;

                MenuItem.Builder builder = MenuItem.builder(layoutItem.getItem().copy())
                    .setPriority(layoutItem.getPriority())
                    .setSlots(layoutItem.getSlots());

                ItemHandler handler = layoutItem.getHandler();
                if (handler != null) {
                    String handlerName = handler.getName();
                    if (handlerName.equalsIgnoreCase(ItemHandler.RETURN)) {
                        builder.setHandler((viewer1, event) -> {
                            this.runNextTick(() -> this.module.openShopOptions(viewer1.getPlayer(), shop));
                        });
                    }
                    else if (handlerName.equalsIgnoreCase(ItemHandler.NEXT_PAGE)) {
                        builder.setHandler(ItemHandler.forNextPage(this));
                    }
                    else if (handlerName.equalsIgnoreCase(ItemHandler.PREVIOUS_PAGE)) {
                        builder.setHandler(ItemHandler.forPreviousPage(this));
                    }
                }

                this.addItem(viewer, builder.build());

                IntStream.of(layoutItem.getSlots()).forEach(freeSlots::remove);
            }
        }

        for (VirtualProduct product : shop.getProducts()) {
            if (product.isRotating()) continue;
            if (product.getPage() != page) continue;

            int slot = product.getSlot();

            this.addItem(viewer, NightItem.fromItemStack(product.getPreviewOrPlaceholder())
                .setHideComponents(true)
                .localized(VirtualLocales.PRODUCT_OBJECT)
                .replacement(replacer -> replacer.replace(product.replacePlaceholders()))
                .toMenuItem()
                .setSlots(slot)
                .setPriority(MenuItem.HIGH_PRIORITY)
                .setHandler((viewer1, event) -> {
                    if (event.isLeftClick()) {
//                        this.handleInput(Dialog.builder(viewer, Lang.EDITOR_PRODUCT_ENTER_UNI_PRICE, input -> {
//                            String[] split = input.getTextRaw().split(" ");
//                            double min = NumberUtil.getAnyDouble(split[0], -1);
//                            double max = split.length >= 2 ? NumberUtil.getAnyDouble(split[1], -1) : min;
//
//                            product.getPricer().setPrice(TradeType.BUY, min);
//                            product.getPricer().setPrice(TradeType.SELL, max);
//                            product.save();
//                            return true;
//                        }));
                        this.runNextTick(() -> this.module.openProductOptions(viewer1.getPlayer(), product));
                        return;
                    }

                    if (!event.isRightClick()) return;

                    // Cache clicked product to item stack then remove it from the shop
                    ItemStack saved = this.cacheProduct(product);
                    shop.removeProduct(product);
                    shop.markDirty();

                    // Replace current product with the one from player's cursor.
                    ItemStack cursor = event.getCursor();
                    if (cursor != null && !cursor.getType().isAir()) {
                        VirtualProduct newProduct = this.getCachedProduct(cursor);
                        if (newProduct == null) {
                            newProduct = shop.createProduct(ContentType.ITEM, cursor);
                        }
                        if (newProduct != null) {
                            newProduct.setSlot(event.getRawSlot());
                            newProduct.setPage(page);
                            shop.markDirty();
                            shop.addProduct(newProduct);
                        }
                    }

                    // Set cached item to cursor so player can put it somewhere
                    event.getView().setCursor(null);

                    this.runNextTick(() -> {
                        this.flush(viewer1);
                        viewer1.getPlayer().getOpenInventory().setCursor(saved);
                    });
                }));

            freeSlots.remove(slot);
        }

        shop.getRotations().forEach(other -> {
            other.getSlots(page).forEach(slot -> {
                this.addItem(viewer, NightItem.fromType(Material.RED_STAINED_GLASS_PANE)
                    .setHideComponents(true)
                    .localized(VirtualLocales.PRODUCT_ROTATION_SLOT)
                    .toMenuItem()
                    .setSlots(slot)
                    .setPriority(MenuItem.HIGH_PRIORITY));

                freeSlots.remove(slot);
            });
        });

        this.addItem(viewer, NightItem.fromType(Material.LIME_STAINED_GLASS_PANE)
            .setHideComponents(true)
            .localized(VirtualLocales.PRODUCT_FREE_SLOT)
            .toMenuItem()
            .setSlots(freeSlots.stream().mapToInt(Number::intValue).toArray())
            .setHandler((viewer1, event) -> {
                ItemStack cursor = event.getCursor();
                boolean hasCursor = cursor != null && !cursor.getType().isAir();
                int slot = event.getRawSlot();

                if (!hasCursor) {
                    this.runNextTick(() -> this.module.openProductCreation(player, shop, false, page, slot));
                    return;
                }

                VirtualProduct product = this.getCachedProduct(cursor);
                if (product == null) {
                    product = shop.createProduct(ContentType.ITEM, cursor);
                }
                if (product == null) return;

                product.setSlot(event.getRawSlot());
                product.setPage(page);
                shop.markDirty();

                shop.addProduct(product);
                event.getView().setCursor(null);
                this.runNextTick(() -> this.flush(viewer));
            }));
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
            InventoryType type = player.getOpenInventory().getType();
            if (type != InventoryType.CRAFTING && type != InventoryType.CREATIVE) return;

            this.module.openShopOptions(player, shop);
        });

        super.onClose(viewer, event);
    }
}
