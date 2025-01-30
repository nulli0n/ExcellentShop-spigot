package su.nightexpress.nexshop.shop.virtual.editor.shop;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.Comparator;
import java.util.stream.IntStream;

@SuppressWarnings("UnstableApiUsage")
public class ShopListMenu extends NormalMenu<ShopPlugin> implements Filled<VirtualShop> {

    private final VirtualShopModule module;

    public ShopListMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, VirtualLang.EDITOR_TITLE_SHOP_LIST.getString());
        this.module = module;

        this.addItem(MenuItem.buildExit(this, 39));
        this.addItem(MenuItem.buildNextPage(this, 44));
        this.addItem(MenuItem.buildPreviousPage(this, 36));

        this.addItem(NightItem.fromType(Material.ANVIL).localized(VirtualLocales.SHOP_CREATE).toMenuItem()
            .setSlots(41)
            .setHandler((viewer, event) -> {
                this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_SHOP_ID, input -> {
                    return module.createShop(viewer.getPlayer(), input.getTextRaw());
                }));
            }));
    }

    @Override
    @NotNull
    public MenuFiller<VirtualShop> createFiller(@NotNull MenuViewer viewer) {
        return MenuFiller.builder(this)
            .setSlots(IntStream.range(0, 36).toArray())
            .setItems(this.module.getShops().stream().sorted(Comparator.comparing(VirtualShop::getId)).toList())
            .setItemCreator(shop -> {
                return shop.getIcon()
                    .localized(VirtualLocales.SHOP_OBJECT)
                    .setHideComponents(true)
                    .replacement(replacer -> replacer.replace(Placeholders.forVirtualShopEditor(shop)));
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
