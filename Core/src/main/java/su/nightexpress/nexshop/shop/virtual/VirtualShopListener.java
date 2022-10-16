package su.nightexpress.nexshop.shop.virtual;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.shop.chest.event.ChestShopPurchaseEvent;
import su.nightexpress.nexshop.api.shop.virtual.IProductVirtualPrepared;
import su.nightexpress.nexshop.api.shop.virtual.event.VirtualShopPurchaseEvent;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.object.ShopUser;

public class VirtualShopListener extends AbstractListener<ExcellentShop> {

    private final VirtualShop virtualShop;

    public VirtualShopListener(@NotNull VirtualShop virtualShop) {
        super(virtualShop.plugin());
        this.virtualShop = virtualShop;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShopPurchase(VirtualShopPurchaseEvent e) {
        Player player = e.getPlayer();
        IProductVirtualPrepared prepared = (IProductVirtualPrepared) e.getPrepared();

        if (e.isCancelled()) {
            if (e.getResult() == Result.TOO_EXPENSIVE) {
                plugin.getMessage(Lang.Shop_Product_Error_TooExpensive)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            else if (e.getResult() == Result.NOT_ENOUGH_ITEMS) {
                plugin.getMessage(Lang.Shop_Product_Error_NotEnoughItems)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            else if (e.getResult() == ChestShopPurchaseEvent.Result.OUT_OF_MONEY) {
                plugin.getMessage(Lang.Shop_Product_Error_OutOfFunds)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            MessageUtil.sound(player, Config.SOUND_PURCHASE_FAILURE);
            return;
        }

        this.virtualShop.getLogger().logTransaction(e);

        ShopUser user = plugin.getUserManager().getUserData(player);
        user.addVirtualProductLimit(prepared, e.getTradeType());

        MessageUtil.sound(player, Config.SOUND_PURCHASE_SUCCESS);
        (e.getTradeType() == TradeType.BUY ? plugin.getMessage(Lang.Virtual_Shop_Product_Purchase_Buy) : plugin.getMessage(Lang.Virtual_Shop_Product_Purchase_Sell))
                .replace(prepared.replacePlaceholders())
                .send(player);
    }
}
