package su.nightexpress.nexshop.shop.price;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

import java.util.HashSet;
import java.util.Set;

public class PriceUpdateTask extends AbstractTask<ExcellentShop> {

    public PriceUpdateTask(@NotNull ExcellentShop plugin) {
        super(plugin, 60, true);
    }

    @Override
    public void action() {
        Set<Product<?, ?, ?>> products = new HashSet<>();

        VirtualShopModule module = plugin.getVirtualShop();
        if (module != null) {
            module.getShops().forEach(shop -> {
                products.addAll(shop.getProducts());

                shop.getDiscountConfigs().forEach(discount -> {
                    if (discount.isDiscountTime()) {
                        discount.update();
                    }
                });
            });
        }

        ChestShopModule chestShopModule = plugin.getChestShop();
        if (chestShopModule != null) {
            chestShopModule.getShops().forEach(shop -> products.addAll(shop.getProducts()));
        }

        products.forEach(product -> {
            if (product.getPricer() instanceof FloatProductPricer pricer && pricer.isUpdateTime()) {
                pricer.randomize();
            }
        });
    }
}
