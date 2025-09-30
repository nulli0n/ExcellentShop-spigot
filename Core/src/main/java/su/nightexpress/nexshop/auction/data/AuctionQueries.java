package su.nightexpress.nexshop.auction.data;

import com.google.gson.reflect.TypeToken;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.db.sql.query.impl.DeleteQuery;
import su.nightexpress.nightcore.db.sql.query.impl.InsertQuery;
import su.nightexpress.nightcore.db.sql.query.impl.UpdateQuery;
import su.nightexpress.nightcore.db.sql.util.WhereOperator;
import su.nightexpress.nightcore.integration.currency.CurrencyId;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Function;

public class AuctionQueries {

    public static final Function<ResultSet, ActiveListing> ACTIVE_LISTING_LOADER = resultSet -> {
        try {
            UUID id = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_ID.getName()));
            UUID owner = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_OWNER.getName()));
            String ownerName = resultSet.getString(AuctionDatabase.COLUMN_OWNER_NAME.getName());

            ItemContent type = DataHandler.GSON.fromJson(resultSet.getString(AuctionDatabase.COLUMN_ITEM_NEW.getName()), new TypeToken<ItemContent>(){}.getType());

            String currencyId = resultSet.getString(AuctionDatabase.COLUMN_CURRENCY.getName());
            Currency currency = currencyId == null ? ShopAPI.getAuctionManager().getDefaultCurrency() : EconomyBridge.getCurrency(CurrencyId.reroute(currencyId));
            if (currency == null) return null;

            double price = resultSet.getDouble(AuctionDatabase.COLUMN_PRICE.getName());
            long expireDate = resultSet.getLong(AuctionDatabase.COLUMN_EXPIRE_DATE.getName());
            long dateCreation = resultSet.getLong(AuctionDatabase.COLUMN_DATE_CREATION.getName());
            long deletionDate = resultSet.getLong(AuctionDatabase.COLUMN_DELETE_DATE.getName());
            if (deletionDate == 0L) {
                deletionDate = AuctionUtils.generatePurgeDate(dateCreation);
            }

            return new ActiveListing(id, owner, ownerName, type, currency, price, dateCreation, expireDate, deletionDate);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final Function<ResultSet, CompletedListing> COMPLETED_LISTING_LOADER = resultSet -> {
        try {
            UUID id = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_ID.getName()));
            UUID owner = UUID.fromString(resultSet.getString(AuctionDatabase.COLUMN_OWNER.getName()));
            String ownerName = resultSet.getString(AuctionDatabase.COLUMN_OWNER_NAME.getName());
            String buyerName = resultSet.getString(AuctionDatabase.COLUMN_BUYER_NAME.getName());

            ItemContent type = DataHandler.GSON.fromJson(resultSet.getString(AuctionDatabase.COLUMN_ITEM_NEW.getName()), new TypeToken<ItemContent>(){}.getType());

            String currencyId = resultSet.getString(AuctionDatabase.COLUMN_CURRENCY.getName());
            Currency currency = currencyId == null ? ShopAPI.getAuctionManager().getDefaultCurrency() : EconomyBridge.getCurrency(CurrencyId.reroute(currencyId));
            if (currency == null) return null;

            double price = resultSet.getDouble(AuctionDatabase.COLUMN_PRICE.getName());
            boolean isNotified = resultSet.getBoolean(AuctionDatabase.COLUMN_IS_PAID.getName());
            long buyDate = resultSet.getLong(AuctionDatabase.COLUMN_BUY_DATE.getName());
            long dateCreation = resultSet.getLong(AuctionDatabase.COLUMN_DATE_CREATION.getName());
            long deletionDate = resultSet.getLong(AuctionDatabase.COLUMN_DELETE_DATE.getName());
            if (deletionDate == 0L) {
                deletionDate = AuctionUtils.generatePurgeDate(dateCreation);
            }

            return new CompletedListing(id, owner, ownerName, buyerName, type, currency, price, dateCreation, buyDate, deletionDate, isNotified);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final DeleteQuery<AbstractListing> LISTING_DELETE_QUERY = new DeleteQuery<AbstractListing>()
        .whereIgnoreCase(AuctionDatabase.COLUMN_ID, WhereOperator.EQUAL, listing -> listing.getId().toString());

    public static final InsertQuery<ActiveListing> ACTIVE_LISTING_INSERT_QUERY = new InsertQuery<ActiveListing>()
        .setValue(AuctionDatabase.COLUMN_ID, listing -> listing.getId().toString())
        .setValue(AuctionDatabase.COLUMN_OWNER, listing -> listing.getOwner().toString())
        .setValue(AuctionDatabase.COLUMN_OWNER_NAME, AbstractListing::getOwnerName)
        .setValue(AuctionDatabase.COLUMN_ITEM_NEW, listing -> DataHandler.GSON.toJson(listing.getTyping()))
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
        .setValue(AuctionDatabase.COLUMN_ITEM_NEW, listing -> DataHandler.GSON.toJson(listing.getTyping()))
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
