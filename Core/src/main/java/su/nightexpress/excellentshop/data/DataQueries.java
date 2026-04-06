package su.nightexpress.excellentshop.data;

import su.nightexpress.excellentshop.feature.virtualshop.rotation.data.RotationData;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.data.RotationItemData;
import su.nightexpress.excellentshop.shop.data.*;
import su.nightexpress.excellentshop.data.legacy.LegacyPriceData;
import su.nightexpress.excellentshop.data.legacy.LegacyStockData;
import su.nightexpress.excellentshop.data.legacy.LegacyRotationData;
import su.nightexpress.nexshop.user.ShopUser;
import su.nightexpress.nexshop.user.UserSettings;
import su.nightexpress.nightcore.db.statement.RowMapper;
import su.nightexpress.nightcore.db.statement.template.InsertStatement;
import su.nightexpress.nightcore.db.statement.template.UpdateStatement;

import java.util.List;
import java.util.UUID;

public class DataQueries {

    // LEGACY - START

    public static final RowMapper<LegacyStockData> LEGACY_STOCK_DATA_LOADER = resultSet -> {
        String shopId = resultSet.getString(DataColumns.LEGACY_SHOP_ID.getName());
        String productId = resultSet.getString(DataColumns.LEGACY_PRODUCT_ID.getName());
        String holderId = resultSet.getString(DataColumns.LEGACY_HOLDER_ID.getName());

        int buyStock = resultSet.getInt(DataColumns.LEGACY_STOCK_BUY.getName());
        int sellStock = resultSet.getInt(DataColumns.LEGACY_STOCK_SELL.getName());
        long restockDate = resultSet.getLong(DataColumns.STOCK_RESTOCK_DATE.getName());

        return new LegacyStockData(shopId, productId, holderId, buyStock, sellStock, restockDate);
    };

    public static final RowMapper<LegacyPriceData> LEGACY_PRICE_DATA_LOADER = resultSet -> {
        String shopId = resultSet.getString(DataColumns.LEGACY_SHOP_ID.getName());
        String productId = resultSet.getString(DataColumns.LEGACY_PRODUCT_ID.getName());

        double buyOffset = resultSet.getDouble(DataColumns.PRICE_BUY_OFFSET.getName());
        double sellOffset = resultSet.getDouble(DataColumns.PRICE_SELL_OFFSET.getName());

        long expireDate = resultSet.getLong(DataColumns.PRICE_EXPIRE_DATE.getName());
        int purchases = resultSet.getInt(DataColumns.PRICE_PURCHASES.getName());
        int sales = resultSet.getInt(DataColumns.PRICE_SALES.getName());

        return new LegacyPriceData(shopId, productId, buyOffset, sellOffset, expireDate, purchases, sales);
    };

    public static final RowMapper<LegacyRotationData> LEGACY_ROTATION_DATA_LOADER = resultSet -> {
        String shopId = resultSet.getString(DataColumns.LEGACY_SHOP_ID.getName());
        String rotationId = resultSet.getString(DataColumns.LEGACY_HOLDER_ID.getName());
        long nextRotation = resultSet.getLong(DataColumns.ROTATION_NEXT_ROTATION.getName());
        List<RotationItemData> products = DataColumns.ROTATION_PRODUCTS.readOrThrow(resultSet);

        return new LegacyRotationData(shopId, rotationId, nextRotation, products);
    };

    // LEGACY - END

    public static final RowMapper<ShopUser> SHOP_USER_MAPPER = resultSet -> {
        UUID uuid = DataColumns.USER_ID.readOrThrow(resultSet);
        String name = DataColumns.USER_NAME.readOrThrow(resultSet);
        UserSettings settings = DataColumns.USER_SETTINGS.readOrThrow(resultSet);

        return new ShopUser(uuid, name, settings);
    };

    public static final RowMapper<ProductPriceData> PRICE_DATA_LOADER = resultSet -> {
        UUID productId = DataColumns.PRICE_PRODUCT_ID.readOrThrow(resultSet);
        double buyOffset = DataColumns.PRICE_BUY_OFFSET.readOrThrow(resultSet);
        double sellOffset = DataColumns.PRICE_SELL_OFFSET.readOrThrow(resultSet);

        long expireDate = DataColumns.PRICE_EXPIRE_DATE.readOrThrow(resultSet);
        int purchases = DataColumns.PRICE_PURCHASES.readOrThrow(resultSet);
        int sales = DataColumns.PRICE_SALES.readOrThrow(resultSet);

        return new ProductPriceData(productId, buyOffset, sellOffset, expireDate, purchases, sales);
    };

    public static final RowMapper<ProductStockData> STOCK_DATA_LOADER = resultSet -> {
        UUID productId = DataColumns.STOCK_PRODUCT_ID.readOrThrow(resultSet);
        int stock = DataColumns.STOCK_UNITS.readOrThrow(resultSet);
        long restockDate = DataColumns.STOCK_RESTOCK_DATE.readOrThrow(resultSet);

        return new ProductStockData(productId, stock, restockDate);
    };

    public static final RowMapper<ProductLimitData> LIMIT_DATA_LOADER = resultSet -> {
        UUID playerId = DataColumns.LIMIT_PLAYER_ID.readOrThrow(resultSet);
        UUID productId = DataColumns.LIMIT_PRODUCT_ID.readOrThrow(resultSet);
        int pruchases = DataColumns.LIMIT_PURCHASES.readOrThrow(resultSet);
        int sales = DataColumns.LIMIT_SALES.readOrThrow(resultSet);
        long restockDate = DataColumns.LIMIT_RESTOCK_DATE.readOrThrow(resultSet);

        return new ProductLimitData(playerId, productId, pruchases, sales, restockDate);
    };

    public static final RowMapper<RotationData> ROTATION_DATA_LOADER = resultSet -> {
        UUID rotationId = DataColumns.ROTATION_ID.readOrThrow(resultSet);
        long nextRotation = DataColumns.ROTATION_NEXT_ROTATION.readOrThrow(resultSet);
        List<RotationItemData> products = DataColumns.ROTATION_PRODUCTS.readOrThrow(resultSet);

        return new RotationData(rotationId, nextRotation, products);
    };



    public static final InsertStatement<ShopUser> USER_INSERT = InsertStatement.builder(ShopUser.class)
        .ignoreDuplications()
        .setUUID(DataColumns.USER_ID, ShopUser::getId)
        .setString(DataColumns.USER_NAME, ShopUser::getName)
        .setString(DataColumns.USER_SETTINGS, user -> DataHandler.GSON.toJson(user.getSettings()))
        .build();

    public static final UpdateStatement<ShopUser> USER_UPDATE = UpdateStatement.builder(ShopUser.class)
        .setString(DataColumns.USER_NAME, ShopUser::getName)
        .setString(DataColumns.USER_SETTINGS, user -> DataHandler.GSON.toJson(user.getSettings()))
        .build();

    public static final UpdateStatement<ShopUser> USER_TINY_UPDATE = UpdateStatement.builder(ShopUser.class)
        .setString(DataColumns.USER_NAME, ShopUser::getName)
        .build();



    public static final InsertStatement<ProductStockData> STOCK_DATA_INSERT = InsertStatement.builder(ProductStockData.class)
        .updateOnConflict()
        .setUUID(DataColumns.STOCK_PRODUCT_ID, ProductStockData::getProductId)
        .setInt(DataColumns.STOCK_UNITS, ProductStockData::getStock)
        .setLong(DataColumns.STOCK_RESTOCK_DATE, ProductStockData::getRestockDate)
        .build();

    /*public static final UpdateStatement<ProductStockData> STOCK_DATA_UPDATE = UpdateStatement.builder(ProductStockData.class)
        .setInt(DataColumns.STOCK_UNITS, ProductStockData::getStock)
        .setLong(DataColumns.STOCK_RESTOCK_DATE, ProductStockData::getRestockDate)
        .build();*/



    public static final InsertStatement<ProductLimitData> LIMIT_DATA_INSERT = InsertStatement.builder(ProductLimitData.class)
        .updateOnConflict()
        .setUUID(DataColumns.LIMIT_PLAYER_ID, ProductLimitData::getPlayerId)
        .setUUID(DataColumns.LIMIT_PRODUCT_ID, ProductLimitData::getProductId)
        .setInt(DataColumns.LIMIT_PURCHASES, ProductLimitData::getPurchases)
        .setInt(DataColumns.LIMIT_SALES, ProductLimitData::getSales)
        .setLong(DataColumns.LIMIT_RESTOCK_DATE, ProductLimitData::getRestockDate)
        .build();

    /*public static final UpdateStatement<ProductLimitData> LIMIT_DATA_UPDATE = UpdateStatement.builder(ProductLimitData.class)
        .setInt(DataColumns.LIMIT_PURCHASES, ProductLimitData::getPurchases)
        .setInt(DataColumns.LIMIT_SALES, ProductLimitData::getSales)
        .setLong(DataColumns.LIMIT_RESTOCK_DATE, ProductLimitData::getRestockDate)
        .build();*/



    public static final InsertStatement<ProductPriceData> PRICE_DATA_INSERT = InsertStatement.builder(ProductPriceData.class)
        .updateOnConflict()
        .setUUID(DataColumns.PRICE_PRODUCT_ID, ProductPriceData::getProductId)
        .setDouble(DataColumns.PRICE_BUY_OFFSET, ProductPriceData::getBuyOffset)
        .setDouble(DataColumns.PRICE_SELL_OFFSET, ProductPriceData::getSellOffset)
        .setLong(DataColumns.PRICE_EXPIRE_DATE, ProductPriceData::getExpireDate)
        .setInt(DataColumns.PRICE_PURCHASES, ProductPriceData::getPurchases)
        .setInt(DataColumns.PRICE_SALES, ProductPriceData::getSales)
        .build();

    /*public static final UpdateStatement<ProductPriceData> PRICE_DATA_UPDATE = UpdateStatement.builder(ProductPriceData.class)
        .setDouble(DataColumns.PRICE_BUY_OFFSET, ProductPriceData::getBuyOffset)
        .setDouble(DataColumns.PRICE_SELL_OFFSET, ProductPriceData::getSellOffset)
        .setLong(DataColumns.PRICE_EXPIRE_DATE, ProductPriceData::getExpireDate)
        .setInt(DataColumns.PRICE_PURCHASES, ProductPriceData::getPurchases)
        .setInt(DataColumns.PRICE_SALES, ProductPriceData::getSales)
        .build();*/



    public static final InsertStatement<RotationData> ROTATION_DATA_INSERT = InsertStatement.builder(RotationData.class)
        .updateOnConflict()
        .setUUID(DataColumns.ROTATION_ID, RotationData::getRotationId)
        .setLong(DataColumns.ROTATION_NEXT_ROTATION, RotationData::getNextRotationDate)
        .setString(DataColumns.ROTATION_PRODUCTS, data -> DataHandler.GSON.toJson(data.getProducts()))
        .build();

    /*public static final UpdateStatement<RotationData> ROTATION_DATA_UPDATE = UpdateStatement.builder(RotationData.class)
        .setLong(DataColumns.ROTATION_NEXT_ROTATION, RotationData::getNextRotationDate)
        .setString(DataColumns.ROTATION_PRODUCTS, data -> DataHandler.GSON.toJson(data.getProducts()))
        .build();*/
}
