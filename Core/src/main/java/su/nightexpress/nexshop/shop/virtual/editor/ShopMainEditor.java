package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualStock;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.click.ClickResult;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Players;

public class ShopMainEditor extends EditorMenu<ShopPlugin, VirtualShop> implements ShopEditor {

    private static final String TEXTURE_SETTINGS   = "e3c81adc6c06d95c65b6c1089755a04d7ebc414f51ba66d14d0c4c1d71520df6";
    private static final String TEXTURE_BOX        = "b663a178638400f16c7073c63fef13572fab9b6f42df9ea039c3b3f6773faf94";
    private static final String TEXTURE_PAINT      = "5e44280d42db07b012eb76dee73249c86fc712e8eb14fa2b44847714bbb95f83";
    private static final String TEXTURE_TNT_RED    = "dc75cd6f9c713e9bf43fea963990d142fc0d252974ebe04b2d882166cbb6d294";
    private static final String TEXTURE_TNT_ORANGE = "802246ff8b6c617168edaec39660612e72a54ab2eacc27c5e815e4ac70239e3a";

    private final VirtualShopModule module;

    public ShopMainEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, VirtualLang.EDITOR_TITLE_SHOP_SETTINGS.getString(), MenuSize.CHEST_54);
        this.module = module;

        this.addReturn(49, (viewer, event, shop) -> {
            this.runNextTick(() -> this.module.openShopsEditor(viewer.getPlayer()));
        });

        this.addItem(Material.NAME_TAG, VirtualLocales.SHOP_DISPLAY_NAME, 10, (viewer, event, shop) -> {
            this.handleInput(viewer, Lang.EDITOR_GENERIC_ENTER_NAME, (dialog, input) -> {
                shop.setName(input.getText());
                this.save(viewer, shop);
                return true;
            });
        });

        this.addItem(Material.WRITABLE_BOOK, VirtualLocales.SHOP_DESCRIPTION, 11, (viewer, event, shop) -> {
            if (event.isRightClick()) {
                shop.getDescription().clear();
                this.saveAndFlush(viewer, shop);
                return;
            }

            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_DESCRIPTION, (dialog, input) -> {
                shop.getDescription().add(input.getText());
                this.save(viewer, shop);
                return true;
            });
        });

        this.addItem(Material.ITEM_FRAME, VirtualLocales.SHOP_ICON, 12, (viewer, event, shop) -> {
            if (event.isRightClick()) {
                Players.addItem(viewer.getPlayer(), shop.getIcon());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            shop.setIcon(cursor);
            event.getView().setCursor(null);
            this.saveAndFlush(viewer, shop);
        }).getOptions().setDisplayModifier((viewer, item) -> {
            VirtualShop shop = this.getLink(viewer);
            item.setType(shop.getIcon().getType());
            item.setItemMeta(shop.getIcon().getItemMeta());
            ItemReplacer.create(item).readLocale(VirtualLocales.SHOP_ICON).hideFlags()
                .replacement(replacer -> replacer.replace(shop.replacePlaceholders()))
                .writeMeta();
        });

        this.addItem(Material.COMPASS, VirtualLocales.SHOP_MENU_SLOT, 4, (viewer, event, shop) -> {
            if (event.isRightClick()) {
                shop.setMainMenuSlot(-1);
                this.saveAndFlush(viewer, shop);
                return;
            }

            this.handleInput(viewer.getPlayer(), Lang.EDITOR_GENERIC_ENTER_VALUE, (dialog, input) -> {
                shop.setMainMenuSlot(input.asInt());
                this.save(viewer, shop);
                return true;
            });
        }).getOptions().setVisibilityPolicy(viewer -> VirtualConfig.isCentralMenuEnabled());

        this.addItem(ItemUtil.getSkinHead(TEXTURE_PAINT), VirtualLocales.SHOP_LAYOUT, 13, (viewer, event, shop) -> {
            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_TITLE, (dialog, input) -> {
                shop.setLayoutName(input.getTextRaw());
                this.save(viewer, shop);
                return true;
            }).setSuggestions(this.module.getLayoutNames(), true);
        });

        this.addItem(Material.LIME_DYE, VirtualLocales.SHOP_BUYING, 14, (viewer, event, shop) -> {
            shop.setTransactionEnabled(TradeType.BUY, !shop.isTransactionEnabled(TradeType.BUY));
            this.saveAndFlush(viewer, shop);
        }).getOptions().addDisplayModifier((viewer, itemStack) -> {
            itemStack.setType(this.getLink(viewer).isTransactionEnabled(TradeType.BUY) ? Material.LIME_DYE : Material.GRAY_DYE);
        });

        this.addItem(Material.LIME_DYE, VirtualLocales.SHOP_SELLING, 15, (viewer, event, shop) -> {
            shop.setTransactionEnabled(TradeType.SELL, !shop.isTransactionEnabled(TradeType.SELL));
            this.saveAndFlush(viewer, shop);
        }).getOptions().addDisplayModifier((viewer, itemStack) -> {
            itemStack.setType(this.getLink(viewer).isTransactionEnabled(TradeType.SELL) ? Material.LIME_DYE : Material.GRAY_DYE);
        });

        this.addItem(Material.REDSTONE, VirtualLocales.SHOP_PERMISSION, 16, (viewer, event, shop) -> {
            shop.setPermissionRequired(!shop.isPermissionRequired());
            this.saveAndFlush(viewer, shop);
        }).getOptions().addDisplayModifier((viewer, itemStack) -> {
            itemStack.setType(this.getLink(viewer).isPermissionRequired() ? Material.REDSTONE : Material.GUNPOWDER);
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_SETTINGS), VirtualLocales.SHOP_SPECIFIC, 28, (viewer, event, shop) -> {
            this.runNextTick(() -> this.module.openSpecificEditor(viewer.getPlayer(), shop));
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_BOX), VirtualLocales.SHOP_PRODUCTS, 30, (viewer, event, shop) -> {
            this.runNextTick(() -> this.module.openProductsEditor(viewer.getPlayer(), shop));
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_TNT_RED), VirtualLocales.SHOP_RESET_PRICE_DATA, 32, (viewer, event, shop) -> {
            this.plugin.runTaskAsync(task -> {
                shop.getPricer().deleteData();
                shop.getPricer().updatePrices();
            });
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_TNT_ORANGE), VirtualLocales.SHOP_RESET_STOCK_DATA, 34, (viewer, event, shop) -> {
            VirtualStock stock = (VirtualStock) shop.getStock();
            stock.deleteData();
        });

//        this.addItem(ItemUtil.getSkinHead(TEXTURE_NPC), VirtualLocales.SHOP_ATTACHED_NPCS, 4, (viewer, event, shop) -> {
//            if (event.isRightClick()) {
//                shop.getNPCIds().clear();
//                this.saveAndFlush(viewer, shop);
//                return;
//            }
//
//            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_NPC_ID, (dialog, input) -> {
//                int id = input.asInt(-1);
//                if (id < 0) return true;
//
//                shop.getNPCIds().add(id);
//                this.save(viewer, shop);
//                return true;
//            });
//        }).getOptions().setVisibilityPolicy(viewer -> Plugins.isLoaded(HookId.CITIZENS));

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

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);
        if (result.isInventory()) {
            event.setCancelled(false);
        }
    }
}
