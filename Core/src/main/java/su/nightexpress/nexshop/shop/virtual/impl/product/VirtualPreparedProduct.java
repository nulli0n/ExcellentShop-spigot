package su.nightexpress.nexshop.shop.virtual.impl.product;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IPurchaseListener;
import su.nightexpress.nexshop.shop.util.TransactionResult.Result;
import su.nightexpress.nexshop.api.event.VirtualShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.util.TransactionResult;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

public class VirtualPreparedProduct extends PreparedProduct<VirtualProduct> {

    public VirtualPreparedProduct(@NotNull Player player, @NotNull VirtualProduct product, @NotNull TradeType tradeType, boolean all) {
        super(player, product, tradeType, all);
    }

    @Override
    @NotNull
    protected TransactionResult buy() {
        Player player = this.getPlayer();
        VirtualProduct product = this.getProduct();
        VirtualShop shop = product.getShop();
        ExcellentShop plugin = shop.plugin();

        double price = this.getPrice();
        double balance = product.getCurrency().getHandler().getBalance(player);

        Result result = TransactionResult.Result.SUCCESS;
        if (balance < price) {
            result = TransactionResult.Result.TOO_EXPENSIVE;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_TOO_EXPENSIVE).replace(this.replacePlaceholders()).send(player);
        }

        // Call custom event
        TransactionResult transactionResult = new TransactionResult(product, TradeType.BUY, this.getUnits(), price, result);
        VirtualShopTransactionEvent event = new VirtualShopTransactionEvent(player, transactionResult);
        shop.plugin().getPluginManager().callEvent(event);

        if (result == TransactionResult.Result.SUCCESS) {
            product.getStock().onPurchase(event);
            if (product.getPricer() instanceof IPurchaseListener listener) {
                listener.onPurchase(event);
            }

            // Process transaction
            product.delivery(player, this.getUnits());
            product.getCurrency().getHandler().take(player, price);
            //shop.getBank().deposit(product.getCurrency(), price);
            shop.getModule().getLogger().logTransaction(event);
            plugin.getMessage(VirtualLang.PRODUCT_PURCHASE_BUY).replace(this.replacePlaceholders()).send(player);
        }
        return transactionResult;
    }

    @Override
    @NotNull
    protected TransactionResult sell() {
        Player player = this.getPlayer();
        VirtualProduct product = this.getProduct();
        VirtualShop shop = product.getShop();
        ExcellentShop plugin = shop.plugin();

        int possible = product.getStock().getPossibleAmount(this.getTradeType(), player);
        int userHas = product.countUnits(player);
        int fined;
        if (this.isAll()) {
            fined = (possible >= 0 && possible < userHas) ? possible : userHas;
        }
        else {
            fined = (possible >= 0 && possible < this.getUnits()) ? possible : this.getUnits();
        }
        this.setUnits(fined);

        double price = this.getPrice();

        Result result = TransactionResult.Result.SUCCESS;
        if ((userHas < fined) || (this.isAll() && userHas < 1)) {
            result = TransactionResult.Result.NOT_ENOUGH_ITEMS;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS).replace(this.replacePlaceholders()).send(player);
        }
        else if (!this.isAll() && fined < 1) {
            result = TransactionResult.Result.OUT_OF_STOCK;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK).replace(this.replacePlaceholders()).send(player);
        }
        /*else if (!shop.getBank().hasEnough(product.getCurrency(), price)) {
            result = TransactionResult.Result.OUT_OF_MONEY;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_FUNDS).replace(this.replacePlaceholders()).send(player);
        }*/

        // Call custom event
        TransactionResult transactionResult = new TransactionResult(product, TradeType.SELL, fined, price, result);
        VirtualShopTransactionEvent event = new VirtualShopTransactionEvent(player, transactionResult);
        shop.plugin().getPluginManager().callEvent(event);

        if (result == TransactionResult.Result.SUCCESS) {
            product.getStock().onPurchase(event);
            if (product.getPricer() instanceof IPurchaseListener listener) {
                listener.onPurchase(event);
            }

            //shop.getBank().withdraw(product.getCurrency(), price);
            shop.getModule().getLogger().logTransaction(event);
            product.getCurrency().getHandler().give(player, price);
            product.take(player, fined);
            plugin.getMessage(VirtualLang.PRODUCT_PURCHASE_SELL).replace(this.replacePlaceholders()).send(player);
        }
        return transactionResult;
    }
}
