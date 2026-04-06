package su.nightexpress.excellentshop.feature.virtualshop.shop.editor;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualIconsLang;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.Comparator;
import java.util.stream.IntStream;

public class ShopListMenu extends NormalMenu<ShopPlugin> implements Filled<VirtualShop> {

    private static final int[] SHOP_SLOTS = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34};

    private final VirtualShopModule module;

    public ShopListMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, VirtualLang.EDITOR_TITLE_SHOP_LIST.text());
        this.module = module;

        this.addItem(NightItem.fromType(Material.GRAY_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setPriority(-1)
            .setSlots(0,1,2,3,4,5,6,7,8,17,26,35,9,18,27,36,37,38,39,40,41,42,43,44));
        this.addItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setPriority(-1)
            .setSlots(IntStream.range(45, 54).toArray()));

        this.addItem(MenuItem.buildNextPage(this, 26));
        this.addItem(MenuItem.buildPreviousPage(this, 18));

        this.addItem(NightItem.fromType(Material.ANVIL).localized(VirtualIconsLang.ICON_ADD_SHOP).toMenuItem()
            .setSlots(49)
            .setHandler((viewer, event) -> {
                this.module.openShopCreationDialog(viewer.getPlayer(), () -> this.flush(viewer));
            }));
    }

    @Override
    @NotNull
    public MenuFiller<VirtualShop> createFiller(@NotNull MenuViewer viewer) {
        return MenuFiller.builder(this)
            .setSlots(SHOP_SLOTS)
            .setItems(this.module.getShops().stream().sorted(Comparator.comparing(VirtualShop::getId)).toList())
            .setItemCreator(shop -> {
                return shop.getIcon()
                    .localized(VirtualIconsLang.ICON_SHOP)
                    .hideAllComponents()
                    .replace(replacer -> replacer.with(shop.placeholders()));
            })
            .setItemClick(shop -> (viewer1, event) -> {
                this.runNextTick(() -> this.module.openShopOptions(viewer.getPlayer(), shop));
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
