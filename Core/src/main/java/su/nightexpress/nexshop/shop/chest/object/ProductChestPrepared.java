package su.nightexpress.nexshop.shop.chest.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.api.shop.AbstractProductPrepared;
import su.nightexpress.nexshop.api.shop.chest.IShopChest;
import su.nightexpress.nexshop.api.shop.chest.IProductChest;
import su.nightexpress.nexshop.api.shop.chest.IProductChestPrepared;
import su.nightexpress.nexshop.api.shop.chest.event.ChestShopPurchaseEvent;
import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.type.TradeType;

public class ProductChestPrepared extends AbstractProductPrepared<IProductChest> implements IProductChestPrepared {

    public ProductChestPrepared(@NotNull IProductChest product, @NotNull TradeType tradeType) {
        super(product, tradeType);
    }

    @Override
    public boolean buy(@NotNull Player player) {
        if (this.getTradeType() != TradeType.BUY) return false;
        if (this.getProduct().isEmpty()) return false;

        IShopChest shop = this.getShop();
        IProductChest product = this.getProduct();

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
            PlayerUtil.addItem(player, item);
            /*for (String cmd : product.getCommands()) {
                PlayerUT.execCmd(player, cmd);
            }*/
        }

        if (!shop.isAdminShop()) {
            shop.takeProduct(product, amountUser);
            shop.getBank().deposit(product.getCurrency(), price);
            shop.save();
        }
        product.getCurrency().take(player, price);

        return true;
    }

    @Override
    public boolean sell(@NotNull Player player, boolean isAll) {
        if (this.getTradeType() != TradeType.SELL) return false;

        IShopChest shop = this.getShop();
        IProductChest product = this.getProduct();

        boolean isAdmin = shop.isAdminShop();
        int space = shop.getProductSpace(product);
        int amountPlayer = product.getItemAmount(player);
        int amountCan = isAll ? Math.min(!isAdmin ? space : amountPlayer, amountPlayer) : this.getAmount();
        this.setAmount(amountCan);

        int amount = this.getAmount();

        double price = this.getPrice();
        //double balanceShop = shop.getBank().getBalance(product.getCurrency());

        ChestShopPurchaseEvent event = new ChestShopPurchaseEvent(player, this);

        if ((amountPlayer < amountCan) || (isAll && amountPlayer < 1)) {
            event.setResult(Result.NOT_ENOUGH_ITEMS);
        }
        else if (!shop.getBank().hasEnough(product.getCurrency(), price)) {
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
            shop.getBank().withdraw(product.getCurrency(), price);
            shop.save();
        }
        product.getCurrency().give(player, price);
        product.takeItemAmount(player, amountCan);
        return true;
    }
}
