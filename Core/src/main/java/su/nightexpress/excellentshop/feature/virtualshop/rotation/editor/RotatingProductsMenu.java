package su.nightexpress.excellentshop.feature.virtualshop.rotation.editor;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.product.ContentType;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.ui.inventory.action.ActionContext;
import su.nightexpress.nightcore.ui.inventory.item.ItemPopulator;
import su.nightexpress.nightcore.ui.inventory.item.ItemState;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractObjectMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RotatingProductsMenu extends AbstractObjectMenu<VirtualShop> {

    private static final IconLocale LOCALE_CREATION = VirtualLang.iconBuilder("UI.Editor.Shop.RotatingProducts.Creation")
        .accentColor(TagWrappers.GREEN)
        .name("Add Product")
        .appendInfo("Drag and drop an item onto", "this icon to create a new", "product from it.")
        .build();

    private static final IconLocale LOCALE_PRODUCT = VirtualLang.iconBuilder("UI.Editor.Shop.RotatingProducts.Item")
        .name(ShopPlaceholders.PRODUCT_PREVIEW_NAME)
        .appendCurrent("Handler", ShopPlaceholders.PRODUCT_HANDLER)
        .appendCurrent("Price Type", ShopPlaceholders.PRODUCT_PRICE_TYPE)
        .appendCurrent("Buy Price", ShopPlaceholders.PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY))
        .appendCurrent("Sell Price", ShopPlaceholders.PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL))
        .br()
        .appendClick("Click to edit")
        .build();

    private final VirtualShopModule module;
    private final ItemPopulator<VirtualProduct> productPopulator;

    public RotatingProductsMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, VirtualLang.EDITOR_TITLE_PRODUCTS_ROTATING.text(), VirtualShop.class);
        this.module = module;
        this.productPopulator = ItemPopulator.builder(VirtualProduct.class)
            .slots(IntStream.range(0, 36).toArray())
            .itemProvider((context, product) -> {
                return NightItem.fromItemStack(product.getEffectivePreview())
                    .localized(LOCALE_PRODUCT)
                    .hideAllComponents()
                    .replace(builder -> builder.with(product.placeholders()));
            })
            .actionProvider(product -> context -> this.module.openProductOptions(context.getPlayer(), product))
            .build();
    }

    @Override
    public void registerActions() {

    }

    @Override
    public void registerConditions() {

    }

    @Override
    public void defineDefaultLayout() {
        this.addBackgroundItem(Material.GRAY_STAINED_GLASS_PANE, IntStream.range(0, 36).toArray());
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(36, 45).toArray());

        this.addNextPageButton(41);
        this.addPreviousPageButton(39);
        this.addBackButton(this::handleBack, 36);

        this.addDefaultButton("add_product", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.ANVIL).hideAllComponents().localized(LOCALE_CREATION))
                .action(this::handleCreation)
                .build()
            )
            .slots(40)
            .build());
    }

    @Override
    protected void onLoad(@NonNull FileConfig config) {

    }

    @Override
    protected void onClick(@NonNull ViewerContext context, @NonNull InventoryClickEvent event) {
        if (event.getRawSlot() >= event.getInventory().getSize()) {
            event.setCancelled(false);
        }
    }

    @Override
    protected void onDrag(@NonNull ViewerContext context, @NonNull InventoryDragEvent event) {

    }

    @Override
    protected void onClose(@NonNull ViewerContext context, @NonNull InventoryCloseEvent event) {

    }

    @Override
    public void onPrepare(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory, @NonNull List<MenuItem> items) {
        VirtualShop shop = this.getObject(context);
        List<VirtualProduct> products = shop.getProducts().stream().filter(VirtualProduct::isRotating)
            .sorted(Comparator.comparing(Product::getId)).collect(Collectors.toCollection(ArrayList::new));

        this.productPopulator.populateTo(context, products, items);
    }

    @Override
    public void onReady(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

    }

    @Override
    public void onRender(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

    }

    private void handleBack(@NonNull ActionContext context) {
        this.module.openShopOptions(context.getPlayer(), this.getObject(context));
    }

    private void handleCreation(@NonNull ActionContext context) {
        InventoryClickEvent event = context.getEvent();
        ItemStack cursor = event.getCursor();
        if (cursor.getType().isAir()) return;

        VirtualShop shop = this.getObject(context);
        VirtualProduct product = this.module.uncacheProduct(cursor);
        if (product == null) {
            product = shop.createProduct(ContentType.ITEM, cursor);
        }

        product.setRotating(true);
        shop.markDirty();
        shop.addProduct(product);

        event.getView().setCursor(null);
        context.getViewer().refresh();
    }
}
