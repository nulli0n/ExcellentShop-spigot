package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.listing.AuctionCompletedListing;

import java.util.List;
import java.util.UUID;

public class AuctionUnclaimedMenu extends AbstractAuctionMenu<AuctionCompletedListing> {

    public AuctionUnclaimedMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);

        this.load();
    }

    @Override
    @NotNull
    public List<AuctionCompletedListing> getObjects(@NotNull Player player) {
        UUID id = this.seeOthers.getOrDefault(player, player.getUniqueId());
        return this.auctionManager.getUnclaimedListings(id);
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull AuctionCompletedListing listing) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            this.auctionManager.claimRewards(player, listing);
            this.plugin.runTask(task -> {
                this.open(player, viewer.getPage(), this.seeOthers.getOrDefault(player, player.getUniqueId()));
            });
        };
    }
}
