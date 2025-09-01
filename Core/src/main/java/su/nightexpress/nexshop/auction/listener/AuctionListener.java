package su.nightexpress.nexshop.auction.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.auction.config.AuctionLang;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nightcore.manager.AbstractListener;

import java.util.List;

public class AuctionListener extends AbstractListener<ShopPlugin> {

    private final AuctionManager auctionManager;

    public AuctionListener(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager) {
        super(plugin);
        this.auctionManager = auctionManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSellerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        List<CompletedListing> unclaimed = this.auctionManager.getListings().getUnclaimed(player);
        int expired = this.auctionManager.getListings().getExpired(player).size();

        if (!unclaimed.isEmpty()) {
            if (AuctionConfig.LISINGS_AUTO_CLAIM.get()) {
                this.auctionManager.claimRewards(player, unclaimed);
            }
            else if (AuctionConfig.NOTIFY_UNCLAIMED_ON_JOIN.get()) {
                AuctionLang.NOTIFY_UNCLAIMED_LISTINGS.message().send(player, replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, unclaimed.size()));
            }
        }
        if (expired > 0 && AuctionConfig.NOTIFY_EXPIRED_ON_JOIN.get()) {
            AuctionLang.NOTIFY_EXPIRED_LISTINGS.message().send(player, replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, expired));
        }
    }
}
