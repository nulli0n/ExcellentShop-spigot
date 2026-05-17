package su.nightexpress.excellentshop.virtualshop.rotation.editor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.virtualshop.core.VirtualLang;
import su.nightexpress.excellentshop.virtualshop.core.VirtualLocales;
import su.nightexpress.excellentshop.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.virtualshop.rotation.Rotation;
import su.nightexpress.excellentshop.virtualshop.rotation.RotationItem;
import su.nightexpress.excellentshop.virtualshop.shop.VirtualShop;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.Comparator;
import java.util.stream.IntStream;

@SuppressWarnings("UnstableApiUsage")
public class RotationItemsListMenu extends LinkedMenu<ShopPlugin, Rotation> implements Filled<RotationItem> {

    //private final VirtualShopModule module;

    public RotationItemsListMenu(@NonNull ShopPlugin plugin, @NonNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, VirtualLang.EDITOR_TITLE_ROTATION_ITEMS.text());
        //this.module = module;

        this.addItem(MenuItem.buildReturn(this, 39, (viewer, event) -> {
            this.runNextTick(() -> module.openRotationOptions(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addItem(MenuItem.buildNextPage(this, 44));
        this.addItem(MenuItem.buildPreviousPage(this, 36));

        this.addItem(Material.ANVIL, VirtualLocales.ROTATION_ADD_ITEM, 41, (viewer, event, rotation) -> {
            this.runNextTick(() -> module.openRotationItemSelection(viewer.getPlayer(), rotation));
        });
    }

    @Override
    @NonNull
    public MenuFiller<RotationItem> createFiller(@NonNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Rotation rotation = this.getLink(player);
        VirtualShop shop = rotation.getShop();

        return MenuFiller.builder(this)
            .setSlots(IntStream.range(0, 36).toArray())
            .setItems(rotation.getItems().stream().sorted(Comparator.comparing(RotationItem::getProductId)).toList())
            .setItemCreator(rotationItem -> {
                VirtualProduct product = shop.getProductById(rotationItem.getProductId());
                if (product == null) return NightItem.fromType(Material.BARRIER);

                return NightItem.fromItemStack(product.getEffectivePreview())
                    .localized(VirtualLocales.ROTATION_ITEM_OBJECT)
                    .setHideComponents(true)
                    .replace(replacer -> replacer
                        .with(product.placeholders())
                        .with(ShopPlaceholders.GENERIC_WEIGHT, () -> NumberUtil.format(rotationItem.getWeight()))
                    );
            })
            .setItemClick(rotationItem -> (viewer1, event) -> {
                if (event.isLeftClick()) {
                    this.handleInput(Dialog.builder(viewer1, VirtualLang.EDITOR_ENTER_WEIGHT.text(), input -> {
                        rotationItem.setWeight(input.asDouble(0));
                        this.save(viewer, rotation);
                        return true;
                    }));
                    return;
                }

                if (event.getClick() == ClickType.DROP) {
                    rotation.removeItem(rotationItem);
                    this.saveAndFlush(viewer, rotation);
                }
            })
            .build();
    }

    private void save(@NonNull MenuViewer viewer, @NonNull Rotation rotation) {
        rotation.getShop().markDirty();
        this.runNextTick(() -> this.flush(viewer));
    }

    private void saveAndFlush(@NonNull MenuViewer viewer, @NonNull Rotation rotation) {
        this.save(viewer, rotation);
        this.runNextTick(() -> this.flush(viewer));
    }

    @Override
    public void onPrepare(@NonNull MenuViewer viewer, @NonNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NonNull MenuViewer viewer, @NonNull Inventory inventory) {

    }
}
