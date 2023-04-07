package su.nightexpress.nexshop.shop.virtual.impl.product;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IPurchaseListener;
import su.nightexpress.nexshop.api.event.ShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.event.VirtualShopPurchaseEvent;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
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
        ExcellentShop plugin = shop.plugin();

        double price = this.getPrice();
        double balance = product.getCurrency().getBalance(player);

        Result result = Result.SUCCESS;
        if (balance < price) {
            result = Result.TOO_EXPENSIVE;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_TOO_EXPENSIVE).replace(this.replacePlaceholders()).send(player);
        }

        // Call custom event
        VirtualShopPurchaseEvent event = new VirtualShopPurchaseEvent(player, this, result);
        shop.plugin().getPluginManager().callEvent(event);

        if (result != Result.SUCCESS) return false;

        product.getStock().onPurchase(event);
        if (product.getPricer() instanceof IPurchaseListener listener) {
            listener.onPurchase(event);
        }

        // Process transaction
        product.delivery(player, this.getAmount());
        product.getCurrency().take(player, price);
        shop.getBank().deposit(product.getCurrency(), price);
        shop.getModule().getLogger().logTransaction(event);
        plugin.getMessage(VirtualLang.PRODUCT_PURCHASE_BUY).replace(this.replacePlaceholders()).send(player);
        return true;
    }

    @Override
    public boolean sell(@NotNull Player player, boolean isAll) {
        if (this.getTradeType() != TradeType.SELL) return false;

        VirtualProduct product = this.getProduct();
        VirtualShop shop = product.getShop();
        ExcellentShop plugin = shop.plugin();

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

        double price = this.getPrice();

        Result result = Result.SUCCESS;
        if ((userHas < fined) || (isAll && userHas < 1)) {
            result = Result.NOT_ENOUGH_ITEMS;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS).replace(this.replacePlaceholders()).send(player);
        }
        else if (!isAll && fined < 1) {
            result = Result.OUT_OF_STOCK;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK).replace(this.replacePlaceholders()).send(player);
        }
        else if (!shop.getBank().hasEnough(product.getCurrency(), price)) {
            result = Result.OUT_OF_MONEY;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_FUNDS).replace(this.replacePlaceholders()).send(player);
        }

        // Call custom event
        VirtualShopPurchaseEvent event = new VirtualShopPurchaseEvent(player, this, result);
        shop.plugin().getPluginManager().callEvent(event);

        if (result != Result.SUCCESS) return false;

        // Process transaction
        product.getStock().onPurchase(event);
        if (product.getPricer() instanceof IPurchaseListener listener) {
            listener.onPurchase(event);
        }

        shop.getBank().withdraw(product.getCurrency(), price);
        shop.getModule().getLogger().logTransaction(event);
        product.getCurrency().give(player, price);
        product.take(player, fined);
        plugin.getMessage(VirtualLang.PRODUCT_PURCHASE_SELL).replace(this.replacePlaceholders()).send(player);
        return true;
    }
}
