package su.nightexpress.nexshop.shop.virtual.impl.product;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.IPurchaseListener;
import su.nightexpress.nexshop.api.event.ShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.event.VirtualShopPurchaseEvent;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

public class VirtualPreparedProduct extends PreparedProduct<VirtualProduct> {

    public VirtualPreparedProduct(@NotNull VirtualProduct product, @NotNull TradeType tradeType) {
        super(product, tradeType);
    }

    @Override
    public boolean buy(@NotNull Player player) {
        if (this.getTradeType() != TradeType.BUY) return false;

        VirtualProduct product = this.getProduct();
        VirtualShop shop = product.getShop();

        double price = this.getPrice();
        double balance = product.getCurrency().getBalance(player);

        VirtualShopPurchaseEvent event = new VirtualShopPurchaseEvent(player, this);

        if (balance < price) {
            event.setResult(Result.TOO_EXPENSIVE);
        }

        // Call custom event
        shop.plugin().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        product.getStock().onPurchase(event);
        if (product.getPricer() instanceof IPurchaseListener listener) {
            listener.onPurchase(event);
        }

        // Process transaction
        product.delivery(player, this.getAmount());
        product.getCurrency().take(player, price);
        shop.getBank().deposit(product.getCurrency(), price);
        return true;
    }

    @Override
    public boolean sell(@NotNull Player player, boolean isAll) {
        if (this.getTradeType() != TradeType.SELL) return false;

        VirtualProduct product = this.getProduct();
        VirtualShop shop = product.getShop();

        int possible = product.getStock().getPossibleAmount(this.getTradeType(), player);//product.getStockAmountLeft(player, this.getTradeType());
        int userHas = product.countUnits(player);//product.count(player);
        int fined;// = isAll ? ((possible >= 0 && possible < userHas) ? possible : userHas) : possible < 0 ? this.getAmount() : Math.min(possible, this.getAmount());

        if (isAll) {
            fined = (possible >= 0 && possible < userHas) ? possible : userHas;
        }
        else {
            fined = (possible >= 0 && possible < this.getAmount()) ? possible : this.getAmount();
        }
        this.setAmount(fined);

        VirtualShopPurchaseEvent event = new VirtualShopPurchaseEvent(player, this);

        double price = this.getPrice();
        //double balanceShop = shop.getBank().getBalance(product.getCurrency());

        if ((userHas < fined) || (isAll && userHas < 1)) {
            event.setResult(Result.NOT_ENOUGH_ITEMS);
        }
        else if (!isAll && fined < 1) {
            event.setResult(Result.OUT_OF_STOCK);
        }
        else if (!shop.getBank().hasEnough(product.getCurrency(), price)) {
            event.setResult(Result.OUT_OF_MONEY);
        }

        // Call custom event
        shop.plugin().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        // Process transaction
        product.getStock().onPurchase(event);
        if (product.getPricer() instanceof IPurchaseListener listener) {
            listener.onPurchase(event);
        }

        shop.getBank().withdraw(product.getCurrency(), price);
        product.getCurrency().give(player, price);
        product.take(player, fined);
        return true;
    }
}
