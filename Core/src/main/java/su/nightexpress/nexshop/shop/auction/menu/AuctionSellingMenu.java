package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.listing.AuctionListing;

import java.util.List;
import java.util.UUID;

public class AuctionSellingMenu extends AbstractAuctionMenu<AuctionListing>  {

    public AuctionSellingMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
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
    protected List<AuctionListing> getObjects(@NotNull Player player) {
        UUID id = this.seeOthers.getOrDefault(player, player.getUniqueId());
        return this.auctionManager.getActiveListings(id);
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull AuctionListing item) {
        return (player1, type, e) -> {
            if (e.isRightClick() || PlayerUtil.isBedrockPlayer(player1)) {
                this.auctionManager.takeListing(player1, item);
                this.open(player1, this.getPage(player1));
            }
        };
    }
}
