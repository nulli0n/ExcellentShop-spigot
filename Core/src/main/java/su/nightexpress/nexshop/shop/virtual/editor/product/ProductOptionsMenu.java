package su.nightexpress.nexshop.shop.virtual.editor.product;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.product.content.ContentTypes;
import su.nightexpress.nexshop.product.content.ProductContent;
import su.nightexpress.nexshop.product.content.impl.CommandContent;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.click.ClickResult;
import su.nightexpress.nightcore.ui.menu.confirmation.Confirmation;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.ArrayList;

public class ProductOptionsMenu extends LinkedMenu<ShopPlugin, VirtualProduct> {

    private static final String SKULL_NBT     = "1789b3e2868d716a921dec5932d530a892f600235f187766bc02d145ed16865b";
    private static final String SKULL_PRICE   = "1b7a1c5da1d362fa357db9889fa6a06a6032e24508481daca914e762660b948";
    private static final String SKULL_STOCK   = "351502ce9c7eaa5e073b4e49b70be64842adefe12fc17d96d38e56f63db293ad";
    private static final String SKULL_DELETE  = "b465f80bf02b408885987b00957ca5e9eb874c3fa88305099597a333a336ee15";

    private final VirtualShopModule module;

    public ProductOptionsMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, VirtualLang.EDITOR_TITLE_PRODUCT_OPTIONS.text());
        this.module = module;

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
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.builder()
                .onAccept((viewer1, event1) -> {
                    int page = product.getPage();
                    boolean rotating = product.isRotating();
                    VirtualShop shop = product.getShop();

                    shop.removeProduct(product);
                    shop.markDirty();

                    if (rotating) {
                        module.openRotatingsProducts(viewer.getPlayer(), shop);
                    }
                    else module.openNormalProducts(viewer.getPlayer(), shop, page);
                })
                .onReturn((viewer1, event1) -> {
                    module.openProductOptions(viewer1.getPlayer(), product);
                })
                .returnOnAccept(false)
                .build()
            ));
        });

        // =============================================
        // Command Product stuff
        // =============================================
        this.addItem(Material.ITEM_FRAME, VirtualLocales.PRODUCT_EDIT_ICON, 10, (viewer, event, product) -> {
            if (event.isRightClick() && product.isValid()) {
                Players.addItem(viewer.getPlayer(), product.getPreview());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            CommandContent type = (CommandContent) product.getContent();
            type.setPreview(cursor);
            event.getView().setCursor(null);
            this.saveAndFlush(viewer, product);
        }, ItemOptions.builder()
            .setVisibilityPolicy(viewer -> this.getLink(viewer).getContent() instanceof CommandContent)
            .setDisplayModifier((viewer, item) -> {
                // TODO
                ItemStack original = this.getLink(viewer).getContent().getPreview();
                item.inherit(NightItem.fromItemStack(original)).localized(VirtualLocales.PRODUCT_EDIT_ICON).setHideComponents(true);
            }).build());



        // =============================================
        // Item Product stuff
        // =============================================
        this.addItem(Material.ITEM_FRAME, VirtualLocales.PRODUCT_EDIT_ITEM, 10, (viewer, event, product) -> {
            if (event.isRightClick() && product.isValid()) {
                ItemContent packer = (ItemContent) product.getContent();
                Players.addItem(viewer.getPlayer(), packer.getItem());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            ProductContent content = ContentTypes.fromItem(cursor, this.module::isItemProviderAllowed);

            product.setContent(content);
            event.getView().setCursor(null);
            this.saveAndFlush(viewer, product);
        }, ItemOptions.builder()
            .setVisibilityPolicy(viewer -> this.getLink(viewer).getContent() instanceof ItemContent)
            .setDisplayModifier((viewer, item) -> {
                // TODO
                ItemContent type = (ItemContent) this.getLink(viewer).getContent();
                ItemStack original = type.getItem();
                item.inherit(NightItem.fromItemStack(original)).localized(VirtualLocales.PRODUCT_EDIT_ITEM).setHideComponents(true);
            }).build());

        this.addItem(ItemUtil.getSkinHead(SKULL_NBT), VirtualLocales.PRODUCT_EDIT_NBT_MATCH, 13, (viewer, event, product) -> {
            if (!(product.getContent() instanceof ItemContent typing)) return;
            if (!typing.getAdaptedItem().getAdapter().isVanilla()) return;

            typing.setCompareNbt(!typing.isCompareNbt());
            this.saveAndFlush(viewer, product);
        }, ItemOptions.builder().setVisibilityPolicy(viewer -> this.getLink(viewer).getContent() instanceof ItemContent type && type.getAdaptedItem().getAdapter().isVanilla()).build());

        // =============================================
        // Normal Product stuff
        // =============================================
        this.addItem(NightItem.asCustomHead(SKULL_PRICE), VirtualLocales.PRODUCT_EDIT_PRICE, 16, (viewer, event, product) -> {
            if (this.module.handleDialogs(dialogs -> dialogs.openProductPrice(viewer.getPlayer(), product))) return;

            this.runNextTick(() -> module.openPriceOptions(viewer.getPlayer(), product));
        });

        this.addItem(Material.GOLDEN_HELMET, VirtualLocales.PRODUCT_EDIT_RANKS_REQUIRED, 28, (viewer, event, product) -> {
            if (event.isRightClick()) {
                product.getAllowedRanks().clear();
                this.saveAndFlush(viewer, product);
                return;
            }

            this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_RANK.text(), input -> {
                product.getAllowedRanks().add(input.getTextRaw().toLowerCase());
                product.getShop().markDirty();
                return true;
            }));
        });

        this.addItem(NightItem.asCustomHead(SKULL_STOCK), VirtualLocales.PRODUCT_EDIT_STOCK, 30, (viewer, event, product) -> {
            this.runNextTick(() -> module.openStockOptions(viewer.getPlayer(), product));
        });

        this.addItem(Material.REDSTONE, VirtualLocales.PRODUCT_EDIT_REQUIRED_PERMISSIONS, 32, (viewer, event, product) -> {
            if (event.isRightClick()) {
                product.getRequiredPermissions().clear();
                this.saveAndFlush(viewer, product);
                return;
            }

            this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_PERMISSION.text(), input -> {
                product.getRequiredPermissions().add(input.getTextRaw());
                product.getShop().markDirty();
                return true;
            }));
        });

        this.addItem(Material.GUNPOWDER, VirtualLocales.PRODUCT_EDIT_FORBIDDEN_PERMISSIONS, 34, (viewer, event, product) -> {
            if (event.isRightClick()) {
                product.getForbiddenPermissions().clear();
                this.saveAndFlush(viewer, product);
                return;
            }

            this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_PERMISSION.text(), input -> {
                product.getForbiddenPermissions().add(input.getTextRaw());
                product.getShop().markDirty();
                return true;
            }));
        });
    }

    private void saveAndFlush(@NotNull MenuViewer viewer, @NotNull VirtualProduct product) {
        product.getShop().markDirty();
        this.runNextTick(() -> this.flush(viewer));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        item.replacement(replacer -> replacer.replace(this.getLink(viewer).replacePlaceholders()));
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        VirtualProduct product = this.getLink(player);
        ProductContent content = product.getContent();

        if (content instanceof CommandContent commandContent) {
            this.addCommandContentButtons(player, viewer, product, commandContent);
        }
    }

    private void addCommandContentButtons(@NotNull Player player, @NotNull MenuViewer viewer, @NotNull VirtualProduct product, @NotNull CommandContent content) {
        viewer.addItem(NightItem.fromType(Material.COMMAND_BLOCK)
            .localized(VirtualLang.EDITOR_PRODUCT_COMMANDS)
            .replacement(replacer -> replacer.replace(Placeholders.PRODUCT_COMMANDS, () -> String.join(TagWrappers.BR, Lists.modify(content.getCommands(), CoreLang::goodEntry))))
            .toMenuItem()
            .setSlots(13)
            .setHandler((viewer1, event) -> {
                if (this.module.handleDialogs(dialogs -> dialogs.openProductCommandsDialog(player, product))) return;

                if (event.isRightClick()) {
                    content.setCommands(new ArrayList<>());
                    this.saveAndFlush(viewer, product);
                    return;
                }

                this.handleInput(Dialog.builder(viewer, VirtualLang.EDITOR_ENTER_COMMAND.text(), input -> {
                    content.getCommands().add(input.getText());
                    product.getShop().markDirty();
                    return true;
                }));
            })
            .build()
        );
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
