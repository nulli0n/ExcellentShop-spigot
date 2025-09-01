package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.Transaction.Result;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.impl.AbstractPreparedProduct;

public class ChestPreparedProduct extends AbstractPreparedProduct<ChestProduct> {

    public ChestPreparedProduct(@NotNull Player player, @NotNull ChestProduct product, @NotNull TradeType tradeType, boolean all) {
        super(player, product, tradeType, all);
    }

    @Override
    @NotNull
    protected Transaction buy() {
        ChestProduct product = this.getProduct();
        ChestShop shop = product.getShop();

        int amountToBuy = this.getUnits();
        int amountShopHas = product.countStock(TradeType.BUY, null);
        double price = this.getPrice();
        double balanceUser = product.getCurrency().getBalance(player);

        Result result = Transaction.Result.SUCCESS;
        if (balanceUser < price) {
            result = Transaction.Result.TOO_EXPENSIVE;
        }
        else if (amountShopHas >= 0 && amountToBuy > amountShopHas) {
            result = Transaction.Result.OUT_OF_STOCK;
        }

        // Call custom event
        Transaction transaction = new Transaction(product, TradeType.BUY, amountToBuy, price, result);
        ShopTransactionEvent event = new ShopTransactionEvent(player, shop, transaction);
        Bukkit.getPluginManager().callEvent(event);

        result = event.getTransaction().getResult();
        transaction.sendError(player);

        if (result == Transaction.Result.SUCCESS) {
            // TODO Store price in prepared product?
            if (!this.isSilent()) {
                ChestLang.SHOP_TRADE_BUY_INFO_USER.message().send(player, replacer -> replacer
                    .replace(this.replacePlaceholders())
                    .replace(shop.replacePlaceholders())
                );

                Player owner = shop.getOwner().getPlayer();
                if (owner != null && !shop.isAdminShop()) {
                    ChestLang.SHOP_TRADE_BUY_INFO_OWNER.message().send(owner, replacer -> replacer
                        .replace(Placeholders.forPlayer(player))
                        .replace(this.replacePlaceholders())
                        .replace(shop.replacePlaceholders())
                    );
                }
            }

            shop.onTransaction(event);

            if (!shop.isAdminShop()) {
                product.consumeStock(TradeType.BUY, transaction.getUnits(), null); // Take item from shop's inventory.
                shop.getRentersOrOwnerBank().deposit(product.getCurrency(), transaction.getPrice());
                shop.getModule().savePlayerBank(shop.getRentersOrOwnerBank());
            }

            // Process transaction
            product.delivery(this.getInventory(), transaction.getUnits());
            product.getCurrency().take(player, transaction.getPrice());
            shop.getModule().getLogger().logTransaction(event);
        }

        return transaction;
    }

    @Override
    @NotNull
    protected Transaction sell() {
        Inventory inventory = this.getInventory();
        ChestProduct product = this.getProduct();
        ChestShop shop = product.getShop();

        boolean isUnlimited = shop.isAdminShop() || ChestUtils.isInfiniteStorage();
        int shopSpace = product.countStock(TradeType.SELL, null);//shop.getStock().count(product, TradeType.SELL);
        int userCount = product.countUnits(inventory);
        int fined;
        if (this.isAll()) {
            fined = Math.min((!isUnlimited ? shopSpace : userCount), userCount);
        }
        else {
            fined = this.getUnits();
        }
        this.setUnits(fined);

        double price = this.getPrice();


        Result result = Transaction.Result.SUCCESS;
        if ((userCount < fined) || (this.isAll() && userCount < 1)) {
            result = Transaction.Result.NOT_ENOUGH_ITEMS;
        }
        else if (shopSpace >= 0 && shopSpace < fined) {
            result = Transaction.Result.OUT_OF_SPACE;
        }
        else if (!shop.isAdminShop() && !shop.getRentersOrOwnerBank().hasEnough(product.getCurrency(), price)) {
            result = Transaction.Result.OUT_OF_MONEY;
        }

        // Call custom event
        Transaction transaction = new Transaction(product, TradeType.SELL, fined, price, result);
        ShopTransactionEvent event = new ShopTransactionEvent(player, shop, transaction);
        Bukkit.getPluginManager().callEvent(event);

        if (!shop.isAdminShop() && event.getTransactionResult() == Result.SUCCESS) {
            if (!product.storeStock(TradeType.SELL, transaction.getUnits(), null)) {
                transaction.setResult(Transaction.Result.OUT_OF_SPACE);
            }
        }

        transaction.sendError(player);

        if (event.getTransactionResult() == Transaction.Result.SUCCESS) {
            shop.onTransaction(event);

            // Process transaction
            if (!shop.isAdminShop()) {
                shop.getRentersOrOwnerBank().withdraw(product.getCurrency(), transaction.getPrice());
                shop.getModule().savePlayerBank(shop.getRentersOrOwnerBank());
            }
            product.getCurrency().give(player, transaction.getPrice());
            product.take(inventory, transaction.getUnits());
            shop.getModule().getLogger().logTransaction(event);

            if (!this.isSilent()) {
                ChestLang.SHOP_TRADE_SELL_INFO_USER.message().send(player, replacer -> replacer
                    .replace(this.replacePlaceholders())
                    .replace(shop.replacePlaceholders())
                );

                Player owner = shop.getOwner().getPlayer();
                if (owner != null && !shop.isAdminShop()) {
                    ChestLang.SHOP_TRADE_SELL_INFO_OWNER.message().send(owner, replacer -> replacer
                        .replace(Placeholders.forPlayer(player))
                        .replace(this.replacePlaceholders())
                        .replace(shop.replacePlaceholders())
                    );
                }
            }
        }
        return transaction;
    }
}
