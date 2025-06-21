package su.nightexpress.nexshop.auction.data;

import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nexshop.product.type.ProductTypes;
import su.nightexpress.nexshop.product.type.impl.PluginProductType;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.db.AbstractDataManager;
import su.nightexpress.nightcore.db.config.DatabaseConfig;
import su.nightexpress.nightcore.db.sql.column.Column;
import su.nightexpress.nightcore.db.sql.column.ColumnType;
import su.nightexpress.nightcore.db.sql.query.SQLQueries;
import su.nightexpress.nightcore.db.sql.query.impl.DeleteQuery;
import su.nightexpress.nightcore.db.sql.query.impl.SelectQuery;
import su.nightexpress.nightcore.db.sql.util.WhereOperator;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class AuctionDatabase extends AbstractDataManager<ShopPlugin> {

    static final Column COLUMN_ID            = Column.of("aucId", ColumnType.STRING);
    static final Column COLUMN_OWNER         = Column.of("owner", ColumnType.STRING);
    static final Column COLUMN_OWNER_NAME    = Column.of("ownerName", ColumnType.STRING);
    static final Column COLUMN_BUYER_NAME    = Column.of("buyerName", ColumnType.STRING);
    static final Column COLUMN_ITEM          = Column.of("itemStack", ColumnType.STRING);
    static final Column COLUMN_HANDLER       = Column.of("itemHandler", ColumnType.STRING);
    static final Column COLUMN_CURRENCY      = Column.of("currency", ColumnType.STRING);
    static final Column COLUMN_PRICE         = Column.of("price", ColumnType.DOUBLE);
    static final Column COLUMN_EXPIRE_DATE   = Column.of("expireDate", ColumnType.LONG);
    static final Column COLUMN_DELETE_DATE   = Column.of("deleteDate", ColumnType.LONG);
    static final Column COLUMN_BUY_DATE      = Column.of("buyDate", ColumnType.LONG);
    static final Column COLUMN_DATE_CREATION = Column.of("dateCreation", ColumnType.LONG);
    static final Column COLUMN_IS_PAID       = Column.of("isPaid", ColumnType.BOOLEAN);

    private final AuctionManager manager;

    private final String tableListings;
    private final String tableCompletedListings;

    private final Function<ResultSet, ActiveListing>    funcListing;
    private final Function<ResultSet, CompletedListing> funcCompletedListing;

    public AuctionDatabase(@NotNull ShopPlugin plugin, @NotNull AuctionManager manager, @NotNull FileConfig config) {
        super(plugin, DatabaseConfig.read(config, "excellentshop_auction"));
        this.manager = manager;

        this.tableListings = this.getTablePrefix() + "_items";
        this.tableCompletedListings = this.getTablePrefix() + "_history";

        this.funcListing = (resultSet) -> {
            try {
                UUID id = UUID.fromString(resultSet.getString(COLUMN_ID.getName()));
                UUID owner = UUID.fromString(resultSet.getString(COLUMN_OWNER.getName()));
                String ownerName = resultSet.getString(COLUMN_OWNER_NAME.getName());

                ProductTyping typing;

                String serialized = resultSet.getString(COLUMN_ITEM.getName());
                String handlerName = resultSet.getString(COLUMN_HANDLER.getName());

                ProductType type = StringUtil.getEnum(handlerName, ProductType.class).orElse(null);
                if (type != null) {
                    typing = ProductTypes.deserialize(type, serialized);
                }
                // ------ REVERT 4.13.3 CHANGES - START ------
                else {
                    if (handlerName.equalsIgnoreCase("bukkit_item") || handlerName.isBlank()) {
                        String delimiter = " \\| ";
                        String[] split = serialized.split(delimiter);
                        String tagString = split[0];

                        typing = ProductTypes.deserialize(ProductType.VANILLA, tagString);
                    }
                    else {
                        String delimiter = " \\| ";
                        String[] split = serialized.split(delimiter);
                        String itemId = split[0];
                        int amount = split.length >= 2 ? NumberUtil.getIntegerAbs(split[1]) : 1;

                        typing = new PluginProductType(handlerName, itemId, amount);
                    }
                }
                // ------ REVERT 4.13.3 CHANGES - END ------

                if (!(typing instanceof PhysicalTyping physicalTyping) || !physicalTyping.isValid()) {
                    this.manager.error("Invalid listing data: '" + serialized + "'. Handler: '" + handlerName + "'.");
                    return null;
                }

                String currencyId = resultSet.getString(COLUMN_CURRENCY.getName());
                Currency currency = currencyId == null ? this.manager.getDefaultCurrency() : EconomyBridge.getCurrency(CurrencyId.reroute(currencyId));
                if (currency == null || !this.manager.isEnabledCurrency(currency)) {
                    this.manager.error("Invalid listing currency '" + currencyId + "'!");
                    return null;
                }

                double price = resultSet.getDouble(COLUMN_PRICE.getName());
                long expireDate = resultSet.getLong(COLUMN_EXPIRE_DATE.getName());
                long dateCreation = resultSet.getLong(COLUMN_DATE_CREATION.getName());
                long deletionDate = resultSet.getLong(COLUMN_DELETE_DATE.getName());
                if (deletionDate == 0L) {
                    deletionDate = AuctionUtils.generatePurgeDate(dateCreation);
                }

                return new ActiveListing(id, owner, ownerName, physicalTyping, currency, price, dateCreation, expireDate, deletionDate);
            }
            catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        };

        this.funcCompletedListing = (resultSet) -> {
            try {
                UUID id = UUID.fromString(resultSet.getString(COLUMN_ID.getName()));
                UUID owner = UUID.fromString(resultSet.getString(COLUMN_OWNER.getName()));
                String ownerName = resultSet.getString(COLUMN_OWNER_NAME.getName());
                String buyerName = resultSet.getString(COLUMN_BUYER_NAME.getName());

                ProductTyping typing;

                String serialized = resultSet.getString(COLUMN_ITEM.getName());
                String handlerName = resultSet.getString(COLUMN_HANDLER.getName());

                ProductType type = StringUtil.getEnum(handlerName, ProductType.class).orElse(null);
                if (type != null) {
                    typing = ProductTypes.deserialize(type, serialized);
                }
                // ------ REVERT 4.13.3 CHANGES - START ------
                else {
                    if (handlerName.equalsIgnoreCase("bukkit_item") || handlerName.isBlank()) {
                        String delimiter = " \\| ";
                        String[] split = serialized.split(delimiter);
                        String tagString = split[0];

                        typing = ProductTypes.deserialize(ProductType.VANILLA, tagString);
                    }
                    else {
                        String delimiter = " \\| ";
                        String[] split = serialized.split(delimiter);
                        String itemId = split[0];
                        int amount = split.length >= 2 ? NumberUtil.getIntegerAbs(split[1]) : 1;

                        typing = new PluginProductType(handlerName, itemId, amount);
                    }
                }
                // ------ REVERT 4.13.3 CHANGES - END ------

                if (!(typing instanceof PhysicalTyping physicalTyping) || !physicalTyping.isValid()) {
                    this.manager.error("Invalid listing data: '" + serialized + "'. Handler: '" + handlerName + "'.");
                    return null;
                }

                String currencyId = resultSet.getString(COLUMN_CURRENCY.getName());
                Currency currency = currencyId == null ? this.manager.getDefaultCurrency() : EconomyBridge.getCurrency(CurrencyId.reroute(currencyId));
                if (currency == null || !this.manager.isEnabledCurrency(currency)) {
                    this.manager.error("Invalid listing currency '" + currencyId + "'!");
                    return null;
                }

                double price = resultSet.getDouble(COLUMN_PRICE.getName());
                boolean isNotified = resultSet.getBoolean(COLUMN_IS_PAID.getName());
                long buyDate = resultSet.getLong(COLUMN_BUY_DATE.getName());
                long dateCreation = resultSet.getLong(COLUMN_DATE_CREATION.getName());
                long deletionDate = resultSet.getLong(COLUMN_DELETE_DATE.getName());
                if (deletionDate == 0L) {
                    deletionDate = AuctionUtils.generatePurgeDate(dateCreation);
                }

                return new CompletedListing(id, owner, ownerName, buyerName, physicalTyping, currency, price, dateCreation, buyDate, deletionDate, isNotified);
            }
            catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        };
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
            COLUMN_ITEM, COLUMN_HANDLER, COLUMN_CURRENCY, COLUMN_PRICE,
            COLUMN_EXPIRE_DATE, COLUMN_DELETE_DATE, COLUMN_DATE_CREATION
        ));

        // Create auction history table
        this.createTable(this.tableCompletedListings, Arrays.asList(
            COLUMN_ID, COLUMN_OWNER, COLUMN_OWNER_NAME, COLUMN_BUYER_NAME,
            COLUMN_ITEM, COLUMN_HANDLER, COLUMN_CURRENCY, COLUMN_PRICE, COLUMN_IS_PAID,
            COLUMN_BUY_DATE, COLUMN_DATE_CREATION, COLUMN_DELETE_DATE
        ));

        this.addColumn(this.tableListings, COLUMN_HANDLER, ProductType.VANILLA.name());
        this.addColumn(this.tableListings, COLUMN_CURRENCY, this.manager.getDefaultCurrency().getInternalId());
        this.addColumn(this.tableListings, COLUMN_DATE_CREATION, String.valueOf(System.currentTimeMillis()));

        this.addColumn(this.tableCompletedListings, COLUMN_HANDLER, ProductType.VANILLA.name());
        this.addColumn(this.tableCompletedListings, COLUMN_CURRENCY, this.manager.getDefaultCurrency().getInternalId());
        this.addColumn(this.tableCompletedListings, COLUMN_DATE_CREATION, String.valueOf(System.currentTimeMillis()));

        if (SQLQueries.hasTable(this.connector, this.tableCompletedListings)) {
            DeleteQuery<Long> query = new DeleteQuery<Long>().where(COLUMN_DELETE_DATE, WhereOperator.SMALLER, String::valueOf);
            this.delete(this.tableCompletedListings, query, System.currentTimeMillis());
        }

        if (SQLQueries.hasTable(this.connector, this.tableListings)) {
            DeleteQuery<Long> query = new DeleteQuery<Long>().where(COLUMN_DELETE_DATE, WhereOperator.SMALLER, String::valueOf);
            this.delete(this.tableListings, query, System.currentTimeMillis());
        }
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
        SelectQuery<ActiveListing> query = new SelectQuery<>(this.funcListing).all();
        return this.select(this.tableListings, query);
    }

    @NotNull
    public List<CompletedListing> getCompletedListings() {
        SelectQuery<CompletedListing> query = new SelectQuery<>(this.funcCompletedListing).all();
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
