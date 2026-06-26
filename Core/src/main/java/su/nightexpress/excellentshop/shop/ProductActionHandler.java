package su.nightexpress.excellentshop.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeStatus;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.click.ProductClickAction;
import su.nightexpress.excellentshop.api.transaction.EPreparedTransaction;
import su.nightexpress.excellentshop.api.transaction.ETransactionResult;
import su.nightexpress.excellentshop.core.Perms;
import su.nightexpress.excellentshop.product.click.ProductClickContext;
import su.nightexpress.nightcore.core.config.CoreLang;

@NullMarked
public class ProductActionHandler {

    private final AbstractShopModule module;

    public ProductActionHandler(AbstractShopModule module) {
        this.module = module;
    }

    public void execute(ProductClickContext context, ProductClickAction action, @Nullable Runnable callback) {
        Player player = context.player();
        Product product = context.product();
        TradeStatus status = product.getTradeStatus();

        if (status == TradeStatus.UNAVAILABLE) return;
        if (action == ProductClickAction.NONE) return;

        if (/* Players.isBedrock(player) ||  */action == ProductClickAction.PURCHASE_DIALOG) {
            this.module.openPurchaseOptionsDialog(context);
            return;
        }

        if (!this.checkPermissions(player, action)) return;
        if (this.handleMenuRouting(player, action, context)) return;

        TradeType type = getTradeType(action);
        if (type == null) return;

        int units = this.calculateUnits(player, action, context);

        EPreparedTransaction transaction = EPreparedTransaction.builder(player, type)
            .addProduct(product, units)
            .build();

        this.module.proceedTransaction(transaction, completed -> {
            if (callback != null) callback.run();
        });
    }

    private boolean checkPermissions(Player player, ProductClickAction action) {
        if (action == ProductClickAction.SELL_ALL && !player.hasPermission(Perms.KEY_SELL_ALL)) {
            this.module.sendPrefixed(CoreLang.ERROR_NO_PERMISSION, player);
            return false;
        }

        if (action == ProductClickAction.BUY_ALL && !player.hasPermission(Perms.KEY_BUY_ALL)) {
            this.module.sendPrefixed(CoreLang.ERROR_NO_PERMISSION, player);
            return false;
        }

        return true;
    }

    private boolean handleMenuRouting(Player player, ProductClickAction action, ProductClickContext context) {
        if (action != ProductClickAction.OPEN_BUY_MENU && action != ProductClickAction.OPEN_SELL_MENU) return false;

        TradeType type = getTradeType(action);
        if (type == null) return false;

        Product product = context.product();
        boolean allowMenu = type == TradeType.BUY ? product.isBuyMenuAllowed() : product.isSellMenuAllowed();
        if (!allowMenu) return false;

        EPreparedTransaction transaction = EPreparedTransaction.builder(player, type)
            .addProduct(product, 1)
            .setPreview(true)
            .build();

        this.module.previewTransaction(transaction, result -> {
            if (result != ETransactionResult.SUCCESS) return;

            switch (type) {
                case BUY -> this.module.openBuyingMenu(player, product, context.shopPage(), 1);
                case SELL -> this.module.openSellingMenu(player, product, context.shopPage());
            }
        });
        return true;
    }

    private int calculateUnits(Player player, ProductClickAction action, ProductClickContext context) {
        int units = 1;
        Product product = context.product();
        InventoryClickEvent event = context.event();

        if (event != null && event
            .getClick() == ClickType.NUMBER_KEY && (action == ProductClickAction.BUY_ONE || action == ProductClickAction.SELL_ONE)) {
            int hotbarButton = event.getHotbarButton();
            if (hotbarButton >= 0) units = hotbarButton + 1;
        }
        else {
            if (action == ProductClickAction.SELL_ALL) units = product.getMaxSellableUnitAmount(player, player
                .getInventory());
            if (action == ProductClickAction.BUY_ALL) units = product.getMaxBuyableUnitAmount(player, player
                .getInventory());
        }

        return Math.max(1, units); // Prevent zero values.
    }

    private static @Nullable TradeType getTradeType(ProductClickAction action) {
        return switch (action) {
            case BUY_ALL, BUY_ONE, OPEN_BUY_MENU -> TradeType.BUY;
            case SELL_ALL, SELL_ONE, OPEN_SELL_MENU -> TradeType.SELL;
            default -> null;
        };
    }
}
