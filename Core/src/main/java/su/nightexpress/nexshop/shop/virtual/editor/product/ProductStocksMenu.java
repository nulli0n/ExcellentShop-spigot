package su.nightexpress.nexshop.shop.virtual.editor.product;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.menu.Confirmation;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

@SuppressWarnings("UnstableApiUsage")
public class ProductStocksMenu extends LinkedMenu<ShopPlugin, VirtualProduct> {

    private static final String SKULL_RESET      = "802246ff8b6c617168edaec39660612e72a54ab2eacc27c5e815e4ac70239e3a";
    private static final String SKULL_CLOCK      = "cbbc06a8d6b1492e40f0e7c3b632b6fd8e66dc45c15234990caa5410ac3ac3fd";
    private static final String SKULL_BUY_STOCK  = "77334cddfab45d75ad28e1a47bf8cf5017d2f0982f6737da22d4972952510661";
    private static final String SKULL_SELL_STOCK = "7189c997db7cbfd632c2298f6db0c0a3dd4fc4cbbb278be75484fc82c6b806d4";
    private static final String SKULL_BUY_LIMIT  = "f1d4fdd091840d9e7df0601681addec6051485a484ba7f536b35d4e05aa86ef9";
    private static final String SKULL_SELL_LIMIT = "b3a8fb20553e1e8db47f3883f7f491c1ee6bfd4d2ca97773a8db960eb543170e";

    public ProductStocksMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, VirtualLang.EDITOR_TITLE_PRODUCT_STOCKS.getString());

        this.addItem(MenuItem.buildReturn(this, 49, (viewer, event) -> {
            this.runNextTick(() -> module.openProductOptions(viewer.getPlayer(), this.getLink(viewer)));
        }));

        this.addItem(ItemUtil.getSkinHead(SKULL_RESET), VirtualLocales.PRODUCT_EDIT_STOCK_RESET, 26, (viewer, event, product) -> {
            this.runNextTick(() -> plugin.getShopManager().openConfirmation(viewer.getPlayer(), Confirmation.create(
                (viewer1, event1) -> {
                    plugin.getDataManager().resetStockDatas(product);
                    module.openStockOptions(viewer.getPlayer(), product);
                },
                (viewer1, event1) -> {
                    module.openStockOptions(viewer.getPlayer(), product);
                }
            )));
        });


        this.addItem(ItemUtil.getSkinHead(SKULL_BUY_STOCK), VirtualLocales.PRODUCT_EDIT_STOCK_BUY, 11, (viewer, event, product) -> {
            this.onClickInitial(viewer, event, product, product.getStockValues(), TradeType.BUY);
        });

        this.addItem(ItemUtil.getSkinHead(SKULL_SELL_STOCK), VirtualLocales.PRODUCT_EDIT_STOCK_SELL, 13, (viewer, event, product) -> {
            this.onClickInitial(viewer, event, product, product.getStockValues(), TradeType.SELL);
        });

        this.addItem(NightItem.asCustomHead(SKULL_CLOCK), VirtualLocales.PRODUCT_EDIT_STOCK_RESET_TIME, 15, (viewer, event, product) -> {
            this.onClickRestock(viewer, event, product, product.getStockValues());
        });


        this.addItem(ItemUtil.getSkinHead(SKULL_BUY_LIMIT), VirtualLocales.PRODUCT_EDIT_LIMIT_BUY, 29, (viewer, event, product) -> {
            this.onClickInitial(viewer, event, product, product.getLimitValues(), TradeType.BUY);
        });

        this.addItem(ItemUtil.getSkinHead(SKULL_SELL_LIMIT), VirtualLocales.PRODUCT_EDIT_LIMIT_SELL, 31, (viewer, event, product) -> {
            this.onClickInitial(viewer, event, product, product.getLimitValues(), TradeType.SELL);
        });

        this.addItem(NightItem.asCustomHead(SKULL_CLOCK), VirtualLocales.PRODUCT_EDIT_LIMIT_RESET_TIME, 33, (viewer, event, product) -> {
            this.onClickRestock(viewer, event, product, product.getLimitValues());
        });
    }

    private void onClickInitial(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull VirtualProduct product,
                                @NotNull StockValues values, @NotNull TradeType tradeType) {
        if (event.isRightClick()) {
            values.setAmount(tradeType, StockValues.UNLIMITED);
            this.saveAndFlush(viewer, product);
            return;
        }

        this.handleInput(Dialog.builder(viewer,  Lang.EDITOR_GENERIC_ENTER_AMOUNT, input -> {
            values.setAmount(tradeType, input.asInt(0));
            product.save();
            return true;
        }));
    }

    private void onClickRestock(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event, @NotNull VirtualProduct product, @NotNull StockValues values) {
        if (event.isRightClick()) {
            values.setRestockTime(-1);
            this.saveAndFlush(viewer, product);
            return;
        }

        this.handleInput(Dialog.builder(viewer,  Lang.EDITOR_GENERIC_ENTER_SECONDS, input -> {
            values.setRestockTime(input.asInt(0));
            product.save();
            return true;
        }));
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
}
