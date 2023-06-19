package su.nightexpress.nexshop.shop.auction.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractDataHandler;
import su.nexmedia.engine.api.data.config.DataConfig;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLCondition;
import su.nexmedia.engine.api.data.sql.SQLQueries;
import su.nexmedia.engine.api.data.sql.column.ColumnType;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.listing.AuctionCompletedListing;
import su.nightexpress.nexshop.shop.auction.listing.AuctionListing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class AuctionDataHandler extends AbstractDataHandler<ExcellentShop> {

    private static final SQLColumn COLUMN_AUC_ID        = SQLColumn.of("aucId", ColumnType.STRING);
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

    private static AuctionDataHandler instance;

    private final AuctionManager                               auctionManager;
    private final String                                       tableListings;
    private final String                                       tableCompletedListings;
    private final Function<ResultSet, AuctionListing>          funcListing;
    private final Function<ResultSet, AuctionCompletedListing> funcCompletedListing;

    @NotNull
    public static AuctionDataHandler getInstance(@NotNull AuctionManager auctionManager) {
        if (instance == null) {
            instance = new AuctionDataHandler(auctionManager);
        }
        return instance;
    }

    protected AuctionDataHandler(@NotNull AuctionManager auctionManager) {
        super(auctionManager.plugin(), new DataConfig(auctionManager.getConfig()));
        this.auctionManager = auctionManager;

        this.tableListings = this.getTablePrefix() + "_items";
        this.tableCompletedListings = this.getTablePrefix() + "_history";

        this.funcListing = (resultSet) -> {
            try {
                UUID id = UUID.fromString(resultSet.getString(COLUMN_AUC_ID.getName()));
                UUID owner = UUID.fromString(resultSet.getString(COLUMN_OWNER.getName()));
                String ownerName = resultSet.getString(COLUMN_OWNER_NAME.getName());
                ItemStack itemStack = ItemUtil.fromBase64(resultSet.getString(COLUMN_ITEM.getName()));
                if (itemStack == null) {
                    this.auctionManager.error("Invalid listing item stack!");
                    return null;
                }

                String currencyId = resultSet.getString(COLUMN_CURRENCY.getName());
                Currency currency = currencyId == null ? this.auctionManager.getCurrencyDefault() : this.plugin().getCurrencyManager().getCurrency(currencyId);
                if (currency == null || !this.auctionManager.getCurrencies().contains(currency)) {
                    this.auctionManager.error("Invalid listing currency '" + currencyId + "'!");
                    return null;
                }

                double price = resultSet.getDouble(COLUMN_PRICE.getName());
                long expireDate = resultSet.getLong(COLUMN_EXPIRE_DATE.getName());
                long dateCreation = resultSet.getLong(COLUMN_DATE_CREATION.getName());

                return new AuctionListing(id, owner, ownerName, itemStack, currency, price, dateCreation, expireDate);
            }
            catch (SQLException e) {
                return null;
            }
        };

        this.funcCompletedListing = (resultSet) -> {
            try {
                UUID id = UUID.fromString(resultSet.getString(COLUMN_AUC_ID.getName()));
                UUID owner = UUID.fromString(resultSet.getString(COLUMN_OWNER.getName()));
                String ownerName = resultSet.getString(COLUMN_OWNER_NAME.getName());
                String buyerName = resultSet.getString(COLUMN_BUYER_NAME.getName());

                ItemStack itemStack = ItemUtil.fromBase64(resultSet.getString(COLUMN_ITEM.getName()));
                if (itemStack == null) {
                    this.auctionManager.error("Invalid listing item stack!");
                    return null;
                }

                String currencyId = resultSet.getString(COLUMN_CURRENCY.getName());
                Currency currency = currencyId == null ? this.auctionManager.getCurrencyDefault() : this.plugin().getCurrencyManager().getCurrency(currencyId);
                if (currency == null || !this.auctionManager.getCurrencies().contains(currency)) {
                    this.auctionManager.error("Invalid listing currency '" + currencyId + "'!");
                    return null;
                }

                double price = resultSet.getDouble(COLUMN_PRICE.getName());
                boolean isNotified = resultSet.getBoolean(COLUMN_IS_PAID.getName());
                long buyDate = resultSet.getLong(COLUMN_BUY_DATE.getName());
                long dateCreation = resultSet.getLong(COLUMN_DATE_CREATION.getName());

                return new AuctionCompletedListing(id, owner, ownerName, buyerName, itemStack, currency, price, dateCreation, isNotified, buyDate);
            }
            catch (SQLException e) {
                return null;
            }
        };
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        // Create auction items table
        this.createTable(this.tableListings, Arrays.asList(
            COLUMN_AUC_ID, COLUMN_OWNER, COLUMN_OWNER_NAME,
            COLUMN_ITEM, COLUMN_CURRENCY, COLUMN_PRICE,
            COLUMN_EXPIRE_DATE, COLUMN_DELETE_DATE, COLUMN_DATE_CREATION
        ));

        /*LinkedHashMap<String, String> aucLis = new LinkedHashMap<>();
        aucLis.put("aucId", DataTypes.STRING.build(this.getDataType()));
        aucLis.put("owner", DataTypes.STRING.build(this.getDataType()));
        aucLis.put("ownerName", DataTypes.STRING.build(this.getDataType()));
        aucLis.put("itemStack", DataTypes.STRING.build(this.getDataType()));
        aucLis.put("currency", DataTypes.STRING.build(this.getDataType()));
        aucLis.put("price", DataTypes.DOUBLE.build(this.getDataType()));
        aucLis.put("expireDate", DataTypes.LONG.build(this.getDataType()));
        aucLis.put("deleteDate", DataTypes.LONG.build(this.getDataType()));
        aucLis.put("dateCreation", DataTypes.LONG.build(this.getDataType()));
        this.createTable(this.tableListings, aucLis);*/

        // Create auction history table
        this.createTable(this.tableCompletedListings, Arrays.asList(
            COLUMN_AUC_ID, COLUMN_OWNER, COLUMN_OWNER_NAME, COLUMN_BUYER_NAME,
            COLUMN_ITEM, COLUMN_CURRENCY, COLUMN_PRICE, COLUMN_IS_PAID,
            COLUMN_BUY_DATE, COLUMN_DATE_CREATION, COLUMN_DELETE_DATE
        ));

        /*LinkedHashMap<String, String> aucHis = new LinkedHashMap<>();
        aucHis.put("aucId", DataTypes.STRING.build(this.getDataType()));
        aucHis.put("owner", DataTypes.STRING.build(this.getDataType()));
        aucHis.put("ownerName", DataTypes.STRING.build(this.getDataType()));
        aucHis.put("buyerName", DataTypes.STRING.build(this.getDataType()));
        aucHis.put("itemStack", DataTypes.STRING.build(this.getDataType()));
        aucHis.put("currency", DataTypes.STRING.build(this.getDataType()));
        aucHis.put("price", DataTypes.DOUBLE.build(this.getDataType()));
        aucHis.put("isPaid", DataTypes.BOOLEAN.build(this.getDataType()));
        aucHis.put("buyDate", DataTypes.LONG.build(this.getDataType()));
        aucHis.put("dateCreation", DataTypes.LONG.build(this.getDataType()));
        aucHis.put("deleteDate", DataTypes.LONG.build(this.getDataType()));
        this.createTable(this.tableCompletedListings, aucHis);*/

        this.addColumn(this.tableListings,
            COLUMN_CURRENCY.toValue(this.auctionManager.getCurrencyDefault().getId()),
            COLUMN_DATE_CREATION.toValue(System.currentTimeMillis()));
        this.addColumn(this.tableCompletedListings,
            COLUMN_CURRENCY.toValue(this.auctionManager.getCurrencyDefault().getId()),
            COLUMN_DATE_CREATION.toValue(System.currentTimeMillis()));

        //this.addColumn(this.tableCompletedListings, "currency", DataTypes.LONG.build(this.getDataType()), this.auctionManager.getCurrencyDefault().getId());
        //this.addColumn(this.tableListings, "dateCreation", DataTypes.LONG.build(this.getDataType()), String.valueOf(System.currentTimeMillis()));
        //this.addColumn(this.tableCompletedListings, "dateCreation", DataTypes.LONG.build(this.getDataType()), String.valueOf(System.currentTimeMillis()));
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();
        instance = null;
    }

    @Override
    public void onPurge() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(this.getConfig().purgePeriod);
        long deadlineMs = TimeUtil.toEpochMillis(deadline);

        if (SQLQueries.hasTable(this.getConnector(), this.tableCompletedListings)) {
            String sql = "DELETE FROM " + this.tableCompletedListings + " WHERE buyDate < " + deadlineMs + " AND isPaid = 0";
            SQLQueries.executeStatement(this.getConnector(), sql);
        }
        if (SQLQueries.hasTable(this.getConnector(), this.tableListings)) {
            String sql = "DELETE FROM " + this.tableListings + " WHERE expireDate < " + deadlineMs;
            SQLQueries.executeStatement(this.getConnector(), sql);
        }
    }

    @Override
    public void onSave() {
        // TODO
    }

    @Override
    public void onSynchronize() {
        List<AuctionListing> listings = this.getListings();
        List<AuctionCompletedListing> completedListings = this.getCompletedListings();

        this.auctionManager.clearListings();
        listings.forEach(this.auctionManager::addListing);
        completedListings.forEach(this.auctionManager::addCompletedListing);
    }

    @NotNull
    public List<AuctionListing> getListings() {
        return this.load(this.tableListings, this.funcListing, Collections.emptyList(), Collections.emptyList(), -1);
    }

    @NotNull
    public List<AuctionCompletedListing> getCompletedListings() {
        return this.load(this.tableCompletedListings, this.funcCompletedListing, Collections.emptyList(), Collections.emptyList(), -1);
    }

    public void addListing(@NotNull AuctionListing listing) {
        this.insert(this.tableListings, Arrays.asList(
            COLUMN_AUC_ID.toValue(listing.getId().toString()),
            COLUMN_OWNER.toValue(listing.getOwner().toString()),
            COLUMN_OWNER_NAME.toValue(listing.getOwnerName()),
            COLUMN_ITEM.toValue(String.valueOf(ItemUtil.toBase64(listing.getItemStack()))),
            COLUMN_CURRENCY.toValue(listing.getCurrency().getId()),
            COLUMN_PRICE.toValue(listing.getPrice()),
            COLUMN_EXPIRE_DATE.toValue(listing.getExpireDate()),
            COLUMN_DELETE_DATE.toValue(0L),
            COLUMN_DATE_CREATION.toValue(listing.getDateCreation())
        ));

        /*LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("aucId", listing.getId().toString());
        map.put("owner", listing.getOwner().toString());
        map.put("ownerName", listing.getOwnerName());
        map.put("itemStack", ItemUtil.toBase64(listing.getItemStack()));
        map.put("currency", listing.getCurrency().getId());
        map.put("price", String.valueOf(listing.getPrice()));
        map.put("expireDate", String.valueOf(listing.getExpireDate()));
        map.put("deleteDate", String.valueOf(0L));
        map.put("dateCreation", String.valueOf(listing.getDateCreation()));

        this.plugin.runTask(c -> this.addData(this.tableListings, map), async);*/
    }

    public void deleteListing(@NotNull AuctionListing listing) {
        String sql = "DELETE FROM " + this.tableListings + " WHERE `aucId` = '" + listing.getId() + "'";
        SQLQueries.executeStatement(this.getConnector(), sql);
    }

    public void addCompletedListing(@NotNull AuctionCompletedListing listing) {
        this.insert(this.tableCompletedListings, Arrays.asList(
            COLUMN_AUC_ID.toValue(listing.getId().toString()),
            COLUMN_OWNER.toValue(listing.getOwner().toString()),
            COLUMN_OWNER_NAME.toValue(listing.getOwnerName()),
            COLUMN_BUYER_NAME.toValue(listing.getBuyerName()),
            COLUMN_ITEM.toValue(String.valueOf(ItemUtil.toBase64(listing.getItemStack()))),
            COLUMN_CURRENCY.toValue(listing.getCurrency().getId()),
            COLUMN_PRICE.toValue(listing.getPrice()),
            COLUMN_IS_PAID.toValue(listing.isRewarded() ? 1 : 0),
            COLUMN_BUY_DATE.toValue(listing.getBuyDate()),
            COLUMN_DELETE_DATE.toValue(0L),
            COLUMN_DATE_CREATION.toValue(listing.getDateCreation())
        ));

        /*LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("aucId", listing.getId().toString());
        map.put("owner", listing.getOwner().toString());
        map.put("ownerName", listing.getOwnerName());
        map.put("buyerName", listing.getBuyerName());
        map.put("itemStack", ItemUtil.toBase64(listing.getItemStack()));
        map.put("currency", listing.getCurrency().getId());
        map.put("price", String.valueOf(listing.getPrice()));
        map.put("isPaid", String.valueOf(listing.isRewarded() ? 1 : 0));
        map.put("buyDate", String.valueOf(listing.getBuyDate()));
        map.put("deleteDate", String.valueOf(0L));
        map.put("dateCreation", String.valueOf(listing.getDateCreation()));

        this.plugin.runTask((c) -> this.addData(this.tableCompletedListings, map), async);*/
    }

    public void saveCompletedListing(@NotNull AuctionCompletedListing historyItem) {
        this.update(this.tableCompletedListings, Collections.singletonList(
            COLUMN_IS_PAID.toValue(historyItem.isRewarded() ? 1 : 0)
            ),
            SQLCondition.equal(COLUMN_AUC_ID.toValue(historyItem.getId()))
        );

        /*LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("isPaid", String.valueOf(historyItem.isRewarded() ? 1 : 0));

        LinkedHashMap<String, String> mapWhere = new LinkedHashMap<>();
        mapWhere.put("aucId", historyItem.getId().toString());

        this.plugin.runTask((c) -> this.saveData(tableCompletedListings, map, mapWhere), async);*/
    }

    public void deleteCompletedListing(@NotNull AuctionCompletedListing historyItem) {
        String sql = "DELETE FROM " + this.tableCompletedListings + " WHERE `aucId` = '" + historyItem.getId() + "'";
        SQLQueries.executeStatement(this.getConnector(), sql);
    }

    public boolean isListingExist(@NotNull UUID id) {
        //Map<String, String> whereMap = new LinkedHashMap<>();
        //whereMap.put("aucId", id.toString());

        return this.contains(this.tableListings,
            Collections.singletonList(COLUMN_AUC_ID),
            SQLCondition.equal(COLUMN_AUC_ID.toValue(id))
        );
    }

    public boolean isCompletedListingExist(@NotNull UUID id) {
        //Map<String, String> whereMap = new LinkedHashMap<>();
        //whereMap.put("aucId", id.toString());

        return this.contains(this.tableCompletedListings,
            Collections.singletonList(COLUMN_AUC_ID),
            SQLCondition.equal(COLUMN_AUC_ID.toValue(id))
        );
    }
}
