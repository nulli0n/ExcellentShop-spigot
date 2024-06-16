package su.nightexpress.nexshop.auction;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nexshop.shop.util.ShopUtils;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

public class Placeholders extends su.nightexpress.nexshop.Placeholders {

    public static final String LISTING_ITEM_NAME     = "%listing_item_name%";
    public static final String LISTING_ITEM_LORE     = "%listing_item_lore%";
    public static final String LISTING_ITEM_AMOUNT   = "%listing_item_amount%";
    public static final String LISTING_ITEM_VALUE    = "%listing_item_value%";
    public static final String LISTING_SELLER        = "%listing_seller%";
    public static final String LISTING_PRICE         = "%listing_price%";
    public static final String LISTING_DATE_CREATION = "%listing_date_creation%";
    public static final String LISTING_DELETES_IN    = "%listing_deletes_in%";
    public static final String LISTING_DELETE_DATE   = "%listing_delete_date%";
    public static final String LISTING_EXPIRES_IN    = "%listing_expires_in%";
    public static final String LISTING_EXPIRE_DATE   = "%listing_expire_date%";
    public static final String LISTING_BUYER         = "%listing_buyer%";
    public static final String LISTING_BUY_DATE      = "%listing_buy_date%";

    public static final String CATEGORY_ID   = "%category_id%";
    public static final String CATEGORY_NAME = "%category_name%";

    @NotNull
    public static PlaceholderMap forListing(@NotNull AbstractListing listing) {
        return new PlaceholderMap()
            .add(LISTING_SELLER, listing.getOwnerName())
            .add(LISTING_PRICE, listing.getCurrency().format(listing.getPrice()))
            .add(LISTING_DATE_CREATION, ShopUtils.getDateFormatter().format(TimeUtil.getLocalDateTimeOf(listing.getCreationDate())))
            .add(LISTING_ITEM_AMOUNT, String.valueOf(listing.getItemStack().getAmount()))
            .add(LISTING_ITEM_NAME, ItemUtil.getItemName(listing.getItemStack()))
            .add(LISTING_ITEM_LORE, String.join("\n", ItemUtil.getLore(listing.getItemStack())))
            .add(LISTING_ITEM_VALUE, String.valueOf(ItemUtil.compress(listing.getItemStack())))
            .add(LISTING_DELETES_IN, () -> TimeUtil.formatDuration(listing.getDeleteDate()))
            .add(LISTING_DELETE_DATE, ShopUtils.getDateFormatter().format(TimeUtil.getLocalDateTimeOf(listing.getDeleteDate())));
    }

    @NotNull
    public static PlaceholderMap forActiveListing(@NotNull ActiveListing listing) {
        return new PlaceholderMap()
            .add(LISTING_EXPIRES_IN, () -> TimeUtil.formatDuration(listing.getExpireDate()))
            .add(LISTING_EXPIRE_DATE, ShopUtils.getDateFormatter().format(TimeUtil.getLocalDateTimeOf(listing.getExpireDate())));
    }

    @NotNull
    public static PlaceholderMap forCompletedListing(@NotNull CompletedListing listing) {
        return new PlaceholderMap()
            .add(LISTING_BUYER, listing.getBuyerName())
            .add(LISTING_BUY_DATE, ShopUtils.getDateFormatter().format(TimeUtil.getLocalDateTimeOf(listing.getBuyDate())));
    }
}
