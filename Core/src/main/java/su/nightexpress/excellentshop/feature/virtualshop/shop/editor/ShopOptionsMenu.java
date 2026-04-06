package su.nightexpress.excellentshop.feature.virtualshop.shop.editor;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.core.*;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.confirmation.Confirmation;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShopOptionsMenu extends LinkedMenu<ShopPlugin, VirtualShop> {

    public ShopOptionsMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, VirtualLang.EDITOR_TITLE_SHOP_SETTINGS.text());

        this.addItem(NightItem.fromType(Material.GRAY_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setPriority(-1)
            .setSlots(0,1,2,3,4,5,6,7,8,17,26,35,9,18,27,36,37,38,39,40,41,42,43,44));
        this.addItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setPriority(-1)
            .setSlots(IntStream.range(45, 54).toArray()));
        this.addItem(NightItem.fromType(Material.GLASS_PANE).setHideTooltip(true).toMenuItem().setPriority(-1)
            .setSlots(IntStream.range(19, 26).toArray()));

        this.addItem(MenuItem.buildReturn(this, 49, (viewer, event) -> {
            this.runNextTick(() -> module.openShopsEditor(viewer.getPlayer()));
        }));

        this.addItem(Material.NAME_TAG, VirtualIconsLang.ICON_SHOP_NAME, 10, (viewer, event, shop) -> {
            module.openShopNameDialog(viewer.getPlayer(), shop, () -> this.flush(viewer));
        });

        this.addItem(Material.LECTERN, VirtualIconsLang.ICON_SHOP_DESCRIPTION, 11, (viewer, event, shop) -> {
            module.openShopDescription(viewer.getPlayer(), shop, () -> this.flush(viewer));
        });

        // <-- Shop Icon is in #onPrepare -->

        this.addItem(NightItem.fromType(Material.COMMAND_BLOCK), VirtualIconsLang.ICON_SHOP_ALIASES, 13, (viewer, event, shop) -> {
            module.openShopAliasesDialog(viewer.getPlayer(), shop, () -> this.flush(viewer));
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> VirtualConfig.SHOP_SHORTCUTS_ENABLED.get()).build());

        this.addItem(Material.COMPASS, VirtualIconsLang.ICON_SHOP_SLOTS, 14, (viewer, event, shop) -> {
            module.openShopMenuSlotsDialog(viewer.getPlayer(), shop, () -> this.flush(viewer));
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> VirtualConfig.isCentralMenuEnabled()).build());

        this.addItem(Material.ENDER_PEARL, VirtualIconsLang.ICON_SHOP_PAGES, 15, (viewer, event, shop) -> {
            module.openShopPagesDialog(viewer.getPlayer(), shop, () -> this.flush(viewer));
        });

        this.addItem(Material.REDSTONE, VirtualLocales.SHOP_EDIT_PERMISSION, 16, (viewer, event, shop) -> {
            shop.setPermissionRequired(!shop.isPermissionRequired());
            this.saveAndFlush(viewer, shop);
        }, ItemOptions.builder().setDisplayModifier((viewer, itemStack) -> {
            itemStack.setMaterial(this.getLink(viewer).isPermissionRequired() ? Material.REDSTONE : Material.GUNPOWDER);
        }).build());



        this.addItem(NightItem.fromType(Material.GLOW_ITEM_FRAME), VirtualLocales.SHOP_EDIT_LAYOUTS, 28, (viewer, event, shop) -> {
            module.openShopPageLayoutsDialog(viewer.getPlayer(), shop, () -> this.flush(viewer));
        });

        this.addItem(NightItem.fromType(Material.LIME_DYE), VirtualLocales.SHOP_EDIT_BUYING, 29, (viewer, event, shop) -> {
            shop.setBuyingAllowed(!shop.isBuyingAllowed());
            this.saveAndFlush(viewer, shop);
        }, ItemOptions.builder().setDisplayModifier((viewer, item) -> item.setMaterial(this.getLink(viewer).isBuyingAllowed() ? Material.LIME_DYE : Material.GRAY_DYE)).build());

        this.addItem(NightItem.fromType(Material.LIME_DYE), VirtualLocales.SHOP_EDIT_SELLING, 30, (viewer, event, shop) -> {
            shop.setSellingAllowed(!shop.isSellingAllowed());
            this.saveAndFlush(viewer, shop);
        }, ItemOptions.builder().setDisplayModifier((viewer, item) -> item.setMaterial(this.getLink(viewer).isSellingAllowed() ? Material.LIME_DYE : Material.GRAY_DYE)).build());





        this.addItem(NightItem.fromType(Material.CHEST), VirtualLocales.SHOP_EDIT_PRODUCTS_NORMAL, 32, (viewer, event, shop) -> {
            this.runNextTick(() -> module.openNormalProducts(viewer.getPlayer(), shop));
        });

        this.addItem(NightItem.fromType(Material.ENDER_CHEST), VirtualLocales.SHOP_EDIT_PRODUCTS_ROTATING, 33, (viewer, event, shop) -> {
            this.runNextTick(() -> module.openRotatingsProducts(viewer.getPlayer(), shop));
        });

        this.addItem(NightItem.fromType(Material.CLOCK), VirtualLocales.SHOP_EDIT_ROTATIONS, 34, (viewer, event, shop) -> {
            this.runNextTick(() -> module.openRotationsList(viewer.getPlayer(), shop));
        });

        // TODO Dialog only
        /*this.addItem(NightItem.fromType(Material.TNT), VirtualLocales.SHOP_RESET_PRICE_DATA, 45, (viewer, event, shop) -> {
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
        });*/

        this.addItem(NightItem.fromType(Material.BARRIER), VirtualLocales.SHOP_DELETE, 53, (viewer, event, shop) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.builder()
                .onAccept((viewer1, event1) -> {
                    module.delete(shop);
                    module.openShopsEditor(viewer1.getPlayer());
                })
                .onReturn((viewer1, event1) -> {
                    module.openShopOptions(viewer1.getPlayer(), shop);
                })
                .returnOnAccept(false)
                .build()
            ));
        });

        /*this.addItem(ItemUtil.getSkinHead(SKULL_RESET_STOCKS), VirtualLocales.SHOP_RESET_STOCK_DATA, 2, (viewer, event, shop) -> {
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
        });*/
    }

    private void saveAndFlush(@NotNull MenuViewer viewer, @NotNull VirtualShop shop) {
        shop.markDirty();
        this.runNextTick(() -> this.flush(viewer));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        VirtualShop shop = this.getLink(viewer);

        item.replace(builder -> builder
            .with(shop.placeholders())
            .with(ShopPlaceholders.VIRTUAL_SHOP_PERMISSION_NODE, () -> VirtualPerms.PREFIX_SHOP + shop.getId())
            .with(ShopPlaceholders.VIRTUAL_SHOP_PERMISSION_REQUIRED, () -> CoreLang.STATE_YES_NO.get(shop.isPermissionRequired()))
            .with(ShopPlaceholders.VIRTUAL_SHOP_MENU_SLOTS, () -> !shop.hasMenuSlots() ? CoreLang.STATE_ENABLED_DISALBED.get(false) : shop.getMenuSlots().stream().map(i -> "#" + i).collect(Collectors.joining(", ")))
            .with(ShopPlaceholders.VIRTUAL_SHOP_ALIASES, () -> !shop.hasAliases() ? Lang.OTHER_UNDEFINED.text() : shop.getSlashedAliases().stream().map(CoreLang::goodEntry).collect(Collectors.joining(TagWrappers.BR)))
        );
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        VirtualShop shop = this.getLink(viewer);

        viewer.addItem(shop.getIcon().localized(VirtualIconsLang.ICON_SHOP_ICON).toMenuItem().setSlots(12).build());
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);

        if (!result.isInventory()) return;

        ItemStack target = result.getItemStack();
        if (target == null || target.getType().isAir()) return;

        VirtualShop shop = this.getLink(viewer);
        shop.setIcon(NightItem.fromItemStack(target));
        this.saveAndFlush(viewer, shop);
    }
}
