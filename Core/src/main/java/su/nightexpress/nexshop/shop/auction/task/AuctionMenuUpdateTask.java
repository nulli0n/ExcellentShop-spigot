package su.nightexpress.nexshop.shop.auction.task;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.auction.AuctionManager;

public class AuctionMenuUpdateTask extends AbstractTask<ExcellentShop> {

    private final AuctionManager auctionManager;

    public AuctionMenuUpdateTask(@NotNull AuctionManager auctionManager) {
        super(auctionManager.plugin(), 1, false);
        this.auctionManager = auctionManager;
    }

    @Override
    public void action() {
        this.auctionManager.getMainMenu().update();
        this.auctionManager.getExpiredMenu().update();
        this.auctionManager.getUnclaimedMenu().update();
        this.auctionManager.getSellingMenu().update();
    }
}
