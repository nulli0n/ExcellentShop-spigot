package su.nightexpress.nexshop.auction.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.listing.ActiveListing;

import java.util.List;
import java.util.UUID;

public class AuctionExpiredMenu extends AbstractAuctionMenu<ActiveListing> {

    public AuctionExpiredMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);

        this.registerHandler(ItemType.class)
            .addClick(ItemType.TAKE_ALL, (viewer, event) -> {
                Player player = viewer.getPlayer();
                this.auctionManager.getExpiredListings(player).forEach(listing -> {
                    this.auctionManager.takeListing(player, listing);
                });
                this.openNextTick(viewer, viewer.getPage());
            });

        this.load();
    }

    private enum ItemType {
        TAKE_ALL
    }

    @Override
    @NotNull
    public List<ActiveListing> getObjects(@NotNull Player player) {
        UUID id = this.seeOthers.getOrDefault(player, player.getUniqueId());
        return this.auctionManager.getExpiredListings(id);
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull ActiveListing item) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            this.auctionManager.takeListing(player, item);
            this.openNextTick(viewer, viewer.getPage());
        };
    }
}
