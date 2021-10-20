package su.nightexpress.nexshop.shop.auction.compatibility;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.modules.Listings;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.shop.auction.AuctionConfig;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.object.AuctionListing;

import java.util.UUID;

public class ImportAuctionHouse {

    public static void importData(@NotNull AuctionManager auc) {
        Listings listings = AuctionHouse.listings;

        long expireDate = System.currentTimeMillis() + AuctionConfig.STORAGE_EXPIRE_IN;
        long deleteDate = System.currentTimeMillis() + AuctionConfig.STORAGE_DELETE_EXPIRED;

        listings.getListings().values().forEach(listing -> {
            UUID id = UUID.randomUUID();
            UUID owner = UUID.fromString(listing.getSeller_UUID());
            String ownerName = listing.getSellerName();
            ItemStack itemStack = listing.getItem();
            double price = listing.getPrice();

            AuctionListing aucListing = new AuctionListing(id, owner, ownerName, itemStack, price, expireDate, deleteDate);
            auc.getListings().add(aucListing);

            auc.plugin().getData().addAuctionListing(aucListing, false);
        });
    }
}
