package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.ItemReplacer;

public class StaticSettingsEditor extends EditorMenu<ShopPlugin, StaticShop> implements ShopEditor {

    private final VirtualShopModule module;

    public StaticSettingsEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, VirtualLang.EDITOR_TITLE_SHOP_SETTINGS.getString(), MenuSize.CHEST_36);
        this.module = module;

        this.addReturn(31, (viewer, event, shop) -> {
            this.runNextTick(() -> this.module.openShopEditor(viewer.getPlayer(), this.getLink(viewer)));
        });

        this.addItem(Material.ENDER_PEARL, VirtualLocales.SHOP_STATIC_PAGES, 12, (viewer, event, shop) -> {
            int add = event.isLeftClick() ? 1 : -1;
            shop.setPages(shop.getPages() + add);
            this.saveAndFlush(viewer, shop);
        });

        this.addItem(Material.GOLD_NUGGET, VirtualLocales.SHOP_DISCOUNTS, 14, (viewer, event, shop) -> {
            this.runNextTick(() -> this.module.openDiscountsEditor(viewer.getPlayer(), shop));
        });

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.replace(item, Placeholders.forVirtualShopEditor(this.getLink(viewer)));
        }));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        options.editTitle(this.getLink(viewer).replacePlaceholders());
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }
}
