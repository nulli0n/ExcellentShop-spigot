package su.nightexpress.nexshop.shop.auction.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.shop.auction.config.AuctionLang;
import su.nightexpress.nexshop.shop.auction.listing.CompletedListing;

import java.util.List;

public class AuctionListener extends AbstractListener<ExcellentShop> {

    private final AuctionManager auctionManager;

    public AuctionListener(@NotNull AuctionManager auctionManager) {
        super(auctionManager.plugin());
        this.auctionManager = auctionManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSellerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        List<CompletedListing> unclaimed = this.auctionManager.getUnclaimedListings(player);
        int expired = this.auctionManager.getExpiredListings(player).size();

        if (unclaimed.size() > 0) {
            if (AuctionConfig.LISINGS_AUTO_CLAIM.get()) {
                this.auctionManager.claimRewards(player, unclaimed);
            }
            else {
                this.plugin.getMessage(AuctionLang.NOTIFY_LISTING_UNCLAIMED)
                    .replace(Placeholders.GENERIC_AMOUNT, unclaimed.size())
                    .send(player);
            }
        }
        if (expired > 0) {
            this.plugin.getMessage(AuctionLang.NOTIFY_LISTING_EXPIRED)
                .replace(Placeholders.GENERIC_AMOUNT, expired)
                .send(player);
        }
    }
}
