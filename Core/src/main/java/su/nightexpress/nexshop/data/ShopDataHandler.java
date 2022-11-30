package su.nightexpress.nexshop.data;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserDataHandler;
import su.nexmedia.engine.api.data.DataQueries;
import su.nexmedia.engine.api.data.DataTypes;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.data.user.UserSettings;
import su.nightexpress.nexshop.data.price.ProductPriceData;
import su.nightexpress.nexshop.data.stock.ProductStockData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class ShopDataHandler extends AbstractUserDataHandler<ExcellentShop, ShopUser> {

    private static final String COL_GEN_HOLDER         = "holder";
    private static final String COL_GEN_SHOP_ID        = "shopId";
    private static final String COL_GEN_PRODUCT_ID     = "productId";
    private static final String COL_USER_SETTINGS      = "settings";
    private static final String COL_STOCK_TRADE_TYPE   = "tradeType";
    private static final String COL_STOCK_TYPE         = "stockType";
    private static final String COL_STOCK_RESTOCK_DATE = "restockDate";
    private static final String COL_STOCK_ITEMS_LEFT   = "itemsLeft";
    private static final String COL_PRICE_LAST_BUY     = "lastBuyPrice";
    private static final String COL_PRICE_LAST_SELL    = "lastSellPrice";
    private static final String COL_PRICE_LAST_UPDATED = "lastUpdated";
    private static final String COL_PRICE_PURCHASES    = "purchases";
    private static final String COL_PRICE_SALES        = "sales";

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
                UUID uuid = UUID.fromString(resultSet.getString(COL_USER_UUID));
                String name = resultSet.getString(COL_USER_NAME);
                long dateCreated = resultSet.getLong(COL_USER_DATE_CREATED);
                long date = resultSet.getLong(COL_USER_LAST_ONLINE);

                UserSettings settings = gson.fromJson(resultSet.getString(COL_USER_SETTINGS), new TypeToken<UserSettings>() {
                }.getType());

                return new ShopUser(plugin, uuid, name, dateCreated, date, settings);
            }
            catch (SQLException e) {
                return null;
            }
        };

        this.funcStockData = resultSet -> {
            try {
                TradeType tradeType = CollectionsUtil.getEnum(resultSet.getString(COL_STOCK_TRADE_TYPE), TradeType.class);
                if (tradeType == null) return null;

                StockType stockType = CollectionsUtil.getEnum(resultSet.getString(COL_STOCK_TYPE), StockType.class);
                if (stockType == null) return null;

                String shopId = resultSet.getString(COL_GEN_SHOP_ID);
                String productId = resultSet.getString(COL_GEN_PRODUCT_ID);
                int itemsLeft = resultSet.getInt(COL_STOCK_ITEMS_LEFT);
                long restockDate = resultSet.getLong(COL_STOCK_RESTOCK_DATE);

                return new ProductStockData(tradeType, stockType, shopId, productId, itemsLeft, restockDate);
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };

        this.funcPriceData = resultSet -> {
            try {
                String shopId = resultSet.getString(COL_GEN_SHOP_ID);
                String productId = resultSet.getString(COL_GEN_PRODUCT_ID);
                double lastBuyPrice = resultSet.getDouble(COL_PRICE_LAST_BUY);
                double lastSellPrice = resultSet.getDouble(COL_PRICE_LAST_SELL);
                long lastUpdated = resultSet.getLong(COL_PRICE_LAST_UPDATED);
                int purchases = resultSet.getInt(COL_PRICE_PURCHASES);
                int sales = resultSet.getInt(COL_PRICE_SALES);

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

        if (this.hasTable(this.tableStockData)) {
            String sql = "DELETE FROM " + this.tableStockData + " WHERE " + COL_STOCK_RESTOCK_DATE + " < " + deadlineMs;
            DataQueries.executeStatement(this.getConnector(), sql);
        }
        if (this.hasTable(this.tablePriceData)) {
            String sql = "DELETE FROM " + this.tablePriceData + " WHERE " + COL_PRICE_LAST_UPDATED + " < " + deadlineMs;
            DataQueries.executeStatement(this.getConnector(), sql);
        }
    }

    @Override
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
    }

    @Override
    @NotNull
    protected Function<ResultSet, ShopUser> getFunctionToUser() {
        return this.funcUser;
    }

    @Override
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
    }

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
        Map<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_HOLDER, holder);

        return DataQueries.readData(this.getConnector(), this.tableStockData, whereMap, this.funcStockData, -1);
    }

    @NotNull
    public List<ProductPriceData> getProductPriceData(@NotNull String shopId) {
        Map<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_SHOP_ID, shopId);

        return DataQueries.readData(this.getConnector(), this.tablePriceData, whereMap, this.funcPriceData, -1);
    }

    public void createProductStockData(@NotNull String holder, @NotNull ProductStockData stockData) {
        LinkedHashMap<String, String> keys = new LinkedHashMap<>();
        keys.put(COL_GEN_HOLDER, holder);
        keys.put(COL_GEN_SHOP_ID, stockData.getShopId());
        keys.put(COL_GEN_PRODUCT_ID, stockData.getProductId());
        keys.put(COL_STOCK_TRADE_TYPE, stockData.getTradeType().name());
        keys.put(COL_STOCK_TYPE, stockData.getStockType().name());
        keys.put(COL_STOCK_ITEMS_LEFT, String.valueOf(stockData.getItemsLeft()));
        keys.put(COL_STOCK_RESTOCK_DATE, String.valueOf(stockData.getRestockDate()));

        DataQueries.executeInsert(this.getConnector(), this.tableStockData, keys);
    }

    public void createProductPriceData(@NotNull ProductPriceData priceData) {
        LinkedHashMap<String, String> keys = new LinkedHashMap<>();
        keys.put(COL_GEN_SHOP_ID, priceData.getShopId());
        keys.put(COL_GEN_PRODUCT_ID, priceData.getProductId());
        keys.put(COL_PRICE_LAST_BUY, String.valueOf(priceData.getLastBuyPrice()));
        keys.put(COL_PRICE_LAST_SELL, String.valueOf(priceData.getLastSellPrice()));
        keys.put(COL_PRICE_LAST_UPDATED, String.valueOf(priceData.getLastUpdated()));
        keys.put(COL_PRICE_PURCHASES, String.valueOf(priceData.getPurchases()));
        keys.put(COL_PRICE_SALES, String.valueOf(priceData.getSales()));

        DataQueries.executeInsert(this.getConnector(), this.tablePriceData, keys);
    }

    public void saveProductStockData(@NotNull String holder, @NotNull ProductStockData stockData) {
        LinkedHashMap<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_HOLDER, holder);
        whereMap.put(COL_GEN_SHOP_ID, stockData.getShopId());
        whereMap.put(COL_GEN_PRODUCT_ID, stockData.getProductId());
        whereMap.put(COL_STOCK_TRADE_TYPE, stockData.getTradeType().name());
        whereMap.put(COL_STOCK_TYPE, stockData.getStockType().name());

        LinkedHashMap<String, String> valuesMap = new LinkedHashMap<>();
        valuesMap.put(COL_STOCK_ITEMS_LEFT, String.valueOf(stockData.getItemsLeft()));
        valuesMap.put(COL_STOCK_RESTOCK_DATE, String.valueOf(stockData.getRestockDate()));

        DataQueries.executeUpdate(this.getConnector(), this.tableStockData, valuesMap, whereMap);
    }

    public void saveProductPriceData(@NotNull ProductPriceData priceData) {
        LinkedHashMap<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_SHOP_ID, priceData.getShopId());
        whereMap.put(COL_GEN_PRODUCT_ID, priceData.getProductId());

        LinkedHashMap<String, String> valuesMap = new LinkedHashMap<>();
        valuesMap.put(COL_PRICE_LAST_BUY, String.valueOf(priceData.getLastBuyPrice()));
        valuesMap.put(COL_PRICE_LAST_SELL, String.valueOf(priceData.getLastSellPrice()));
        valuesMap.put(COL_PRICE_LAST_UPDATED, String.valueOf(priceData.getLastUpdated()));
        valuesMap.put(COL_PRICE_PURCHASES, String.valueOf(priceData.getPurchases()));
        valuesMap.put(COL_PRICE_SALES, String.valueOf(priceData.getSales()));

        DataQueries.executeUpdate(this.getConnector(), this.tablePriceData, valuesMap, whereMap);
    }

    public void removeProductStockData(@NotNull String holder, @NotNull Product<?, ?, ?> product,
                                       @NotNull StockType stockType, @NotNull TradeType tradeType) {
        LinkedHashMap<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_HOLDER, holder);
        whereMap.put(COL_GEN_SHOP_ID, product.getShop().getId());
        whereMap.put(COL_GEN_PRODUCT_ID, product.getId());
        whereMap.put(COL_STOCK_TRADE_TYPE, tradeType.name());
        whereMap.put(COL_STOCK_TYPE, stockType.name());

        DataQueries.executeDelete(this.getConnector(), this.tableStockData, whereMap);
    }

    public void removeProductPriceData(@NotNull Product<?, ?, ?> product) {
        LinkedHashMap<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_GEN_SHOP_ID, product.getShop().getId());
        whereMap.put(COL_GEN_PRODUCT_ID, product.getId());

        DataQueries.executeDelete(this.getConnector(), this.tablePriceData, whereMap);
    }
}
