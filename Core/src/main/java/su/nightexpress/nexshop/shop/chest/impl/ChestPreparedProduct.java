package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IPurchaseListener;
import su.nightexpress.nexshop.api.event.ChestShopPurchaseEvent;
import su.nightexpress.nexshop.api.event.ShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;

public class ChestPreparedProduct extends PreparedProduct<ChestProduct> {

    public ChestPreparedProduct(@NotNull ChestProduct product, @NotNull TradeType tradeType) {
        super(product, tradeType);
    }

    @Override
    public boolean buy(@NotNull Player player) {
        if (this.getTradeType() != TradeType.BUY) return false;

        ChestProduct product = this.getProduct();
        ChestShop shop = product.getShop();
        ExcellentShop plugin = shop.plugin();

        int amountToBuy = this.getAmount();
        int amountShopHas = product.getStock().getLeftAmount(TradeType.BUY);
        double price = this.getPrice();
        double balanceUser = product.getCurrency().getBalance(player);

        Result result = Result.SUCCESS;
        if (balanceUser < price) {
            result = Result.TOO_EXPENSIVE;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_TOO_EXPENSIVE).replace(this.replacePlaceholders()).send(player);
        }
        else if (amountShopHas >= 0 && amountToBuy > amountShopHas) {
            result = Result.OUT_OF_STOCK;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK).replace(this.replacePlaceholders()).send(player);
        }

        // Call custom event
        ChestShopPurchaseEvent event = new ChestShopPurchaseEvent(player, this, result);
        plugin.getPluginManager().callEvent(event);

        if (result != Result.SUCCESS) return false;

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
        shop.getModule().getLogger().logTransaction(event);

        plugin.getMessage(ChestLang.SHOP_TRADE_BUY_INFO_USER)
            .replace(this.replacePlaceholders())
            .replace(shop.replacePlaceholders())
            .send(player);

        Player owner = shop.getOwner().getPlayer();
        if (owner != null && !shop.isAdminShop()) {
            plugin.getMessage(ChestLang.SHOP_TRADE_BUY_INFO_OWNER)
                .replace("%player%", player.getDisplayName())
                .replace(this.replacePlaceholders())
                .replace(shop.replacePlaceholders())
                .send(owner);
        }
        return true;
    }

    @Override
    public boolean sell(@NotNull Player player, boolean isAll) {
        if (this.getTradeType() != TradeType.SELL) return false;

        ChestProduct product = this.getProduct();
        ChestShop shop = product.getShop();
        ExcellentShop plugin = shop.plugin();

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

        double price = this.getPrice();


        Result result = Result.SUCCESS;
        if ((userCount < fined) || (isAll && userCount < 1)) {
            result = Result.NOT_ENOUGH_ITEMS;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS).replace(this.replacePlaceholders()).send(player);
        }
        else if (shopSpace >= 0 && shopSpace < fined) {
            result = Result.OUT_OF_SPACE;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE).replace(this.replacePlaceholders()).send(player);
        }
        else if (!shop.getBank().hasEnough(product.getCurrency(), price)) {
            result = Result.OUT_OF_MONEY;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_FUNDS).replace(this.replacePlaceholders()).send(player);
        }

        // Call custom event
        ChestShopPurchaseEvent event = new ChestShopPurchaseEvent(player, this, result);
        plugin.getPluginManager().callEvent(event);

        if (result != Result.SUCCESS) return false;

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
        shop.getModule().getLogger().logTransaction(event);

        plugin.getMessage(ChestLang.SHOP_TRADE_SELL_INFO_USER)
            .replace(this.replacePlaceholders())
            .replace(shop.replacePlaceholders())
            .send(player);

        Player owner = shop.getOwner().getPlayer();
        if (owner != null && !shop.isAdminShop()) {
            plugin.getMessage(ChestLang.SHOP_TRADE_SELL_INFO_OWNER)
                .replace("%player%", player.getDisplayName())
                .replace(this.replacePlaceholders())
                .replace(shop.replacePlaceholders())
                .send(owner);
        }
        return true;
    }
}
