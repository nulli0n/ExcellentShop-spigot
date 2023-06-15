package su.nightexpress.nexshop.data;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserDataHandler;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLCondition;
import su.nexmedia.engine.api.data.sql.SQLQueries;
import su.nexmedia.engine.api.data.sql.SQLValue;
import su.nexmedia.engine.api.data.sql.column.ColumnType;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.price.ProductPriceData;
import su.nightexpress.nexshop.data.stock.ProductStockData;
import su.nightexpress.nexshop.data.stock.ProductStockStorage;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.data.user.UserSettings;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class DataHandler extends AbstractUserDataHandler<ExcellentShop, ShopUser> {

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

    private static DataHandler INSTANCE;

    private final String                                tableStockData;
    private final String                                tablePriceData;
    private final Function<ResultSet, ShopUser>         funcUser;
    private final Function<ResultSet, ProductStockData> funcStockData;
    private final Function<ResultSet, ProductPriceData> funcPriceData;

    protected DataHandler(@NotNull ExcellentShop plugin) {
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
                TradeType tradeType = StringUtil.getEnum(resultSet.getString(COL_STOCK_TRADE_TYPE.getName()), TradeType.class).orElse(null);
                if (tradeType == null) return null;

                StockType stockType = StringUtil.getEnum(resultSet.getString(COL_STOCK_TYPE.getName()), StockType.class).orElse(null);
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
    public static DataHandler getInstance(@NotNull ExcellentShop plugin) {
        if (INSTANCE == null) {
            INSTANCE = new DataHandler(plugin);
        }
        return INSTANCE;
    }

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

    @Override
    public void onSynchronize() {
        VirtualShopModule module = this.plugin.getVirtualShop();
        if (module != null) {
            module.updateShopPricesStocks();
        }
        this.plugin.getUserManager().getUsersLoaded().forEach(ProductStockStorage::loadData);
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
    }

    @NotNull
    public List<ProductPriceData> getProductPriceData(@NotNull String shopId) {
        return this.load(this.tablePriceData, this.funcPriceData,
            Collections.emptyList(),
            Collections.singletonList(SQLCondition.equal(COL_GEN_SHOP_ID.toValue(shopId))), -1
        );
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
    }

    public void removeProductPriceData(@NotNull Product<?, ?, ?> product) {
        this.delete(this.tablePriceData,
            SQLCondition.equal(COL_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COL_GEN_PRODUCT_ID.toValue(product.getId()))
        );
    }
}
