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
import su.nightexpress.nexshop.shop.auction.config.AuctionLang;

public class AuctionListener extends AbstractListener<ExcellentShop> {

    private final AuctionManager auctionManager;

    public AuctionListener(@NotNull AuctionManager auctionManager) {
        super(auctionManager.plugin());
        this.auctionManager = auctionManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSellerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        int unclaimed = this.auctionManager.getUnclaimedListings(player).size();
        int expired = this.auctionManager.getExpiredListings(player).size();

        if (unclaimed > 0) {
            this.plugin.getMessage(AuctionLang.NOTIFY_LISTING_UNCLAIMED)
                .replace(Placeholders.GENERIC_AMOUNT, unclaimed)
                .send(player);
        }
        if (expired > 0) {
            this.plugin.getMessage(AuctionLang.NOTIFY_LISTING_EXPIRED)
                .replace(Placeholders.GENERIC_AMOUNT, expired)
                .send(player);
        }
    }
}
