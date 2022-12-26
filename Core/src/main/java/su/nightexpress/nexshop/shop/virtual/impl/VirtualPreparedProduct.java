package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.api.IPurchaseListener;
import su.nightexpress.nexshop.api.event.ShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.event.VirtualShopPurchaseEvent;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.type.TradeType;

public class VirtualPreparedProduct extends PreparedProduct<VirtualProduct> {

    public VirtualPreparedProduct(@NotNull VirtualProduct product, @NotNull TradeType tradeType) {
        super(product, tradeType);
    }

    @Override
    public boolean buy(@NotNull Player player) {
        if (this.getTradeType() != TradeType.BUY) return false;
        if (this.getProduct().isEmpty()) return false;

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
        ItemStack item = product.getItem();
        for (int stack = 0; stack < this.getAmount(); stack++) {
            if (!item.getType().isAir()) {
                PlayerUtil.addItem(player, item);
            }
            product.getCommands().forEach(command -> PlayerUtil.dispatchCommand(player, command));
        }

        shop.getBank().deposit(product.getCurrency(), price);
        product.getCurrency().take(player, price);
        return true;
    }

    @Override
    public boolean sell(@NotNull Player player, boolean isAll) {
        if (this.getTradeType() != TradeType.SELL) return false;
        if (!this.getProduct().hasItem()) return false;

        VirtualProduct product = this.getProduct();
        VirtualShop shop = product.getShop();

        int possible = product.getStock().getPossibleAmount(this.getTradeType(), player);//product.getStockAmountLeft(player, this.getTradeType());
        int amountHas = product.countItem(player);
        int amountCan = isAll ? ((possible >= 0 && possible < amountHas) ? possible : amountHas) : possible < 0 ? this.getAmount() : Math.min(possible, this.getAmount());

        this.setAmount(amountCan);

        VirtualShopPurchaseEvent event = new VirtualShopPurchaseEvent(player, this);

        double price = this.getPrice();
        //double balanceShop = shop.getBank().getBalance(product.getCurrency());

        if ((amountHas < amountCan) || (isAll && amountHas < 1)) {
            event.setResult(Result.NOT_ENOUGH_ITEMS);
        }
        else if (!isAll && amountCan < 1) {
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
        product.takeItem(player, amountCan);
        return true;
    }
}
