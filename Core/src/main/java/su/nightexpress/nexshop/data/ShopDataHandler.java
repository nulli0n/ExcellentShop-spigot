package su.nightexpress.nexshop.data;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserDataHandler;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLCondition;
import su.nexmedia.engine.api.data.sql.SQLQueries;
import su.nexmedia.engine.api.data.sql.SQLValue;
import su.nexmedia.engine.api.data.sql.column.ColumnType;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.price.ProductPriceData;
import su.nightexpress.nexshop.data.stock.ProductStockData;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.data.user.UserSettings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class ShopDataHandler extends AbstractUserDataHandler<ExcellentShop, ShopUser> {

    private static final SQLColumn COL_USER_SETTINGS      = SQLColumn.of("settings", ColumnType.STRING);

    private static final SQLColumn COL_GEN_HOLDER         = SQLColumn.of("holder", ColumnType.STRING);
    private static final SQLColumn COL_GEN_SHOP_ID        = SQLColumn.of("shopId", ColumnType.STRING);
    private static final SQLColumn COL_GEN_PRODUCT_ID     = SQLColumn.of("productId", ColumnType.STRING);
    private static final SQLColumn COL_STOCK_TRADE_TYPE   = SQLColumn.of("tradeType", ColumnType.STRING);
    private static final SQLColumn COL_STOCK_TYPE         = SQLColumn.of("stockType", ColumnType.STRING);
    private static final SQLColumn COL_STOCK_RESTOCK_DATE = SQLColumn.of("restockDate", ColumnType.LONG);
    private static final SQLColumn COL_STOCK_ITEMS_LEFT   = SQLColumn.of("itemsLeft", ColumnType.INTEGER);
    private static final SQLColumn COL_PRICE_LAST_BUY     = SQLColumn.of("lastBuyPrice", ColumnType.DOUBLE);
    private static final SQLColumn COL_PRICE_LAST_SELL    = SQLColumn.of("lastSellPrice", ColumnType.DOUBLE);
    private static final SQLColumn COL_PRICE_LAST_UPDATED = SQLColumn.of("lastUpdated", ColumnType.LONG);
    private static final SQLColumn COL_PRICE_PURCHASES    = SQLColumn.of("purchases", ColumnType.INTEGER);
    private static final SQLColumn COL_PRICE_SALES        = SQLColumn.of("sales", ColumnType.INTEGER);

    private static ShopDataHandler INSTANCE;

    private final String                                tableStockData;
    private final String                                tablePriceData;
    private final Function<ResultSet, ShopUser>         funcUser;
    private final Function<ResultSet, ProductStockData> funcStockData;
    private final Function<ResultSet, ProductPriceData> funcPriceData;

    protected ShopDataHandler(@NotNull ExcellentShop plugin) {
        super(plugin, plugin);
        this.tableStockData = this.getTablePrefix() + "_stock_data";
        this.tablePriceData = this.getTablePrefix() + "_price_data";

        this.funcUser = (resultSet) -> {
            try {
                UUID uuid = UUID.fromString(resultSet.getString(COLUMN_USER_ID.getName()));
                String name = resultSet.getString(COLUMN_USER_NAME.getName());
                long dateCreated = resultSet.getLong(COLUMN_USER_DATE_CREATED.getName());
                long date = resultSet.getLong(COLUMN_USER_LAST_ONLINE.getName());

                UserSettings settings = gson.fromJson(resultSet.getString(COL_USER_SETTINGS.getName()), new TypeToken<UserSettings>() {
                }.getType());

                return new ShopUser(plugin, uuid, name, dateCreated, date, settings);
            }
            catch (SQLException e) {
                return null;
            }
        };

        this.funcStockData = resultSet -> {
            try {
                TradeType tradeType = CollectionsUtil.getEnum(resultSet.getString(COL_STOCK_TRADE_TYPE.getName()), TradeType.class);
                if (tradeType == null) return null;

                StockType stockType = CollectionsUtil.getEnum(resultSet.getString(COL_STOCK_TYPE.getName()), StockType.class);
                if (stockType == null) return null;

                String shopId = resultSet.getString(COL_GEN_SHOP_ID.getName());
                String productId = resultSet.getString(COL_GEN_PRODUCT_ID.getName());
                int itemsLeft = resultSet.getInt(COL_STOCK_ITEMS_LEFT.getName());
                long restockDate = resultSet.getLong(COL_STOCK_RESTOCK_DATE.getName());

                return new ProductStockData(tradeType, stockType, shopId, productId, itemsLeft, restockDate);
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };

        this.funcPriceData = resultSet -> {
            try {
                String shopId = resultSet.getString(COL_GEN_SHOP_ID.getName());
                String productId = resultSet.getString(COL_GEN_PRODUCT_ID.getName());
                double lastBuyPrice = resultSet.getDouble(COL_PRICE_LAST_BUY.getName());
                double lastSellPrice = resultSet.getDouble(COL_PRICE_LAST_SELL.getName());
                long lastUpdated = resultSet.getLong(COL_PRICE_LAST_UPDATED.getName());
                int purchases = resultSet.getInt(COL_PRICE_PURCHASES.getName());
                int sales = resultSet.getInt(COL_PRICE_SALES.getName());

                return new ProductPriceData(shopId, productId, lastBuyPrice, lastSellPrice, lastUpdated, purchases, sales);
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    @NotNull
    public static ShopDataHandler getInstance(@NotNull ExcellentShop plugin) {
        if (INSTANCE == null) {
            INSTANCE = new ShopDataHandler(plugin);
        }
        return INSTANCE;
    }
	
	/*@Override
	@NotNull
	protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
		return super.registerAdapters(builder)
            .registerTypeAdapter(ProductStockData.class, new ProductStockDataSerializer())
            ;
	}*/

    @Override
    protected void onShutdown() {
        super.onShutdown();
        INSTANCE = null;
    }

    @Override
    public void onPurge() {
        super.onPurge();

        LocalDateTime deadline = LocalDateTime.now().minusDays(this.getConfig().purgePeriod);
        long deadlineMs = TimeUtil.toEpochMillis(deadline);

        if (SQLQueries.hasTable(this.getConnector(), this.tableStockData)) {
            String sql = "DELETE FROM " + this.tableStockData + " WHERE " + COL_STOCK_RESTOCK_DATE.getName() + " < " + deadlineMs;
            SQLQueries.executeStatement(this.getConnector(), sql);
        }
        if (SQLQueries.hasTable(this.getConnector(), this.tablePriceData)) {
            String sql = "DELETE FROM " + this.tablePriceData + " WHERE " + COL_PRICE_LAST_UPDATED.getName() + " < " + deadlineMs;
            SQLQueries.executeStatement(this.getConnector(), sql);
        }
    }

    @Override
    @NotNull
    protected List<SQLColumn> getExtraColumns() {
        return Collections.singletonList(COL_USER_SETTINGS);
    }

    @Override
    @NotNull
    protected List<SQLValue> getSaveColumns(@NotNull ShopUser shopUser) {
        return Collections.singletonList(
            COL_USER_SETTINGS.toValue(this.gson.toJson(shopUser.getSettings()))
        );
    }

    /*@Override
    @NotNull
    protected LinkedHashMap<String, String> getColumnsToCreate() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(COL_USER_SETTINGS, DataTypes.STRING.build(this.getDataType()));
        return map;
    }

    @Override
    @NotNull
    protected LinkedHashMap<String, String> getColumnsToSave(@NotNull ShopUser user) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(COL_USER_SETTINGS, this.gson.toJson(user.getSettings()));
        return map;
    }*/

    @Override
    @NotNull
    protected Function<ResultSet, ShopUser> getFunctionToUser() {
        return this.funcUser;
    }

    @Override
    protected void createUserTable() {
        super.createUserTable();

        this.dropColumn(this.tableUsers,
            SQLColumn.of("virtualshop_limits", ColumnType.STRING),
            SQLColumn.of("virtual_limits", ColumnType.STRING));

        this.createTable(this.tableStockData, Arrays.asList(
            COL_GEN_HOLDER, COL_GEN_SHOP_ID, COL_GEN_PRODUCT_ID,
            COL_STOCK_TRADE_TYPE, COL_STOCK_TYPE, COL_STOCK_ITEMS_LEFT, COL_STOCK_RESTOCK_DATE
        ));

        this.createTable(this.tablePriceData, Arrays.asList(
            COL_GEN_SHOP_ID, COL_GEN_PRODUCT_ID,
            COL_PRICE_LAST_BUY, COL_PRICE_LAST_SELL, COL_PRICE_LAST_UPDATED,
            COL_PRICE_PURCHASES, COL_PRICE_SALES
        ));
    }

    /*@Override
    protected void onTableCreate() {
        super.onTableCreate();
        this.removeColumn(this.tableUsers, "virtualshop_limits");
        this.removeColumn(this.tableUsers, "virtual_limits");

        LinkedHashMap<String, String> mapStock = new LinkedHashMap<>();
        mapStock.put(COL_GEN_HOLDER, DataTypes.STRING.build(this.getDataType()));
        mapStock.put(COL_GEN_SHOP_ID, DataTypes.STRING.build(this.getDataType()));
        mapStock.put(COL_GEN_PRODUCT_ID, DataTypes.STRING.build(this.getDataType()));
        mapStock.put(COL_STOCK_TRADE_TYPE, DataTypes.STRING.build(this.getDataType()));
        mapStock.put(COL_STOCK_TYPE, DataTypes.STRING.build(this.getDataType()));
        mapStock.put(COL_STOCK_ITEMS_LEFT, DataTypes.INTEGER.build(this.getDataType()));
        mapStock.put(COL_STOCK_RESTOCK_DATE, DataTypes.LONG.build(this.getDataType()));
        this.createTable(this.tableStockData, mapStock);

        LinkedHashMap<String, String> mapPrice = new LinkedHashMap<>();
        mapPrice.put(COL_GEN_SHOP_ID, DataTypes.STRING.build(this.getDataType()));
        mapPrice.put(COL_GEN_PRODUCT_ID, DataTypes.STRING.build(this.getDataType()));
        mapPrice.put(COL_PRICE_LAST_BUY, DataTypes.DOUBLE.build(this.getDataType()));
        mapPrice.put(COL_PRICE_LAST_SELL, DataTypes.DOUBLE.build(this.getDataType()));
        mapPrice.put(COL_PRICE_LAST_UPDATED, DataTypes.LONG.build(this.getDataType()));
        mapPrice.put(COL_PRICE_PURCHASES, DataTypes.INTEGER.build(this.getDataType()));
        mapPrice.put(COL_PRICE_SALES, DataTypes.INTEGER.build(this.getDataType()));
        this.createTable(this.tablePriceData, mapPrice);
    }*/

    @Override
    public void onSynchronize() {
        // TODO Sync virtual shop limits
    }

    @NotNull
    public Function<ResultSet, ProductStockData> getFunctionStockData() {
        return funcStockData;
    }

    @NotNull
    public Function<ResultSet, ProductPriceData> getFunctionPriceData() {
        return funcPriceData;
    }

    @NotNull
    public List<ProductStockData> getProductStockData(@NotNull String holder) {
        return this.load(this.tableStockData, this.funcStockData,
            Collections.emptyList(),
            Collections.singletonList(SQLCondition.equal(COL_GEN_HOLDER.toValue(holder))), -1
        );

        /*Map<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_HOLDER, holder);

        return DataQueries.readData(this.getConnector(), this.tableStockData, whereMap, this.funcStockData, -1);*/
    }

    @NotNull
    public List<ProductPriceData> getProductPriceData(@NotNull String shopId) {
        return this.load(this.tablePriceData, this.funcPriceData,
            Collections.emptyList(),
            Collections.singletonList(SQLCondition.equal(COL_GEN_SHOP_ID.toValue(shopId))), -1
        );

        /*Map<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_SHOP_ID, shopId);

        return DataQueries.readData(this.getConnector(), this.tablePriceData, whereMap, this.funcPriceData, -1);*/
    }

    public void createProductStockData(@NotNull String holder, @NotNull ProductStockData stockData) {
        this.insert(this.tableStockData, Arrays.asList(
            COL_GEN_HOLDER.toValue(holder),
            COL_GEN_SHOP_ID.toValue(stockData.getShopId()),
            COL_GEN_PRODUCT_ID.toValue(stockData.getProductId()),
            COL_STOCK_TRADE_TYPE.toValue(stockData.getTradeType().name()),
            COL_STOCK_TYPE.toValue(stockData.getStockType().name()),
            COL_STOCK_ITEMS_LEFT.toValue(stockData.getItemsLeft()),
            COL_STOCK_RESTOCK_DATE.toValue(stockData.getRestockDate())
        ));

        /*LinkedHashMap<String, String> keys = new LinkedHashMap<>();
        keys.put(COL_GEN_HOLDER, holder);
        keys.put(COL_GEN_SHOP_ID, stockData.getShopId());
        keys.put(COL_GEN_PRODUCT_ID, stockData.getProductId());
        keys.put(COL_STOCK_TRADE_TYPE, stockData.getTradeType().name());
        keys.put(COL_STOCK_TYPE, stockData.getStockType().name());
        keys.put(COL_STOCK_ITEMS_LEFT, String.valueOf(stockData.getItemsLeft()));
        keys.put(COL_STOCK_RESTOCK_DATE, String.valueOf(stockData.getRestockDate()));

        DataQueries.executeInsert(this.getConnector(), this.tableStockData, keys);*/
    }

    public void createProductPriceData(@NotNull ProductPriceData priceData) {
        this.insert(this.tablePriceData, Arrays.asList(
            COL_GEN_SHOP_ID.toValue(priceData.getShopId()),
            COL_GEN_PRODUCT_ID.toValue(priceData.getProductId()),
            COL_PRICE_LAST_BUY.toValue(String.valueOf(priceData.getLastBuyPrice())),
            COL_PRICE_LAST_SELL.toValue(String.valueOf(priceData.getLastSellPrice())),
            COL_PRICE_LAST_UPDATED.toValue(String.valueOf(priceData.getLastUpdated())),
            COL_PRICE_PURCHASES.toValue(String.valueOf(priceData.getPurchases())),
            COL_PRICE_SALES.toValue(String.valueOf(priceData.getSales()))
        ));

        /*LinkedHashMap<String, String> keys = new LinkedHashMap<>();
        keys.put(COL_GEN_SHOP_ID, priceData.getShopId());
        keys.put(COL_GEN_PRODUCT_ID, priceData.getProductId());
        keys.put(COL_PRICE_LAST_BUY, String.valueOf(priceData.getLastBuyPrice()));
        keys.put(COL_PRICE_LAST_SELL, String.valueOf(priceData.getLastSellPrice()));
        keys.put(COL_PRICE_LAST_UPDATED, String.valueOf(priceData.getLastUpdated()));
        keys.put(COL_PRICE_PURCHASES, String.valueOf(priceData.getPurchases()));
        keys.put(COL_PRICE_SALES, String.valueOf(priceData.getSales()));

        DataQueries.executeInsert(this.getConnector(), this.tablePriceData, keys);*/
    }

    public void saveProductStockData(@NotNull String holder, @NotNull ProductStockData stockData) {
        this.update(this.tableStockData, Arrays.asList(
            COL_STOCK_ITEMS_LEFT.toValue(stockData.getItemsLeft()),
            COL_STOCK_RESTOCK_DATE.toValue(stockData.getRestockDate())
        ),
            SQLCondition.equal(COL_GEN_HOLDER.toValue(holder)),
            SQLCondition.equal(COL_GEN_SHOP_ID.toValue(stockData.getShopId())),
            SQLCondition.equal(COL_GEN_PRODUCT_ID.toValue(stockData.getProductId())),
            SQLCondition.equal(COL_STOCK_TRADE_TYPE.toValue(stockData.getTradeType().name())),
            SQLCondition.equal(COL_STOCK_TYPE.toValue(stockData.getStockType().name()))
        );

        /*LinkedHashMap<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_HOLDER, holder);
        whereMap.put(COL_GEN_SHOP_ID, stockData.getShopId());
        whereMap.put(COL_GEN_PRODUCT_ID, stockData.getProductId());
        whereMap.put(COL_STOCK_TRADE_TYPE, stockData.getTradeType().name());
        whereMap.put(COL_STOCK_TYPE, stockData.getStockType().name());

        LinkedHashMap<String, String> valuesMap = new LinkedHashMap<>();
        valuesMap.put(COL_STOCK_ITEMS_LEFT, String.valueOf(stockData.getItemsLeft()));
        valuesMap.put(COL_STOCK_RESTOCK_DATE, String.valueOf(stockData.getRestockDate()));

        DataQueries.executeUpdate(this.getConnector(), this.tableStockData, valuesMap, whereMap);*/
    }

    public void saveProductPriceData(@NotNull ProductPriceData priceData) {
        this.update(this.tablePriceData, Arrays.asList(
            COL_PRICE_LAST_BUY.toValue(priceData.getLastBuyPrice()),
            COL_PRICE_LAST_SELL.toValue(priceData.getLastSellPrice()),
            COL_PRICE_LAST_UPDATED.toValue(priceData.getLastUpdated()),
            COL_PRICE_PURCHASES.toValue(priceData.getPurchases()),
            COL_PRICE_SALES.toValue(priceData.getSales())
        ),
            SQLCondition.equal(COL_GEN_SHOP_ID.toValue(priceData.getShopId())),
            SQLCondition.equal(COL_GEN_PRODUCT_ID.toValue(priceData.getProductId()))
        );

        /*LinkedHashMap<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_SHOP_ID, priceData.getShopId());
        whereMap.put(COL_GEN_PRODUCT_ID, priceData.getProductId());

        LinkedHashMap<String, String> valuesMap = new LinkedHashMap<>();
        valuesMap.put(COL_PRICE_LAST_BUY, String.valueOf(priceData.getLastBuyPrice()));
        valuesMap.put(COL_PRICE_LAST_SELL, String.valueOf(priceData.getLastSellPrice()));
        valuesMap.put(COL_PRICE_LAST_UPDATED, String.valueOf(priceData.getLastUpdated()));
        valuesMap.put(COL_PRICE_PURCHASES, String.valueOf(priceData.getPurchases()));
        valuesMap.put(COL_PRICE_SALES, String.valueOf(priceData.getSales()));

        DataQueries.executeUpdate(this.getConnector(), this.tablePriceData, valuesMap, whereMap);*/
    }

    public void removeProductStockData(@NotNull String holder, @NotNull Product<?, ?, ?> product,
                                       @NotNull StockType stockType, @NotNull TradeType tradeType) {

        this.delete(this.tableStockData,
            SQLCondition.equal(COL_GEN_HOLDER.toValue(holder)),
            SQLCondition.equal(COL_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COL_GEN_PRODUCT_ID.toValue(product.getId())),
            SQLCondition.equal(COL_STOCK_TRADE_TYPE.toValue(tradeType.name())),
            SQLCondition.equal(COL_STOCK_TYPE.toValue(stockType.name()))
        );

        /*LinkedHashMap<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_HOLDER, holder);
        whereMap.put(COL_GEN_SHOP_ID, product.getShop().getId());
        whereMap.put(COL_GEN_PRODUCT_ID, product.getId());
        whereMap.put(COL_STOCK_TRADE_TYPE, tradeType.name());
        whereMap.put(COL_STOCK_TYPE, stockType.name());

        DataQueries.executeDelete(this.getConnector(), this.tableStockData, whereMap);*/
    }

    public void removeProductPriceData(@NotNull Product<?, ?, ?> product) {
        this.delete(this.tablePriceData,
            SQLCondition.equal(COL_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COL_GEN_PRODUCT_ID.toValue(product.getId()))
        );

        /*LinkedHashMap<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_SHOP_ID, product.getShop().getId());
        whereMap.put(COL_GEN_PRODUCT_ID, product.getId());

        DataQueries.executeDelete(this.getConnector(), this.tablePriceData, whereMap);*/
    }
}
