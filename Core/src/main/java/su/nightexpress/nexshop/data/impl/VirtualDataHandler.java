package su.nightexpress.nexshop.data.impl;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.product.data.AbstractData;
import su.nightexpress.nexshop.product.data.*;
import su.nightexpress.nexshop.product.data.impl.PriceData;
import su.nightexpress.nexshop.product.stock.StockAmount;
import su.nightexpress.nexshop.product.data.impl.StockData;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.data.RotationData;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nightcore.database.sql.SQLColumn;
import su.nightexpress.nightcore.database.sql.SQLCondition;
import su.nightexpress.nightcore.database.sql.SQLQueries;
import su.nightexpress.nightcore.database.sql.column.ColumnType;
import su.nightexpress.nightcore.database.sql.query.UpdateEntity;
import su.nightexpress.nightcore.database.sql.query.UpdateQuery;
import su.nightexpress.nightcore.util.Lists;
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

    @Deprecated private static final SQLColumn COLUMN_STOCK_TRADE_TYPE   = SQLColumn.of("tradeType", ColumnType.STRING);
    @Deprecated private static final SQLColumn COLUMN_STOCK_RESTOCK_DATE = SQLColumn.of("restockDate", ColumnType.LONG);
    @Deprecated private static final SQLColumn COLUMN_STOCK_ITEMS_LEFT   = SQLColumn.of("itemsLeft", ColumnType.INTEGER);

    private static final SQLColumn COLUMN_STOCK_GLOBAL_AMOUNT   = SQLColumn.of("globalAmount", ColumnType.STRING);
    private static final SQLColumn COLUMN_STOCK_PLAYER_AMOUNT   = SQLColumn.of("playerAmount", ColumnType.STRING);

    private static final SQLColumn COLUMN_PRICE_LAST_BUY     = SQLColumn.of("lastBuyPrice", ColumnType.DOUBLE);
    private static final SQLColumn COLUMN_PRICE_LAST_SELL    = SQLColumn.of("lastSellPrice", ColumnType.DOUBLE);
    private static final SQLColumn COLUMN_PRICE_LAST_UPDATED = SQLColumn.of("lastUpdated", ColumnType.LONG);
    private static final SQLColumn COLUMN_PRICE_EXPIRE_DATE  = SQLColumn.of("expireDate", ColumnType.LONG);
    private static final SQLColumn COLUMN_PRICE_PURCHASES    = SQLColumn.of("purchases", ColumnType.INTEGER);
    private static final SQLColumn COLUMN_PRICE_SALES        = SQLColumn.of("sales", ColumnType.INTEGER);

    private static final SQLColumn COLUMN_ROTATE_PRODUCTS    = SQLColumn.of("products", ColumnType.STRING);

    private final ShopPlugin  plugin;
    private final DataHandler dataHandler;

    @Deprecated private final String tableStockDataOld;
    @Deprecated private final String tableStockDataLegacy;

    private final String      tableStockDataFused;
    private final String      tablePlayerLimits;
    private final String      tablePriceData;
    private final String      tableRotationData;

    private final Function<ResultSet, LegacyStockData>      leagcyStockDataFunction;
    private final Function<ResultSet, LegacyOwnedStockData> legacyLimitDataFunction;

    private final Function<ResultSet, StockData> stockDataFunction;
    private final Function<ResultSet, PriceData> priceDataFunction;
    private final Function<ResultSet, RotationData> rotationDataFunction;

    public VirtualDataHandler(@NotNull ShopPlugin plugin, @NotNull DataHandler dataHandler) {
        this.plugin = plugin;
        this.dataHandler = dataHandler;
        this.tableStockDataOld = dataHandler.getTablePrefix() + "_stock_data";
        this.tableStockDataLegacy = dataHandler.getTablePrefix() + "_virtual_stock_data";
        this.tableStockDataFused = dataHandler.getTablePrefix() + "_virtual_stock_data_fused";
        this.tablePlayerLimits = dataHandler.getTablePrefix() + "_virtual_player_limits";
        this.tablePriceData = dataHandler.getTablePrefix() + "_price_data";
        this.tableRotationData = dataHandler.getTablePrefix() + "_rotation_data";

        this.stockDataFunction = resultSet -> {
            try {
                String shopId = resultSet.getString(COLUMN_GEN_SHOP_ID.getName());
                String productId = resultSet.getString(COLUMN_GEN_PRODUCT_ID.getName());

                Map<TradeType, StockAmount> globalAmount = dataHandler.gson()
                    .fromJson(resultSet.getString(COLUMN_STOCK_GLOBAL_AMOUNT.getName()), new TypeToken<Map<TradeType, StockAmount>>(){}.getType());

                Map<TradeType, Map<UUID, StockAmount>> playerAmount = dataHandler.gson()
                    .fromJson(resultSet.getString(COLUMN_STOCK_PLAYER_AMOUNT.getName()), new TypeToken<Map<TradeType, Map<UUID, StockAmount>>>(){}.getType());

                return new StockData(shopId, productId, globalAmount, playerAmount);
            }
            catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        };

        this.leagcyStockDataFunction = resultSet -> {
            try {
                TradeType tradeType = StringUtil.getEnum(resultSet.getString(COLUMN_STOCK_TRADE_TYPE.getName()), TradeType.class).orElse(null);
                if (tradeType == null) return null;

                String shopId = resultSet.getString(COLUMN_GEN_SHOP_ID.getName());
                String productId = resultSet.getString(COLUMN_GEN_PRODUCT_ID.getName());
                int itemsLeft = resultSet.getInt(COLUMN_STOCK_ITEMS_LEFT.getName());
                long restockDate = resultSet.getLong(COLUMN_STOCK_RESTOCK_DATE.getName());

                return new LegacyStockData(tradeType, shopId, productId, itemsLeft, restockDate);
            }
            catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        };

        this.legacyLimitDataFunction = resultSet -> {
            try {
                TradeType tradeType = StringUtil.getEnum(resultSet.getString(COLUMN_STOCK_TRADE_TYPE.getName()), TradeType.class).orElse(null);
                if (tradeType == null) return null;

                UUID ownerId = UUID.fromString(resultSet.getString(COLUMN_GEN_PLAYER_ID.getName()));

                String shopId = resultSet.getString(COLUMN_GEN_SHOP_ID.getName());
                String productId = resultSet.getString(COLUMN_GEN_PRODUCT_ID.getName());
                int itemsLeft = resultSet.getInt(COLUMN_STOCK_ITEMS_LEFT.getName());
                long restockDate = resultSet.getLong(COLUMN_STOCK_RESTOCK_DATE.getName());

                return new LegacyOwnedStockData(ownerId, tradeType, shopId, productId, itemsLeft, restockDate);
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
            catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        };
    }

    public void load() {
        this.dataHandler.createTable(this.tableStockDataFused, Arrays.asList(
            COLUMN_GEN_SHOP_ID, COLUMN_GEN_PRODUCT_ID,
            COLUMN_STOCK_PLAYER_AMOUNT, COLUMN_STOCK_GLOBAL_AMOUNT
        ));

//        this.dataHandler.createTable(this.tableStockData, Arrays.asList(
//            COLUMN_GEN_SHOP_ID, COLUMN_GEN_PRODUCT_ID,
//            COLUMN_STOCK_TRADE_TYPE, COLUMN_STOCK_ITEMS_LEFT, COLUMN_STOCK_RESTOCK_DATE
//        ));
//
//        this.dataHandler.createTable(this.tablePlayerLimits, Arrays.asList(
//            COLUMN_GEN_PLAYER_ID,
//            COLUMN_GEN_SHOP_ID, COLUMN_GEN_PRODUCT_ID,
//            COLUMN_STOCK_TRADE_TYPE, COLUMN_STOCK_ITEMS_LEFT, COLUMN_STOCK_RESTOCK_DATE
//        ));

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
                this.leagcyStockDataFunction,
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
                    this.legacyLimitDataFunction,
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



        // ---- UPDATE OLD DATA - START ----

        if (SQLQueries.hasTable(this.dataHandler.getConnector(), this.tableStockDataLegacy) && SQLQueries.hasTable(this.dataHandler.getConnector(), this.tablePlayerLimits)) {
            Map<String, Map<String, StockData>> dataMap = new HashMap<>();

            this.getStocksAndLimits().forEach(legacyStockData -> {
                var productMap = dataMap.computeIfAbsent(legacyStockData.getShopId(), k -> new HashMap<>());
                StockData fusedData = productMap
                    .computeIfAbsent(legacyStockData.getProductId(), k -> new StockData(legacyStockData.getShopId(), legacyStockData.getProductId(), new HashMap<>(), new HashMap<>()));

                StockAmount amounts;
                if (legacyStockData instanceof LegacyOwnedStockData ownedStockData) {
                    amounts = fusedData.getPlayerAmount(legacyStockData.getTradeType(), ownedStockData.getOwnerId());
                }
                else {
                    amounts = fusedData.getGlobalAmount(legacyStockData.getTradeType());
                }

                amounts.setItemsLeft(legacyStockData.getItemsLeft());
                amounts.setRestockDate(legacyStockData.getRestockDate());
            });

            dataMap.values().forEach(map -> map.values().forEach(this::insertStockData));

            this.dataHandler.delete(this.tableStockDataLegacy);
            this.dataHandler.delete(this.tablePlayerLimits);
        }
        // ---- UPDATE OLD DATA - END ----


        this.dataHandler.addColumn(this.tablePriceData, COLUMN_PRICE_EXPIRE_DATE.toValue(0));
    }

    public void purge() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(dataHandler.getConfig().getPurgePeriod());
        long deadlineMs = TimeUtil.toEpochMillis(deadline);

//        if (SQLQueries.hasTable(dataHandler.getConnector(), this.tableStockData)) {
//            String sql = "DELETE FROM " + this.tableStockData + " WHERE " + COLUMN_STOCK_RESTOCK_DATE.getName() + " < " + deadlineMs;
//            SQLQueries.executeStatement(dataHandler.getConnector(), sql);
//        }
//        if (SQLQueries.hasTable(dataHandler.getConnector(), this.tablePlayerLimits)) {
//            String sql = "DELETE FROM " + this.tablePlayerLimits + " WHERE " + COLUMN_STOCK_RESTOCK_DATE.getName() + " < " + deadlineMs;
//            SQLQueries.executeStatement(dataHandler.getConnector(), sql);
//        }
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
            module.loadRotationData();
        }
    }

//    @NotNull
//    public List<StockData> getStockDatas(@NotNull String shopId) {
//        return this.dataHandler.load(this.tableStockData, this.stockDataFunction,
//            Collections.emptyList(),
//            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId))),
//            -1
//        );
//    }

//    @NotNull
//    public List<StockData> getStocksAndLimits(@NotNull String shopId) {
//        List<StockData> list = new ArrayList<>();
//
//        list.addAll(this.dataHandler.load(this.tableStockData, this.stockDataFunction,
//            Collections.emptyList(),
//            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId))),
//            -1
//        ));
//
//        list.addAll(this.dataHandler.load(this.tablePlayerLimits, this.ownedStockDataFunction,
//            Collections.emptyList(),
//            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId))),
//            -1
//        ));
//
//        return list;
//    }

    @NotNull
    private List<LegacyStockData> getStocksAndLimits() {
        List<LegacyStockData> list = new ArrayList<>();

        list.addAll(this.dataHandler.load(this.tableStockDataLegacy, this.leagcyStockDataFunction,
            Collections.emptyList(),
            Collections.emptyList(),
            -1
        ));

        list.addAll(this.dataHandler.load(this.tablePlayerLimits, this.legacyLimitDataFunction,
            Collections.emptyList(),
            Collections.emptyList(),
            -1
        ));

        return list;
    }

    @NotNull
    public List<StockData> getStockDatas() {
        return this.dataHandler.load(this.tableStockDataFused, this.stockDataFunction,
            Collections.emptyList(),
            Collections.emptyList(),
            -1
        );
    }

//    @NotNull
//    public List<OwnedStockData> getPlayerLimits(@NotNull UUID playerId) {
//        return this.dataHandler.load(this.tablePlayerLimits, this.ownedStockDataFunction,
//            Collections.emptyList(),
//            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_PLAYER_ID.toValue(playerId.toString()))),
//            -1
//        );
//    }
//
//    @NotNull
//    public List<OwnedStockData> getPlayerLimits() {
//        return this.dataHandler.load(this.tablePlayerLimits, this.ownedStockDataFunction,
//            Collections.emptyList(),
//            Collections.emptyList(),
//            -1
//        );
//    }

    @NotNull
    public List<PriceData> getPriceDatas() {
        return this.dataHandler.load(this.tablePriceData, this.priceDataFunction,
            Collections.emptyList(),
            Collections.emptyList(),
            -1
        );
    }

//    @NotNull
//    public List<PriceData> getPriceData(@NotNull Shop shop) {
//        return this.getPriceData(shop.getId());
//    }
//
//    @NotNull
//    public List<PriceData> getPriceData(@NotNull String shopId) {
//        return this.dataHandler.load(this.tablePriceData, this.priceDataFunction,
//            Collections.emptyList(),
//            Collections.singletonList(SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId))), -1
//        );
//    }

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
        this.dataHandler.insert(this.tableStockDataFused, Lists.newList(
            COLUMN_GEN_SHOP_ID.toValue(data.getShopId()),
            COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId()),
            COLUMN_STOCK_GLOBAL_AMOUNT.toValue(this.dataHandler.gson().toJson(data.getGlobalAmounts())),
            COLUMN_STOCK_PLAYER_AMOUNT.toValue(this.dataHandler.gson().toJson(data.getPlayerAmounts()))
        ));
    }

    public void insertStockData(@NotNull LegacyStockData data) {
        this.dataHandler.insert(this.tableStockDataLegacy, Arrays.asList(
            COLUMN_GEN_SHOP_ID.toValue(data.getShopId()),
            COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId()),
            COLUMN_STOCK_TRADE_TYPE.toValue(data.getTradeType().name()),
            COLUMN_STOCK_ITEMS_LEFT.toValue(data.getItemsLeft()),
            COLUMN_STOCK_RESTOCK_DATE.toValue(data.getRestockDate())
        ));
    }

    public void insertPlayerLimit(@NotNull LegacyOwnedStockData data) {
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

    public void saveProductDatas(@NotNull Set<AbstractData> datas) {
        if (datas.isEmpty()) return;

        UpdateQuery stockQuery = new UpdateQuery(this.tableStockDataFused);
        UpdateQuery priceQuery = new UpdateQuery(this.tablePriceData);

        datas.forEach(data -> {
            if (data instanceof StockData stockData) {
                stockQuery.append(this.createUpdateEntity(stockData));
            }
            else if (data instanceof PriceData priceData) {
                priceQuery.append(this.createUpdateEntity(priceData));
            }
        });

        this.dataHandler.executeUpdate(stockQuery);
        this.dataHandler.executeUpdate(priceQuery);
    }

    @NotNull
    private UpdateEntity createUpdateEntity(@NotNull StockData data) {
        return UpdateEntity.create(
            //this.tableStockDataFused,
            Lists.newList(
                COLUMN_STOCK_GLOBAL_AMOUNT.toValue(this.dataHandler.gson().toJson(data.getGlobalAmounts())),
                COLUMN_STOCK_PLAYER_AMOUNT.toValue(this.dataHandler.gson().toJson(data.getPlayerAmounts()))
            ),
            Lists.newList(
                SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(data.getShopId())),
                SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId()))
            )
        );
    }

    public void saveStockData(@NotNull StockData data) {
        this.dataHandler.executeUpdate(UpdateQuery.create(this.tableStockDataFused, this.createUpdateEntity(data)));
    }

    @NotNull
    private UpdateEntity createUpdateEntity(@NotNull PriceData data) {
        return UpdateEntity.create(
            //this.tablePriceData,
            Lists.newList(
                COLUMN_PRICE_LAST_BUY.toValue(data.getLastBuyPrice()),
                COLUMN_PRICE_LAST_SELL.toValue(data.getLastSellPrice()),
                COLUMN_PRICE_LAST_UPDATED.toValue(data.getLastUpdated()),
                COLUMN_PRICE_EXPIRE_DATE.toValue(data.getExpireDate()),
                COLUMN_PRICE_PURCHASES.toValue(data.getPurchases()),
                COLUMN_PRICE_SALES.toValue(data.getSales())
            ),
            Lists.newList(
                SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(data.getShopId())),
                SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId()))
            )
        );
    }

    public void savePriceData(@NotNull PriceData data) {
        this.dataHandler.executeUpdate(UpdateQuery.create(this.tablePriceData, this.createUpdateEntity(data)));
    }

//    public void saveStockData(@NotNull LegacyStockData data) {
//        UpdateQuery query = UpdateQuery.create(
//            this.tableStockData,
//            Lists.newList(
//                COLUMN_STOCK_ITEMS_LEFT.toValue(data.getItemsLeft()),
//                COLUMN_STOCK_RESTOCK_DATE.toValue(data.getRestockDate())
//            ),
//            Lists.newList(
//                SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(data.getShopId())),
//                SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId())),
//                SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(data.getTradeType().name()))
//            )
//        );
//
//        this.dataHandler.executeUpdate(query);
//    }
//
//    public void savePlayerLimit(@NotNull LegacyOwnedStockData data) {
//        UpdateQuery query = UpdateQuery.create(
//            this.tablePlayerLimits,
//            Lists.newList(
//                COLUMN_STOCK_ITEMS_LEFT.toValue(data.getItemsLeft()),
//                COLUMN_STOCK_RESTOCK_DATE.toValue(data.getRestockDate())
//            ),
//            Lists.newList(
//                SQLCondition.equal(COLUMN_GEN_PLAYER_ID.toValue(data.getOwnerId().toString()/*playerId.toString()*/)),
//                SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(data.getShopId())),
//                SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(data.getProductId())),
//                SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(data.getTradeType().name()))
//            )
//        );
//
//        this.dataHandler.executeUpdate(query);
//    }



    public void saveRotationData(@NotNull RotationData rotationData) {
        UpdateQuery query = UpdateQuery.create(
            this.tableRotationData,
            Lists.newList(
                COLUMN_PRICE_LAST_UPDATED.toValue(rotationData.getLatestRotation()),
                COLUMN_ROTATE_PRODUCTS.toValue(dataHandler.gson().toJson(rotationData.getProducts()))
            ),
            Lists.newList(
                SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(rotationData.getShopId()))
            )
        );

        this.dataHandler.executeUpdate(query);
    }




//    public void deletePlayerLimit(@NotNull UUID playerId) {
//        this.dataHandler.delete(this.tablePlayerLimits,
//            SQLCondition.equal(COLUMN_GEN_PLAYER_ID.toValue(playerId.toString()))
//        );
//    }
//
//    public void deletePlayerLimit(@NotNull UUID playerId, @NotNull Product product) {
//        this.dataHandler.delete(this.tablePlayerLimits,
//            SQLCondition.equal(COLUMN_GEN_PLAYER_ID.toValue(playerId.toString())),
//            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
//            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId()))
//        );
//    }
//
//    public void deletePlayerLimit(@NotNull UUID playerId, @NotNull Product product, @NotNull TradeType tradeType) {
//        this.deletePlayerLimit(playerId, product.getShop().getId(), product.getId(), tradeType);
//    }
//
//    public void deletePlayerLimit(@NotNull UUID playerId, @NotNull String shopId, @NotNull String productId, @NotNull TradeType tradeType) {
//        this.dataHandler.delete(this.tablePlayerLimits,
//            SQLCondition.equal(COLUMN_GEN_PLAYER_ID.toValue(playerId.toString())),
//            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId)),
//            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(productId)),
//            SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(tradeType.name()))
//        );
//    }

//    public void deletePlayerLimit(@NotNull Product product, @NotNull TradeType tradeType) {
//        this.dataHandler.delete(this.tablePlayerLimits,
//            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
//            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId())),
//            SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(tradeType.name()))
//        );
//    }
//
//    public void deletePlayerLimit(@NotNull Product product) {
//        this.dataHandler.delete(this.tablePlayerLimits,
//            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
//            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId()))
//        );
//    }
//
//    public void deletePlayerLimits(@NotNull Shop shop) {
//        this.deletePlayerLimits(shop.getId());
//    }
//
//    public void deletePlayerLimits(@NotNull String shopId) {
//        this.dataHandler.delete(this.tablePlayerLimits,
//            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId))
//        );
//    }



//    public void deleteStockData(@NotNull Product product) {
//        this.dataHandler.delete(this.tableStockData,
//            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(product.getShop().getId())),
//            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(product.getId()))
//        );
//    }
//
//    public void deleteStockData(@NotNull Product product, @NotNull TradeType tradeType) {
//        this.deleteStockData(product.getShop().getId(), product.getId(), tradeType);
//    }
//
//    public void deleteStockData(@NotNull String shopId, @NotNull String productId, @NotNull TradeType tradeType) {
//        this.dataHandler.delete(this.tableStockData,
//            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId)),
//            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(productId)),
//            SQLCondition.equal(COLUMN_STOCK_TRADE_TYPE.toValue(tradeType.name()))
//        );
//    }

    public void deleteStockData(@NotNull Shop shop) {
        this.deleteStockData(shop.getId());
    }

    public void deleteStockData(@NotNull String shopId) {
        this.dataHandler.delete(this.tableStockDataFused,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId))
        );
    }

    public void deleteStockData(@NotNull String shopId, @NotNull String productId) {
        this.dataHandler.delete(this.tableStockDataFused,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId)),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(productId))
        );
    }



    public void deletePriceData(@NotNull Product product) {
        this.deletePriceData(product.getShop().getId(), product.getId());
    }

    public void deletePriceData(@NotNull String shopId, @NotNull String productId) {
        this.dataHandler.delete(this.tablePriceData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId)),
            SQLCondition.equal(COLUMN_GEN_PRODUCT_ID.toValue(productId))
        );
    }

    public void deletePriceData(@NotNull Shop shop) {
        this.deletePriceData(shop.getId());
    }

    public void deletePriceData(@NotNull String shopId) {
        this.dataHandler.delete(this.tablePriceData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shopId))
        );
    }

    public void deleteRotationData(@NotNull RotatingShop shop) {
        this.dataHandler.delete(this.tableRotationData,
            SQLCondition.equal(COLUMN_GEN_SHOP_ID.toValue(shop.getId()))
        );
    }
}
