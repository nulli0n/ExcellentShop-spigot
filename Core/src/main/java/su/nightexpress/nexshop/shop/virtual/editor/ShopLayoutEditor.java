package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.Replacer;

import java.util.stream.IntStream;

public class ShopLayoutEditor extends EditorMenu<ShopPlugin, VirtualShop> implements ShopEditor, AutoFilled<Integer> {

    private final VirtualShopModule module;

    public ShopLayoutEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, VirtualLang.EDITOR_TITLE_SHOP_LAYOUTS.getString(), MenuSize.CHEST_54);
        this.module = module;

        this.addItem(Material.PAINTING, VirtualLocales.SHOP_LAYOUT_BY_DEFAULT, 4, (viewer, event, shop) -> {
            this.handleInput(viewer, VirtualLang.EDITOR_GENERIC_ENTER_NAME, (dialog, input) -> {
                shop.setDefaultLayout(input.getTextRaw());
                this.save(viewer, shop);
                return true;
            }).setSuggestions(this.module.getLayoutNames(), true);
        });

        this.addReturn(49, (viewer, event, shop) -> {
            this.runNextTick(() -> module.openShopEditor(viewer.getPlayer(), shop));
        });

        this.addNextPage(50);
        this.addPreviousPage(48);

        this.getItems().forEach(menuItem -> {
            menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> Replacer.create().replace(Placeholders.forVirtualShopEditor(this.getLink(viewer))).apply(itemStack));
        });
    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<Integer> autoFill) {
        Player player = viewer.getPlayer();
        VirtualShop shop = this.getLink(player);

        autoFill.setSlots(IntStream.range(9, 36).toArray());
        autoFill.setItems(IntStream.range(1, shop.getPages() + 1).boxed().toList());
        autoFill.setItemCreator(page -> {
            NightItem item = new NightItem(Material.MAP);

            item.setAmount(page);
            item.setDisplayName(VirtualLocales.SHOP_LAYOUT_BY_PAGE.getLocalizedName());
            item.setLore(VirtualLocales.SHOP_LAYOUT_BY_PAGE.getLocalizedLore());

            return item.getTranslated(Replacer.create()
                .replace(Placeholders.GENERIC_PAGE, String.valueOf(page))
                .replace(Placeholders.GENERIC_NAME, shop.getLayout(page))
            );
        });
        autoFill.setClickAction(page -> (viewer1, event) -> {
            if (event.isRightClick()) {
                shop.setLayout(page, null);
                this.saveAndFlush(viewer, shop);
                return;
            }

            this.handleInput(viewer, VirtualLang.EDITOR_GENERIC_ENTER_NAME, (dialog, input) -> {
                shop.setLayout(page, input.getTextRaw());
                this.save(viewer, shop);
                return true;
            }).setSuggestions(this.module.getLayoutNames(), true);
        });
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        options.editTitle(this.getLink(viewer).replacePlaceholders());
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }
}
