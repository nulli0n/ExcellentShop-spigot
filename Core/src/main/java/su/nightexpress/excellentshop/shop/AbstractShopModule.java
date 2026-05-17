package su.nightexpress.excellentshop.shop;

import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jspecify.annotations.NonNull;

import su.nightexpress.excellentshop.api.ShopModule;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeStatus;
import su.nightexpress.excellentshop.api.product.click.ProductClickAction;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.api.transaction.ERawTransaction;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.excellentshop.module.AbstractModule;
import su.nightexpress.excellentshop.module.ModuleContext;
import su.nightexpress.excellentshop.product.click.ProductClickContext;
import su.nightexpress.excellentshop.shop.dialog.ShopDialogKeys;
import su.nightexpress.excellentshop.shop.dialog.impl.ProductCustomBuyAmountDialog;
import su.nightexpress.excellentshop.shop.transaction.TransactionCallback;
import su.nightexpress.excellentshop.shop.transaction.TransactionEngine;
import su.nightexpress.nightcore.ui.inventory.Menu;

public abstract class AbstractShopModule extends AbstractModule implements ShopModule {

    protected final ShopManager          shopManager;
    protected final ProductActionHandler productActionHandler;
    protected final TransactionEngine    transactionEngine;

    protected AbstractShopModule(@NonNull ModuleContext context, @NonNull ShopManager shopManager) {
        super(context);
        this.shopManager = shopManager;
        this.productActionHandler = new ProductActionHandler(this);
        this.transactionEngine = new TransactionEngine(this.plugin, this);
    }

    public TransactionEngine getTransactionEngine() {
        return this.transactionEngine;
    }

    @NonNull
    public abstract ShopModuleSettings getSettings();

    public abstract Set<? extends Shop> getShops(@NonNull Player player);

    public abstract void openPurchaseOptionsDialog(@NonNull ProductClickContext context);

    public void openCustomBuyAmountDialog(@NonNull Player player,
                                          @NonNull Product product,
                                          int shopPage,
                                          int initialUnits) {
        ProductCustomBuyAmountDialog.Data data = new ProductCustomBuyAmountDialog.Data(this, product, shopPage, initialUnits);

        this.plugin.showDialog(player, ShopDialogKeys.PRODUCT_BUY_CUSTOM_AMOUNT, data, null);
    }

    public boolean openBuyingMenu(@NonNull Player player, @NonNull Product product, int shopPage, int initialUnits) {
        return this.shopManager.openBuyingMenu(player, this, product, shopPage, initialUnits);
    }

    public boolean openSellingMenu(@NonNull Player player) {
        return this.shopManager.openSellingMenu(player, this, null, null, 1);
    }

    public boolean openSellingMenu(@NonNull Player player, @NonNull Shop shop, int shopPage) {
        return this.shopManager.openSellingMenu(player, this, shop, null, shopPage);
    }

    public boolean openSellingMenu(@NonNull Player player, @NonNull Product product, int shopPage) {
        return this.shopManager.openSellingMenu(player, this, null, product, shopPage);
    }

    public void handleProductClick(@NonNull Player player,
                                   @NonNull Product product,
                                   int shopPage,
                                   @NonNull InventoryClickEvent event) {

        TradeStatus status = product.getTradeStatus();
        ClickType clickType = event.getClick();
        ProductClickAction action = this.getSettings().getProductClickSettings().getClickAction(status, clickType);
        ProductClickContext context = new ProductClickContext(player, product, event, shopPage);

        this.handleProductClick(action, context);
    }

    public void handleProductClick(@NonNull ProductClickAction action,
                                   @NonNull ProductClickContext context) {

        Player player = context.player();

        this.productActionHandler.execute(context, action, () -> {
            Menu menu = this.plugin.getMenuRegistry().getActiveMenu(player);
            if (menu != null) {
                menu.refresh(player);
            }
        });
    }

    public void previewTransaction(@NonNull ERawTransaction transaction,
                                   @NonNull Consumer<ETransactionResult> callback) {
        if (!transaction.isPreview()) throw new IllegalArgumentException("Transaction MUST be preview!");

        this.transactionEngine.processTransaction(transaction, completed -> callback.accept(completed.result()));
    }

    public void proceedTransaction(@NonNull ERawTransaction transaction,
                                   @NonNull TransactionCallback callback) {
        if (transaction.isPreview()) throw new IllegalArgumentException("Transaction must NOT be preview!");

        this.transactionEngine.processTransaction(transaction, callback);
    }

    public void previewTransaction(@NonNull EPreparedTransaction transaction,
                                   @NonNull Consumer<ETransactionResult> callback) {
        if (!transaction.isPreview()) throw new IllegalArgumentException("Transaction MUST be preview!");

        this.transactionEngine.processTransaction(transaction, completed -> callback.accept(completed.result()));
    }

    public void proceedTransaction(@NonNull EPreparedTransaction transaction,
                                   @NonNull TransactionCallback callback) {
        if (transaction.isPreview()) throw new IllegalArgumentException("Transaction must NOT be preview!");

        this.transactionEngine.processTransaction(transaction, callback);
    }

    public abstract void notifySuccessfulTransaction(@NonNull ECompletedTransaction transaction);

}
