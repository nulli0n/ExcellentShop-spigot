package su.nightexpress.nexshop.shop.virtual.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.api.shop.AbstractProductPrepared;
import su.nightexpress.nexshop.api.shop.IShop;
import su.nightexpress.nexshop.api.shop.IProduct;
import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtual;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtualPrepared;
import su.nightexpress.nexshop.api.shop.virtual.event.VirtualShopPurchaseEvent;

public class ProductVirtualPrepared extends AbstractProductPrepared<IProductVirtual> implements IProductVirtualPrepared {

    public ProductVirtualPrepared(@NotNull IProductVirtual product, @NotNull TradeType tradeType) {
        super(product, tradeType);
    }

    @Override
    public boolean buy(@NotNull Player player) {
        if (this.getTradeType() != TradeType.BUY) return false;
        if (this.getProduct().isEmpty()) return false;

        IShopVirtual shop = this.getShop();
        IProductVirtual product = this.getProduct();

        double price = this.getPrice();
        double balance = product.getCurrency().getBalance(player);

        VirtualShopPurchaseEvent event = new VirtualShopPurchaseEvent(player, this);

        if (balance < price) {
            event.setResult(Result.TOO_EXPENSIVE);
        }

        // Call custom event
        shop.plugin().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

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

        IShop shop = this.getShop();
        IProduct product = this.getProduct();

        int possible = product.getStockAmountLeft(player, this.getTradeType());
        int amountHas = product.getItemAmount(player);
        int amountCan = isAll ? ((possible >= 0 && possible < amountHas) ? possible : amountHas) : this.getAmount();

        this.setAmount(amountCan);

        VirtualShopPurchaseEvent event = new VirtualShopPurchaseEvent(player, this);

        double price = this.getPrice();
        //double balanceShop = shop.getBank().getBalance(product.getCurrency());

        if ((amountHas < amountCan) || (isAll && amountHas < 1)) {
            event.setResult(Result.NOT_ENOUGH_ITEMS);
        }
        else if (!shop.getBank().hasEnough(product.getCurrency(), price)) {
            event.setResult(Result.OUT_OF_MONEY);
        }

        // Call custom event
        shop.plugin().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        // Process transaction
        shop.getBank().withdraw(product.getCurrency(), price);
        product.getCurrency().give(player, price);
        product.takeItemAmount(player, amountCan);
        return true;
    }
}
