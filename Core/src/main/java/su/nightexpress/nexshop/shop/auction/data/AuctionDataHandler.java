package su.nightexpress.nexshop.shop.auction.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractDataHandler;
import su.nexmedia.engine.api.data.DataQueries;
import su.nexmedia.engine.api.data.DataTypes;
import su.nexmedia.engine.api.data.config.DataConfig;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.listing.AuctionCompletedListing;
import su.nightexpress.nexshop.shop.auction.listing.AuctionListing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public class AuctionDataHandler extends AbstractDataHandler<ExcellentShop> {

    private static AuctionDataHandler instance;

    private final AuctionManager auctionManager;
    private final String                              tableListings;
    private final String                              tableCompletedListings;
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
                UUID id = UUID.fromString(resultSet.getString("aucId"));
                UUID owner = UUID.fromString(resultSet.getString("owner"));
                String ownerName = resultSet.getString("ownerName");
                ItemStack itemStack = ItemUtil.fromBase64(resultSet.getString("itemStack"));
                if (itemStack == null) {
                    this.auctionManager.error("Invalid listing item stack!");
                    return null;
                }

                String currencyId = resultSet.getString("currency");
                ICurrency currency = currencyId == null ? this.auctionManager.getCurrencyDefault() : this.plugin().getCurrencyManager().getCurrency(currencyId);
                if (currency == null || !this.auctionManager.getCurrencies().contains(currency)) {
                    this.auctionManager.error("Invalid listing currency '" + currencyId + "'!");
                    return null;
                }

                double price = resultSet.getDouble("price");
                long expireDate = resultSet.getLong("expireDate");
                long dateCreation = resultSet.getLong("dateCreation");

                return new AuctionListing(id, owner, ownerName, itemStack, currency, price, dateCreation, expireDate);
            }
            catch (SQLException e) {
                return null;
            }
        };

        this.funcCompletedListing = (resultSet) -> {
            try {
                UUID id = UUID.fromString(resultSet.getString("aucId"));
                UUID owner = UUID.fromString(resultSet.getString("owner"));
                String ownerName = resultSet.getString("ownerName");
                String buyerName = resultSet.getString("buyerName");

                ItemStack itemStack = ItemUtil.fromBase64(resultSet.getString("itemStack"));
                if (itemStack == null) {
                    this.auctionManager.error("Invalid listing item stack!");
                    return null;
                }

                String currencyId = resultSet.getString("currency");
                ICurrency currency = currencyId == null ? this.auctionManager.getCurrencyDefault() : this.plugin().getCurrencyManager().getCurrency(currencyId);
                if (currency == null || !this.auctionManager.getCurrencies().contains(currency)) {
                    this.auctionManager.error("Invalid listing currency '" + currencyId + "'!");
                    return null;
                }

                double price = resultSet.getDouble("price");
                boolean isNotified = resultSet.getBoolean("isPaid");
                long buyDate = resultSet.getLong("buyDate");
                long dateCreation = resultSet.getLong("dateCreation");

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
        LinkedHashMap<String, String> aucLis = new LinkedHashMap<>();
        aucLis.put("aucId", DataTypes.STRING.build(this.getDataType()));
        aucLis.put("owner", DataTypes.STRING.build(this.getDataType()));
        aucLis.put("ownerName", DataTypes.STRING.build(this.getDataType()));
        aucLis.put("itemStack", DataTypes.STRING.build(this.getDataType()));
        aucLis.put("currency", DataTypes.STRING.build(this.getDataType()));
        aucLis.put("price", DataTypes.DOUBLE.build(this.getDataType()));
        aucLis.put("expireDate", DataTypes.LONG.build(this.getDataType()));
        aucLis.put("deleteDate", DataTypes.LONG.build(this.getDataType()));
        aucLis.put("dateCreation", DataTypes.LONG.build(this.getDataType()));
        this.createTable(this.tableListings, aucLis);

        // Create auction history table
        LinkedHashMap<String, String> aucHis = new LinkedHashMap<>();
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
        this.createTable(this.tableCompletedListings, aucHis);

        this.addColumn(this.tableListings, "currency", DataTypes.LONG.build(this.getDataType()), this.auctionManager.getCurrencyDefault().getId());
        this.addColumn(this.tableCompletedListings, "currency", DataTypes.LONG.build(this.getDataType()), this.auctionManager.getCurrencyDefault().getId());
        this.addColumn(this.tableListings, "dateCreation", DataTypes.LONG.build(this.getDataType()), String.valueOf(System.currentTimeMillis()));
        this.addColumn(this.tableCompletedListings, "dateCreation", DataTypes.LONG.build(this.getDataType()), String.valueOf(System.currentTimeMillis()));
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

        if (this.hasTable(this.tableCompletedListings)) {
            String sql = "DELETE FROM " + this.tableCompletedListings + " WHERE buyDate < " + deadlineMs + " AND isPaid = 0";
            DataQueries.executeStatement(this.getConnector(), sql);
        }
        if (this.hasTable(this.tableListings)) {
            String sql = "DELETE FROM " + this.tableListings + " WHERE expireDate < " + deadlineMs;
            DataQueries.executeStatement(this.getConnector(), sql);
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
        return this.getDatas(this.tableListings, Collections.emptyMap(), this.funcListing, -1);
    }

    @NotNull
    public List<AuctionCompletedListing> getCompletedListings() {
        return this.getDatas(this.tableCompletedListings, Collections.emptyMap(), this.funcCompletedListing, -1);
    }

    public void addListing(@NotNull AuctionListing listing, boolean async) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("aucId", listing.getId().toString());
        map.put("owner", listing.getOwner().toString());
        map.put("ownerName", listing.getOwnerName());
        map.put("itemStack", ItemUtil.toBase64(listing.getItemStack()));
        map.put("currency", listing.getCurrency().getId());
        map.put("price", String.valueOf(listing.getPrice()));
        map.put("expireDate", String.valueOf(listing.getExpireDate()));
        map.put("deleteDate", String.valueOf(0L));
        map.put("dateCreation", String.valueOf(listing.getDateCreation()));

        this.plugin.runTask(c -> this.addData(this.tableListings, map), async);
    }

    public void deleteListing(@NotNull AuctionListing listing, boolean async) {
        String sql = "DELETE FROM " + this.tableListings + " WHERE `aucId` = '" + listing.getId() + "'";
        this.plugin.runTask((c) -> DataQueries.executeStatement(this.getConnector(), sql), async);
    }

    public void addCompletedListing(@NotNull AuctionCompletedListing listing, boolean async) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
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

        this.plugin.runTask((c) -> this.addData(this.tableCompletedListings, map), async);
    }

    public void saveCompletedListing(@NotNull AuctionCompletedListing historyItem, boolean async) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("isPaid", String.valueOf(historyItem.isRewarded() ? 1 : 0));

        LinkedHashMap<String, String> mapWhere = new LinkedHashMap<>();
        mapWhere.put("aucId", historyItem.getId().toString());

        this.plugin.runTask((c) -> this.saveData(tableCompletedListings, map, mapWhere), async);
    }

    public void deleteCompletedListing(@NotNull AuctionCompletedListing historyItem, boolean async) {
        String sql = "DELETE FROM " + this.tableCompletedListings + " WHERE `aucId` = '" + historyItem.getId() + "'";
        this.plugin.runTask(c -> DataQueries.executeStatement(this.getConnector(), sql), async);
    }

    public boolean isListingExist(@NotNull UUID id) {
        Map<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put("aucId", id.toString());

        return this.hasData(this.tableListings, whereMap);
    }

    public boolean isCompletedListingExist(@NotNull UUID id) {
        Map<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put("aucId", id.toString());

        return this.hasData(this.tableCompletedListings, whereMap);
    }
}
