package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.IPurchaseListener;
import su.nightexpress.nexshop.api.event.ChestShopPurchaseEvent;
import su.nightexpress.nexshop.api.event.ShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.type.TradeType;

public class ChestPreparedProduct extends PreparedProduct<ChestProduct> {

    public ChestPreparedProduct(@NotNull ChestProduct product, @NotNull TradeType tradeType) {
        super(product, tradeType);
    }

    @Override
    public boolean buy(@NotNull Player player) {
        if (this.getTradeType() != TradeType.BUY) return false;

        ChestProduct product = this.getProduct();
        ChestShop shop = product.getShop();

        int amountToBuy = this.getAmount();
        int amountShopHas = product.getStock().getLeftAmount(TradeType.BUY);
        double price = this.getPrice();
        double balanceUser = product.getCurrency().getBalance(player);

        ChestShopPurchaseEvent event = new ChestShopPurchaseEvent(player, this);

        if (balanceUser < price) {
            event.setResult(Result.TOO_EXPENSIVE);
        }
        else if (amountShopHas >= 0 && amountToBuy > amountShopHas) {
            event.setResult(Result.OUT_OF_STOCK);
        }

        // Call custom event
        shop.plugin().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        product.getStock().onPurchase(event);
        if (product.getPricer() instanceof IPurchaseListener listener) {
            listener.onPurchase(event);
        }

        if (!shop.isAdminShop()) {
            shop.getBank().deposit(product.getCurrency(), price);
            shop.save();
        }

        // Process transaction
        product.delivery(player, amountToBuy);
        product.getCurrency().take(player, price);
        return true;
    }

    @Override
    public boolean sell(@NotNull Player player, boolean isAll) {
        if (this.getTradeType() != TradeType.SELL) return false;

        ChestProduct product = this.getProduct();
        ChestShop shop = product.getShop();

        boolean isAdmin = shop.isAdminShop();
        int shopSpace = product.getStock().getLeftAmount(TradeType.SELL);
        int userCount = product.countUnits(player);//product.count(player);
        int fined;// = isAll ? Math.min((!isAdmin ? shopSpace : userCount), userCount) : this.getAmount();
        if (isAll) {
            fined = Math.min((!isAdmin ? shopSpace : userCount), userCount);
        }
        else {
            fined = this.getAmount();
        }
        this.setAmount(fined);

        //int amountFinal = this.getAmount();

        double price = this.getPrice();
        //double balanceShop = shop.getBank().getBalance(product.getCurrency());

        ChestShopPurchaseEvent event = new ChestShopPurchaseEvent(player, this);

        if ((userCount < fined) || (isAll && userCount < 1)) {
            event.setResult(Result.NOT_ENOUGH_ITEMS);
        }
        else if (shopSpace >= 0 && shopSpace < fined) {
            event.setResult(Result.OUT_OF_SPACE);
        }
        else if (!shop.getBank().hasEnough(product.getCurrency(), price)) {
            event.setResult(Result.OUT_OF_MONEY);
        }

        // Call custom event
        shop.plugin().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        product.getStock().onPurchase(event);
        if (product.getPricer() instanceof IPurchaseListener listener) {
            listener.onPurchase(event);
        }

        // Process transaction
        if (!isAdmin) {
            shop.getBank().withdraw(product.getCurrency(), price);
            shop.save();
        }
        product.getCurrency().give(player, price);
        product.take(player, fined);
        return true;
    }
}
