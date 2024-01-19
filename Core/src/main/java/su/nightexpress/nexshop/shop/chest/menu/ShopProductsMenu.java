package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.ItemOptions;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.api.menu.link.Linked;
import su.nexmedia.engine.api.menu.link.ViewLink;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class ShopProductsMenu extends ConfigEditorMenu implements Linked<ChestShop> {

    public static final String FILE = "shop_products.yml";

    private final int[] productSlots;
    private final ItemStack productFree;
    private final ItemStack productLocked;
    private final String productName;
    private final List<String> productLore;

    private final ViewLink<ChestShop> link;

    public ShopProductsMenu(@NotNull ExcellentShop plugin, @NotNull ChestShopModule module) {
        super(plugin, JYML.loadOrExtract(plugin, module.getMenusPath(), FILE));
        this.link = new ViewLink<>();

        this.productSlots = cfg.getIntArray("Products.Slots");
        this.productFree = cfg.getItem("Products.Free");
        this.productLocked = cfg.getItem("Products.Locked");
        this.productName = Colorizer.apply(cfg.getString("Products.Product.Name", Placeholders.PRODUCT_PREVIEW_NAME));
        this.productLore = Colorizer.apply(cfg.getStringList("Products.Product.Lore"));

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, ClickHandler.forClose(this))
            .addClick(MenuItemType.RETURN, (viewer, event) -> plugin.runTask(task -> this.getShop(viewer).openMenu(viewer.getPlayer())));

        this.load();
    }

    @NotNull
    @Override
    public ViewLink<ChestShop> getLink() {
        return link;
    }

    @NotNull
    private ChestShop getShop(@NotNull MenuViewer viewer) {
        return this.getLink().get(viewer);
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        ChestShop shop = this.getShop(viewer);
        Player player = viewer.getPlayer();
        int page = viewer.getPage();

        int maxProducts = ChestUtils.getProductLimit(player);
        if (maxProducts < 0) maxProducts = this.productSlots.length;

        PriorityQueue<ChestProduct> queue = new PriorityQueue<>(Comparator.comparing(AbstractProduct::getId));
        queue.addAll(shop.getProducts());

        int productCount = 0;

        for (int productSlot : this.productSlots) {
            // If no products left to display in slots, then display free/locked slot items.
            if (queue.isEmpty()) {
                MenuItem item;
                if (maxProducts - productCount > 0) {
                    productCount++;
                    item = new MenuItem(this.productFree);
                    item.setOptions(ItemOptions.personalWeak(player));
                    item.setClick((viewer2, event) -> {
                        ItemStack cursor = event.getCursor();
                        if (cursor == null || !shop.createProduct(viewer2.getPlayer(), cursor)) return;

                        event.getView().setCursor(null);
                        PlayerUtil.addItem(viewer2.getPlayer(), cursor);
                        shop.save();
                        this.openNextTick(viewer2, page);
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
                    .replace(product.replacePlaceholders())
                    .writeMeta();

                MenuItem item = new MenuItem(productIcon);
                item.setOptions(ItemOptions.personalWeak(player));
                item.setSlots(productSlot);
                item.setClick((viewer2, event) -> {
                    if (event.isShiftClick()) {
                        if (event.isRightClick()) {
                            if (shop.getStock().count(product, TradeType.BUY) > 0) {
                                plugin.getMessage(ChestLang.EDITOR_ERROR_PRODUCT_LEFT).send(viewer2.getPlayer());
                                return;
                            }
                            shop.removeProduct(product.getId());
                            shop.save();
                            this.openNextTick(viewer2.getPlayer(), page);
                        }
                        return;
                    }
                    if (event.isRightClick() || (PlayerUtil.isBedrockPlayer(player))) {
                        product.getPriceEditor().openNextTick(viewer2.getPlayer(), 1);
                        return;
                    }
                    if (event.isLeftClick()) {
                        List<Currency> currencies = new ArrayList<>(ChestUtils.getAllowedCurrencies());
                        int index = currencies.indexOf(product.getCurrency()) + 1;
                        if (index >= currencies.size()) index = 0;
                        product.setCurrency(currencies.get(index));
                        shop.save();
                        this.openNextTick(viewer.getPlayer(), page);
                    }
                });
                this.addItem(item);
            }
        }
    }

    @Override
    public void onClick(@NotNull MenuViewer viewer, @Nullable ItemStack item, @NotNull SlotType slotType, int slot, @NotNull InventoryClickEvent event) {
        super.onClick(viewer, item, slotType, slot, event);

        ChestShop shop = this.getShop(viewer);
        Player player = viewer.getPlayer();
        int maxProducts = ChestUtils.getProductLimit(player);
        int hasProducts = shop.getProducts().size();
        boolean canAdd = maxProducts < 0 || hasProducts < maxProducts;
        if (!canAdd) return;

        if (slotType == SlotType.MENU_EMPTY) return;
        if (slotType == SlotType.PLAYER || slotType == SlotType.PLAYER_EMPTY) {
            if (event.isShiftClick()) return;
            if (PlayerUtil.isBedrockPlayer(player)) {
                if (item == null || item.getType().isAir()) return;

                shop.createProduct(player, item);
                return;
            }
            event.setCancelled(false);
        }
    }
}
