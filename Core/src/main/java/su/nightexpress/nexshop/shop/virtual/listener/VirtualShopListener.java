package su.nightexpress.nexshop.shop.virtual.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.event.ShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.event.VirtualShopPurchaseEvent;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;

public class VirtualShopListener extends AbstractListener<ExcellentShop> {

    private final VirtualShopModule virtualShop;

    public VirtualShopListener(@NotNull VirtualShopModule virtualShop) {
        super(virtualShop.plugin());
        this.virtualShop = virtualShop;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShopPurchase(VirtualShopPurchaseEvent e) {
        Player player = e.getPlayer();
        PreparedProduct<VirtualProduct> prepared = e.getPrepared();

        if (e.isCancelled()) {
            if (e.getResult() == Result.TOO_EXPENSIVE) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_TOO_EXPENSIVE)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            else if (e.getResult() == Result.NOT_ENOUGH_ITEMS) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_NOT_ENOUGH_ITEMS)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            else if (e.getResult() == Result.OUT_OF_MONEY) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_FUNDS)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            else if (e.getResult() == Result.OUT_OF_STOCK) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK)
                    .replace(prepared.replacePlaceholders())
                    .send(player);
            }
            MessageUtil.sound(player, Config.SOUND_PURCHASE_FAILURE);
            return;
        }
        this.virtualShop.getLogger().logTransaction(e);

        MessageUtil.sound(player, Config.SOUND_PURCHASE_SUCCESS);
        plugin.getMessage(e.getTradeType() == TradeType.BUY ? VirtualLang.PRODUCT_PURCHASE_BUY : VirtualLang.PRODUCT_PURCHASE_SELL)
            .replace(prepared.replacePlaceholders())
            .send(player);
    }
}
