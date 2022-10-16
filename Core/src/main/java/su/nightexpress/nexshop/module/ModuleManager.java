package su.nightexpress.nexshop.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.api.module.AbstractModuleManager;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

public class ModuleManager extends AbstractModuleManager<ExcellentShop> {

    private VirtualShop    virtualShop;
    private ChestShop      chestShop;
    private AuctionManager auctionManager;

    public ModuleManager(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    public void loadModules() {
        this.virtualShop = this.register(new VirtualShop(plugin));
        this.chestShop = this.register(new ChestShop(plugin));
        this.auctionManager = this.register(new AuctionManager(plugin));
    }

    @Nullable
    public VirtualShop getVirtualShop() {
        return this.virtualShop;
    }

    @Nullable
    public ChestShop getChestShop() {
        return this.chestShop;
    }

    @Nullable
    public AuctionManager getAuctionManager() {
        return this.auctionManager;
    }
}
