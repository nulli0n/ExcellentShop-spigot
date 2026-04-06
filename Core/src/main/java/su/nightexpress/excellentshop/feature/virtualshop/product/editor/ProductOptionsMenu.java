package su.nightexpress.excellentshop.feature.virtualshop.product.editor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.product.ContentTypes;
import su.nightexpress.excellentshop.product.ProductContent;
import su.nightexpress.excellentshop.product.content.CommandContent;
import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.locale.entry.IconLocale;
import su.nightexpress.nightcore.ui.inventory.action.ActionContext;
import su.nightexpress.nightcore.ui.inventory.item.ItemState;
import su.nightexpress.nightcore.ui.inventory.item.MenuItem;
import su.nightexpress.nightcore.ui.inventory.menu.AbstractObjectMenu;
import su.nightexpress.nightcore.ui.inventory.viewer.MenuViewer;
import su.nightexpress.nightcore.ui.inventory.viewer.ViewerContext;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.List;
import java.util.stream.IntStream;

public class ProductOptionsMenu extends AbstractObjectMenu<VirtualProduct> {

    private static final IconLocale LOCALE_DELETE = VirtualLang.iconBuilder("UI.Editor.Product.Delete")
        .name("Delete Product", TagWrappers.RED)
        .appendInfo("Permanently deletes the", "product with all settings", "and data.").br()
        .appendClick("Press Control + " + TagWrappers.KEY.apply("key.drop") + " to delete", TagWrappers.RED)
        .build();

    private static final IconLocale LOCALE_CONTENT_TYPE = VirtualLang.iconBuilder("UI.Editor.Product.ContentType")
        .name("Product Type")
        .appendCurrent("Current", ShopPlaceholders.GENERIC_TYPE).br()
        .appendClick("Click to change")
        .build();

    private static final IconLocale LOCALE_ITEM = VirtualLang.iconBuilder("UI.Editor.Product.TargetItem")
        .name("Item")
        .appendInfo("This is what the player", "receives when " + TagWrappers.GREEN.wrap("buying") + " and gives", "away when " + TagWrappers.RED.wrap("selling") + ".").br()
        .appendClick("Drag'n'Drop to replace")
        .build();

    private static final IconLocale LOCALE_ICON = VirtualLang.iconBuilder("UI.Editor.Product.Icon")
        .name("Icon")
        .appendInfo("Use a custom icon with", "a name and lore to provide", "players with a better overview", "of this product.").br()
        .appendClick("Drag'n'Drop to replace")
        .build();

    private static final IconLocale LOCALE_STRICT_NBT = VirtualLang.iconBuilder("UI.Editor.Product.StrictNbtMode")
        .name("Strict NBT Mode")
        .appendCurrent("Enabled", ShopPlaceholders.GENERIC_STATE).br()
        .appendInfo("Determines the \"strictness\"", "of item validation", "when selling.").br()
        .appendInfo(TagWrappers.GREEN.wrap("→" + " ON: ") + " All NBT data must match exactly.")
        .appendInfo(TagWrappers.RED.wrap("→" + " OFF: ") + " Checks only the item type (stone, diamond, etc.).")
        .br()
        .appendClick("Click to toggle")
        .build();

    public static final IconLocale LOCALE_COMMANDS = VirtualLang.iconBuilder("UI.Editor.Product.Commands")
        .name("Commands")
        .appendCurrent("Total Commands", ShopPlaceholders.GENERIC_AMOUNT).br()
        .appendInfo("Commands to run when", "a player buys this item.").br()
        .appendClick("Click to edit")
        .build();

    private static final IconLocale LOCALE_PRICE = VirtualLang.iconBuilder("UI.Editor.Product.PriceManager")
        .name("Price Manager")
        .appendCurrent("Type", ShopPlaceholders.GENERIC_TYPE)
        .appendCurrent("Buy", ShopPlaceholders.GENERIC_BUY)
        .appendCurrent("Sell", ShopPlaceholders.GENERIC_SELL)
        .br()
        .appendInfo("Adjust the product price here.")
        .br()
        .appendClick("Click to open")
        .build();

    private static final IconLocale LOCALE_RANKS = VirtualLang.iconBuilder("UI.Editor.Product.RankRequirements")
        .name("Rank Requirements")
        .appendInfo("Sets product availability", "based on player groups.").br()
        .appendClick("Click to edit")
        .build();

    private static final IconLocale LOCALE_PERMISSIONS = VirtualLang.iconBuilder("UI.Editor.Product.PermissionRequirements")
        .name("Permission Requirements")
        .appendInfo("Sets product availability", "based on player permissions.").br()
        .appendClick("Click to edit")
        .build();

    private static final IconLocale LOCALE_STOCK = VirtualLang.iconBuilder("UI.Editor.Product.Stocks")
        .name("Global Stock")
        .appendInfo("Controls product limits on", TagWrappers.GREEN.wrap("per server") + " basis.")
        .br()
        .appendClick("Click to edit")
        .build();

    private static final IconLocale LOCALE_LIMITS = VirtualLang.iconBuilder("UI.Editor.Product.Limits")
        .name("Player Limits")
        .appendInfo("Controls product limits on", TagWrappers.RED.wrap("per player") + " basis.")
        .br()
        .appendClick("Click to edit")
        .build();

    private final VirtualShopModule module;

    public ProductOptionsMenu(@NonNull ShopPlugin plugin, @NonNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X4, VirtualLang.EDITOR_TITLE_PRODUCT_OPTIONS.text(), VirtualProduct.class);
        this.module = module;
    }

    @Override
    public void registerActions() {

    }

    @Override
    public void registerConditions() {

    }

    @Override
    public void defineDefaultLayout() {
        this.addBackgroundItem(Material.BLACK_STAINED_GLASS_PANE, IntStream.range(27, 36).toArray());
        this.addBackgroundItem(Material.GRAY_STAINED_GLASS_PANE, IntStream.range(0, 27).toArray());

        this.addBackButton(this::handleBack, 27);

        this.addDefaultButton("delete", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.BARRIER).hideAllComponents().localized(LOCALE_DELETE))
                .action(this::handleDelete)
                .build()
            )
            .slots(35)
            .build()
        );

        this.addDefaultButton("product_type", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.ITEM_FRAME).hideAllComponents().localized(LOCALE_CONTENT_TYPE))
                .displayModifier((context, item) -> item.replace(builder -> builder
                    .with(ShopPlaceholders.GENERIC_TYPE, () -> Lang.CONTENT_TYPE.getLocalized(this.getObject(context).getContent().type()))
                ))
                .action(this::handleContentType)
                .build()
            )
            .slots(11)
            .build()
        );

        this.addDefaultButton("product_price", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.EMERALD).hideAllComponents().localized(LOCALE_PRICE))
                .displayModifier((context, item) -> item.replace(builder -> builder
                    .with(ShopPlaceholders.GENERIC_BUY, () -> {
                        VirtualProduct product = this.getObject(context);
                        return product.isBuyable() ? product.getCurrency().format(product.getBuyPrice()) : Lang.OTHER_N_A.text();
                    })
                    .with(ShopPlaceholders.GENERIC_SELL, () -> {
                        VirtualProduct product = this.getObject(context);
                        return product.isSellable() ? product.getCurrency().format(product.getSellPrice()) : Lang.OTHER_N_A.text();
                    })
                    .with(ShopPlaceholders.GENERIC_TYPE, () -> Lang.PRICE_TYPES.getLocalized(this.getObject(context).getPricingType()))
                ))
                .action(this::handleProductPrice)
                .build()
            )
            .slots(4)
            .build()
        );

        this.addDefaultButton("product_stock", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.BARREL).hideAllComponents().localized(LOCALE_STOCK))
                .action(this::handleProductStocks)
                .build()
            )
            .slots(13)
            .build()
        );

        this.addDefaultButton("product_limits", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.DROPPER).hideAllComponents().localized(LOCALE_LIMITS))
                .action(this::handleProductLimits)
                .build()
            )
            .slots(14)
            .build()
        );

        this.addDefaultButton("product_ranks", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.GOLDEN_HELMET).hideAllComponents().localized(LOCALE_RANKS))
                .action(this::handleProductRanks)
                .build()
            )
            .slots(15)
            .build()
        );

        this.addDefaultButton("product_permissions", MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.REDSTONE).hideAllComponents().localized(LOCALE_PERMISSIONS))
                .action(this::handleProductPermissions)
                .build()
            )
            .slots(16)
            .build()
        );
    }

    @Override
    protected void onLoad(@NonNull FileConfig config) {

    }

    @Override
    protected void onClick(@NonNull ViewerContext context, @NonNull InventoryClickEvent event) {
        if (event.getRawSlot() >= event.getInventory().getSize()) {
            event.setCancelled(false);
        }
    }

    @Override
    protected void onDrag(@NonNull ViewerContext context, @NonNull InventoryDragEvent event) {

    }

    @Override
    protected void onClose(@NonNull ViewerContext context, @NonNull InventoryCloseEvent event) {

    }

    @Override
    public void onPrepare(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory, @NonNull List<MenuItem> items) {
        VirtualProduct product = this.getObject(context);
        ProductContent content = product.getContent();

        if (content instanceof CommandContent commandContent) {
            this.addCommandContentButtons(product, commandContent, items);
        }
        else if (content instanceof ItemContent itemContent) {
            this.addItemContentButtons(product, itemContent, items);
        }
    }

    @Override
    public void onReady(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

    }

    @Override
    public void onRender(@NonNull ViewerContext context, @NonNull InventoryView view, @NonNull Inventory inventory) {

    }

    private void addItemContentButtons(@NonNull VirtualProduct product, @NonNull ItemContent content, @NonNull List<MenuItem> items) {
        items.add(MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromItemStack(product.getEffectivePreview()).hideAllComponents().localized(LOCALE_ITEM))
                .action(this::handleProductItem)
                .build()
            )
            .slots(10)
            .build()
        );

        items.add(MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.POTION).hideAllComponents().localized(LOCALE_STRICT_NBT))
                .displayModifier((ctx, item) -> {
                    if (content.isCompareNbt()) item.setMaterial(Material.HONEY_BOTTLE);
                    item.replace(builder -> builder
                        .with(ShopPlaceholders.GENERIC_STATE, () -> CoreLang.STATE_ENABLED_DISALBED.get(content.isCompareNbt()))
                    );
                })
                .condition(ctx -> content.getAdaptedItem().getAdapter().isVanilla())
                .action(this::handleProductNbtMode)
                .build()
            )
            .slots(12)
            .build()
        );
    }

    private void addCommandContentButtons(@NonNull VirtualProduct product, @NonNull CommandContent content, @NonNull List<MenuItem> items) {
        items.add(MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromItemStack(product.getEffectivePreview()).hideAllComponents().localized(LOCALE_ICON))
                .action(this::handleVirtualIcon)
                .build()
            )
            .slots(10)
            .build()
        );

        items.add(MenuItem.button()
            .defaultState(ItemState.builder()
                .icon(NightItem.fromType(Material.COMMAND_BLOCK_MINECART).hideAllComponents().localized(LOCALE_COMMANDS))
                .displayModifier((ctx, item) -> item.replace(builder -> builder
                    .with(ShopPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(content.getCommands().size()))
                ))
                .action(this::handleProductCommands)
                .build()
            )
            .slots(12)
            .build()
        );
    }

    private void goBack(@NonNull Player player, @NonNull VirtualProduct product) {
        if (product.isRotating()) {
            module.openRotatingsProducts(player, product.getShop());
        }
        else module.openNormalProducts(player, product.getShop(), product.getPage());
    }

    private void handleBack(@NonNull ActionContext context) {
        Player player = context.getPlayer();
        VirtualProduct product = this.getObject(context);
        this.goBack(player, product);
    }

    private void handleDelete(@NonNull ActionContext context) {
        if (context.getEvent().getClick() != ClickType.CONTROL_DROP) return;

        Player player = context.getPlayer();
        VirtualProduct product = this.getObject(context);
        VirtualShop shop = product.getShop();

        shop.removeProduct(product);
        shop.markDirty();

        this.goBack(player, product);
    }

    private void handleContentType(@NonNull ActionContext context) {
        this.module.openProductTypeDialog(context.getPlayer(), this.getObject(context), () -> context.getViewer().refresh());
    }

    private void handleVirtualIcon(@NonNull ActionContext context) {
        MenuViewer viewer = context.getViewer();
        InventoryClickEvent event = context.getEvent();
        VirtualProduct product = this.getObject(context);

        if (event.isRightClick() && product.isValid()) {
            Players.addItem(viewer.getPlayer(), product.getPreview());
            return;
        }

        ItemStack cursor = event.getCursor();
        if (cursor.getType().isAir()) return;

        CommandContent type = (CommandContent) product.getContent();
        type.setPreview(cursor);
        product.getShop().markDirty();

        event.getView().setCursor(null);
        viewer.refresh();
    }

    private void handleProductItem(@NonNull ActionContext context) {
        MenuViewer viewer = context.getViewer();
        InventoryClickEvent event = context.getEvent();
        VirtualProduct product = this.getObject(context);

        if (event.isRightClick() && product.isValid()) {
            ItemContent packer = (ItemContent) product.getContent();
            Players.addItem(viewer.getPlayer(), packer.getItem());
            return;
        }

        ItemStack cursor = event.getCursor();
        if (cursor.getType().isAir()) return;

        ProductContent content = ContentTypes.fromItem(cursor, this.module::isItemProviderAllowed);

        product.setContent(content);
        product.getShop().markDirty();

        event.getView().setCursor(null);
        viewer.refresh();
    }

    private void handleProductNbtMode(@NonNull ActionContext context) {
        MenuViewer viewer = context.getViewer();
        VirtualProduct product = this.getObject(context);

        if (!(product.getContent() instanceof ItemContent content)) return;
        if (!content.getAdaptedItem().getAdapter().isVanilla()) return;

        content.setCompareNbt(!content.isCompareNbt());
        product.getShop().markDirty();
        viewer.refresh();
    }

    private void handleProductCommands(@NonNull ActionContext context) {
        Player player = context.getPlayer();
        VirtualProduct product = this.getObject(context);

        this.module.openProductCommandsDialog(player, product, () -> context.getViewer().refresh());
    }

    private void handleProductPrice(@NonNull ActionContext context) {
        Player player = context.getPlayer();
        VirtualProduct product = this.getObject(context);

        this.module.openProductPriceDialog(player, product, () -> context.getViewer().refresh());
    }

    private void handleProductRanks(@NonNull ActionContext context) {
        Player player = context.getPlayer();
        VirtualProduct product = this.getObject(context);

        this.module.openProductRanksDialog(player, product, () -> context.getViewer().refresh());
    }

    private void handleProductPermissions(@NonNull ActionContext context) {
        Player player = context.getPlayer();
        VirtualProduct product = this.getObject(context);

        this.module.openProductPermsDialog(player, product, () -> context.getViewer().refresh());
    }

    private void handleProductStocks(@NonNull ActionContext context) {
        Player player = context.getPlayer();
        VirtualProduct product = this.getObject(context);

        this.module.openProductStocksDialog(player, product, () -> context.getViewer().refresh());
    }

    private void handleProductLimits(@NonNull ActionContext context) {
        Player player = context.getPlayer();
        VirtualProduct product = this.getObject(context);

        this.module.openProductLimitsDialog(player, product, () -> context.getViewer().refresh());
    }
}
