package su.nightexpress.nexshop.shop.virtual.editor.rotation;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.menu.Confirmation;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.Rotation;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.type.RotationType;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;

@SuppressWarnings("UnstableApiUsage")
public class RotationOptionsMenu extends LinkedMenu<ShopPlugin, Rotation> {

    private static final String SKULL_CLOCK = "cbbc06a8d6b1492e40f0e7c3b632b6fd8e66dc45c15234990caa5410ac3ac3fd";
    private static final String SKULL_ITEMS = "909846f8f57371cf0f5a31c5542f170b1682cd28d0cb491e1e5a6c2338fbc93";
    private static final String SKULL_SLOTS = "433f5cc9de3585d8f64330f4468d156baf034a25dcb773c0479d7ca526a13d61";
    private static final String SKULL_DELETE = "dc75cd6f9c713e9bf43fea963990d142fc0d252974ebe04b2d882166cbb6d294";
    private static final String SKULL_RESET = "8069cc1666b4ed76587bb1a44fbb7a4375ea03c26d9a47e357b4139e3da28d";

    public RotationOptionsMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X5, VirtualLang.EDITOR_TITLE_ROTATION_OPTIONS.text());

        this.addItem(MenuItem.buildReturn(this, 40, (viewer, event) -> {
            this.runNextTick(() -> module.openRotationsList(viewer.getPlayer(), this.getLink(viewer).getShop()));
        }));

        this.addItem(NightItem.asCustomHead(SKULL_DELETE), VirtualLocales.ROTATION_DELETE, 8, (viewer, event, rotation) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.create(
                (viewer1, event1) -> {
                    VirtualShop shop = rotation.getShop();
                    shop.removeRotation(rotation);
                    shop.setSaveRequired(true);
                    plugin.getDataManager().deleteRotationData(rotation);
                    module.openRotationsList(viewer1.getPlayer(), shop);
                },
                (viewer1, event1) -> {
                    module.openRotationOptions(viewer1.getPlayer(), rotation);
                }
            )));
        });

        this.addItem(NightItem.asCustomHead(SKULL_RESET), VirtualLocales.ROTATION_RESET, 0, (viewer, event, rotation) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.create(
                (viewer1, event1) -> {
                    rotation.getShop().performRotation(rotation);
                    module.openRotationOptions(viewer1.getPlayer(), rotation);
                },
                (viewer1, event1) -> {
                    module.openRotationOptions(viewer1.getPlayer(), rotation);
                }
            )));
        });

        this.addItem(Material.ITEM_FRAME, VirtualLocales.ROTATION_EDIT_ICON, 4, (viewer, event, rotation) -> {
            if (event.isRightClick()) {
                Players.addItem(viewer.getPlayer(), rotation.getIcon().getItemStack());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            rotation.setIcon(NightItem.fromItemStack(cursor));
            event.getView().setCursor(null);
            this.saveAndFlush(viewer, rotation);

        }, ItemOptions.builder().setDisplayModifier((viewer, item) -> {
            item.inherit(this.getLink(viewer).getIcon()).localized(VirtualLocales.ROTATION_EDIT_ICON).setHideComponents(true);
        }).build());

        this.addItem(Material.OAK_HANGING_SIGN, VirtualLocales.ROTATION_EDIT_TYPE, 19, (viewer, event, rotation) -> {
            rotation.setRotationType(Lists.next(rotation.getRotationType()));
            this.saveAndFlush(viewer, rotation);
        });

        this.addItem(NightItem.asCustomHead(SKULL_CLOCK), VirtualLocales.ROTATION_EDIT_INTERVAL, 21, (viewer, event, rotation) -> {
            this.handleInput(Dialog.builder(viewer, Lang.EDITOR_GENERIC_ENTER_SECONDS.text(), input -> {
                rotation.setRotationInterval(input.asIntAbs(0));
                this.save(viewer, rotation);
                return true;
            }));
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> this.getLink(viewer).getRotationType() == RotationType.INTERVAL).build());

        this.addItem(NightItem.asCustomHead(SKULL_CLOCK), VirtualLocales.ROTATION_EDIT_TIMES, 21, (viewer, event, rotation) -> {
            this.runNextTick(() -> module.openRotationTimes(viewer.getPlayer(), rotation));
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> this.getLink(viewer).getRotationType() == RotationType.FIXED).build());

        this.addItem(NightItem.asCustomHead(SKULL_SLOTS), VirtualLocales.ROTATION_EDIT_SLOTS, 23, (viewer, event, rotation) -> {
            this.runNextTick(() -> module.openRotationSlots(viewer.getPlayer(), rotation));
        });

        this.addItem(NightItem.asCustomHead(SKULL_ITEMS), VirtualLocales.ROTATION_EDIT_PRODUCTS, 25, (viewer, event, rotation) -> {
            this.runNextTick(() -> module.openRotationItemsList(viewer.getPlayer(), rotation));
        });
    }

    private void save(@NotNull MenuViewer viewer, @NotNull Rotation rotation) {
        rotation.setSaveRequired(true);
        this.runNextTick(() -> this.flush(viewer));
    }

    private void saveAndFlush(@NotNull MenuViewer viewer, @NotNull Rotation rotation) {
        this.save(viewer, rotation);
        this.runNextTick(() -> this.flush(viewer));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        item.replacement(replacer -> replacer.replace(this.getLink(viewer).replacePlaceholders()));
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
