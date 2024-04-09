package su.nightexpress.nexshop.shop.virtual.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.Transaction.Result;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.impl.AbstractPreparedProduct;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;

public class VirtualPreparedProduct extends AbstractPreparedProduct<VirtualProduct> {

    public VirtualPreparedProduct(@NotNull ExcellentShop plugin, @NotNull Player player, @NotNull VirtualProduct product, @NotNull TradeType tradeType, boolean all) {
        super(plugin, player, product, tradeType, all);
    }

    @Override
    @NotNull
    protected Transaction buy() {
        Player player = this.getPlayer();
        VirtualProduct product = this.getProduct();
        VirtualShop shop = product.getShop();

        double price = this.getPrice();
        double balance = product.getCurrency().getHandler().getBalance(player);

        Result result = Transaction.Result.SUCCESS;
        if (balance < price) {
            result = Transaction.Result.TOO_EXPENSIVE;
        }

        // Call custom event
        Transaction transaction = new Transaction(plugin, product, TradeType.BUY, this.getUnits(), price, result);
        ShopTransactionEvent event = new ShopTransactionEvent(player, shop, transaction);
        plugin.getPluginManager().callEvent(event);

        result = event.getTransaction().getResult();
        transaction.sendError(player);

        if (result == Transaction.Result.SUCCESS) {
            plugin.getMessage(VirtualLang.PRODUCT_PURCHASE_BUY).replace(this.replacePlaceholders()).send(player);

            shop.getPricer().onTransaction(event);
            shop.getStock().onTransaction(event);

            // Process transaction
            product.delivery(this.getInventory(), transaction.getUnits());
            product.getCurrency().getHandler().take(player, transaction.getPrice());
            //shop.getBank().deposit(product.getCurrency(), price);
            shop.getModule().getLogger().logTransaction(event);
        }
        return transaction;
    }

    @Override
    @NotNull
    protected Transaction sell() {
        Player player = this.getPlayer();
        Inventory inventory = this.getInventory();
        VirtualProduct product = this.getProduct();
        VirtualShop shop = product.getShop();

        int possible = product.getAvailableAmount(player, this.getTradeType());//.getShop().getStock().count(player, product, this.getTradeType());
        int userHas = product.countUnits(inventory);
        int fined;
        if (this.isAll()) {
            fined = (possible >= 0 && possible < userHas) ? possible : userHas;
        }
        else {
            fined = (possible >= 0 && possible < this.getUnits()) ? possible : this.getUnits();
        }
        this.setUnits(fined);

        double price = this.getPrice();

        Result result = Transaction.Result.SUCCESS;
        if ((userHas < fined) || (this.isAll() && userHas < 1)) {
            result = Transaction.Result.NOT_ENOUGH_ITEMS;
        }
        else if (!this.isAll() && fined < 1) {
            result = Transaction.Result.OUT_OF_STOCK;
        }
        /*else if (!shop.getBank().hasEnough(product.getCurrency(), price)) {
            result = TransactionResult.Result.OUT_OF_MONEY;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_FUNDS).replace(this.replacePlaceholders()).send(player);
        }*/

        // Call custom event
        Transaction transaction = new Transaction(plugin, product, TradeType.SELL, fined, price, result);
        ShopTransactionEvent event = new ShopTransactionEvent(player, shop, transaction);
        plugin.getPluginManager().callEvent(event);

        result = event.getTransaction().getResult();
        transaction.sendError(player);

        if (result == Transaction.Result.SUCCESS) {
            plugin.getMessage(VirtualLang.PRODUCT_PURCHASE_SELL).replace(this.replacePlaceholders()).send(player);

            shop.getPricer().onTransaction(event);
            shop.getStock().onTransaction(event);

            //shop.getBank().withdraw(product.getCurrency(), price);
            shop.getModule().getLogger().logTransaction(event);
            product.getCurrency().getHandler().give(player, transaction.getPrice());
            product.take(inventory, transaction.getUnits());
        }
        return transaction;
    }
}
