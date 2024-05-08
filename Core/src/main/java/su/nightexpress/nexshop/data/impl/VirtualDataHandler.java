package su.nightexpress.nexshop.data.impl;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.data.object.OwnedStockData;
import su.nightexpress.nexshop.data.object.PriceData;
import su.nightexpress.nexshop.data.object.RotationData;
import su.nightexpress.nexshop.data.object.StockData;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nightcore.database.sql.SQLColumn;
import su.nightexpress.nightcore.database.sql.SQLCondition;
import su.nightexpress.nightcore.database.sql.SQLQueries;
import su.nightexpress.nightcore.database.sql.column.ColumnType;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.TimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public class VirtualDataHandler {

    private static final SQLColumn COLUMN_GEN_HOLDER = SQLColumn.of("holder", ColumnType.STRING);
    private static final SQLColumn COLUMN_STOCK_TYPE = SQLColumn.of("stockType", ColumnType.STRING);

    private static final SQLColumn COLUMN_GEN_SHOP_ID    = SQLColumn.of("shopId", ColumnType.STRING);
    private static final SQLColumn COLUMN_GEN_PRODUCT_ID = SQLColumn.of("productId", ColumnType.STRING);
    private static final SQLColumn COLUMN_GEN_PLAYER_ID  = SQLColumn.of("playerId", ColumnType.STRING);

    private static final SQLColumn COLUMN_STOCK_TRADE_TYPE   = SQLColumn.of("tradeType", ColumnType.STRING);
    private static final SQLColumn COLUMN_STOCK_RESTOCK_DATE = SQLColumn.of("restockDate", ColumnType.LONG);
    private static final SQLColumn COLUMN_STOCK_ITEMS_LEFT   = SQLColumn.of("itemsLeft", ColumnType.INTEGER);
    private static final SQLColumn COLUMN_PRICE_LAST_BUY     = SQLColumn.of("lastBuyPrice", ColumnType.DOUBLE);
    private static final SQLColumn COLUMN_PRICE_LAST_SELL    = SQLColumn.of("lastSellPrice", ColumnType.DOUBLE);
    private static final SQLColumn COLUMN_PRICE_LAST_UPDATED = SQLColumn.of("lastUpdated", ColumnType.LONG);
    private static final SQLColumn COLUMN_PRICE_EXPIRE_DATE  = SQLColumn.of("expireDate", ColumnType.LONG);
    private static final SQLColumn COLUMN_PRICE_PURCHASES    = SQLColumn.of("purchases", ColumnType.INTEGER);
    private static final SQLColumn COLUMN_PRICE_SALES        = SQLColumn.of("sales", ColumnType.INTEGER);
    private static final SQLColumn COLUMN_ROTATE_PRODUCTS    = SQLColumn.of("products", ColumnType.STRING);

    private final ShopPlugin  plugin;
    private final DataHandler dataHandler;
    private final String      tableStockDataOld;
    private final String      tableStockData;
    private final String      tablePlayerLimits;
    private final String      tablePriceData;
    private final String      tableRotationData;

    private final Function<ResultSet, StockData>      stockDataFunction;
    private final Function<ResultSet, OwnedStockData> ownedStockDataFunction;
    private final Function<ResultSet, PriceData>      priceDataFunction;
    private final Function<ResultSet, RotationData>   rotationDataFunction;

    public VirtualDataHandler(@NotNull ShopPlugin plugin, @NotNull DataHandler dataHandler) {
        this.plugin = plugin;
        this.dataHandler = dataHandler;
        this.tableStockDataOld = dataHandler.getTablePrefix() + "_stock_data";
        this.tableStockData = dataHandler.getTablePrefix() + "_virtual_stock_data";
        this.tablePlayerLimits = dataHandler.getTablePrefix() + "_virtual_player_limits";
        this.tablePriceData = dataHandler.getTablePrefix() + "_price_data";
        this.tableRotationData = dataHandler.getTablePrefix() + "_rotation_data";

        this.stockDataFunction = resultSet -> {
            try {
                TradeType tradeType = StringUtil.getEnum(resultSet.getString(COLUMN_STOCK_TRADE_TYPE.getName()), TradeType.class).orElse(null);
                if (tradeType == null) return null;

                String shopId = resultSet.getString(COLUMN_GEN_SHOP_ID.getName());
                String productId = resultSet.getString(COLUMN_GEN_PRODUCT_ID.getName());
                int itemsLeft = resultSet.getInt(COLUMN_STOCK_ITEMS_LEFT.getName());
                long restockDate = resultSet.getLong(COLUMN_STOCK_RESTOCK_DATE.getName());

                return new StockData(tradeType, shopId, productId, itemsLeft, restockDate);
            }
            catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        };

        this.ownedStockDataFunction = resultSet -> {
            try {
                TradeType tradeType = StringUtil.getEnum(resultSet.getString(COLUMN_STOCK_TRADE_TYPE.getName()), TradeType.class).orElse(null);
                if (tradeType == null) return null;

                UUID ownerId = UUID.fromString(resultSet.getString(COLUMN_GEN_PLAYER_ID.getName()));

                String shopId = resultSet.getString(COLUMN_GEN_SHOP_ID.getName());
                String productId = resultSet.getString(COLUMN_GEN_PRODUCT_ID.getName());
                int itemsLeft = resultSet.getInt(COLUMN_STOCK_ITEMS_LEFT.getName());
                long restockDate = resultSet.getLong(COLUMN_STOCK_RESTOCK_DATE.getName());

                return new OwnedStockData(ownerId, tradeType, shopId, productId, itemsLeft, restockDate);
            }
            catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        };

        this.priceDataFunction = resultSet -> {
            try {
                String shopId = resultSet.getString(COLUMN_GEN_SHOP_ID.getName());
                String productId = resultSet.getString(COLUMN_GEN_PRODUCT_ID.getName());
                double lastBuyPrice = resultSet.getDouble(COLUMN_PRICE_LAST_BUY.getName());
                double lastSellPrice = resultSet.getDouble(COLUMN_PRICE_LAST_SELL.getName());
                long lastUpdated = resultSet.getLong(COLUMN_PRICE_LAST_UPDATED.getName());
                long expireDate = resultSet.getLong(COLUMN_PRICE_EXPIRE_DATE.getName());
                int purchases = resultSet.getInt(COLUMN_PRICE_PURCHASES.getName());
                int sales = resultSet.getInt(COLUMN_PRICE_SALES.getName());

                return new PriceData(shopId, productId, lastBuyPrice, lastSellPrice, lastUpdated, expireDate, purchases, sales);
            }
            catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        };

        this.rotationDataFunction = resultSet -> {
            try {
                String shopId = resultSet.getString(COLUMN_GEN_SHOP_ID.getName());
                long lastRotated = resultSet.getLong(COLUMN_PRICE_LAST_UPDATED.getName());
                Set<String> products = dataHandler.gson().fromJson(resultSet.getString(COLUMN_ROTATE_PRODUCTS.getName()), new TypeToken<Set<String>>(){}.getType());

                return new RotationData(shopId, lastRotated, products);
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    public void load() {
        /*this.dataHandler.createTable(this.tableStockDataOld, Arrays.asList(
            COLUMN_GEN_HOLDER, COLUMN_GEN_SHOP_ID, COLUMN_GEN_PRODUCT_ID,
            COLUMN_STOCK_TRADE_TYPE, COLUMN_STOCK_TYPE, COLUMN_STOCK_ITEMS_LEFT, COLUMN_STOCK_RESTOCK_DATE
        ));*/

        this.dataHandler.createTable(this.tableStockData, Arrays.asList(
            COLUMN_GEN_SHOP_ID, COLUMN_GEN_PRODUCT_ID,
            COLUMN_STOCK_TRADE_TYPE, COLUMN_STOCK_ITEMS_LEFT, COLUMN_STOCK_RESTOCK_DATE
        ));

        this.dataHandler.createTable(this.tablePlayerLimits, Arrays.asList(
            COLUMN_GEN_PLAYER_ID,
            COLUMN_GEN_SHOP_ID, COLUMN_GEN_PRODUCT_ID,
            COLUMN_STOCK_TRADE_TYPE, COLUMN_STOCK_ITEMS_LEFT, COLUMN_STOCK_RESTOCK_DATE
        ));

        this.dataHandler.createTable(this.tablePriceData, Arrays.asList(
            COLUMN_GEN_SHOP_ID, COLUMN_GEN_PRODUCT_ID,
            COLUMN_PRICE_LAST_BUY, COLUMN_PRICE_LAST_SELL,
            COLUMN_PRICE_LAST_UPDATED, COLUMN_PRICE_EXPIRE_DATE,
            COLUMN_PRICE_PURCHASES, COLUMN_PRICE_SALES
        ));

        this.dataHandler.createTable(this.tableRotationData, Arrays.asList(
            COLUMN_GEN_SHOP_ID, COLUMN_PRICE_LAST_UPDATED, COLUMN_ROTATE_PRODUCTS
        ));

        // ---- UPDATE OLD DATA - START ----

        if (SQLQueries.hasTable(this.dataHandler.getConnector(), this.tableStockDataOld)) {
            this.dataHandler.load(
                this.tableStockDataOld,
                this.stockDataFunction,
                Collections.emptyList(),
                List.of(
                    SQLCondition.equal(COLUMN_STOCK_TYPE.toValue("GLOBAL"))
                ),
                -1
            ).forEach(this::insertStockData);

            Function<ResultSet, UUID> function = resultSet -> {
                try {
                    return UUID.fromString(resultSet.getString("uuid"));
                }
                catch (SQLException exception) {
                    exception.printStackTrace();
                }
                return null;
            };

            List<UUID> playerIds = this.dataHandler.load(this.dataHandler.getTablePrefix() + "_users",
                function,
                List.of(SQLColumn.of("uuid", ColumnType.STRING)),
                Collections.emptyList(), -1);

            //Set<UUID> playerIds = this.dataHandler.getUsers().stream().map(AbstractUser::getId).collect(Collectors.toSet());
            playerIds.forEach(id -> {
                /*id, */
                this.dataHandler.load(
                    this.tableStockDataOld,
                    this.ownedStockDataFunction,
                    Collections.emptyList(),
                    Arrays.asList(
                        SQLCondition.equal(COLUMN_STOCK_TYPE.toValue("PLAYER")),
                        SQLCondition.equal(COLUMN_GEN_HOLDER.toValue(id.toString()))
                    ),
                    -1
                ).forEach(this::insertPlayerLimit);
            });

            this.dataHandler.delete(this.tableStockDataOld, SQLCondition.equal(COLUMN_STOCK_TYPE.toValue("GLOBAL")));
            this.dataHandler.delete(this.tableStockDataOld, SQLCondition.equal(COLUMN_STOCK_TYPE.toValue("PLAYER")));
        }
        // ---- UPDATE OLD DATA - END ----

        this.dataHandler.addColumn(this.tablePriceData, COLUMN_PRICE_EXPIRE_DATE.toValue(0));
    }

    public void purge() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(dataHandler.getConfig().getPurgePeriod());
        long deadlineMs = TimeUtil.toEpochMillis(deadline);

        if (SQLQueries.hasTable(dataHandler.getConnector(), this.tableStockData)) {
            String sql = "DELETE FROM " + this.tableStockData + " WHERE " + COLUMN_STOCK_RESTOCK_DATE.getName() + " < " + deadlineMs;
            SQLQueries.executeStatement(dataHandler.getConnector(), sql);
        }
        if (SQLQueries.hasTable(dataHandler.getConnector(), this.tablePlayerLimits)) {
            String sql = "DELETE FROM " + this.tablePlayerLimits + " WHERE " + COLUMN_STOCK_RESTOCK_DATE.getName() + " < " + deadlineMs;
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
        VirtualShopModule module = this.plugin.getVirtualShop();
        if (module != null) {
            module.loadShopData();
        }
    }

    @NotNull
    public List<StockData> getStockDatas(@NotNull String shopId) {
        return this.dataHandler.load(this.tableStockData, this.stockDataFunction,
            Collections.emptyList(),
            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId))),
            -1
        );
    }

    @NotNull
    public List<OwnedStockData> getPlayerLimits(@NotNull UUID playerId) {
        return this.dataHandler.load(this.tablePlayerLimits, this.ownedStockDataFunction,
            Collections.emptyList(),
            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_PLAYER_ID.toValue(playerId.toString()))),
            -1
        );
    }

    @NotNull
    public List<PriceData> getPriceData(@NotNull Shop shop) {
        return this.getPriceData(shop.getId());
    }

    @NotNull
    public List<PriceData> getPriceData(@NotNull String shopId) {
        return this.dataHandler.load(this.tablePriceData, this.priceDataFunction,
            Collections.emptyList(),
            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId))), -1
        );
    }

    @Nullable
    public RotationData getRotationData(@NotNull RotatingShop shop) {
        return this.getRotationData(shop.getId());
    }

    @Nullable
    public RotationData getRotationData(@NotNull String shopId) {
        return this.dataHandler.load(this.tableRotationData, this.rotationDataFunction,
            Collections.emptyList(),
            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId)))
        ).orElse(null);
    }

    public void insertStockData(@NotNull StockData data) {
        this.dataHandler.insert(this.tableStockData, Arrays.asList(
            COLUMN_GEN_SHOP_ID.toValue(data.getShopId()),
            COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId()),
            COLUMN_STOCK_TRADE_TYPE.toValue(data.getTradeType().name()),
            COLUMN_STOCK_ITEMS_LEFT.toValue(data.getItemsLeft()),
            COLUMN_STOCK_RESTOCK_DATE.toValue(data.getRestockDate())
        ));
    }

    public void insertPlayerLimit(@NotNull OwnedStockData data) {
        this.dataHandler.insert(this.tablePlayerLimits, Arrays.asList(
            COLUMN_GEN_PLAYER_ID.toValue(data.getOwnerId().toString()),
            COLUMN_GEN_SHOP_ID.toValue(data.getShopId()),
            COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId()),
            COLUMN_STOCK_TRADE_TYPE.toValue(data.getTradeType().name()),
            COLUMN_STOCK_ITEMS_LEFT.toValue(data.getItemsLeft()),
            COLUMN_STOCK_RESTOCK_DATE.toValue(data.getRestockDate())
        ));
    }

    public void insertPriceData(@NotNull PriceData data) {
        this.dataHandler.insert(this.tablePriceData, Arrays.asList(
            COLUMN_GEN_SHOP_ID.toValue(data.getShopId()),
            COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId()),
            COLUMN_PRICE_LAST_BUY.toValue(String.valueOf(data.getLastBuyPrice())),
            COLUMN_PRICE_LAST_SELL.toValue(String.valueOf(data.getLastSellPrice())),
            COLUMN_PRICE_LAST_UPDATED.toValue(String.valueOf(data.getLastUpdated())),
            COLUMN_PRICE_EXPIRE_DATE.toValue(String.valueOf(data.getExpireDate())),
            COLUMN_PRICE_PURCHASES.toValue(String.valueOf(data.getPurchases())),
            COLUMN_PRICE_SALES.toValue(String.valueOf(data.getSales()))
        ));
    }

    public void insertRotationData(@NotNull RotationData rotationData) {
        this.dataHandler.insert(this.tableRotationData, Arrays.asList(
            COLUMN_GEN_SHOP_ID.toValue(rotationData.getShopId()),
            COLUMN_PRICE_LAST_UPDATED.toValue(rotationData.getLatestRotation()),
            COLUMN_ROTATE_PRODUCTS.toValue(dataHandler.gson().toJson(rotationData.getProducts()))
        ));
    }

    public void saveStockData(@NotNull StockData data) {
        this.dataHandler.update(this.tableStockData,
            Arrays.asList(
                COLUMN_STOCK_ITEMS_LEFT.toValue(data.getItemsLeft()),
                COLUMN_STOCK_RESTOCK_DATE.toValue(data.getRestockDate())
            ),
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(data.getShopId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId())),
            SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(data.getTradeType().name()))
        );
    }

    public void savePlayerLimit(@NotNull OwnedStockData data) {
        this.dataHandler.update(this.tablePlayerLimits,
            Arrays.asList(
                COLUMN_STOCK_ITEMS_LEFT.toValue(data.getItemsLeft()),
                COLUMN_STOCK_RESTOCK_DATE.toValue(data.getRestockDate())
            ),
            SQLCondition.equal(COLUMN_GEN_PLAYER_ID.toValue(data.getOwnerId().toString()/*playerId.toString()*/)),
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(data.getShopId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId())),
            SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(data.getTradeType().name()))
        );
    }

    public void savePriceData(@NotNull PriceData data) {
        this.dataHandler.update(this.tablePriceData, Arrays.asList(
            COLUMN_PRICE_LAST_BUY.toValue(data.getLastBuyPrice()),
            COLUMN_PRICE_LAST_SELL.toValue(data.getLastSellPrice()),
            COLUMN_PRICE_LAST_UPDATED.toValue(data.getLastUpdated()),
            COLUMN_PRICE_EXPIRE_DATE.toValue(data.getExpireDate()),
            COLUMN_PRICE_PURCHASES.toValue(data.getPurchases()),
            COLUMN_PRICE_SALES.toValue(data.getSales())
            ),
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(data.getShopId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId()))
        );
    }

    public void saveRotationData(@NotNull RotationData rotationData) {
        this.dataHandler.update(this.tableRotationData, Arrays.asList(
            COLUMN_PRICE_LAST_UPDATED.toValue(rotationData.getLatestRotation()),
            COLUMN_ROTATE_PRODUCTS.toValue(dataHandler.gson().toJson(rotationData.getProducts()))
            ),
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(rotationData.getShopId()))
        );
    }




    public void deletePlayerLimit(@NotNull UUID playerId) {
        this.dataHandler.delete(this.tablePlayerLimits,
            SQLCondition.equal(COLUMN_GEN_PLAYER_ID.toValue(playerId.toString()))
        );
    }

    public void deletePlayerLimit(@NotNull UUID playerId, @NotNull Product product) {
        this.dataHandler.delete(this.tablePlayerLimits,
            SQLCondition.equal(COLUMN_GEN_PLAYER_ID.toValue(playerId.toString())),
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId()))
        );
    }

    public void deletePlayerLimit(@NotNull UUID playerId, @NotNull Product product, @NotNull TradeType tradeType) {
        this.dataHandler.delete(this.tablePlayerLimits,
            SQLCondition.equal(COLUMN_GEN_PLAYER_ID.toValue(playerId.toString())),
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId())),
            SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(tradeType.name()))
        );
    }

    public void deletePlayerLimit(@NotNull Product product, @NotNull TradeType tradeType) {
        this.dataHandler.delete(this.tablePlayerLimits,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId())),
            SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(tradeType.name()))
        );
    }

    public void deletePlayerLimits(@NotNull Shop shop) {
        this.dataHandler.delete(this.tablePlayerLimits,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shop.getId()))
        );
    }



    public void deleteStockData(@NotNull Product product) {
        this.dataHandler.delete(this.tableStockData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId()))
        );
    }

    public void deleteStockData(@NotNull Product product, @NotNull TradeType tradeType) {
        this.dataHandler.delete(this.tableStockData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId())),
            SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(tradeType.name()))
        );
    }

    public void deleteStockData(@NotNull Shop shop) {
        this.dataHandler.delete(this.tableStockData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shop.getId()))
        );
    }



    public void deletePriceData(@NotNull Product product) {
        this.dataHandler.delete(this.tablePriceData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId()))
        );
    }

    public void deletePriceData(@NotNull Shop shop) {
        this.dataHandler.delete(this.tablePriceData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shop.getId()))
        );
    }

    public void deleteRotationData(@NotNull RotatingShop shop) {
        this.dataHandler.delete(this.tableRotationData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shop.getId()))
        );
    }
}
