package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.config.AuctionLang;
import su.nightexpress.nexshop.shop.auction.listing.AuctionCompletedListing;

import java.util.List;
import java.util.UUID;

public class AuctionUnclaimedMenu extends AbstractAuctionMenu<AuctionCompletedListing> {

    public AuctionUnclaimedMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);

        MenuClick click = (p, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    this.auctionManager.getMainMenu().open(p, 1);
                }
                else this.onItemClickDefault(p, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    @NotNull
    protected List<AuctionCompletedListing> getObjects(@NotNull Player player) {
        UUID id = this.seeOthers.getOrDefault(player, player.getUniqueId());
        return this.auctionManager.getUnclaimedListings(id);
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull AuctionCompletedListing listing) {
        return (player1, type, e) -> {
            listing.getCurrency().give(player, listing.getPrice());
            listing.setRewarded(true);

            this.auctionManager.getDataHandler().saveCompletedListing(listing, true);
            this.open(player, this.getPage(player), this.seeOthers.getOrDefault(player, player.getUniqueId()));
            this.plugin.getMessage(AuctionLang.NOTIFY_LISTING_CLAIM)
                .replace(listing.replacePlaceholders())
                .send(player1);
        };
    }
}
