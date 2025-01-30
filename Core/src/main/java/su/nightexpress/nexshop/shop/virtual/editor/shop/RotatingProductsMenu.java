package su.nightexpress.nexshop.shop.virtual.editor.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("UnstableApiUsage")
public class RotatingProductsMenu extends LinkedMenu<ShopPlugin, VirtualShop> implements Filled<VirtualProduct> {

    private final VirtualShopModule module;

    public RotatingProductsMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, VirtualLang.EDITOR_TITLE_PRODUCTS_ROTATING.getString());
        this.module = module;

        this.addItem(MenuItem.buildReturn(this, 39, (viewer, event) -> {
            this.runNextTick(() -> module.openShopOptions(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addItem(MenuItem.buildNextPage(this, 44));
        this.addItem(MenuItem.buildPreviousPage(this, 36));

        this.addItem(Material.ANVIL, VirtualLocales.PRODUCT_ROTATING_CREATE, 41, (viewer, event, shop) -> {
            this.runNextTick(() -> module.openProductCreation(viewer.getPlayer(), shop, true, -1, -1));
        });
    }

    @Override
    @NotNull
    public MenuFiller<VirtualProduct> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        VirtualShop shop = this.getLink(player);

        return MenuFiller.builder(this)
            .setSlots(IntStream.range(0, 36).toArray())
            .setItems(shop.getProducts().stream().filter(VirtualProduct::isRotating)
                .sorted(Comparator.comparing(Product::getId)).collect(Collectors.toCollection(ArrayList::new)))
            .setItemCreator(product -> {
                return NightItem.fromItemStack(product.getPreview())
                    .localized(VirtualLocales.PRODUCT_ROTATING_OBJECT)
                    .setHideComponents(true)
                    .replacement(replacer -> replacer.replace(product.replacePlaceholders()));
            })
            .setItemClick(product -> (viewer1, event) -> {
                this.runNextTick(() -> this.module.openProductOptions(player, product));
            })
            .build();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
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
}
