package su.nightexpress.nexshop.shop.virtual.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.nexshop.api.AbstractProductPrepared;
import su.nightexpress.nexshop.api.IShop;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProductPrepared;
import su.nightexpress.nexshop.api.virtual.event.VirtualShopPurchaseEvent;

public class VirtualProductPrepared extends AbstractProductPrepared<IShopVirtualProduct> implements IShopVirtualProductPrepared {

    public VirtualProductPrepared(@NotNull IShopVirtualProduct product, @NotNull TradeType tradeType) {
        super(product, tradeType);
    }

    @Override
    public boolean buy(@NotNull Player player) {
        if (this.getTradeType() != TradeType.BUY) return false;
        if (this.getShopProduct().isEmpty()) return false;

        IShopVirtual shop = this.getShop();
        IShopVirtualProduct product = this.getShopProduct();

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
            if (!ItemUT.isAir(item)) {
                ItemUT.addItem(player, item);
            }
            product.getCommands().forEach(command -> PlayerUT.execCmd(player, command));
        }

        shop.addToShopBalance(product.getCurrency(), price);
        product.getCurrency().take(player, price);
        return true;
    }

    @Override
    public boolean sell(@NotNull Player player, boolean isAll) {
        if (this.getTradeType() != TradeType.SELL) return false;
        if (!this.getShopProduct().hasItem()) return false;

        IShop shop = this.getShop();
        IShopProduct product = this.getShopProduct();

        int possible = product.getStockAmountLeft(player, this.getTradeType());
        int amountHas = product.getItemAmount(player);
        int amountCan = isAll ? ((possible >= 0 && possible < amountHas) ? possible : amountHas) : this.getAmount();

        this.setAmount(amountCan);

        VirtualShopPurchaseEvent event = new VirtualShopPurchaseEvent(player, this);

        double price = this.getPrice();
        double balanceShop = shop.getShopBalance(product.getCurrency());

        if ((amountHas < amountCan) || (isAll && amountHas < 1)) {
            event.setResult(Result.NOT_ENOUGH_ITEMS);
        }
        else if (balanceShop >= 0 && balanceShop < price) {
            event.setResult(Result.OUT_OF_MONEY);
        }

        // Call custom event
        shop.plugin().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        // Process transaction
        shop.takeFromShopBalance(product.getCurrency(), price);
        product.getCurrency().give(player, price);
        product.takeItemAmount(player, amountCan);
        return true;
    }
}
