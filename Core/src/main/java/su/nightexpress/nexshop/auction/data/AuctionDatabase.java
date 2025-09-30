package su.nightexpress.nexshop.auction.data;

import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.db.AbstractDataManager;
import su.nightexpress.nightcore.db.config.DatabaseConfig;
import su.nightexpress.nightcore.db.sql.column.Column;
import su.nightexpress.nightcore.db.sql.column.ColumnType;
import su.nightexpress.nightcore.db.sql.query.SQLQueries;
import su.nightexpress.nightcore.db.sql.query.impl.DeleteQuery;
import su.nightexpress.nightcore.db.sql.query.impl.InsertQuery;
import su.nightexpress.nightcore.db.sql.query.impl.SelectQuery;
import su.nightexpress.nightcore.db.sql.util.WhereOperator;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class AuctionDatabase extends AbstractDataManager<ShopPlugin> {

    static final Column COLUMN_ID            = Column.of("aucId", ColumnType.STRING);
    static final Column COLUMN_OWNER         = Column.of("owner", ColumnType.STRING);
    static final Column COLUMN_OWNER_NAME    = Column.of("ownerName", ColumnType.STRING);
    static final Column COLUMN_BUYER_NAME    = Column.of("buyerName", ColumnType.STRING);
    static final Column COLUMN_CURRENCY      = Column.of("currency", ColumnType.STRING);
    static final Column COLUMN_PRICE         = Column.of("price", ColumnType.DOUBLE);
    static final Column COLUMN_ITEM_NEW  = Column.of("item", ColumnType.STRING);
    static final Column COLUMN_EXPIRE_DATE   = Column.of("expireDate", ColumnType.LONG);
    static final Column COLUMN_DELETE_DATE   = Column.of("deleteDate", ColumnType.LONG);
    static final Column COLUMN_BUY_DATE      = Column.of("buyDate", ColumnType.LONG);
    static final Column COLUMN_DATE_CREATION = Column.of("dateCreation", ColumnType.LONG);
    static final Column COLUMN_IS_PAID       = Column.of("isPaid", ColumnType.BOOLEAN);

    private final AuctionManager manager;

    private final String tableListings;
    private final String tableCompletedListings;

    public AuctionDatabase(@NotNull ShopPlugin plugin, @NotNull AuctionManager manager, @NotNull FileConfig config) {
        super(plugin, DatabaseConfig.read(config, "excellentshop_auction"));
        this.manager = manager;

        this.tableListings = this.getTablePrefix() + "_items";
        this.tableCompletedListings = this.getTablePrefix() + "_history";
    }

    @Override
    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        return builder;
    }

    @Override
    protected void onInitialize() {
        // Create auction items table
        this.createTable(this.tableListings, Arrays.asList(
            COLUMN_ID, COLUMN_OWNER, COLUMN_OWNER_NAME,
            COLUMN_ITEM_NEW, COLUMN_CURRENCY, COLUMN_PRICE,
            COLUMN_EXPIRE_DATE, COLUMN_DELETE_DATE, COLUMN_DATE_CREATION
        ));

        // Create auction history table
        this.createTable(this.tableCompletedListings, Arrays.asList(
            COLUMN_ID, COLUMN_OWNER, COLUMN_OWNER_NAME, COLUMN_BUYER_NAME,
            COLUMN_ITEM_NEW, COLUMN_CURRENCY, COLUMN_PRICE, COLUMN_IS_PAID,
            COLUMN_BUY_DATE, COLUMN_DATE_CREATION, COLUMN_DELETE_DATE
        ));

        this.addColumn(this.tableListings, COLUMN_CURRENCY, this.manager.getDefaultCurrency().getInternalId());
        this.addColumn(this.tableListings, COLUMN_DATE_CREATION, String.valueOf(System.currentTimeMillis()));

        this.addColumn(this.tableCompletedListings, COLUMN_CURRENCY, this.manager.getDefaultCurrency().getInternalId());
        this.addColumn(this.tableCompletedListings, COLUMN_DATE_CREATION, String.valueOf(System.currentTimeMillis()));

        this.updateListings(this.tableListings, AuctionLegacyQueries.ACTIVE_LISTING_LOADER, AuctionLegacyQueries.ACTIVE_LISTING_LOADER_2, AuctionQueries.ACTIVE_LISTING_INSERT_QUERY);
        this.updateListings(this.tableCompletedListings, AuctionLegacyQueries.COMPLETED_LISTING_LOADER, AuctionLegacyQueries.COMPLETED_LISTING_LOADER_2, AuctionQueries.COMPLETED_LISTING_INSERT_QUERY);

        if (SQLQueries.hasTable(this.connector, this.tableCompletedListings)) {
            DeleteQuery<Long> query = new DeleteQuery<Long>().where(COLUMN_DELETE_DATE, WhereOperator.SMALLER, String::valueOf);
            this.delete(this.tableCompletedListings, query, System.currentTimeMillis());
        }

        if (SQLQueries.hasTable(this.connector, this.tableListings)) {
            DeleteQuery<Long> query = new DeleteQuery<Long>().where(COLUMN_DELETE_DATE, WhereOperator.SMALLER, String::valueOf);
            this.delete(this.tableListings, query, System.currentTimeMillis());
        }
    }

    private <T extends AbstractListing> void updateListings(@NotNull String table,
                                                            @NotNull Function<ResultSet, T> function1,
                                                            @NotNull Function<ResultSet, T> function2,
                                                            @NotNull InsertQuery<T> insertQuery) {
        if (SQLQueries.hasColumn(this.connector, table, COLUMN_ITEM_NEW)) return;

        boolean isAlreadyUpdated = SQLQueries.hasColumn(this.connector, table, AuctionLegacyQueries.COLUMN_ITEM_DATA);

        this.addColumn(table, COLUMN_ITEM_NEW, "{}");

        SelectQuery<T> query = new SelectQuery<>(isAlreadyUpdated ? function2 : function1).all();
        List<T> listings = this.select(table, query);

        String deleteSql = "DELETE FROM " + table;
        SQLQueries.executeSimpleQuery(this.connector, deleteSql);

        this.dropColumn(table, AuctionLegacyQueries.COLUMN_ITEM);
        this.dropColumn(table, AuctionLegacyQueries.COLUMN_HANDLER);
        this.dropColumn(table, AuctionLegacyQueries.COLUMN_ITEM_DATA);

        this.insert(table, insertQuery, listings);
    }

    @Override
    protected void onClose() {

    }

    @Override
    public void onPurge() {

    }

    @Override
    public void onSynchronize() {
        List<ActiveListing> listings = this.getListings();
        List<CompletedListing> completedListings = this.getCompletedListings();

        this.manager.getListings().clear();

        listings.forEach(listing -> this.manager.getListings().add(listing));
        completedListings.forEach(listing -> this.manager.getListings().addCompleted(listing));
    }

    @NotNull
    public List<ActiveListing> getListings() {
        SelectQuery<ActiveListing> query = new SelectQuery<>(AuctionQueries.ACTIVE_LISTING_LOADER).all();
        return this.select(this.tableListings, query);
    }

    @NotNull
    public List<CompletedListing> getCompletedListings() {
        SelectQuery<CompletedListing> query = new SelectQuery<>(AuctionQueries.COMPLETED_LISTING_LOADER).all();
        return this.select(this.tableCompletedListings, query);
    }

    public void addListing(@NotNull ActiveListing listing) {
        this.insert(this.tableListings, AuctionQueries.ACTIVE_LISTING_INSERT_QUERY, listing);
    }

    public void deleteListing(@NotNull ActiveListing listing) {
        this.delete(this.tableListings, AuctionQueries.LISTING_DELETE_QUERY, listing);
    }

    public void addCompletedListing(@NotNull CompletedListing listing) {
        this.insert(this.tableCompletedListings, AuctionQueries.COMPLETED_LISTING_INSERT_QUERY, listing);
    }

    public void saveCompletedListings(@NotNull List<CompletedListing> listings) {
        this.update(this.tableCompletedListings, AuctionQueries.COMPLETED_LISTING_UPDATE_QUERY, listings);
    }

    public void saveCompletedListing(@NotNull CompletedListing listing) {
        this.update(this.tableCompletedListings, AuctionQueries.COMPLETED_LISTING_UPDATE_QUERY, listing);
    }

    public void deleteCompletedListing(@NotNull CompletedListing listing) {
        this.delete(this.tableCompletedListings, AuctionQueries.LISTING_DELETE_QUERY, listing);
    }

    public boolean isListingExist(@NotNull UUID id) {
        return this.contains(this.tableListings, query -> query
            .column(COLUMN_ID)
            .whereIgnoreCase(COLUMN_ID, WhereOperator.EQUAL, id.toString())
        );
    }

    public boolean isCompletedListingExist(@NotNull UUID id) {
        return this.contains(this.tableCompletedListings, query -> query
            .column(COLUMN_ID)
            .whereIgnoreCase(COLUMN_ID, WhereOperator.EQUAL, id.toString())
        );
    }

    public boolean isCompletedListingClaimed(@NotNull UUID id) {
        return this.contains(this.tableCompletedListings, query -> query
            .column(COLUMN_ID)
            .column(COLUMN_IS_PAID)
            .whereIgnoreCase(COLUMN_ID, WhereOperator.EQUAL, id.toString())
            .where(COLUMN_IS_PAID, WhereOperator.EQUAL, "1")
        );
    }
}
