package su.nightexpress.nexshop.shop.virtual.editor.shop;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.menu.Confirmation;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;

@SuppressWarnings("UnstableApiUsage")
public class ShopOptionsMenu extends LinkedMenu<ShopPlugin, VirtualShop> {

//    private static final String SKULL_NAME = "8ff88b122ff92513c6a27b7f67cb3fea97439e078821d6861b74332a2396";
//    private static final String SKULL_DESCRIPTION = "e71a2285c91c6c72747604819ee5223a90aa51e6e79e4f9af6628ec8f0dd7dfc";
//    private static final String SKULL_PAGES = "291ac432aa40d7e7a687aa85041de636712d4f022632dd5356c880521af2723a";

    private static final String SKULL_BUYING          = "e8f4966bf45d05d85857b1a368632764ea06bb0f43f26aecbee963bc6924b538";
    private static final String SKULL_SELLING         = "9ca79eacafe3db0a3172fed1a48f610016c2db01b299a6b69c2667c1ffac566d";
    private static final String SKULL_ROTATING_ITEMS  = "909846f8f57371cf0f5a31c5542f170b1682cd28d0cb491e1e5a6c2338fbc93";
    private static final String SKULL_NORMAL_ITEMS    = "f1e7dfe5c760eb0c77571074028265311913492e6611ddf6bac793b8f5bb225a";
    private static final String SKULL_ROTATIONS       = "a70216baf1b9675f805dfdf95db043afe6f881c82b25937e46b15068e8f3e882";
    private static final String SKULL_LAYOUT          = "5e44280d42db07b012eb76dee73249c86fc712e8eb14fa2b44847714bbb95f83";
    private static final String SKULL_RESET_PRICES    = "dc75cd6f9c713e9bf43fea963990d142fc0d252974ebe04b2d882166cbb6d294";
    private static final String SKULL_RESET_STOCKS    = "802246ff8b6c617168edaec39660612e72a54ab2eacc27c5e815e4ac70239e3a";
    private static final String SKULL_RESET_ROTATIONS = "8069cc1666b4ed76587bb1a44fbb7a4375ea03c26d9a47e357b4139e3da28d";

    private static final String SKULL_DELETE = "b465f80bf02b408885987b00957ca5e9eb874c3fa88305099597a333a336ee15";

    public ShopOptionsMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, VirtualLang.EDITOR_TITLE_SHOP_SETTINGS.getString());

        this.addItem(MenuItem.buildReturn(this, 49, (viewer, event) -> {
            this.runNextTick(() -> module.openShopsEditor(viewer.getPlayer()));
        }));

        this.addItem(Material.COMPASS, VirtualLocales.SHOP_EDIT_MENU_SLOT, 4, (viewer, event, shop) -> {
            if (event.isRightClick()) {
                shop.setMainMenuSlot(-1);
                this.saveAndFlush(viewer, shop);
                return;
            }

            this.handleInput(Dialog.builder(viewer.getPlayer(), Lang.EDITOR_GENERIC_ENTER_VALUE, input -> {
                shop.setMainMenuSlot(input.asInt(-1));
                shop.saveSettings();
                return true;
            }));
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> VirtualConfig.isCentralMenuEnabled()).build());

        this.addItem(NightItem.asCustomHead(SKULL_DELETE), VirtualLocales.SHOP_DELETE, 7, (viewer, event, shop) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.create(
                (viewer1, event1) -> {
                    module.delete(shop);
                    module.openShopsEditor(viewer1.getPlayer());
                },
                (viewer1, event1) -> {
                    module.openShopOptions(viewer1.getPlayer(), shop);
                }
            )));
        });

        this.addItem(Material.NAME_TAG, VirtualLocales.SHOP_EDIT_NAME, 19, (viewer, event, shop) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_NAME, input -> {
                shop.setName(input.getText());
                shop.saveSettings();
                return true;
            }));
        });

        this.addItem(Material.WRITABLE_BOOK, VirtualLocales.SHOP_EDIT_DESCRIPTION, 20, (viewer, event, shop) -> {
            if (event.isRightClick()) {
                shop.getDescription().clear();
                this.saveAndFlush(viewer, shop);
                return;
            }

            this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_DESCRIPTION, input -> {
                shop.getDescription().add(input.getText());
                shop.saveSettings();
                return true;
            }));
        });

        this.addItem(Material.ITEM_FRAME, VirtualLocales.SHOP_EDIT_ICON, 21, (viewer, event, shop) -> {
            if (event.isRightClick()) {
                Players.addItem(viewer.getPlayer(), shop.getIcon().getItemStack());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            shop.setIcon(NightItem.fromItemStack(cursor));
            event.getView().setCursor(null);
            this.saveAndFlush(viewer, shop);

        }, ItemOptions.builder().setDisplayModifier((viewer, item) -> {
            VirtualShop shop = this.getLink(viewer);
            item.inherit(shop.getIcon()).localized(VirtualLocales.SHOP_EDIT_ICON).setHideComponents(true);
        }).build());




        this.addItem(NightItem.asCustomHead("d719b564f01def417b2beecdb5f4ac474133c12f2b4c4808f27ea01d3fe49ed8"), VirtualLocales.SHOP_EDIT_ALIASES, 13, (viewer, event, shop) -> {
            if (event.isRightClick()) {
                shop.getAliases().clear();
                this.saveAndFlush(viewer, shop);
                return;
            }

            this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_ALIAS, input -> {
                shop.getAliases().add(input.getTextRaw());
                shop.saveSettings();
                return true;
            }));
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> VirtualConfig.SHOP_SHORTCUTS_ENABLED.get()).build());

        this.addItem(Material.ENDER_EYE, VirtualLocales.SHOP_EDIT_PAGES, 22, (viewer, event, shop) -> {
            int add = event.isLeftClick() ? 1 : -1;
            shop.setPages(shop.getPages() + add);
            this.saveAndFlush(viewer, shop);
        });

        this.addItem(Material.REDSTONE, VirtualLocales.SHOP_EDIT_PERMISSION, 23, (viewer, event, shop) -> {
            shop.setPermissionRequired(!shop.isPermissionRequired());
            this.saveAndFlush(viewer, shop);
        }, ItemOptions.builder().setDisplayModifier((viewer, itemStack) -> {
            itemStack.setMaterial(this.getLink(viewer).isPermissionRequired() ? Material.REDSTONE : Material.GUNPOWDER);
        }).build());

        this.addItem(NightItem.asCustomHead(SKULL_BUYING), VirtualLocales.SHOP_EDIT_BUYING, 24, (viewer, event, shop) -> {
            shop.setBuyingAllowed(!shop.isBuyingAllowed());
            this.saveAndFlush(viewer, shop);
        });

        this.addItem(NightItem.asCustomHead(SKULL_SELLING), VirtualLocales.SHOP_EDIT_SELLING, 25, (viewer, event, shop) -> {
            shop.setSellingAllowed(!shop.isSellingAllowed());
            this.saveAndFlush(viewer, shop);
        });



        this.addItem(ItemUtil.getSkinHead(SKULL_LAYOUT), VirtualLocales.SHOP_EDIT_LAYOUTS, 37, (viewer, event, shop) -> {
            this.runNextTick(() -> module.openShopLayouts(viewer.getPlayer(), shop));
        });

        this.addItem(ItemUtil.getSkinHead(SKULL_NORMAL_ITEMS), VirtualLocales.SHOP_EDIT_PRODUCTS_NORMAL, 39, (viewer, event, shop) -> {
            this.runNextTick(() -> module.openNormalProducts(viewer.getPlayer(), shop));
        });

        this.addItem(ItemUtil.getSkinHead(SKULL_ROTATING_ITEMS), VirtualLocales.SHOP_EDIT_PRODUCTS_ROTATING, 41, (viewer, event, shop) -> {
            this.runNextTick(() -> module.openRotatingsProducts(viewer.getPlayer(), shop));
        });

        this.addItem(ItemUtil.getSkinHead(SKULL_ROTATIONS), VirtualLocales.SHOP_EDIT_ROTATIONS, 43, (viewer, event, shop) -> {
            this.runNextTick(() -> module.openRotationsList(viewer.getPlayer(), shop));
        });

        this.addItem(ItemUtil.getSkinHead(SKULL_RESET_PRICES), VirtualLocales.SHOP_RESET_PRICE_DATA, 1, (viewer, event, shop) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.create(
                (viewer1, event1) -> {
                    plugin.getDataManager().resetPriceDatas(shop); // Reset price data (mark all as 'expired').
                    shop.updatePrices(true); // Refresh price values based on fresh, clean data.
                    module.openShopOptions(viewer1.getPlayer(), shop);
                },
                (viewer1, event1) -> {
                    module.openShopOptions(viewer1.getPlayer(), shop);
                }
            )));
        });

        this.addItem(ItemUtil.getSkinHead(SKULL_RESET_STOCKS), VirtualLocales.SHOP_RESET_STOCK_DATA, 2, (viewer, event, shop) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.create(
                (viewer1, event1) -> {
                    plugin.getDataManager().resetStockDatas(shop); // Reset stock data (mark all as 'expired').
                    module.openShopOptions(viewer1.getPlayer(), shop);
                },
                (viewer1, event1) -> {
                    module.openShopOptions(viewer1.getPlayer(), shop);
                }
            )));
        });

        this.addItem(ItemUtil.getSkinHead(SKULL_RESET_ROTATIONS), VirtualLocales.SHOP_RESET_ROTATION_DATA, 6, (viewer, event, shop) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.create(
                (viewer1, event1) -> {
                    shop.performRotation();
                    module.openShopOptions(viewer1.getPlayer(), shop);
                },
                (viewer1, event1) -> {
                    module.openShopOptions(viewer1.getPlayer(), shop);
                }
            )));
        });

//        this.addItem(Material.GOLD_NUGGET, VirtualLocales.SHOP_DISCOUNTS, 14, (viewer, event, shop) -> {
//            // TODO this.runNextTick(() -> this.module.openDiscountsEditor(viewer.getPlayer(), shop));
//        });
    }

    private void saveAndFlush(@NotNull MenuViewer viewer, @NotNull VirtualShop shop) {
        shop.saveSettings();
        this.runNextTick(() -> this.flush(viewer));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        item.replacement(replacer -> replacer.replace(Placeholders.forVirtualShopEditor(this.getLink(viewer))));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {

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
