package su.nightexpress.nexshop.data;

import com.google.gson.reflect.TypeToken;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.legacy.LegacyStockAmount;
import su.nightexpress.nexshop.data.legacy.LegacyStockData;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nexshop.data.product.StockData;
import su.nightexpress.nexshop.data.shop.RotationData;
import su.nightexpress.nexshop.shop.chest.impl.ChestBank;
import su.nightexpress.nexshop.shop.virtual.impl.Rotation;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nightcore.db.sql.query.impl.DeleteQuery;
import su.nightexpress.nightcore.db.sql.query.impl.InsertQuery;
import su.nightexpress.nightcore.db.sql.query.impl.UpdateQuery;
import su.nightexpress.nightcore.db.sql.util.WhereOperator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class DataQueries {

    public static final Function<ResultSet, LegacyStockData> LEGACY_STOCK_DATA_LOADER = resultSet -> {
        try {
            String shopId = resultSet.getString(DataHandler.COLUMN_GEN_SHOP_ID.getName());
            String productId = resultSet.getString(DataHandler.COLUMN_GEN_PRODUCT_ID.getName());

            Map<TradeType, LegacyStockAmount> globalAmount = DataHandler.GSON
                .fromJson(resultSet.getString("globalAmount"), new TypeToken<Map<TradeType, LegacyStockAmount>>(){}.getType());

            Map<TradeType, Map<UUID, LegacyStockAmount>> playerAmount = DataHandler.GSON
                .fromJson(resultSet.getString("playerAmount"), new TypeToken<Map<TradeType, Map<UUID, LegacyStockAmount>>>(){}.getType());

            return new LegacyStockData(shopId, productId, globalAmount, playerAmount);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final Function<ResultSet, PriceData> PRICE_DATA_LOADER = resultSet -> {
        try {
            String shopId = resultSet.getString(DataHandler.COLUMN_GEN_SHOP_ID.getName());
            String productId = resultSet.getString(DataHandler.COLUMN_GEN_PRODUCT_ID.getName());

            double buyOffset = resultSet.getDouble(DataHandler.COLUMN_PRICE_BUY_OFFSET.getName());
            double sellOffset = resultSet.getDouble(DataHandler.COLUMN_PRICE_SELL_OFFSET.getName());

            long expireDate = resultSet.getLong(DataHandler.COLUMN_PRICE_EXPIRE_DATE.getName());
            int purchases = resultSet.getInt(DataHandler.COLUMN_PRICE_PURCHASES.getName());
            int sales = resultSet.getInt(DataHandler.COLUMN_PRICE_SALES.getName());

            return new PriceData(shopId, productId, buyOffset, sellOffset, expireDate, purchases, sales);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final Function<ResultSet, StockData> STOCK_DATA_LOADER = resultSet -> {
        try {
            String shopId = resultSet.getString(DataHandler.COLUMN_GEN_SHOP_ID.getName());
            String productId = resultSet.getString(DataHandler.COLUMN_GEN_PRODUCT_ID.getName());
            String holderId = resultSet.getString(DataHandler.COLUMN_GEN_HOLDER_ID.getName());

            int buyStock = resultSet.getInt(DataHandler.COLUMN_STOCK_BUY_STOCK.getName());
            int sellStock = resultSet.getInt(DataHandler.COLUMN_STOCK_SELL_STOCK.getName());
            long restockDate = resultSet.getLong(DataHandler.COLUMN_STOCK_RESTOCK_DATE.getName());

            return new StockData(shopId, productId, holderId, buyStock, sellStock, restockDate);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final InsertQuery<StockData> STOCK_DATA_INSERT = new InsertQuery<StockData>()
        .setValue(DataHandler.COLUMN_GEN_SHOP_ID, StockData::getShopId)
        .setValue(DataHandler.COLUMN_GEN_PRODUCT_ID, StockData::getProductId)
        .setValue(DataHandler.COLUMN_GEN_HOLDER_ID, StockData::getHolder)
        .setValue(DataHandler.COLUMN_STOCK_BUY_STOCK, data -> String.valueOf(data.getBuyStock()))
        .setValue(DataHandler.COLUMN_STOCK_SELL_STOCK, data -> String.valueOf(data.getSellStock()))
        .setValue(DataHandler.COLUMN_STOCK_RESTOCK_DATE, data -> String.valueOf(data.getRestockDate()));

    public static final UpdateQuery<StockData> STOCK_DATA_UPDATE = new UpdateQuery<StockData>()
        .setValue(DataHandler.COLUMN_STOCK_BUY_STOCK, data -> String.valueOf(data.getBuyStock()))
        .setValue(DataHandler.COLUMN_STOCK_SELL_STOCK, data -> String.valueOf(data.getSellStock()))
        .setValue(DataHandler.COLUMN_STOCK_RESTOCK_DATE, data -> String.valueOf(data.getRestockDate()))
        .whereIgnoreCase(DataHandler.COLUMN_GEN_SHOP_ID, WhereOperator.EQUAL, StockData::getShopId)
        .whereIgnoreCase(DataHandler.COLUMN_GEN_PRODUCT_ID, WhereOperator.EQUAL, StockData::getProductId)
        .whereIgnoreCase(DataHandler.COLUMN_GEN_HOLDER_ID, WhereOperator.EQUAL, StockData::getHolder);


    public static final InsertQuery<PriceData> PRICE_DATA_INSERT = new InsertQuery<PriceData>()
        .setValue(DataHandler.COLUMN_GEN_SHOP_ID, PriceData::getShopId)
        .setValue(DataHandler.COLUMN_GEN_PRODUCT_ID, PriceData::getProductId)
        .setValue(DataHandler.COLUMN_PRICE_BUY_OFFSET, data -> String.valueOf(data.getBuyOffset()))
        .setValue(DataHandler.COLUMN_PRICE_SELL_OFFSET, data -> String.valueOf(data.getSellOffset()))
        .setValue(DataHandler.COLUMN_PRICE_EXPIRE_DATE, d -> String.valueOf(d.getExpireDate()))
        .setValue(DataHandler.COLUMN_PRICE_PURCHASES, d -> String.valueOf(d.getPurchases()))
        .setValue(DataHandler.COLUMN_PRICE_SALES, d -> String.valueOf(d.getSales()));

    public static final UpdateQuery<PriceData> PRICE_DATA_UPDATE = new UpdateQuery<PriceData>()
        .setValue(DataHandler.COLUMN_PRICE_BUY_OFFSET, data -> String.valueOf(data.getBuyOffset()))
        .setValue(DataHandler.COLUMN_PRICE_SELL_OFFSET, data -> String.valueOf(data.getSellOffset()))
        .setValue(DataHandler.COLUMN_PRICE_EXPIRE_DATE, d -> String.valueOf(d.getExpireDate()))
        .setValue(DataHandler.COLUMN_PRICE_PURCHASES, d -> String.valueOf(d.getPurchases()))
        .setValue(DataHandler.COLUMN_PRICE_SALES, d -> String.valueOf(d.getSales()))
        .whereIgnoreCase(DataHandler.COLUMN_GEN_SHOP_ID, WhereOperator.EQUAL, PriceData::getShopId)
        .whereIgnoreCase(DataHandler.COLUMN_GEN_PRODUCT_ID, WhereOperator.EQUAL, PriceData::getProductId);


    public static final Function<ResultSet, RotationData> ROTATION_DATA_LOADER = resultSet -> {
        try {
            String shopId = resultSet.getString(DataHandler.COLUMN_GEN_SHOP_ID.getName());
            String rotationId = resultSet.getString(DataHandler.COLUMN_GEN_HOLDER_ID.getName());
            long nextRotation = resultSet.getLong(DataHandler.COLUMN_ROTATE_NEXT_ROTATION.getName());
            Map<Integer, List<String>> products = DataHandler.GSON.fromJson(resultSet.getString(DataHandler.COLUMN_ROTATE_PRODUCTS.getName()), new TypeToken<Map<Integer, List<String>>>(){}.getType());

            return new RotationData(shopId, rotationId, nextRotation, products);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final InsertQuery<RotationData> ROTATION_DATA_INSERT = new InsertQuery<RotationData>()
        .setValue(DataHandler.COLUMN_GEN_SHOP_ID, RotationData::getShopId)
        .setValue(DataHandler.COLUMN_GEN_HOLDER_ID, RotationData::getRotationId)
        .setValue(DataHandler.COLUMN_ROTATE_NEXT_ROTATION, data -> String.valueOf(data.getNextRotationDate()))
        .setValue(DataHandler.COLUMN_ROTATE_PRODUCTS, data -> DataHandler.GSON.toJson(data.getProducts()));

    public static final UpdateQuery<RotationData> ROTATION_DATA_UPDATE = new UpdateQuery<RotationData>()
        .setValue(DataHandler.COLUMN_ROTATE_NEXT_ROTATION, data -> String.valueOf(data.getNextRotationDate()))
        .setValue(DataHandler.COLUMN_ROTATE_PRODUCTS, data -> DataHandler.GSON.toJson(data.getProducts()))
        .whereIgnoreCase(DataHandler.COLUMN_GEN_SHOP_ID, WhereOperator.EQUAL, RotationData::getShopId)
        .whereIgnoreCase(DataHandler.COLUMN_GEN_HOLDER_ID, WhereOperator.EQUAL, RotationData::getRotationId);

    public static final DeleteQuery<Rotation> ROTATION_DATA_DELETE_BY_SELF = new DeleteQuery<Rotation>()
        .whereIgnoreCase(DataHandler.COLUMN_GEN_SHOP_ID, WhereOperator.EQUAL, rotation -> rotation.getShop().getId())
        .whereIgnoreCase(DataHandler.COLUMN_GEN_HOLDER_ID, WhereOperator.EQUAL, Rotation::getId);

    public static final DeleteQuery<VirtualShop> ROTATION_DATA_DELETE_BY_SHOP = new DeleteQuery<VirtualShop>()
        .whereIgnoreCase(DataHandler.COLUMN_GEN_SHOP_ID, WhereOperator.EQUAL, Shop::getId);



    public static final Function<ResultSet, ChestBank> CHEST_BANK_LOADER = resultSet -> {
        try {
            UUID holder = UUID.fromString(resultSet.getString(DataHandler.COLUMN_BANK_HOLDER.getName()));

            Map<String, Double> balanceRaw = DataHandler.GSON.fromJson(resultSet.getString(DataHandler.COLUMN_BANK_BALANCE.getName()), new TypeToken<Map<String, Double>>(){}.getType());
            if (balanceRaw == null) balanceRaw = new HashMap<>();

//            Map<Currency, Double> balanceMap = new HashMap<>();
//            balanceRaw.forEach((id, amount) -> {
//                Currency currency = EconomyBridge.getCurrency(CurrencyId.reroute(id));
//                if (currency == null) return;
//
//                balanceMap.put(currency, amount);
//            });

            return new ChestBank(holder, balanceRaw);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final InsertQuery<ChestBank> CHEST_BANK_INSERT = new InsertQuery<ChestBank>()
        .setValue(DataHandler.COLUMN_BANK_HOLDER, bank -> bank.getHolder().toString())
        .setValue(DataHandler.COLUMN_BANK_BALANCE, bank -> DataHandler.GSON.toJson(bank.getBalanceMap()));

    public static final UpdateQuery<ChestBank> CHEST_BANK_UPDATE = new UpdateQuery<ChestBank>()
        .setValue(DataHandler.COLUMN_BANK_BALANCE, bank -> DataHandler.GSON.toJson(bank.getBalanceMap()))
        .whereIgnoreCase(DataHandler.COLUMN_BANK_HOLDER, WhereOperator.EQUAL, bank -> bank.getHolder().toString());

    public static final DeleteQuery<ChestBank> CHEST_BANK_DELETE_BY_SELF = new DeleteQuery<ChestBank>()
        .whereIgnoreCase(DataHandler.COLUMN_BANK_HOLDER, WhereOperator.EQUAL, bank -> bank.getHolder().toString());
}
