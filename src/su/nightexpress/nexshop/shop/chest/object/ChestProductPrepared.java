package su.nightexpress.nexshop.shop.chest.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.api.AbstractProductPrepared;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;
import su.nightexpress.nexshop.api.chest.IShopChestProductPrepared;
import su.nightexpress.nexshop.api.chest.event.ChestShopPurchaseEvent;
import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.type.TradeType;

public class ChestProductPrepared extends AbstractProductPrepared<IShopChestProduct> implements IShopChestProductPrepared {

    public ChestProductPrepared(@NotNull IShopChestProduct product, @NotNull TradeType tradeType) {
        super(product, tradeType);
    }

    @Override
    public boolean buy(@NotNull Player player) {
        if (this.getTradeType() != TradeType.BUY) return false;
        if (this.getShopProduct().isEmpty()) return false;

        IShopChest shop = this.getShop();
        IShopChestProduct product = this.getShopProduct();

        int amountUser = this.getAmount();
        int amountShop = shop.getProductAmount(product);
        double price = this.getPrice();
        double balanceUser = product.getCurrency().getBalance(player);

        ChestShopPurchaseEvent event = new ChestShopPurchaseEvent(player, this);

        if (balanceUser < price) {
            event.setResult(Result.TOO_EXPENSIVE);
        }
        else if (amountShop >= 0 && amountUser > amountShop) {
            event.setResult(Result.OUT_OF_STOCK);
        }

        // Call custom event
        shop.plugin().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        // Process transaction
        ItemStack item = product.getItem();
        for (int stack = 0; stack < amountUser; stack++) {
            ItemUT.addItem(player, item);
            /*for (String cmd : product.getCommands()) {
                PlayerUT.execCmd(player, cmd);
            }*/
        }

        if (!shop.isAdminShop()) {
            shop.takeProduct(product, amountUser);
            shop.addToShopBalance(product.getCurrency(), price);
        }
        product.getCurrency().take(player, price);

        return true;
    }

    @Override
    public boolean sell(@NotNull Player player, boolean isAll) {
        if (this.getTradeType() != TradeType.SELL) return false;

        IShopChest shop = this.getShop();
        IShopChestProduct product = this.getShopProduct();

        boolean isAdmin = shop.isAdminShop();
        int space = shop.getProductSpace(product);
        int amountPlayer = product.getItemAmount(player);
        int amountCan = isAll ? Math.min(!isAdmin ? space : amountPlayer, amountPlayer) : this.getAmount();
        this.setAmount(amountCan);

        int amount = this.getAmount();

        double price = this.getPrice();
        double balanceShop = shop.getShopBalance(product.getCurrency());

        ChestShopPurchaseEvent event = new ChestShopPurchaseEvent(player, this);

        if ((amountPlayer < amountCan) || (isAll && amountPlayer < 1)) {
            event.setResult(Result.NOT_ENOUGH_ITEMS);
        }
        else if (balanceShop >= 0 && balanceShop < price) {
            event.setResult(Result.OUT_OF_MONEY);
        }
        else if (space >= 0 && space < amount) {
            event.setResult(Result.OUT_OF_SPACE);
        }

        // Call custom event
        shop.plugin().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        // Process transaction
        if (!isAdmin) {
            shop.addProduct(product, amount);
            shop.takeFromShopBalance(product.getCurrency(), price);
        }
        product.getCurrency().give(player, price);
        product.takeItemAmount(player, amountCan);
        return true;
    }
}
