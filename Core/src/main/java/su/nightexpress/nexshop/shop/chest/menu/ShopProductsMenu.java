package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;
import su.nightexpress.nexshop.shop.virtual.menu.LegacyShopEditor;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.click.ClickResult;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.ItemOptions;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ShopProductsMenu extends ShopEditorMenu implements Linked<ChestShop>, LegacyShopEditor {

    public static final String FILE_NAME = "shop_products.yml";

    private final ChestShopModule module;
    private final ViewLink<ChestShop> link;
    private final ItemHandler returnHandler;

    private int[] productSlots;
    private ItemStack productFree;
    private ItemStack productLocked;
    private String productName;
    private List<String> productLore;

    public ShopProductsMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE_NAME));
        this.module = module;
        this.link = new ViewLink<>();

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            this.runNextTick(() -> module.openShopSettings(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.load();

        if (Config.GUI_PLACEHOLDER_API.get()) {
            this.getItems().forEach(menuItem -> {
                menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                    ItemReplacer.replacePlaceholderAPI(itemStack, viewer.getPlayer());
                });
            });
        }
    }

    @NotNull
    @Override
    public ViewLink<ChestShop> getLink() {
        return link;
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        ChestShop shop = this.getLink(viewer);
        Player player = viewer.getPlayer();

        int maxProducts = ChestUtils.getProductLimit(player);
        if (maxProducts < 0) maxProducts = this.productSlots.length;

        PriorityQueue<ChestProduct> queue = new PriorityQueue<>(Comparator.comparing(AbstractProduct::getId));
        queue.addAll(shop.getValidProducts());

        int productCount = 0;

        for (int productSlot : this.productSlots) {
            // If no products left to display in slots, then display free/locked slot items.
            if (queue.isEmpty()) {
                MenuItem item;
                if (maxProducts - productCount > 0) {
                    productCount++;
                    item = new MenuItem(this.productFree);
                    item.setOptions(ItemOptions.personalWeak(player));
                    item.setHandler((viewer2, event) -> {
                        ItemStack cursor = event.getCursor();
                        if (cursor == null || cursor.getType().isAir()) return;
                        if (shop.createProduct(player, cursor, ChestUtils.bypassHandlerDetection(event)) == null) return;

                        event.getView().setCursor(null);
                        Players.addItem(viewer2.getPlayer(), cursor);
                        this.saveProductsAndFlush(viewer, shop);
                    });
                }
                else {
                    item = new MenuItem(this.productLocked);
                    item.setOptions(ItemOptions.personalWeak(player));
                }
                item.setSlots(productSlot);
                this.addItem(item);
            }
            // Diplay current shop products
            else {
                productCount++;
                ChestProduct product = queue.poll();
                ItemStack productIcon = new ItemStack(product.getPreview());
                ItemReplacer.create(productIcon).trimmed().hideFlags()
                    .setDisplayName(this.productName).setLore(this.productLore)
                    .replace(product.replacePlaceholders(viewer.getPlayer()))
                    .replacePlaceholderAPI(viewer.getPlayer())
                    .writeMeta();

                MenuItem item = new MenuItem(productIcon);
                item.setOptions(ItemOptions.personalWeak(player));
                item.setSlots(productSlot);
                item.setHandler((viewer2, event) -> {
                    if (event.isShiftClick() && event.isRightClick()) {
                        if (product.countStock(TradeType.BUY, null) > 0) {
                            ChestLang.EDITOR_ERROR_PRODUCT_LEFT.getMessage().send(viewer2.getPlayer());
                            return;
                        }
                        shop.removeProduct(product.getId());
                        this.saveProductsAndFlush(viewer, shop);
                        return;
                    }

                    this.runNextTick(() -> this.module.openPriceMenu(viewer2.getPlayer(), product));
                });
                this.addItem(item);
            }
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @NotNull ClickResult result, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, result, event);

        ChestShop shop = this.getLink(viewer);
        Player player = viewer.getPlayer();
        int maxProducts = ChestUtils.getProductLimit(player);
        int hasProducts = shop.getValidProducts().size();
        boolean canAdd = maxProducts < 0 || hasProducts < maxProducts;
        if (!canAdd) return;

        //if (result.isMenu() && result.isEmptySlot()) return;
        if (result.isInventory()) {
            if (event.isShiftClick()) return;
            if (Players.isBedrock(player)) {
                ItemStack item = result.getItemStack();
                if (item == null || item.getType().isAir()) return;

                shop.createProduct(player, item, false);
                return;
            }
            event.setCancelled(false);
        }
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Shop Products"), MenuSize.CHEST_18);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack backItem = ItemUtil.getSkinHead(SKIN_ARROW_DOWN);
        ItemUtil.editMeta(backItem, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_RETURN.getDefaultName());
        });
        list.add(new MenuItem(backItem).setSlots(13).setPriority(10).setHandler(this.returnHandler));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.productSlots = ConfigValue.create("Products.Slots", IntStream.range(0, 9).toArray()).read(cfg);

        ItemStack freeSlot = ItemUtil.getSkinHead("dd10aa51522d3d6a2d8232ed886c79987e1e53956646dafbabd9eddff6986");
        ItemUtil.editMeta(freeSlot, meta -> {
            meta.setDisplayName(LIGHT_GREEN.enclose(BOLD.enclose("✔ Available Slot")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Drop item here to add it to the shop!")
            ));
        });

        ItemStack lockedSlot = ItemUtil.getSkinHead("4051b59085d2c4249577823f63e1e2eb9f7cf64b7c78785a21805fad3ef14");
        ItemUtil.editMeta(lockedSlot, meta -> {
            meta.setDisplayName(LIGHT_RED.enclose(BOLD.enclose("✘ Locked Slot")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Oh! This slot is not available currently."),
                "",
                LIGHT_GRAY.enclose("You can unlock slots with our ranks:"),
                LIGHT_RED.enclose("www.put_your_store.com")
            ));
        });

        this.productFree = ConfigValue.create("Products.Free", freeSlot).read(cfg);

        this.productLocked = ConfigValue.create("Products.Locked", lockedSlot).read(cfg);

        this.productName = ConfigValue.create("Products.Product.Name", 
            LIGHT_YELLOW.enclose(BOLD.enclose(PRODUCT_PREVIEW_NAME))
        ).read(cfg);

        this.productLore = ConfigValue.create("Products.Product.Lore", Lists.newList(
            PRODUCT_PREVIEW_LORE,
            "",
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Currency: ") + PRODUCT_CURRENCY),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Price Type: ") + PRODUCT_PRICE_TYPE),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Buy Price: ") + PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY)),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Sell Price: ") + PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL)),
            "",
            LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("edit") + "."),
            LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Shift-Right to " + LIGHT_YELLOW.enclose("delete") + ".")
        )).read(cfg);
    }
}
