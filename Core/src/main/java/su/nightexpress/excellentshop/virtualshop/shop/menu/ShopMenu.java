package su.nightexpress.excellentshop.virtualshop.shop.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.product.ContentType;
import su.nightexpress.excellentshop.api.product.TradeStatus;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.transaction.ERawTransaction;
import su.nightexpress.excellentshop.shop.formatter.ProductFormatter;
import su.nightexpress.excellentshop.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.virtualshop.core.VirtualLang;
import su.nightexpress.excellentshop.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.virtualshop.rotation.Rotation;
import su.nightexpress.excellentshop.virtualshop.rotation.data.RotationData;
import su.nightexpress.excellentshop.virtualshop.rotation.data.RotationItemData;
import su.nightexpress.excellentshop.virtualshop.shop.VirtualShop;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.ui.inventory.action.ActionContext;
import su.nightexpress.nightcore.ui.inventory.action.NamedAction;
import su.nightexpress.nightcore.ui.inventory.condition.ItemStateConditions;
import su.nightexpress.nightcore.ui.inventory.item.ItemState;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractObjectMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.MenuViewer;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

@NullMarked
public class ShopMenu extends AbstractObjectMenu<ShopMenu.Data> {

    private static final String DEF_TITLE = TagWrappers.COLOR.with("#3E3E3E").wrap(
        "Shop → %s (" + TagWrappers.WHITE.wrap("%s") + "/" + TagWrappers.WHITE.wrap("%s") + ")")
        .formatted(ShopPlaceholders.SHOP_NAME, ShopPlaceholders.GENERIC_PAGE, ShopPlaceholders.GENERIC_PAGES);


    private final VirtualShopModule                module;
    private final ProductFormatter<VirtualProduct> productFormatter;

    private final NamedAction nextShopPageAction;
    private final NamedAction previousShopPageAction;

    public record Data(VirtualShop shop, ViewMode viewMode, @Nullable Rotation rotation) {
    }

    public ShopMenu(ShopPlugin plugin,
                    VirtualShopModule module,
                    ProductFormatter<VirtualProduct> productFormatter) {
        super(plugin, MenuType.GENERIC_9X6, DEF_TITLE, Data.class);
        this.module = module;
        this.productFormatter = productFormatter;

        this.nextShopPageAction = new NamedAction("next_page", this.createObjectAction(this::handleNextPage));
        this.previousShopPageAction = new NamedAction("previous_page", this.createObjectAction(
            this::handlePreviousPage));
    }

    @Override

    protected String getRawTitle(ViewerContext context) {
        return PlaceholderContext.builder()
            .with(this.getObject(context).shop.placeholders())
            .with(ShopPlaceholders.GENERIC_PAGE, () -> String.valueOf(context.getViewer().getCurrentPage()))
            .with(ShopPlaceholders.GENERIC_PAGES, () -> String.valueOf(context.getViewer().getTotalPages()))
            .andThen(CommonPlaceholders.forPlaceholderAPI(context.getPlayer()))
            .build().apply(super.getRawTitle(context));
    }

    @Override
    public void refresh() {
        // Do not auto refresh in editor.
        this.getViewers().stream().filter(viewer -> this.getObject(viewer.createContext()).viewMode == ViewMode.NORMAL)
            .forEach(MenuViewer::refresh);
    }

    public boolean show(Player player, VirtualShop shop, int page, ViewMode viewMode,
                        @Nullable Rotation rotation) {
        return this.show(player, new Data(shop, viewMode, rotation), viewer -> {
            viewer.setTotalPages(shop.getPages());
            viewer.setCurrentPage(Math.min(page, viewer.getTotalPages()));
        });
    }

    private void displayStatic(ViewerContext context, List<MenuItem> menuItems) {
        MenuViewer viewer = context.getViewer();
        Data data = this.getObject(context);
        VirtualShop shop = data.shop;
        int page = context.getViewer().getCurrentPage();

        shop.getValidProducts().forEach(product -> {
            if (product.isRotating()) return;
            if (product.getPage() != page) return;

            this.addProductItem(viewer, data, product, product.getSlot(), menuItems);
        });
    }

    private void displayRotating(ViewerContext context, List<MenuItem> menuItems) {
        MenuViewer viewer = context.getViewer();
        Data data = this.getObject(context);
        VirtualShop shop = data.shop;
        ViewMode mode = data.viewMode;
        int page = context.getViewer().getCurrentPage();

        shop.getRotations().forEach(rotation -> {
            List<Integer> slots = new ArrayList<>(rotation.getSlots(page));
            int limit = slots.size();

            if (mode == ViewMode.EDIT_ROTATION_SLOTS || mode == ViewMode.EDIT_PRODUCTS) {
                boolean isCurrent = rotation == data.rotation;
                Material material = isCurrent ? Material.CYAN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
                IconLocale locale = isCurrent ? VirtualLang.UI_EDITOR_SHOP_ROTATION_SELECTED_SLOT : VirtualLang.UI_EDITOR_SHOP_ROTATION_OTHER_SLOT;

                slots.forEach(slot -> {
                    menuItems.add(MenuItem.custom()
                        .defaultState(ItemState.builder()
                            .icon(NightItem.fromType(material)
                                .hideAllComponents()
                                .localized(locale)
                            )
                            .action(ctx -> {
                                if (data.rotation == null) return;
                                if (!isCurrent) return;

                                rotation.removeSlot(page, slot);
                                rotation.getShop().markDirty();
                                ctx.getViewer().refresh();
                            })
                            .build()
                        )
                        .slots(slot)
                        .build());
                });
                return;
            }

            RotationData rotationData = shop.getRotationData(rotation);
            List<String> productIds = rotationData.getProducts().stream().filter(itemData -> itemData.page() == page)
                .map(RotationItemData::productId).toList();
            if (productIds.isEmpty()) return;

            int count = 0;
            for (String productId : productIds) {
                if (count >= limit) break;

                VirtualProduct product = shop.getProductById(productId);
                if (product == null) continue;
                if (!product.isRotating()) continue;
                if (!product.isValid()) continue;

                int slot = slots.get(count++);
                this.addProductItem(viewer, data, product, slot, menuItems);
            }
        });
    }

    private void addProductItem(MenuViewer viewer, Data data, VirtualProduct product,
                                int slot, List<MenuItem> menuItems) {
        VirtualShop shop = data.shop;
        Player player = viewer.getPlayer();
        ItemStack preview = product.getEffectivePreview();

        NightItem icon = NightItem.fromItemStack(preview);

        if (data.viewMode != ViewMode.EDIT_PRODUCTS) {
            TradeStatus status = product.getTradeStatus();
            List<String> masterLore = this.module.getSettings().getProductDisplayMasterInfo(status);

            icon.setLore(this.productFormatter.format(product, masterLore, player));
        }
        else {
            icon.localized(VirtualLang.UI_EDITOR_SHOP_PRODUCTS_OBJECT);
        }

        menuItems.add(MenuItem.custom()
            .defaultState(ItemState.builder()
                .icon(icon
                    .replace(builder -> builder
                        .with(product.placeholders())
                        .with(shop.placeholders())
                        .andThen(product.getCurrency().replacePlaceholders())
                    )
                )
                .action(context -> this.handleProductItem(context, product))
                .build()
            )
            .slots(slot)
            .build());
    }

    @Override
    public void registerActions() {
        this.dataRegistry.registerAction(this.nextShopPageAction);
        this.dataRegistry.registerAction(this.previousShopPageAction);
    }

    @Override
    public void registerConditions() {

    }

    @Override
    public void defineDefaultLayout() {
        this.addBackgroundItem(Material.GRAY_STAINED_GLASS_PANE, IntStream.range(0, 45).toArray());
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(45, 54).toArray());

        this.addDefaultButton("next_page", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.ARROW).hideAllComponents().localized(CoreLang.MENU_ICON_NEXT_PAGE))
                .condition(ItemStateConditions.NEXT_PAGE)
                .action(this.nextShopPageAction)
                .build()
            )
            .slots(53)
            .build()
        );

        this.addDefaultButton("previous_page", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.ARROW).hideAllComponents().localized(
                    CoreLang.MENU_ICON_PREVIOUS_PAGE))
                .condition(ItemStateConditions.PREVIOUS_PAGE)
                .action(this.previousShopPageAction)
                .build()
            )
            .slots(45)
            .build()
        );

        this.addDefaultButton("back", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.ENDER_EYE)
                    .setDisplayName(TagWrappers.GREEN.and(TagWrappers.BOLD).wrap("Back to Shops"))
                )
                .condition(context -> this.module.hasCentralMenu() || this.getObject(
                    context).viewMode != ViewMode.NORMAL)
                .action(this::handleBack)
                .build()
            )
            .slots(49)
            .build()
        );

        this.addDefaultButton("sell_all", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.asCustomHead("9fd108383dfa5b02e86635609541520e4e158952d68c1c8f8f200ec7e88642d")
                    .setDisplayName(TagWrappers.COLOR.with("#ebd12a").and(TagWrappers.BOLD).wrap("SELL ALL"))
                    .setLore(Lists.newList(
                        TagWrappers.GRAY.wrap("Sells everything from your"),
                        TagWrappers.GRAY.wrap("inventory to all available shops."),
                        "",
                        TagWrappers.GRAY.wrap(TagWrappers.COLOR.with("#ebd12a").wrap("➥") + " Sell Multiplier: " +
                            TagWrappers.COLOR.with("#ebd12a").wrap("x" + ShopPlaceholders.GENERIC_SELL_MULTIPLIER)),
                        "",
                        TagWrappers.COLOR.with("#ebd12a").wrap("→ " + TagWrappers.BOLD.wrap(TagWrappers.UNDERLINED.wrap(
                            "CLICK")) + " to sell")
                    ))
                    .hideAllComponents()
                )
                .displayModifier((context, item) -> item.replace(builder -> builder
                    .with(ShopPlaceholders.GENERIC_SELL_MULTIPLIER, () -> NumberUtil.format(VirtualShopModule
                        .getSellMultiplier(context.getPlayer())))
                ))
                .action(this::handleSellAll)
                .build()
            )
            .slots(51)
            .build()
        );

        this.addDefaultButton("balance", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.asCustomHead("3324a7d61ccd44b031744b517f911a5c461614b953b17f648282e147b29d10e")
                    .setDisplayName(TagWrappers.COLOR.with("#7cf1de").and(TagWrappers.BOLD).wrap("BALANCE"))
                    .setLore(Lists.newList(
                        TagWrappers.GRAY.wrap("Here's displayed how much"),
                        TagWrappers.GRAY.wrap("money you have."),
                        "",
                        TagWrappers.COLOR.with("#7cf1de").wrap("➥") + " " + TagWrappers.WHITE.wrap(
                            ShopPlaceholders.GENERIC_BALANCE))
                    )
                    .hideAllComponents()
                )
                .displayModifier((context, item) -> item.replace(builder -> builder
                    .with(ShopPlaceholders.GENERIC_BALANCE, () -> {
                        Currency currency = this.module.getDefaultCurrency();
                        return currency.format(currency.queryBalance(context.getPlayer()));
                    })
                ))
                .build()
            )
            .slots(47)
            .build()
        );

        this.addDefaultButton("free_slot", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.LIME_STAINED_GLASS_PANE)
                    .setDisplayName(TagWrappers.GREEN.and(TagWrappers.BOLD).wrap("Free Slot"))
                    .setLore(Lists.newList(
                        TagWrappers.GRAY.wrap("Drag and drop an item."),
                        TagWrappers.GRAY.wrap("onto this icon to create."),
                        TagWrappers.GRAY.wrap("a new product from it.")
                    ))
                    .hideAllComponents()
                )
                .action(this::handleFreeSlot)
                .condition(context -> this.getObject(context).viewMode == ViewMode.EDIT_PRODUCTS)
                .build()
            )
            .state("rotation_mode", ItemState.builder()
                .icon(NightItem.fromType(Material.LIME_STAINED_GLASS_PANE)
                    .setDisplayName(TagWrappers.GREEN.and(TagWrappers.BOLD).wrap("Free Rotation Slot"))
                    .setLore(Lists.newList(
                        TagWrappers.GREEN.wrap("→ " + TagWrappers.UNDERLINED.wrap("Click to select"))
                    ))
                    .hideAllComponents()
                )
                .action(this::handleFreeSlot)
                .condition(context -> this.getObject(context).viewMode == ViewMode.EDIT_ROTATION_SLOTS)
                .build()
            )
            .slots(IntStream.range(0, 45).toArray())
            .build()
        );
    }

    @Override
    protected void onLoad(FileConfig config) {

    }

    @Override
    protected void onClick(ViewerContext context, InventoryClickEvent event) {
        Player player = context.getPlayer();
        Data data = this.getObject(context);
        ViewMode mode = data.viewMode;
        if (mode == ViewMode.NORMAL) return;

        // Back to editor when click outside of the GUI.
        if (event.getRawSlot() < 0) {
            VirtualShop shop = data.shop;

            this.plugin.runTask(() -> {
                if (mode == ViewMode.EDIT_PRODUCTS) {
                    this.module.openShopOptions(player, shop);
                }
                else if (data.rotation != null) {
                    this.module.openRotationOptions(player, data.rotation);
                }
            });
            return;
        }

        if (event.getRawSlot() >= event.getInventory().getSize()) {
            event.setCancelled(false);
        }
    }

    @Override
    protected void onDrag(ViewerContext context, InventoryDragEvent event) {

    }

    @Override
    protected void onClose(ViewerContext context, InventoryCloseEvent event) {

    }

    @Override
    public void onPrepare(ViewerContext context, InventoryView view, Inventory inventory,
                          List<MenuItem> items) {
        this.displayStatic(context, items);
        this.displayRotating(context, items);
    }

    @Override
    public void onReady(ViewerContext context, InventoryView view, Inventory inventory) {

    }

    @Override
    public void onRender(ViewerContext context, InventoryView view, Inventory inventory) {

    }

    private void handleNextPage(ActionContext context) {
        this.handlePage(context, context.getViewer().getCurrentPage() + 1);
    }

    private void handlePreviousPage(ActionContext context) {
        this.handlePage(context, context.getViewer().getCurrentPage() - 1);
    }

    private void handlePage(ActionContext context, int page) {
        Data data = this.getObject(context);
        VirtualShop shop = data.shop;
        Player player = context.getPlayer();

        context.getViewer().flushView();

        this.module.openShop(player, shop, page, false, data.viewMode, data.rotation);
    }

    private void handleBack(ActionContext context) {
        Data data = this.getObject(context);
        ViewMode mode = data.viewMode;
        VirtualShop shop = data.shop;
        Player player = context.getPlayer();

        switch (mode) {
            case NORMAL -> this.module.openMainMenu(context.getPlayer());
            case EDIT_PRODUCTS -> this.module.openShopOptions(player, shop);
            case EDIT_ROTATION_SLOTS -> {
                if (data.rotation != null) {
                    this.module.openRotationOptions(player, data.rotation);
                }
            }
        }
    }

    private void handleSellAll(ActionContext context) {
        Player player = context.getPlayer();
        ERawTransaction transaction = ERawTransaction.builder(player, TradeType.SELL)
            .addItems(player.getInventory())
            .targetShop(this.getObject(context).shop)
            .setStrict(false)
            .build();

        this.module.proceedTransaction(transaction, completed -> {
        });
    }

    private void handleFreeSlot(ActionContext context) {
        Data data = this.getObject(context);
        ViewMode mode = data.viewMode;
        if (mode == ViewMode.NORMAL) return;

        InventoryClickEvent event = context.getEvent();
        int slot = event.getRawSlot();
        int page = context.getViewer().getCurrentPage();

        if (data.viewMode == ViewMode.EDIT_ROTATION_SLOTS) {
            if (data.rotation != null) {
                data.rotation.addSlot(page, slot);
                data.rotation.getShop().markDirty();
                context.getViewer().refresh();
            }
            return;
        }

        ItemStack cursor = event.getCursor();
        if (cursor.getType().isAir()) return;

        VirtualShop shop = data.shop;
        VirtualProduct product = this.module.uncacheProduct(cursor);
        if (product == null) {
            product = shop.createProduct(ContentType.ITEM, cursor);
        }

        product.setRotating(false);
        product.setSlot(slot);
        product.setPage(page);

        shop.markDirty();
        shop.addProduct(product);

        event.getView().setCursor(null);
        context.getViewer().refresh();
    }

    private void handleProductItem(ActionContext context, VirtualProduct product) {
        Data data = this.getObject(context);
        if (data.viewMode == ViewMode.EDIT_ROTATION_SLOTS) return;

        Player player = context.getPlayer();
        VirtualShop shop = data.shop;
        int page = context.getViewer().getCurrentPage();
        InventoryClickEvent event = context.getEvent();

        if (data.viewMode == ViewMode.NORMAL) {
            this.module.handleProductClick(player, product, page, event);
            return;
        }

        if (event.isLeftClick()) {
            this.module.openProductOptions(player, product);
            return;
        }

        if (!event.isRightClick()) return;

        // Cache clicked product to item stack then remove it from the shop
        ItemStack saved = this.module.cacheProduct(product);
        shop.removeProduct(product);
        shop.markDirty();

        // Replace current product with the one from player's cursor.
        ItemStack cursor = event.getCursor();
        if (!cursor.getType().isAir()) {
            VirtualProduct newProduct = this.module.uncacheProduct(cursor);
            if (newProduct == null) {
                newProduct = shop.createProduct(ContentType.ITEM, cursor);
            }
            newProduct.setSlot(event.getRawSlot());
            newProduct.setPage(page);
            shop.markDirty();
            shop.addProduct(newProduct);
        }

        // Set cached item to cursor so player can put it somewhere
        event.getView().setCursor(null);

        context.getViewer().refresh();

        InventoryView view = context.getViewer().getCurrentView();
        if (view != null) {
            view.setCursor(saved);
        }
    }
}
