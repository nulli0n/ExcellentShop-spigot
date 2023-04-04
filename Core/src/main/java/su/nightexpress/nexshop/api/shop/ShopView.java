package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nightexpress.nexshop.ExcellentShop;

public abstract class ShopView<
    S extends Shop<S, P>,
    P extends Product<P, S, ?>
    > extends ConfigMenu<ExcellentShop> {

    protected final S shop;

    public ShopView(@NotNull S shop, @NotNull JYML cfg) {
        super(shop.plugin(), cfg);
        this.shop = shop;
    }

    @NotNull
    public S getShop() {
        return this.shop;
    }
}
