package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.listing.ActiveListing;

import java.util.List;
import java.util.UUID;

public class AuctionSellingMenu extends AbstractAuctionMenu<ActiveListing>  {

    public AuctionSellingMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);

        this.load();
    }

    @Override
    @NotNull
    public List<ActiveListing> getObjects(@NotNull Player player) {
        UUID id = this.seeOthers.getOrDefault(player, player.getUniqueId());
        return this.auctionManager.getActiveListings(id);
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull ActiveListing item) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            if (event.isRightClick() || PlayerUtil.isBedrockPlayer(player)) {
                this.auctionManager.takeListing(player, item);
                this.openNextTick(viewer, viewer.getPage());
            }
        };
    }
}
