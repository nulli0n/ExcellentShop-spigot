package su.nightexpress.nexshop.data.impl;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLCondition;
import su.nexmedia.engine.api.data.sql.SQLQueries;
import su.nexmedia.engine.api.data.sql.column.ColumnType;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.data.price.ProductPriceData;
import su.nightexpress.nexshop.data.rotation.ShopRotationData;
import su.nightexpress.nexshop.data.stock.ProductStockData;
import su.nightexpress.nexshop.data.stock.ProductStockStorage;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class VirtualDataHandler {

    private static final SQLColumn COLUMN_GEN_HOLDER     = SQLColumn.of("holder", ColumnType.STRING);
    private static final SQLColumn COLUMN_GEN_SHOP_ID    = SQLColumn.of("shopId", ColumnType.STRING);
    private static final SQLColumn COLUMN_GEN_PRODUCT_ID = SQLColumn.of("productId", ColumnType.STRING);

    private static final SQLColumn COLUMN_STOCK_TRADE_TYPE   = SQLColumn.of("tradeType", ColumnType.STRING);
    private static final SQLColumn COLUMN_STOCK_TYPE         = SQLColumn.of("stockType", ColumnType.STRING);
    private static final SQLColumn COLUMN_STOCK_RESTOCK_DATE = SQLColumn.of("restockDate", ColumnType.LONG);
    private static final SQLColumn COLUMN_STOCK_ITEMS_LEFT   = SQLColumn.of("itemsLeft", ColumnType.INTEGER);
    private static final SQLColumn COLUMN_PRICE_LAST_BUY     = SQLColumn.of("lastBuyPrice", ColumnType.DOUBLE);
    private static final SQLColumn COLUMN_PRICE_LAST_SELL    = SQLColumn.of("lastSellPrice", ColumnType.DOUBLE);
    private static final SQLColumn COLUMN_PRICE_LAST_UPDATED = SQLColumn.of("lastUpdated", ColumnType.LONG);
    private static final SQLColumn COLUMN_PRICE_PURCHASES    = SQLColumn.of("purchases", ColumnType.INTEGER);
    private static final SQLColumn COLUMN_PRICE_SALES        = SQLColumn.of("sales", ColumnType.INTEGER);
    private static final SQLColumn COLUMN_ROTATE_PRODUCTS    = SQLColumn.of("products", ColumnType.STRING);

    private final DataHandler dataHandler;
    private final String      tableStockData;
    private final String      tablePriceData;
    private final String      tableRotationData;

    private final Function<ResultSet, ProductStockData> funcStockData;
    private final Function<ResultSet, ProductPriceData> funcPriceData;
    private final Function<ResultSet, ShopRotationData> funcRotateData;

    public VirtualDataHandler(@NotNull DataHandler dataHandler) {
        this.dataHandler = dataHandler;
        this.tableStockData = dataHandler.getTablePrefix() + "_stock_data";
        this.tablePriceData = dataHandler.getTablePrefix() + "_price_data";
        this.tableRotationData = dataHandler.getTablePrefix() + "_rotation_data";

        this.funcStockData = resultSet -> {
            try {
                TradeType tradeType = StringUtil.getEnum(resultSet.getString(COLUMN_STOCK_TRADE_TYPE.getName()), TradeType.class).orElse(null);
                if (tradeType == null) return null;

                StockType stockType = StringUtil.getEnum(resultSet.getString(COLUMN_STOCK_TYPE.getName()), StockType.class).orElse(null);
                if (stockType == null) return null;

                String shopId = resultSet.getString(COLUMN_GEN_SHOP_ID.getName());
                String productId = resultSet.getString(COLUMN_GEN_PRODUCT_ID.getName());
                int itemsLeft = resultSet.getInt(COLUMN_STOCK_ITEMS_LEFT.getName());
                long restockDate = resultSet.getLong(COLUMN_STOCK_RESTOCK_DATE.getName());

                return new ProductStockData(tradeType, stockType, shopId, productId, itemsLeft, restockDate);
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };

        this.funcPriceData = resultSet -> {
            try {
                String shopId = resultSet.getString(COLUMN_GEN_SHOP_ID.getName());
                String productId = resultSet.getString(COLUMN_GEN_PRODUCT_ID.getName());
                double lastBuyPrice = resultSet.getDouble(COLUMN_PRICE_LAST_BUY.getName());
                double lastSellPrice = resultSet.getDouble(COLUMN_PRICE_LAST_SELL.getName());
                long lastUpdated = resultSet.getLong(COLUMN_PRICE_LAST_UPDATED.getName());
                int purchases = resultSet.getInt(COLUMN_PRICE_PURCHASES.getName());
                int sales = resultSet.getInt(COLUMN_PRICE_SALES.getName());

                return new ProductPriceData(shopId, productId, lastBuyPrice, lastSellPrice, lastUpdated, purchases, sales);
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };

        this.funcRotateData = resultSet -> {
            try {
                String shopId = resultSet.getString(COLUMN_GEN_SHOP_ID.getName());
                long lastRotated = resultSet.getLong(COLUMN_PRICE_LAST_UPDATED.getName());
                Set<String> products = dataHandler.gson().fromJson(resultSet.getString(COLUMN_ROTATE_PRODUCTS.getName()), new TypeToken<Set<String>>(){}.getType());

                return new ShopRotationData(shopId, lastRotated, products);
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    public void load() {
        this.dataHandler.createTable(this.tableStockData, Arrays.asList(
            COLUMN_GEN_HOLDER, COLUMN_GEN_SHOP_ID, COLUMN_GEN_PRODUCT_ID,
            COLUMN_STOCK_TRADE_TYPE, COLUMN_STOCK_TYPE, COLUMN_STOCK_ITEMS_LEFT, COLUMN_STOCK_RESTOCK_DATE
        ));

        this.dataHandler.createTable(this.tablePriceData, Arrays.asList(
            COLUMN_GEN_SHOP_ID, COLUMN_GEN_PRODUCT_ID,
            COLUMN_PRICE_LAST_BUY, COLUMN_PRICE_LAST_SELL, COLUMN_PRICE_LAST_UPDATED,
            COLUMN_PRICE_PURCHASES, COLUMN_PRICE_SALES
        ));

        this.dataHandler.createTable(this.tableRotationData, Arrays.asList(
            COLUMN_GEN_SHOP_ID, COLUMN_PRICE_LAST_UPDATED, COLUMN_ROTATE_PRODUCTS
        ));
    }

    public void purge() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(dataHandler.getConfig().purgePeriod);
        long deadlineMs = TimeUtil.toEpochMillis(deadline);

        if (SQLQueries.hasTable(dataHandler.getConnector(), this.tableStockData)) {
            String sql = "DELETE FROM " + this.tableStockData + " WHERE " + COLUMN_STOCK_RESTOCK_DATE.getName() + " < " + deadlineMs;
            SQLQueries.executeStatement(dataHandler.getConnector(), sql);
        }
        if (SQLQueries.hasTable(dataHandler.getConnector(), this.tablePriceData)) {
            String sql = "DELETE FROM " + this.tablePriceData + " WHERE " + COLUMN_PRICE_LAST_UPDATED.getName() + " < " + deadlineMs;
            SQLQueries.executeStatement(dataHandler.getConnector(), sql);
        }
        if (SQLQueries.hasTable(dataHandler.getConnector(), this.tableRotationData)) {
            String sql = "DELETE FROM " + this.tableRotationData + " WHERE " + COLUMN_PRICE_LAST_UPDATED.getName() + " < " + deadlineMs;
            SQLQueries.executeStatement(dataHandler.getConnector(), sql);
        }
    }

    public void synchronize() {
        VirtualShopModule module = this.dataHandler.plugin().getVirtualShop();
        if (module != null) {
            module.updateShopPricesStocks();
        }
        // TODO Rotation data
        this.dataHandler.plugin().getUserManager().getUsersLoaded().forEach(ProductStockStorage::loadData);
    }

    @NotNull
    public List<ProductStockData> getProductStockData(@NotNull String holder) {
        return this.dataHandler.load(this.tableStockData, this.funcStockData,
            Collections.emptyList(),
            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_HOLDER.toValue(holder))), -1
        );
    }

    @NotNull
    public List<ProductPriceData> getProductPriceData(@NotNull String shopId) {
        return this.dataHandler.load(this.tablePriceData, this.funcPriceData,
            Collections.emptyList(),
            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId))), -1
        );
    }

    @Nullable
    public ShopRotationData getShopRotationData(@NotNull String shopId) {
        return this.dataHandler.load(this.tableRotationData, this.funcRotateData,
            Collections.emptyList(),
            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId)))
        ).orElse(null);
    }

    public void createProductStockData(@NotNull String holder, @NotNull ProductStockData stockData) {
        this.dataHandler.insert(this.tableStockData, Arrays.asList(
            COLUMN_GEN_HOLDER.toValue(holder),
            COLUMN_GEN_SHOP_ID.toValue(stockData.getShopId()),
            COLUMN_GEN_PRODUCT_ID.toValue(stockData.getProductId()),
            COLUMN_STOCK_TRADE_TYPE.toValue(stockData.getTradeType().name()),
            COLUMN_STOCK_TYPE.toValue(stockData.getStockType().name()),
            COLUMN_STOCK_ITEMS_LEFT.toValue(stockData.getItemsLeft()),
            COLUMN_STOCK_RESTOCK_DATE.toValue(stockData.getRestockDate())
        ));
    }

    public void createProductPriceData(@NotNull ProductPriceData priceData) {
        this.dataHandler.insert(this.tablePriceData, Arrays.asList(
            COLUMN_GEN_SHOP_ID.toValue(priceData.getShopId()),
            COLUMN_GEN_PRODUCT_ID.toValue(priceData.getProductId()),
            COLUMN_PRICE_LAST_BUY.toValue(String.valueOf(priceData.getLastBuyPrice())),
            COLUMN_PRICE_LAST_SELL.toValue(String.valueOf(priceData.getLastSellPrice())),
            COLUMN_PRICE_LAST_UPDATED.toValue(String.valueOf(priceData.getLastUpdated())),
            COLUMN_PRICE_PURCHASES.toValue(String.valueOf(priceData.getPurchases())),
            COLUMN_PRICE_SALES.toValue(String.valueOf(priceData.getSales()))
        ));
    }

    public void createShopRotationData(@NotNull ShopRotationData rotationData) {
        this.dataHandler.insert(this.tableRotationData, Arrays.asList(
            COLUMN_GEN_SHOP_ID.toValue(rotationData.getShopId()),
            COLUMN_PRICE_LAST_UPDATED.toValue(rotationData.getLatestRotation()),
            COLUMN_ROTATE_PRODUCTS.toValue(dataHandler.gson().toJson(rotationData.getProducts()))
        ));
    }

    public void saveProductStockData(@NotNull String holder, @NotNull ProductStockData stockData) {
        this.dataHandler.update(this.tableStockData, Arrays.asList(
            COLUMN_STOCK_ITEMS_LEFT.toValue(stockData.getItemsLeft()),
            COLUMN_STOCK_RESTOCK_DATE.toValue(stockData.getRestockDate())
            ),
            SQLCondition.equal(COLUMN_GEN_HOLDER.toValue(holder)),
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(stockData.getShopId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(stockData.getProductId())),
            SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(stockData.getTradeType().name())),
            SQLCondition.equal(COLUMN_STOCK_TYPE.toValue(stockData.getStockType().name()))
        );
    }

    public void saveProductPriceData(@NotNull ProductPriceData priceData) {
        this.dataHandler.update(this.tablePriceData, Arrays.asList(
            COLUMN_PRICE_LAST_BUY.toValue(priceData.getLastBuyPrice()),
            COLUMN_PRICE_LAST_SELL.toValue(priceData.getLastSellPrice()),
            COLUMN_PRICE_LAST_UPDATED.toValue(priceData.getLastUpdated()),
            COLUMN_PRICE_PURCHASES.toValue(priceData.getPurchases()),
            COLUMN_PRICE_SALES.toValue(priceData.getSales())
            ),
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(priceData.getShopId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(priceData.getProductId()))
        );
    }

    public void saveShopRotationData(@NotNull ShopRotationData rotationData) {
        this.dataHandler.update(this.tableRotationData, Arrays.asList(
            COLUMN_PRICE_LAST_UPDATED.toValue(rotationData.getLatestRotation()),
            COLUMN_ROTATE_PRODUCTS.toValue(dataHandler.gson().toJson(rotationData.getProducts()))
            ),
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(rotationData.getShopId()))
        );
    }

    public void removeProductStockData(@NotNull String holder, @NotNull Product<?, ?, ?> product,
                                       @NotNull StockType stockType, @NotNull TradeType tradeType) {

        this.dataHandler.delete(this.tableStockData,
            SQLCondition.equal(COLUMN_GEN_HOLDER.toValue(holder)),
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId())),
            SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(tradeType.name())),
            SQLCondition.equal(COLUMN_STOCK_TYPE.toValue(stockType.name()))
        );
    }

    public void removeProductStockData(@NotNull VirtualProduct<?, ?> product) {

        this.dataHandler.delete(this.tableStockData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId()))
        );
    }

    public void removeProductPriceData(@NotNull Product<?, ?, ?> product) {
        this.dataHandler.delete(this.tablePriceData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId()))
        );
    }

    public void removeShopRotationData(@NotNull VirtualShop<?, ?> shop) {
        this.dataHandler.delete(this.tableRotationData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shop.getId()))
        );
    }
}
