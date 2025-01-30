package su.nightexpress.nexshop.shop.virtual.editor.product;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.typing.CommandTyping;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.product.typing.VanillaTyping;
import su.nightexpress.nexshop.product.type.ProductTypes;
import su.nightexpress.nexshop.shop.menu.Confirmation;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
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
public class ProductOptionsMenu extends LinkedMenu<ShopPlugin, VirtualProduct> {

    private static final String SKULL_COMMAND = "c2af9b072d19455809dc9d09d9da8bb32f63ad16b015ac772acd9a9f22c77098";
    private static final String SKULL_NBT     = "1789b3e2868d716a921dec5932d530a892f600235f187766bc02d145ed16865b";
    private static final String SKULL_PRICE   = "1b7a1c5da1d362fa357db9889fa6a06a6032e24508481daca914e762660b948";
    private static final String SKULL_STOCK   = "351502ce9c7eaa5e073b4e49b70be64842adefe12fc17d96d38e56f63db293ad";
    private static final String SKULL_DELETE  = "b465f80bf02b408885987b00957ca5e9eb874c3fa88305099597a333a336ee15";

    public ProductOptionsMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, VirtualLang.EDITOR_TITLE_PRODUCT_OPTIONS.getString());

        this.addItem(MenuItem.buildReturn(this, 49, (viewer, event) -> {
            VirtualProduct product = this.getLink(viewer);
            this.runNextTick(() -> {
                if (product.isRotating()) {
                    module.openRotatingsProducts(viewer.getPlayer(), product.getShop());
                }
                else module.openNormalProducts(viewer.getPlayer(), product.getShop(), product.getPage());
            });
        }));

        this.addItem(NightItem.asCustomHead(SKULL_DELETE), VirtualLocales.PRODUCT_DELETE, 53, (viewer, event, product) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.create(
                (viewer1, event1) -> {
                    int page = product.getPage();
                    boolean rotating = product.isRotating();
                    VirtualShop shop = product.getShop();

                    shop.removeProduct(product);
                    shop.saveProducts();

                    if (rotating) {
                        module.openRotatingsProducts(viewer.getPlayer(), shop);
                    }
                    else module.openNormalProducts(viewer.getPlayer(), shop, page);
                },
                (viewer1, event1) -> {
                    module.openProductOptions(viewer1.getPlayer(), product);
                }
            )));
        });

        // =============================================
        // Command Product stuff
        // =============================================
        this.addItem(Material.ITEM_FRAME, VirtualLocales.PRODUCT_EDIT_ICON, 10, (viewer, event, product) -> {
            if (event.isRightClick()) {
                Players.addItem(viewer.getPlayer(), product.getPreview());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            CommandTyping type = (CommandTyping) product.getType();
            type.setPreview(cursor);
            event.getView().setCursor(null);
            this.saveAndFlush(viewer, product);
        }, ItemOptions.builder()
            .setVisibilityPolicy(viewer -> this.getLink(viewer).getType() instanceof CommandTyping)
            .setDisplayModifier((viewer, item) -> {
                ItemStack original = this.getLink(viewer).getType().getPreview();
                item.inherit(NightItem.fromItemStack(original)).localized(VirtualLocales.PRODUCT_EDIT_ICON).setHideComponents(true);
            }).build());

        this.addItem(ItemUtil.getSkinHead(SKULL_COMMAND), VirtualLocales.PRODUCT_EDIT_COMMANDS, 13, (viewer, event, product) -> {
            CommandTyping type = (CommandTyping) product.getType();

            if (event.isRightClick()) {
                type.getCommands().clear();
                this.saveAndFlush(viewer, product);
                return;
            }

            this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_COMMAND, input -> {
                type.getCommands().add(input.getText());
                product.save();
                return true;
            }));
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> this.getLink(viewer).getType() instanceof CommandTyping).build());

        // =============================================
        // Item Product stuff
        // =============================================
        this.addItem(Material.ITEM_FRAME, VirtualLocales.PRODUCT_EDIT_ITEM, 10, (viewer, event, product) -> {
            if (event.isRightClick()) {
                PhysicalTyping packer = (PhysicalTyping) product.getType();
                Players.addItem(viewer.getPlayer(), packer.getItem());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            boolean isBypass = false;//event.isShiftClick();
            ProductTyping typing = ProductTypes.fromItem(cursor, isBypass);

            product.setType(typing);
            event.getView().setCursor(null);
            this.saveAndFlush(viewer, product);
        }, ItemOptions.builder()
            .setVisibilityPolicy(viewer -> this.getLink(viewer).getType() instanceof PhysicalTyping)
            .setDisplayModifier((viewer, item) -> {
                PhysicalTyping type = (PhysicalTyping) this.getLink(viewer).getType();
                ItemStack original = type.getItem();
                item.inherit(NightItem.fromItemStack(original)).localized(VirtualLocales.PRODUCT_EDIT_ITEM).setHideComponents(true);
            }).build());

        this.addItem(ItemUtil.getSkinHead(SKULL_NBT), VirtualLocales.PRODUCT_EDIT_NBT_MATCH, 13, (viewer, event, product) -> {
            if (!(product.getType() instanceof VanillaTyping typing)) return;

            typing.setRespectMeta(!typing.isRespectMeta());
            this.saveAndFlush(viewer, product);
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> this.getLink(viewer).getType() instanceof VanillaTyping).build());

        // =============================================
        // Normal Product stuff
        // =============================================
        this.addItem(NightItem.asCustomHead(SKULL_PRICE), VirtualLocales.PRODUCT_EDIT_PRICE, 16, (viewer, event, product) -> {
            this.runNextTick(() -> module.openPriceOptions(viewer.getPlayer(), product));
        });

        this.addItem(Material.GOLDEN_HELMET, VirtualLocales.PRODUCT_EDIT_RANKS_REQUIRED, 29, (viewer, event, product) -> {
            if (event.isRightClick()) {
                product.getAllowedRanks().clear();
                this.saveAndFlush(viewer, product);
                return;
            }

            this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_RANK, input -> {
                product.getAllowedRanks().add(input.getTextRaw().toLowerCase());
                product.save();
                return true;
            }));
        });

        this.addItem(Material.REDSTONE, VirtualLocales.PRODUCT_EDIT_PERMISIONS_REQUIRED, 33, (viewer, event, product) -> {
            if (event.isRightClick()) {
                product.getRequiredPermissions().clear();
                this.saveAndFlush(viewer, product);
                return;
            }

            this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_PERMISSION, input -> {
                product.getRequiredPermissions().add(input.getTextRaw());
                product.save();
                return true;
            }));
        });

        this.addItem(NightItem.asCustomHead(SKULL_STOCK), VirtualLocales.PRODUCT_EDIT_STOCK, 31, (viewer, event, product) -> {
            this.runNextTick(() -> module.openStockOptions(viewer.getPlayer(), product));
        });
    }

    private void saveAndFlush(@NotNull MenuViewer viewer, @NotNull VirtualProduct product) {
        product.save();
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
