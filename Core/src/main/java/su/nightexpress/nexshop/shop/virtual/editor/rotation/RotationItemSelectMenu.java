package su.nightexpress.nexshop.shop.virtual.editor.rotation;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.Rotation;
import su.nightexpress.nexshop.shop.virtual.impl.RotationItem;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
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

    public RotationItemSelectMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, VirtualLang.EDITOR_TITLE_ROTATION_ITEM_SELECTION.text());
        this.module = module;

        this.addItem(MenuItem.buildReturn(this, 40, (viewer, event) -> {
            this.runNextTick(() -> module.openRotationItemsList(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addItem(MenuItem.buildNextPage(this, 44));
        this.addItem(MenuItem.buildPreviousPage(this, 36));
    }

    @Override
    @NotNull
    public MenuFiller<VirtualProduct> createFiller(@NotNull MenuViewer viewer) {
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
                return NightItem.fromItemStack(product.getPreviewOrPlaceholder())
                    .localized(VirtualLocales.PRODUCT_ROTATING_OBJECT)
                    .setHideComponents(true)
                    .replacement(replacer -> replacer.replace(product.replacePlaceholders()));
            })
            .setItemClick(product -> (viewer1, event) -> {
                rotation.addItem(new RotationItem(product.getId(), 5D));
                rotation.setSaveRequired(true);
                this.runNextTick(() -> this.module.openRotationItemsList(player, rotation));
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
}
