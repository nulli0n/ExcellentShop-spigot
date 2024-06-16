package su.nightexpress.nexshop.auction.data;

import com.google.common.collect.Lists;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.database.AbstractDataHandler;
import su.nightexpress.nightcore.database.DatabaseConfig;
import su.nightexpress.nightcore.database.sql.SQLColumn;
import su.nightexpress.nightcore.database.sql.SQLCondition;
import su.nightexpress.nightcore.database.sql.SQLQueries;
import su.nightexpress.nightcore.database.sql.column.ColumnType;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.TimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class AuctionDataHandler extends AbstractDataHandler<ShopPlugin> {

    private static final SQLColumn COLUMN_ID            = SQLColumn.of("aucId", ColumnType.STRING);
    private static final SQLColumn COLUMN_OWNER         = SQLColumn.of("owner", ColumnType.STRING);
    private static final SQLColumn COLUMN_OWNER_NAME    = SQLColumn.of("ownerName", ColumnType.STRING);
    private static final SQLColumn COLUMN_BUYER_NAME    = SQLColumn.of("buyerName", ColumnType.STRING);
    private static final SQLColumn COLUMN_ITEM          = SQLColumn.of("itemStack", ColumnType.STRING);
    private static final SQLColumn COLUMN_CURRENCY      = SQLColumn.of("currency", ColumnType.STRING);
    private static final SQLColumn COLUMN_PRICE         = SQLColumn.of("price", ColumnType.DOUBLE);
    private static final SQLColumn COLUMN_EXPIRE_DATE   = SQLColumn.of("expireDate", ColumnType.LONG);
    private static final SQLColumn COLUMN_DELETE_DATE   = SQLColumn.of("deleteDate", ColumnType.LONG);
    private static final SQLColumn COLUMN_BUY_DATE      = SQLColumn.of("buyDate", ColumnType.LONG);
    private static final SQLColumn COLUMN_DATE_CREATION = SQLColumn.of("dateCreation", ColumnType.LONG);
    private static final SQLColumn COLUMN_IS_PAID       = SQLColumn.of("isPaid", ColumnType.BOOLEAN);

    private final AuctionManager auctionManager;

    private final String tableListings;
    private final String tableCompletedListings;

    private final Function<ResultSet, ActiveListing>    funcListing;
    private final Function<ResultSet, CompletedListing> funcCompletedListing;

    public AuctionDataHandler(@NotNull AuctionManager auctionManager, @NotNull FileConfig config) {
        super(auctionManager.plugin(), DatabaseConfig.read(config, "excellentshop_auction"));
        this.auctionManager = auctionManager;

        this.tableListings = this.getTablePrefix() + "_items";
        this.tableCompletedListings = this.getTablePrefix() + "_history";

        this.funcListing = (resultSet) -> {
            try {
                UUID id = UUID.fromString(resultSet.getString(COLUMN_ID.getName()));
                UUID owner = UUID.fromString(resultSet.getString(COLUMN_OWNER.getName()));
                String ownerName = resultSet.getString(COLUMN_OWNER_NAME.getName());
                ItemStack itemStack = ItemUtil.decompress(resultSet.getString(COLUMN_ITEM.getName()));
                if (itemStack == null) {
                    this.auctionManager.error("Invalid listing item stack!");
                    return null;
                }

                String currencyId = resultSet.getString(COLUMN_CURRENCY.getName());
                Currency currency = currencyId == null ? this.auctionManager.getDefaultCurrency() : this.plugin().getCurrencyManager().getCurrency(currencyId);
                if (currency == null || !this.auctionManager.getAllowedCurrencies().contains(currency)) {
                    this.auctionManager.error("Invalid listing currency '" + currencyId + "'!");
                    return null;
                }

                double price = resultSet.getDouble(COLUMN_PRICE.getName());
                long expireDate = resultSet.getLong(COLUMN_EXPIRE_DATE.getName());
                long dateCreation = resultSet.getLong(COLUMN_DATE_CREATION.getName());
                long deletionDate = resultSet.getLong(COLUMN_DELETE_DATE.getName());
                if (deletionDate == 0L) {
                    deletionDate = AuctionUtils.generatePurgeDate(dateCreation);
                }

                return new ActiveListing(id, owner, ownerName, itemStack, currency, price, dateCreation, expireDate, deletionDate);
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

                ItemStack itemStack = ItemUtil.decompress(resultSet.getString(COLUMN_ITEM.getName()));
                if (itemStack == null) {
                    this.auctionManager.error("Invalid listing item stack!");
                    return null;
                }

                String currencyId = resultSet.getString(COLUMN_CURRENCY.getName());
                Currency currency = currencyId == null ? this.auctionManager.getDefaultCurrency() : this.plugin().getCurrencyManager().getCurrency(currencyId);
                if (currency == null || !this.auctionManager.getAllowedCurrencies().contains(currency)) {
                    this.auctionManager.error("Invalid listing currency '" + currencyId + "'!");
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

                return new CompletedListing(id, owner, ownerName, buyerName, itemStack, currency, price, dateCreation, buyDate, deletionDate, isNotified);
            }
            catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        };
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        // Create auction items table
        this.createTable(this.tableListings, Arrays.asList(
            COLUMN_ID, COLUMN_OWNER, COLUMN_OWNER_NAME,
            COLUMN_ITEM, COLUMN_CURRENCY, COLUMN_PRICE,
            COLUMN_EXPIRE_DATE, COLUMN_DELETE_DATE, COLUMN_DATE_CREATION
        ));

        // Create auction history table
        this.createTable(this.tableCompletedListings, Arrays.asList(
            COLUMN_ID, COLUMN_OWNER, COLUMN_OWNER_NAME, COLUMN_BUYER_NAME,
            COLUMN_ITEM, COLUMN_CURRENCY, COLUMN_PRICE, COLUMN_IS_PAID,
            COLUMN_BUY_DATE, COLUMN_DATE_CREATION, COLUMN_DELETE_DATE
        ));

        this.addColumn(this.tableListings,
            COLUMN_CURRENCY.toValue(this.auctionManager.getDefaultCurrency().getId()),
            COLUMN_DATE_CREATION.toValue(System.currentTimeMillis()));

        this.addColumn(this.tableCompletedListings,
            COLUMN_CURRENCY.toValue(this.auctionManager.getDefaultCurrency().getId()),
            COLUMN_DATE_CREATION.toValue(System.currentTimeMillis()));
    }

    @Override
    public void onPurge() {
        //LocalDateTime deadline = LocalDateTime.now().minusDays(this.getConfig().getPurgePeriod());
        //long deadlineMs = TimeUtil.toEpochMillis(deadline);

        if (SQLQueries.hasTable(this.getConnector(), this.tableCompletedListings)) {
            this.delete(this.tableCompletedListings,
                SQLCondition.smaller(COLUMN_DELETE_DATE.toValue(System.currentTimeMillis()))
                //SQLCondition.equal(COLUMN_IS_PAID.toValue(0))
            );

            //String sql = "DELETE FROM " + this.tableCompletedListings + " WHERE buyDate < " + deadlineMs + " AND isPaid = 0";
            //SQLQueries.executeStatement(this.getConnector(), sql);
        }
        if (SQLQueries.hasTable(this.getConnector(), this.tableListings)) {
            this.delete(this.tableListings,
                //SQLCondition.smaller(COLUMN_EXPIRE_DATE.toValue(deadlineMs))
                SQLCondition.smaller(COLUMN_DELETE_DATE.toValue(System.currentTimeMillis()))
            );

            //String sql = "DELETE FROM " + this.tableListings + " WHERE expireDate < " + deadlineMs;
            //SQLQueries.executeStatement(this.getConnector(), sql);
        }
    }

    @Override
    public void onSave() {

    }

    @Override
    public void onSynchronize() {
        List<ActiveListing> listings = this.getListings();
        List<CompletedListing> completedListings = this.getCompletedListings();

        this.auctionManager.getListings().clear();

        listings.forEach(listing -> this.auctionManager.getListings().add(listing));
        completedListings.forEach(listing -> this.auctionManager.getListings().addCompleted(listing));
    }

    @NotNull
    public List<ActiveListing> getListings() {
        return this.load(this.tableListings, this.funcListing, Collections.emptyList(), Collections.emptyList(), -1);
    }

    @NotNull
    public List<CompletedListing> getCompletedListings() {
        return this.load(this.tableCompletedListings, this.funcCompletedListing, Collections.emptyList(), Collections.emptyList(), -1);
    }

    public void addListing(@NotNull ActiveListing listing) {
        this.insert(this.tableListings, Arrays.asList(
            COLUMN_ID.toValue(listing.getId().toString()),
            COLUMN_OWNER.toValue(listing.getOwner().toString()),
            COLUMN_OWNER_NAME.toValue(listing.getOwnerName()),
            COLUMN_ITEM.toValue(String.valueOf(ItemUtil.compress(listing.getItemStack()))),
            COLUMN_CURRENCY.toValue(listing.getCurrency().getId()),
            COLUMN_PRICE.toValue(listing.getPrice()),
            COLUMN_EXPIRE_DATE.toValue(listing.getExpireDate()),
            COLUMN_DELETE_DATE.toValue(listing.getDeleteDate()),
            COLUMN_DATE_CREATION.toValue(listing.getCreationDate())
        ));
    }

    public void deleteListing(@NotNull ActiveListing listing) {
        this.delete(this.tableListings, SQLCondition.equal(COLUMN_ID.toValue(listing.getId().toString())));

        //String sql = "DELETE FROM " + this.tableListings + " WHERE `aucId` = '" + listing.getId() + "'";
        //SQLQueries.executeStatement(this.getConnector(), sql);
    }

    public void addCompletedListing(@NotNull CompletedListing listing) {
        this.insert(this.tableCompletedListings, Arrays.asList(
            COLUMN_ID.toValue(listing.getId().toString()),
            COLUMN_OWNER.toValue(listing.getOwner().toString()),
            COLUMN_OWNER_NAME.toValue(listing.getOwnerName()),
            COLUMN_BUYER_NAME.toValue(listing.getBuyerName()),
            COLUMN_ITEM.toValue(String.valueOf(ItemUtil.compress(listing.getItemStack()))),
            COLUMN_CURRENCY.toValue(listing.getCurrency().getId()),
            COLUMN_PRICE.toValue(listing.getPrice()),
            COLUMN_IS_PAID.toValue(listing.isClaimed() ? 1 : 0),
            COLUMN_BUY_DATE.toValue(listing.getBuyDate()),
            COLUMN_DELETE_DATE.toValue(listing.getDeleteDate()),
            COLUMN_DATE_CREATION.toValue(listing.getCreationDate())
        ));
    }

    public void saveCompletedListings(@NotNull CompletedListing... listings) {
        for (CompletedListing listing : listings) {
            this.saveCompletedListing(listing);
        }
    }

    public void saveCompletedListing(@NotNull CompletedListing listing) {
        this.update(this.tableCompletedListings, Collections.singletonList(
            COLUMN_IS_PAID.toValue(listing.isClaimed() ? 1 : 0)
            ),
            SQLCondition.equal(COLUMN_ID.toValue(listing.getId()))
        );
    }

    public void deleteCompletedListing(@NotNull CompletedListing listing) {
        this.delete(this.tableCompletedListings, SQLCondition.equal(COLUMN_ID.toValue(listing.getId().toString())));

        //String sql = "DELETE FROM " + this.tableCompletedListings + " WHERE `aucId` = '" + historyItem.getId() + "'";
        //SQLQueries.executeStatement(this.getConnector(), sql);
    }

    public boolean isListingExist(@NotNull UUID id) {
        return this.contains(this.tableListings,
            Collections.singletonList(COLUMN_ID),
            SQLCondition.equal(COLUMN_ID.toValue(id))
        );
    }

    public boolean isCompletedListingExist(@NotNull UUID id) {
        return this.contains(this.tableCompletedListings,
            Collections.singletonList(COLUMN_ID),
            SQLCondition.equal(COLUMN_ID.toValue(id))
        );
    }

    public boolean isCompletedListingClaimed(@NotNull UUID id) {
        return this.contains(this.tableCompletedListings,
            Lists.newArrayList(COLUMN_ID, COLUMN_IS_PAID),
            SQLCondition.equal(COLUMN_ID.toValue(id)),
            SQLCondition.equal(COLUMN_IS_PAID.toValue(1))
        );
    }
}
