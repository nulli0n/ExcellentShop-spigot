package su.nightexpress.nexshop.shop.virtual.editor.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
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
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.stream.IntStream;

@SuppressWarnings("UnstableApiUsage")
public class ShopLayoutsMenu extends LinkedMenu<ShopPlugin, VirtualShop> implements Filled<Integer> {

    private final VirtualShopModule module;

    public ShopLayoutsMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, VirtualLang.EDITOR_TITLE_SHOP_LAYOUTS.getString());
        this.module = module;

        this.addItem(Material.PAINTING, VirtualLocales.SHOP_EDIT_LAYOUT_BY_DEFAULT, 4, (viewer, event, shop) -> {
            this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_GENERIC_ENTER_NAME, input -> {
                shop.setDefaultLayout(input.getTextRaw());
                shop.saveSettings();
                return true;
            }).setSuggestions(this.module.getLayoutNames(), true));
        });

        this.addItem(MenuItem.buildReturn(this, 49, (viewer, event) -> {
            this.runNextTick(() -> module.openShopOptions(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addItem(MenuItem.buildNextPage(this, 50));
        this.addItem(MenuItem.buildPreviousPage(this, 48));
    }

    @Override
    @NotNull
    public MenuFiller<Integer> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        VirtualShop shop = this.getLink(player);

        return MenuFiller.builder(this)
            .setSlots(IntStream.range(9, 36).toArray())
            .setItems(IntStream.range(1, shop.getPages() + 1).boxed().toList())
            .setItemCreator(page -> {
                return new NightItem(Material.MAP)
                    .setAmount(page)
                    .localized(VirtualLocales.SHOP_EDIT_LAYOUT_BY_PAGE)
                    .replacement(replacer -> replacer
                    .replace(Placeholders.GENERIC_PAGE, String.valueOf(page))
                    .replace(Placeholders.GENERIC_NAME, shop.getLayout(page)));
            })
            .setItemClick(page -> (viewer1, event) -> {
                if (event.isRightClick()) {
                    shop.setLayout(page, null);
                    shop.saveSettings();
                    this.runNextTick(() -> this.flush(viewer));
                    return;
                }

                this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_GENERIC_ENTER_NAME, input -> {
                    shop.setLayout(page, input.getTextRaw());
                    shop.saveSettings();
                    return true;
                }).setSuggestions(this.module.getLayoutNames(), true));
            })
            .build();
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        if (viewer.hasItem(menuItem)) return;

        item.replacement(replacer -> replacer.replace(Placeholders.forVirtualShopEditor(this.getLink(viewer))));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }
}
