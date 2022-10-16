package su.nightexpress.nexshop.shop.chest.task;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.chest.ChestShop;

public class ChestProductTask extends AbstractTask<ExcellentShop> {

    private final ChestShop chestShop;

    public ChestProductTask(@NotNull ChestShop chestShop) {
        super(chestShop.plugin(), 60, true);
        this.chestShop = chestShop;
    }

    @Override
    public void action() {
        this.chestShop.getShops().forEach(shop -> {
            shop.getProducts().forEach(product -> product.getPricer().randomizePrices());
        });
    }
}
