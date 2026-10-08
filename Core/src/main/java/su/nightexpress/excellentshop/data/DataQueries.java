package su.nightexpress.excellentshop.data;

import java.util.List;
import java.util.UUID;

import su.nightexpress.excellentshop.shop.data.ProductLimitData;
import su.nightexpress.excellentshop.shop.data.ProductPriceData;
import su.nightexpress.excellentshop.shop.data.ProductStockData;
import su.nightexpress.excellentshop.user.ShopUser;
import su.nightexpress.excellentshop.user.UserSettings;
import su.nightexpress.excellentshop.virtualshop.rotation.data.RotationData;
import su.nightexpress.excellentshop.virtualshop.rotation.data.RotationItemData;
import su.nightexpress.nightcore.db.statement.RowMapper;
import su.nightexpress.nightcore.db.statement.template.InsertStatement;
import su.nightexpress.nightcore.db.statement.template.UpdateStatement;

public class DataQueries {

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
        boolean restocking = DataColumns.LIMIT_RESTOCKING.readOrThrow(resultSet);

        // Patch for 5.1.0 -> 5.1.1
        if (!restocking && restockDate > 0) restocking = true;

        return new ProductLimitData(playerId, productId, pruchases, sales, restockDate, restocking);
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


    public static final InsertStatement<ProductStockData> STOCK_DATA_INSERT = InsertStatement.builder(
        ProductStockData.class)
        .updateOnConflict()
        .setUUID(DataColumns.STOCK_PRODUCT_ID, ProductStockData::getProductId)
        .setInt(DataColumns.STOCK_UNITS, ProductStockData::getStock)
        .setLong(DataColumns.STOCK_RESTOCK_DATE, ProductStockData::getRestockDate)
        .build();

    /*public static final UpdateStatement<ProductStockData> STOCK_DATA_UPDATE = UpdateStatement.builder(ProductStockData.class)
        .setInt(DataColumns.STOCK_UNITS, ProductStockData::getStock)
        .setLong(DataColumns.STOCK_RESTOCK_DATE, ProductStockData::getRestockDate)
        .build();*/


    public static final InsertStatement<ProductLimitData> LIMIT_DATA_INSERT = InsertStatement.builder(
        ProductLimitData.class)
        .updateOnConflict()
        .setUUID(DataColumns.LIMIT_PLAYER_ID, ProductLimitData::getPlayerId)
        .setUUID(DataColumns.LIMIT_PRODUCT_ID, ProductLimitData::getProductId)
        .setInt(DataColumns.LIMIT_PURCHASES, ProductLimitData::getPurchases)
        .setInt(DataColumns.LIMIT_SALES, ProductLimitData::getSales)
        .setLong(DataColumns.LIMIT_RESTOCK_DATE, ProductLimitData::getRestockDate)
        .setBoolean(DataColumns.LIMIT_RESTOCKING, ProductLimitData::isRestocking)
        .build();

    /*public static final UpdateStatement<ProductLimitData> LIMIT_DATA_UPDATE = UpdateStatement.builder(ProductLimitData.class)
        .setInt(DataColumns.LIMIT_PURCHASES, ProductLimitData::getPurchases)
        .setInt(DataColumns.LIMIT_SALES, ProductLimitData::getSales)
        .setLong(DataColumns.LIMIT_RESTOCK_DATE, ProductLimitData::getRestockDate)
        .build();*/


    public static final InsertStatement<ProductPriceData> PRICE_DATA_INSERT = InsertStatement.builder(
        ProductPriceData.class)
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
