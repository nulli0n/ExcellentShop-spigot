package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.object.AuctionListing;

import java.util.List;
import java.util.UUID;

public class AuctionExpiredMenu extends AbstractAuctionMenu<AuctionListing> {

    public AuctionExpiredMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);

        IMenuClick click = (player, type, e) -> {

            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    this.auctionManager.getAuctionMainMenu().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof ItemType type2) {
                if (type2 == ItemType.AUCTION_EXPIRED_TAKE_ALL) {
                    this.auctionManager.getExpired(player).forEach(listing -> {
                        this.auctionManager.takeExpired(player, listing);
                    });
                    this.open(player, 1);
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Special")) {
            IMenuItem menuItem = cfg.getMenuItem("Special." + sId, ItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    private enum ItemType {
        AUCTION_EXPIRED_TAKE_ALL
    }

    @Override
    @NotNull
    protected List<AuctionListing> getObjects(@NotNull Player player) {
        UUID id = this.seeOthers.getOrDefault(player, player.getUniqueId());
        return this.auctionManager.getExpired(id);
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull AuctionListing item) {
        return (player1, type, e) -> {
            this.auctionManager.takeExpired(player1, item);
            this.open(player1, this.getPage(player1));
        };
    }
}
