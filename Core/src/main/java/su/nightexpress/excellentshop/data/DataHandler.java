package su.nightexpress.excellentshop.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.data.legacy.LegacyPriceData;
import su.nightexpress.excellentshop.data.legacy.LegacyRotationData;
import su.nightexpress.excellentshop.data.legacy.LegacyStockData;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.data.RotationData;
import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.excellentshop.shop.data.ProductLimitData;
import su.nightexpress.excellentshop.shop.data.ProductPriceData;
import su.nightexpress.excellentshop.shop.data.ProductStockData;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.core.Config;
import su.nightexpress.excellentshop.data.serialize.ItemProductTypeSerializer;
import su.nightexpress.nexshop.data.serialize.ItemTagSerializer;
import su.nightexpress.nexshop.user.ShopUser;
import su.nightexpress.nightcore.db.AbstractDatabaseManager;
import su.nightexpress.nightcore.db.column.Column;
import su.nightexpress.nightcore.db.statement.RowMapper;
import su.nightexpress.nightcore.db.statement.SQLStatements;
import su.nightexpress.nightcore.db.statement.condition.Operator;
import su.nightexpress.nightcore.db.statement.condition.Wheres;
import su.nightexpress.nightcore.db.statement.template.InsertStatement;
import su.nightexpress.nightcore.db.statement.template.SelectStatement;
import su.nightexpress.nightcore.db.statement.template.UpdateStatement;
import su.nightexpress.nightcore.db.table.Table;
import su.nightexpress.nightcore.user.data.UserDataSchema;
import su.nightexpress.nightcore.util.ItemTag;
import su.nightexpress.nightcore.util.TimeUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

public class DataHandler extends AbstractDatabaseManager<ShopPlugin> implements UserDataSchema<ShopUser> {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .registerTypeAdapter(ItemTag.class, new ItemTagSerializer())
        .registerTypeAdapter(ItemContent.class, new ItemProductTypeSerializer())
        .create();

    private final List<Runnable> onSyncActions;

    private final Table userTable;

    private final Table priceDataTable;
    private final Table globalStockDataTable;
    private final Table playerLimitDataTable;
    private final Table rotationDataTable;

    // LEGACY - START
    private final Table legacyStockDataTable;
    private final Table legacyPriceDataTable;
    private final Table legacyRotationDataTable;
    // LEGACY - END

    private boolean hasLegacyStocks;
    private boolean hasLegacyPrices;
    private boolean hasLegacyRotations;

    public DataHandler(@NonNull ShopPlugin plugin) {
        super(plugin);
        this.onSyncActions = new ArrayList<>();

        this.userTable = Table.builder(this.getTablePrefix() + "_users")
            .withColumn(DataColumns.ID)
            .withColumn(DataColumns.USER_ID)
            .withColumn(DataColumns.USER_NAME)
            .withColumn(DataColumns.USER_SETTINGS)
            .build();

        this.priceDataTable = Table.builder(this.getTablePrefix() + "_" + Config.DATA_PRICE_TABLE.get())
            .withColumn(DataColumns.PRICE_PRODUCT_ID)
            .withColumn(DataColumns.PRICE_BUY_OFFSET)
            .withColumn(DataColumns.PRICE_SELL_OFFSET)
            .withColumn(DataColumns.PRICE_EXPIRE_DATE)
            .withColumn(DataColumns.PRICE_PURCHASES)
            .withColumn(DataColumns.PRICE_SALES)
            .build();

        this.globalStockDataTable = Table.builder(this.getTablePrefix() + "_" + Config.DATA_GLOBAL_STOCK_TABLE.get())
            .withColumn(DataColumns.STOCK_PRODUCT_ID)
            .withColumn(DataColumns.STOCK_UNITS)
            .withColumn(DataColumns.STOCK_RESTOCK_DATE)
            .build();

        this.playerLimitDataTable = Table.builder(this.getTablePrefix() + "_" + Config.DATA_PLAYER_LIMIT_TABLE.get())
            .withColumn(DataColumns.LIMIT_PLAYER_ID)
            .withColumn(DataColumns.LIMIT_PRODUCT_ID)
            .withColumn(DataColumns.LIMIT_PURCHASES)
            .withColumn(DataColumns.LIMIT_SALES)
            .withColumn(DataColumns.LIMIT_RESTOCK_DATE)
            .build();

        this.rotationDataTable = Table.builder(this.getTablePrefix() + "_" + Config.DATA_ROTATIONS_TABLE.get())
            .withColumn(DataColumns.ROTATION_ID)
            .withColumn(DataColumns.ROTATION_NEXT_ROTATION)
            .withColumn(DataColumns.ROTATION_PRODUCTS)
            .build();

        // LEGACY - START

        this.legacyPriceDataTable = Table.builder(this.getTablePrefix() + "_" + Config.DATA_LEGACY_PRICE_TABLE.get())
            .withColumn(DataColumns.ID)
            .withColumn(DataColumns.LEGACY_SHOP_ID)
            .withColumn(DataColumns.LEGACY_PRODUCT_ID)
            .withColumn(DataColumns.PRICE_BUY_OFFSET)
            .withColumn(DataColumns.PRICE_SELL_OFFSET)
            .withColumn(DataColumns.PRICE_EXPIRE_DATE)
            .withColumn(DataColumns.PRICE_PURCHASES)
            .withColumn(DataColumns.PRICE_SALES)
            .build();

        this.legacyStockDataTable = Table.builder(this.getTablePrefix() + "_" + Config.DATA_LEGACY_STOCKS_TABLE.get())
            .withColumn(DataColumns.ID)
            .withColumn(DataColumns.LEGACY_PRODUCT_ID)
            .withColumn(DataColumns.LEGACY_HOLDER_ID)
            .withColumn(DataColumns.LEGACY_STOCK_BUY)
            .withColumn(DataColumns.LEGACY_STOCK_SELL)
            .withColumn(DataColumns.STOCK_RESTOCK_DATE)
            .build();

        this.legacyRotationDataTable = Table.builder(this.getTablePrefix() + "_" + Config.DATA_LEGACY_ROTATIONS_TABLE.get())
            .withColumn(DataColumns.ID)
            .withColumn(DataColumns.LEGACY_SHOP_ID)
            .withColumn(DataColumns.LEGACY_HOLDER_ID)
            .withColumn(DataColumns.ROTATION_NEXT_ROTATION)
            .withColumn(DataColumns.ROTATION_PRODUCTS)
            .build();

        // LEGACY - END
    }

    @Override
    protected void onInitialize() {
        this.createTable(this.userTable);
        this.createTable(this.priceDataTable);
        this.createTable(this.globalStockDataTable);
        this.createTable(this.playerLimitDataTable);
        this.createTable(this.rotationDataTable);

        this.dropColumn(this.userTable, "dateCreated", "last_online");

        this.hasLegacyStocks = SQLStatements.hasTable(this.connector, this.legacyStockDataTable.getName());
        this.hasLegacyPrices = SQLStatements.hasTable(this.connector, this.legacyPriceDataTable.getName());
        this.hasLegacyRotations = SQLStatements.hasTable(this.connector, this.legacyRotationDataTable.getName());

        if (this.hasLegacyPrices) {
            this.dropColumn(this.legacyPriceDataTable, "lastBuyPrice", "lastSellPrice", "lastUpdated");
        }
        if (this.hasLegacyRotations) {
            this.dropColumn(this.legacyRotationDataTable, "products");
        }
    }

    @Override
    public void onPurge() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(this.getConfig().getPurgePeriod());
        long deadlineMs = TimeUtil.toEpochMillis(deadline);

        this.delete(this.priceDataTable, Wheres.where(DataColumns.PRICE_EXPIRE_DATE, Operator.SMALLER, o -> deadlineMs));
        this.delete(this.globalStockDataTable, Wheres.where(DataColumns.STOCK_RESTOCK_DATE, Operator.SMALLER, o -> deadlineMs));
        this.delete(this.playerLimitDataTable, Wheres.where(DataColumns.LIMIT_RESTOCK_DATE, Operator.SMALLER, o -> deadlineMs));
        this.delete(this.rotationDataTable, Wheres.where(DataColumns.ROTATION_NEXT_ROTATION, Operator.SMALLER, o -> deadlineMs));
    }

    @Override
    public void onSynchronize() {
        this.synchronizer.syncAll();
        this.onSyncActions.forEach(Runnable::run);
    }

    @Override
    protected void onClose() {
        this.onSyncActions.clear();
    }

    public boolean hasLegacyStocks() {
        return this.hasLegacyStocks;
    }

    public boolean hasLegacyPrices() {
        return this.hasLegacyPrices;
    }

    public boolean isHasLegacyRotations() {
        return this.hasLegacyRotations;
    }

    public void addSyncAction(@NonNull Runnable runnable) {
        this.onSyncActions.add(runnable);
    }

    public void addPriceDataSync(@NonNull Consumer<ProductPriceData> consumer) {
        this.addDataSync(this.priceDataTable, DataQueries.PRICE_DATA_LOADER, consumer);
    }

    public void addStockDataSync(@NonNull Consumer<ProductStockData> consumer) {
        this.addDataSync(this.globalStockDataTable, DataQueries.STOCK_DATA_LOADER, consumer);
    }

    public void addLimitDataSync(@NonNull Consumer<ProductLimitData> consumer) {
        this.addDataSync(this.playerLimitDataTable, DataQueries.LIMIT_DATA_LOADER, consumer);
    }

    public void addRotationDataSync(@NonNull Consumer<RotationData> consumer) {
        this.addDataSync(this.rotationDataTable, DataQueries.ROTATION_DATA_LOADER, consumer);
    }

    private <T> void addDataSync(@NonNull Table table, @NonNull RowMapper<T> mapper, @NonNull Consumer<T> consumer) {
        this.addTableSync(table, resultSet -> {
            try {
                T data = mapper.map(resultSet);
                if (data != null) {
                    consumer.accept(data);
                }
            }
            catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    @NonNull
    public Table getUsersTable() {
        return this.userTable;
    }

    @Override
    @NonNull
    public Column<UUID> getUserIdColumn() {
        return DataColumns.USER_ID;
    }

    @Override
    @NonNull
    public Column<String> getUserNameColumn() {
        return DataColumns.USER_NAME;
    }

    @Override
    @NonNull
    public SelectStatement<ShopUser> getUserSelectStatement() {
        return SelectStatement.builder(DataQueries.SHOP_USER_MAPPER).build();
    }

    @Override
    @NonNull
    public InsertStatement<ShopUser> getUserInsertStatement() {
        return DataQueries.USER_INSERT;
    }

    @Override
    @NonNull
    public UpdateStatement<ShopUser> getUserUpdateStatement() {
        return DataQueries.USER_UPDATE;
    }

    @Override
    @NonNull
    public UpdateStatement<ShopUser> getUserTinyUpdateStatement() {
        return DataQueries.USER_TINY_UPDATE;
    }



    // LEGACY - START

    @NonNull
    public List<LegacyStockData> loadLegacyStockDatas() {
        return this.selectAny(this.legacyStockDataTable, SelectStatement.builder(DataQueries.LEGACY_STOCK_DATA_LOADER).build());
    }

    public void deleteLegacyStockData(@NonNull Collection<LegacyStockData> data) {
        this.delete(this.legacyStockDataTable, data, Wheres
            .where(DataColumns.LEGACY_PRODUCT_ID, Operator.EQUALS, LegacyStockData::getProductId)
            .and(DataColumns.LEGACY_SHOP_ID, Operator.EQUALS, LegacyStockData::getShopId)
            .and(DataColumns.LEGACY_HOLDER_ID, Operator.EQUALS, LegacyStockData::getHolder)
        );
    }


    @NonNull
    public List<LegacyPriceData> loadLegacyPriceDatas() {
        return this.selectAny(this.legacyPriceDataTable, SelectStatement.builder(DataQueries.LEGACY_PRICE_DATA_LOADER).build());
    }

    public void deleteLegacyPriceData(@NonNull Set<LegacyPriceData> data) {
        this.delete(this.legacyPriceDataTable, data, Wheres
            .where(DataColumns.LEGACY_PRODUCT_ID, Operator.EQUALS, LegacyPriceData::getProductId)
            .and(DataColumns.LEGACY_SHOP_ID, Operator.EQUALS, LegacyPriceData::getShopId)
        );
    }

    @NonNull
    public List<LegacyRotationData> loadLegacyRotationDatas() {
        return this.selectAny(this.legacyRotationDataTable, SelectStatement.builder(DataQueries.LEGACY_ROTATION_DATA_LOADER).build());
    }

    public void deleteLegacyRotationData(@NonNull Collection<LegacyRotationData> data) {
        this.delete(this.legacyRotationDataTable, data, Wheres
            .where(DataColumns.LEGACY_HOLDER_ID, Operator.EQUALS, LegacyRotationData::getRotationId)
            .and(DataColumns.LEGACY_SHOP_ID, Operator.EQUALS, LegacyRotationData::getShopId)
        );
    }

    // LEGACY - END

    @NonNull
    public List<ProductStockData> loadStockDatas() {
        return this.selectAny(this.globalStockDataTable, SelectStatement.builder(DataQueries.STOCK_DATA_LOADER).build());
    }

    @NonNull
    public List<ProductLimitData> loadLimitDatas() {
        return this.selectAny(this.playerLimitDataTable, SelectStatement.builder(DataQueries.LIMIT_DATA_LOADER).build());
    }

    @NonNull
    public List<ProductPriceData> loadPriceDatas() {
        return this.selectAny(this.priceDataTable, SelectStatement.builder(DataQueries.PRICE_DATA_LOADER).build());
    }

    @NonNull
    public List<RotationData> loadRotationDatas() {
        return this.selectAny(this.rotationDataTable, SelectStatement.builder(DataQueries.ROTATION_DATA_LOADER).build());
    }

    public void upsertStockData(@NonNull Collection<ProductStockData> data) {
        this.insert(this.globalStockDataTable, DataQueries.STOCK_DATA_INSERT, data);
    }

    public void upsertLimitData(@NonNull Collection<ProductLimitData> data) {
        this.insert(this.playerLimitDataTable, DataQueries.LIMIT_DATA_INSERT, data);
    }

    public void upsertPriceData(@NonNull Collection<ProductPriceData> data) {
        this.insert(this.priceDataTable, DataQueries.PRICE_DATA_INSERT, data);
    }

    public void upsertRotationData(@NonNull Collection<RotationData> rotationData) {
        this.insert(this.rotationDataTable, DataQueries.ROTATION_DATA_INSERT, rotationData);
    }

    /*public void updateStockDatas(@NonNull Set<ProductStockData> dataSet) {
        this.update(this.globalStockDataTable, DataQueries.STOCK_DATA_UPDATE, dataSet, Wheres
            .whereUUID(DataColumns.STOCK_PRODUCT_ID, ProductStockData::getProductId)
        );
    }

    public void updateLimitDatas(@NonNull Set<ProductLimitData> dataSet) {
        this.update(this.playerLimitDataTable, DataQueries.LIMIT_DATA_UPDATE, dataSet, Wheres
            .whereUUID(DataColumns.LIMIT_PLAYER_ID, ProductLimitData::getPlayerId)
            .and(DataColumns.LIMIT_PRODUCT_ID, Operator.EQUALS, data -> data.getProductId().toString())
        );
    }

    public void updatePriceDatas(@NonNull Set<ProductPriceData> dataSet) {
        this.update(this.priceDataTable, DataQueries.PRICE_DATA_UPDATE, dataSet, Wheres
            .whereUUID(DataColumns.PRICE_PRODUCT_ID, ProductPriceData::getProductId)
        );
    }

    public void updateRotationDatas(@NonNull Set<RotationData> dataSet) {
        this.update(this.rotationDataTable, DataQueries.ROTATION_DATA_UPDATE, dataSet, Wheres
            .whereUUID(DataColumns.ROTATION_ID, RotationData::getRotationId)
        );
    }*/


    public void deletePriceData(@NonNull Collection<ProductPriceData> dataSet) {
        this.delete(this.priceDataTable, dataSet, Wheres.whereUUID(DataColumns.PRICE_PRODUCT_ID, ProductPriceData::getProductId));
    }

    public void deleteStockData(@NonNull Collection<ProductStockData> dataSet) {
        this.delete(this.globalStockDataTable, dataSet, Wheres.whereUUID(DataColumns.STOCK_PRODUCT_ID, ProductStockData::getProductId));
    }

    public void deleteLimitData(@NonNull Collection<ProductLimitData> dataSet) {
        this.delete(this.playerLimitDataTable, dataSet, Wheres
            .whereUUID(DataColumns.LIMIT_PLAYER_ID, ProductLimitData::getPlayerId)
            .and(DataColumns.LIMIT_PRODUCT_ID, Operator.EQUALS, data -> data.getProductId().toString())
        );
    }

    public void deleteRotationData(@NonNull Collection<RotationData> dataSet) {
        this.delete(this.rotationDataTable, dataSet, Wheres.whereUUID(DataColumns.ROTATION_ID, RotationData::getRotationId));
    }
}
