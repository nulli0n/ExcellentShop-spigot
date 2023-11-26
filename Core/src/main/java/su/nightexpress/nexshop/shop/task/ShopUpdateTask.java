package su.nightexpress.nexshop.shop.task;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;

import java.util.HashSet;
import java.util.Set;

public class ShopUpdateTask extends AbstractTask<ExcellentShop> {

    public ShopUpdateTask(@NotNull ExcellentShop plugin) {
        super(plugin, 60, true);
    }

    @Override
    public void action() {
        Set<Shop> shops = new HashSet<>();

        VirtualShopModule virtualShopModule = plugin.getVirtualShop();
        if (virtualShopModule != null) {
            virtualShopModule.getShops().forEach(shop -> {
                shops.add(shop);

                if (shop instanceof StaticShop staticShop) {
                    staticShop.getDiscountConfigs().forEach(discount -> {
                        if (discount.isDiscountTime()) {
                            discount.update();
                        }
                    });
                }
                else if (shop instanceof RotatingShop rotatingShop) {
                    rotatingShop.tryRotate();
                }
            });
        }

        ChestShopModule chestShopModule = plugin.getChestShop();
        if (chestShopModule != null) {
            shops.addAll(chestShopModule.getShops());
        }

        shops.forEach(shop -> shop.getPricer().refreshPrices());
    }
}
