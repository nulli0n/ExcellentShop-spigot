package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualStock;
import su.nightexpress.nexshop.shop.virtual.menu.ShopEditor;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.text.tag.Tags;

public class ProductStockEditor extends EditorMenu<ShopPlugin, VirtualProduct> implements ShopEditor {

    private static final String TEXTURE_GLOBAL = "25485031b37f0d8a4f3b7816eb717f03de89a87f6a40602aef52221cdfaf7488";
    private static final String TEXTURE_PLAYER = "65e5223317a890a30351f6f78d0abf8dd76cbd08df6f918883934564d28e58e";
    private static final String TEXTURE_BUY    = "a2716b37523f453e1871f2263f823c280bb8dd73d696d527b9eec87cdf32";
    private static final String TEXTURE_SELL   = "6eb911ea94b5a1cf77f3ca637a3b1662b35121bd72e118651184f2fb1060d1";

    public ProductStockEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, Tags.BLACK.enclose("Product Stock [" + Placeholders.PRODUCT_ID + "]"), MenuSize.CHEST_54);

        this.addReturn(49, (viewer, event, product) -> {
            this.runNextTick(() -> module.openProductEditor(viewer.getPlayer(), product));
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_GLOBAL), VirtualLocales.PRODUCT_STOCK_GLOBAL_INFO, 10, (viewer, event, product) -> {
            if (event.isRightClick()) {
                VirtualStock stock = (VirtualStock) product.getShop().getStock();
                stock.resetGlobalAmount(product);
            }
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_PLAYER), VirtualLocales.PRODUCT_STOCK_PLAYER_INFO, 28, (viewer, event, product) -> {
            if (event.isRightClick()) {
                VirtualStock stock = (VirtualStock) product.getShop().getStock();
                stock.resetPlayerAmount(product);
            }
        });

        // Global Stock Stuff

        this.addItem(ItemUtil.getSkinHead(TEXTURE_BUY), VirtualLocales.PRODUCT_STOCK_GLOBAL_BUY_INITIAL, 12, (viewer, event, product) -> {
            this.onClickInitial(viewer, event, product, product.getStockValues(), TradeType.BUY);
        });

        this.addItem(Material.CLOCK, VirtualLocales.PRODUCT_STOCK_GLOBAL_RESTOCK_BUY, 13, (viewer, event, product) -> {
            this.onClickRestock(viewer, event, product, product.getStockValues(), TradeType.BUY);
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_SELL), VirtualLocales.PRODUCT_STOCK_GLOBAL_SELL_INITIAL, 15, (viewer, event, product) -> {
            this.onClickInitial(viewer, event, product, product.getStockValues(), TradeType.SELL);
        });

        this.addItem(Material.CLOCK, VirtualLocales.PRODUCT_STOCK_GLOBAL_RESTOCK_SELL, 16, (viewer, event, product) -> {
            this.onClickRestock(viewer, event, product, product.getStockValues(), TradeType.SELL);
        });

        // Player Limits Stuff

        this.addItem(ItemUtil.getSkinHead(TEXTURE_BUY), VirtualLocales.PRODUCT_STOCK_PLAYER_BUY_INITIAL, 30, (viewer, event, product) -> {
            this.onClickInitial(viewer, event, product, product.getLimitValues(), TradeType.BUY);
        });

        this.addItem(Material.CLOCK, VirtualLocales.PRODUCT_STOCK_PLAYER_RESTOCK_BUY, 31, (viewer, event, product) -> {
            this.onClickRestock(viewer, event, product, product.getLimitValues(), TradeType.BUY);
        });

        this.addItem(ItemUtil.getSkinHead(TEXTURE_SELL), VirtualLocales.PRODUCT_STOCK_PLAYER_SELL_INITIAL, 33, (viewer, event, product) -> {
            this.onClickInitial(viewer, event, product, product.getLimitValues(), TradeType.SELL);
        });

        this.addItem(Material.CLOCK, VirtualLocales.PRODUCT_STOCK_PLAYER_RESTOCK_SELL, 34, (viewer, event, product) -> {
            this.onClickRestock(viewer, event, product, product.getLimitValues(), TradeType.SELL);
        });

        // End

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            ItemReplacer.replace(item, this.getLink(viewer).getPlaceholders());
        }));
    }

    private void onClickInitial(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull VirtualProduct product,
                                @NotNull StockValues values, @NotNull TradeType tradeType) {
        if (event.isRightClick()) {
            values.setInitialAmount(tradeType, -1);
            this.saveProductAndFlush(viewer, product);
            return;
        }

        this.handleInput(viewer,  Lang.EDITOR_GENERIC_ENTER_AMOUNT, (dialog, input) -> {
            values.setInitialAmount(tradeType, input.asInt());
            this.saveProduct(viewer, product);
            return true;
        });
    }

    private void onClickRestock(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull VirtualProduct product,
                                @NotNull StockValues values, @NotNull TradeType tradeType) {
        if (event.isRightClick()) {
            values.setRestockSeconds(tradeType, -1);
            this.saveProductAndFlush(viewer, product);
            return;
        }

        this.handleInput(viewer,  Lang.EDITOR_GENERIC_ENTER_SECONDS, (dialog, input) -> {
            values.setRestockSeconds(tradeType, input.asInt());
            this.saveProduct(viewer, product);
            return true;
        });
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        options.setTitle(this.getLink(viewer).replacePlaceholders().apply(options.getTitle()));
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }
}
