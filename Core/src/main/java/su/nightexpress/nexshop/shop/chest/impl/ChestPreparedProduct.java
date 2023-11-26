package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.impl.AbstractPreparedProduct;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.Transaction.Result;

public class ChestPreparedProduct extends AbstractPreparedProduct<ChestProduct> {

    public ChestPreparedProduct(@NotNull ExcellentShop plugin, @NotNull Player player, @NotNull ChestProduct product, @NotNull TradeType tradeType, boolean all) {
        super(plugin, player, product, tradeType, all);
    }

    @Override
    @NotNull
    protected Transaction buy() {
        Player player = this.getPlayer();
        Inventory inventory = this.getInventory();
        ChestProduct product = this.getProduct();
        ChestShop shop = product.getShop();
        ExcellentShop plugin = shop.plugin();

        int amountToBuy = this.getUnits();
        int amountShopHas = shop.getStock().count(product, TradeType.BUY);
        double price = this.getPrice();
        double balanceUser = product.getCurrency().getHandler().getBalance(player);

        Result result = Transaction.Result.SUCCESS;
        if (balanceUser < price) {
            result = Transaction.Result.TOO_EXPENSIVE;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_TOO_EXPENSIVE).replace(this.replacePlaceholders()).send(player);
        }
        else if (amountShopHas >= 0 && amountToBuy > amountShopHas) {
            result = Transaction.Result.OUT_OF_STOCK;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK).replace(this.replacePlaceholders()).send(player);
        }

        // Call custom event
        Transaction transaction = new Transaction(product, TradeType.BUY, amountToBuy, price, result);
        ShopTransactionEvent event = new ShopTransactionEvent(player, transaction);
        plugin.getPluginManager().callEvent(event);

        if (result == Transaction.Result.SUCCESS) {
            shop.getPricer().onTransaction(event);
            shop.getStock().onTransaction(event);

            if (!shop.isAdminShop()) {
                shop.getOwnerBank().deposit(product.getCurrency(), transaction.getPrice());
                shop.getModule().savePlayerBank(shop.getOwnerBank());
            }

            // Process transaction
            product.delivery(inventory, transaction.getUnits());
            product.getCurrency().getHandler().take(player, transaction.getPrice());
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
        return transaction;
    }

    @Override
    @NotNull
    protected Transaction sell() {
        Player player = this.getPlayer();
        Inventory inventory = this.getInventory();
        ChestProduct product = this.getProduct();
        ChestShop shop = product.getShop();
        ExcellentShop plugin = shop.plugin();

        boolean isAdmin = shop.isAdminShop();
        int shopSpace = shop.getStock().count(product, TradeType.SELL);
        int userCount = product.countUnits(inventory);
        int fined;
        if (this.isAll()) {
            fined = Math.min((!isAdmin ? shopSpace : userCount), userCount);
        }
        else {
            fined = this.getUnits();
        }
        this.setUnits(fined);

        double price = this.getPrice();


        Result result = Transaction.Result.SUCCESS;
        if ((userCount < fined) || (this.isAll() && userCount < 1)) {
            result = Transaction.Result.NOT_ENOUGH_ITEMS;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS).replace(this.replacePlaceholders()).send(player);
        }
        else if (shopSpace >= 0 && shopSpace < fined) {
            result = Transaction.Result.OUT_OF_SPACE;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE).replace(this.replacePlaceholders()).send(player);
        }
        else if (!shop.isAdminShop() && !shop.getOwnerBank().hasEnough(product.getCurrency(), price)) {
            result = Transaction.Result.OUT_OF_MONEY;
            plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_FUNDS).replace(this.replacePlaceholders()).send(player);
        }

        // Call custom event
        Transaction transaction = new Transaction(product, TradeType.SELL, fined, price, result);
        ShopTransactionEvent event = new ShopTransactionEvent(player, transaction);
        plugin.getPluginManager().callEvent(event);

        if (result == Transaction.Result.SUCCESS) {
            shop.getPricer().onTransaction(event);
            shop.getStock().onTransaction(event);

            // Process transaction
            if (!isAdmin) {
                shop.getOwnerBank().withdraw(product.getCurrency(), transaction.getPrice());
                shop.getModule().savePlayerBank(shop.getOwnerBank());
            }
            product.getCurrency().getHandler().give(player, transaction.getPrice());
            product.take(inventory, transaction.getUnits());
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
        return transaction;
    }
}
