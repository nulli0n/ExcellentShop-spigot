package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.*;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.listing.AuctionListing;

import java.util.List;
import java.util.UUID;

public class AuctionExpiredMenu extends AbstractAuctionMenu<AuctionListing> {

    public AuctionExpiredMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);

        MenuClick click = (player, type, e) -> {

            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    this.auctionManager.getMainMenu().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof ItemType type2) {
                if (type2 == ItemType.TAKE_ALL) {
                    this.auctionManager.getExpiredListings(player).forEach(listing -> {
                        this.auctionManager.takeListing(player, listing);
                    });
                    this.open(player, 1);
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Special")) {
            MenuItem menuItem = cfg.getMenuItem("Special." + sId, ItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    private enum ItemType {
        TAKE_ALL
    }

    @Override
    @NotNull
    protected List<AuctionListing> getObjects(@NotNull Player player) {
        UUID id = this.seeOthers.getOrDefault(player, player.getUniqueId());
        return this.auctionManager.getExpiredListings(id);
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull AuctionListing item) {
        return (player1, type, e) -> {
            this.auctionManager.takeListing(player1, item);
            this.open(player1, this.getPage(player1));
        };
    }
}
