package su.nightexpress.nexshop.shop.virtual.task;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.shop.RotatingShop;

public class RotationCheckTask extends AbstractTask<ExcellentShop> {

    private final VirtualShopModule module;

    public RotationCheckTask(@NotNull VirtualShopModule module) {
        super(module.plugin(), 60, true);
        this.module = module;
    }

    @Override
    public void action() {
        this.module.getRotatingShops().forEach(RotatingShop::tryRotate);
    }
}
