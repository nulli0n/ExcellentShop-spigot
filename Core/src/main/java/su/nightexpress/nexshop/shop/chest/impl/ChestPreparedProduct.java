package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.IPurchaseListener;
import su.nightexpress.nexshop.api.event.ChestShopTransactionEvent;
import su.nightexpress.nexshop.shop.util.TransactionResult.Result;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.util.TransactionResult;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;

public class ChestPreparedProduct extends PreparedProduct<ChestProduct> {

    public ChestPreparedProduct(@NotNull ChestProduct product, @NotNull TradeType tradeType, boolean all) {
        super(product, tradeType, all);
    }

    @Override
    @NotNull
    protected TransactionResult buy(@NotNull Player player) {
        ChestProduct product = this.getProduct();
        ChestShop shop = product.getShop();
        ExcellentShop plugin = shop.plugin();

        int amountToBuy = this.getUnits();
        int amountShopHas = product.getStock().getLeftAmount(TradeType.BUY);
        double price = this.getPrice();
        double balanceUser = product.getCurrency().getHandler().getBalance(player);

        Result result = TransactionResult.Result.SUCCESS;
        if (balanceUser < price) {
            result = TransactionResult.Result.TOO_EXPENSIVE;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_TOO_EXPENSIVE).replace(this.replacePlaceholders()).send(player);
        }
        else if (amountShopHas >= 0 && amountToBuy > amountShopHas) {
            result = TransactionResult.Result.OUT_OF_STOCK;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK).replace(this.replacePlaceholders()).send(player);
        }

        // Call custom event
        TransactionResult transactionResult = new TransactionResult(product, TradeType.BUY, amountToBuy, price, result);
        ChestShopTransactionEvent event = new ChestShopTransactionEvent(player, transactionResult);
        plugin.getPluginManager().callEvent(event);

        if (result == TransactionResult.Result.SUCCESS) {
            product.getStock().onPurchase(event);
            if (product.getPricer() instanceof IPurchaseListener listener) {
                listener.onPurchase(event);
            }

            if (!shop.isAdminShop()) {
                shop.getOwnerBank().deposit(product.getCurrency(), price);
                shop.getModule().savePlayerBank(shop.getOwnerBank());
            }

            // Process transaction
            product.delivery(player, amountToBuy);
            product.getCurrency().getHandler().take(player, price);
            shop.getModule().getLogger().logTransaction(event);

            plugin.getMessage(ChestLang.SHOP_TRADE_BUY_INFO_USER)
                .replace(this.replacePlaceholders())
                .replace(shop.replacePlaceholders())
                .send(player);

            Player owner = shop.getOwner().getPlayer();
            if (owner != null && !shop.isAdminShop()) {
                plugin.getMessage(ChestLang.SHOP_TRADE_BUY_INFO_OWNER)
                    .replace(Placeholders.forPlayer(player))
                    .replace(this.replacePlaceholders())
                    .replace(shop.replacePlaceholders())
                    .send(owner);
            }
        }
        return transactionResult;
    }

    @Override
    @NotNull
    protected TransactionResult sell(@NotNull Player player) {
        ChestProduct product = this.getProduct();
        ChestShop shop = product.getShop();
        ExcellentShop plugin = shop.plugin();

        boolean isAdmin = shop.isAdminShop();
        int shopSpace = product.getStock().getLeftAmount(TradeType.SELL);
        int userCount = product.countUnits(player);
        int fined;
        if (this.isAll()) {
            fined = Math.min((!isAdmin ? shopSpace : userCount), userCount);
        }
        else {
            fined = this.getUnits();
        }
        this.setUnits(fined);

        double price = this.getPrice();


        Result result = TransactionResult.Result.SUCCESS;
        if ((userCount < fined) || (this.isAll() && userCount < 1)) {
            result = TransactionResult.Result.NOT_ENOUGH_ITEMS;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS).replace(this.replacePlaceholders()).send(player);
        }
        else if (shopSpace >= 0 && shopSpace < fined) {
            result = TransactionResult.Result.OUT_OF_SPACE;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE).replace(this.replacePlaceholders()).send(player);
        }
        else if (!shop.isAdminShop() && !shop.getOwnerBank().hasEnough(product.getCurrency(), price)) {
            result = TransactionResult.Result.OUT_OF_MONEY;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_FUNDS).replace(this.replacePlaceholders()).send(player);
        }

        // Call custom event
        TransactionResult transactionResult = new TransactionResult(product, TradeType.SELL, fined, price, result);
        ChestShopTransactionEvent event = new ChestShopTransactionEvent(player, transactionResult);
        plugin.getPluginManager().callEvent(event);

        if (result == TransactionResult.Result.SUCCESS) {
            product.getStock().onPurchase(event);
            if (product.getPricer() instanceof IPurchaseListener listener) {
                listener.onPurchase(event);
            }

            // Process transaction
            if (!isAdmin) {
                shop.getOwnerBank().withdraw(product.getCurrency(), price);
                shop.getModule().savePlayerBank(shop.getOwnerBank());
            }
            product.getCurrency().getHandler().give(player, price);
            product.take(player, fined);
            shop.getModule().getLogger().logTransaction(event);

            plugin.getMessage(ChestLang.SHOP_TRADE_SELL_INFO_USER)
                .replace(this.replacePlaceholders())
                .replace(shop.replacePlaceholders())
                .send(player);

            Player owner = shop.getOwner().getPlayer();
            if (owner != null && !shop.isAdminShop()) {
                plugin.getMessage(ChestLang.SHOP_TRADE_SELL_INFO_OWNER)
                    .replace(Placeholders.forPlayer(player))
                    .replace(this.replacePlaceholders())
                    .replace(shop.replacePlaceholders())
                    .send(owner);
            }
        }
        return transactionResult;
    }
}
