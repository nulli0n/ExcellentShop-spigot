package su.nightexpress.excellentshop.virtualshop.rotation.editor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.virtualshop.core.VirtualLang;
import su.nightexpress.excellentshop.virtualshop.core.VirtualLocales;
import su.nightexpress.excellentshop.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.virtualshop.rotation.Rotation;
import su.nightexpress.excellentshop.virtualshop.rotation.RotationItem;
import su.nightexpress.excellentshop.virtualshop.shop.VirtualShop;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
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
public class RotationItemSelectMenu extends LinkedMenu<ShopPlugin, Rotation> implements Filled<VirtualProduct> {

    private final VirtualShopModule module;

    public RotationItemSelectMenu(@NonNull ShopPlugin plugin, @NonNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, VirtualLang.EDITOR_TITLE_ROTATION_ITEM_SELECTION.text());
        this.module = module;

        this.addItem(MenuItem.buildReturn(this, 40, (viewer, event) -> {
            this.runNextTick(() -> module.openRotationItemsList(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addItem(MenuItem.buildNextPage(this, 44));
        this.addItem(MenuItem.buildPreviousPage(this, 36));
    }

    @Override
    @NonNull
    public MenuFiller<VirtualProduct> createFiller(@NonNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Rotation rotation = this.getLink(player);
        VirtualShop shop = rotation.getShop();

        return MenuFiller.builder(this)
            .setSlots(IntStream.range(0, 36).toArray())
            .setItems(shop.getProducts().stream()
                .filter(VirtualProduct::isRotating)
                .filter(product -> !rotation.hasProduct(product))
                .sorted(Comparator.comparing(Product::getId)).collect(Collectors.toCollection(ArrayList::new)))
            .setItemCreator(product -> {
                return NightItem.fromItemStack(product.getEffectivePreview())
                    .localized(VirtualLocales.PRODUCT_ROTATING_OBJECT)
                    .setHideComponents(true)
                    .replace(replacer -> replacer.with(product.placeholders()));
            })
            .setItemClick(product -> (viewer1, event) -> {
                rotation.addItem(new RotationItem(product.getId(), 5D));
                rotation.getShop().markDirty();
                this.runNextTick(() -> this.module.openRotationItemsList(player, rotation));
            })
            .build();
    }

    @Override
    public void onPrepare(@NonNull MenuViewer viewer, @NonNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NonNull MenuViewer viewer, @NonNull Inventory inventory) {

    }
}
