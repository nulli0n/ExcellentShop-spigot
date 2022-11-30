package su.nightexpress.nexshop.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.api.module.AbstractModuleManager;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

public class ModuleManager extends AbstractModuleManager<ExcellentShop> {

    private VirtualShopModule virtualShop;
    private ChestShopModule   chestShop;
    private AuctionManager    auctionManager;

    public ModuleManager(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    public void loadModules() {
        this.virtualShop = this.register(new VirtualShopModule(plugin));
        this.chestShop = this.register(new ChestShopModule(plugin));
        this.auctionManager = this.register(new AuctionManager(plugin));
    }

    @Nullable
    public VirtualShopModule getVirtualShop() {
        return this.virtualShop;
    }

    @Nullable
    public ChestShopModule getChestShop() {
        return this.chestShop;
    }

    @Nullable
    public AuctionManager getAuctionManager() {
        return this.auctionManager;
    }
}
