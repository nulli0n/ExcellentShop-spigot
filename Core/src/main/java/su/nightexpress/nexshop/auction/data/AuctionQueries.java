package su.nightexpress.nexshop.auction.data;

import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nightcore.db.sql.query.impl.DeleteQuery;
import su.nightexpress.nightcore.db.sql.query.impl.InsertQuery;
import su.nightexpress.nightcore.db.sql.query.impl.UpdateQuery;
import su.nightexpress.nightcore.db.sql.util.WhereOperator;

public class AuctionQueries {

    public static final DeleteQuery<AbstractListing> LISTING_DELETE_QUERY = new DeleteQuery<AbstractListing>()
        .whereIgnoreCase(AuctionDatabase.COLUMN_ID, WhereOperator.EQUAL, listing -> listing.getId().toString());

    public static final InsertQuery<ActiveListing> ACTIVE_LISTING_INSERT_QUERY = new InsertQuery<ActiveListing>()
        .setValue(AuctionDatabase.COLUMN_ID, listing -> listing.getId().toString())
        .setValue(AuctionDatabase.COLUMN_OWNER, listing -> listing.getOwner().toString())
        .setValue(AuctionDatabase.COLUMN_OWNER_NAME, AbstractListing::getOwnerName)
        .setValue(AuctionDatabase.COLUMN_ITEM, listing -> listing.getTyping().serialize())
        .setValue(AuctionDatabase.COLUMN_HANDLER, listing -> listing.getTyping().type().name())
        .setValue(AuctionDatabase.COLUMN_CURRENCY, listing -> listing.getCurrency().getInternalId())
        .setValue(AuctionDatabase.COLUMN_PRICE, listing -> String.valueOf(listing.getPrice()))
        .setValue(AuctionDatabase.COLUMN_EXPIRE_DATE, listing -> String.valueOf(listing.getExpireDate()))
        .setValue(AuctionDatabase.COLUMN_DELETE_DATE, listing -> String.valueOf(listing.getDeleteDate()))
        .setValue(AuctionDatabase.COLUMN_DATE_CREATION, listing -> String.valueOf(listing.getCreationDate()));

    public static final InsertQuery<CompletedListing> COMPLETED_LISTING_INSERT_QUERY = new InsertQuery<CompletedListing>()
        .setValue(AuctionDatabase.COLUMN_ID, listing -> listing.getId().toString())
        .setValue(AuctionDatabase.COLUMN_OWNER, listing -> listing.getOwner().toString())
        .setValue(AuctionDatabase.COLUMN_OWNER_NAME, AbstractListing::getOwnerName)
        .setValue(AuctionDatabase.COLUMN_BUYER_NAME, CompletedListing::getBuyerName)
        .setValue(AuctionDatabase.COLUMN_ITEM, listing -> listing.getTyping().serialize())
        .setValue(AuctionDatabase.COLUMN_HANDLER, listing -> listing.getTyping().type().name())
        .setValue(AuctionDatabase.COLUMN_CURRENCY, listing -> listing.getCurrency().getInternalId())
        .setValue(AuctionDatabase.COLUMN_PRICE, listing -> String.valueOf(listing.getPrice()))
        .setValue(AuctionDatabase.COLUMN_IS_PAID, listing -> String.valueOf(listing.isClaimed() ? 1 : 0))
        .setValue(AuctionDatabase.COLUMN_BUY_DATE, listing -> String.valueOf(listing.getBuyDate()))
        .setValue(AuctionDatabase.COLUMN_DELETE_DATE, listing -> String.valueOf(listing.getDeleteDate()))
        .setValue(AuctionDatabase.COLUMN_DATE_CREATION, listing -> String.valueOf(listing.getCreationDate()));

    public static final UpdateQuery<CompletedListing> COMPLETED_LISTING_UPDATE_QUERY = new UpdateQuery<CompletedListing>()
        .setValue(AuctionDatabase.COLUMN_IS_PAID, listing -> String.valueOf(listing.isClaimed() ? 1 : 0))
        .whereIgnoreCase(AuctionDatabase.COLUMN_ID, WhereOperator.EQUAL, listing -> listing.getId().toString());
}
