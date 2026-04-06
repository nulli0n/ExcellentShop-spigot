package su.nightexpress.excellentshop.data;

import su.nightexpress.excellentshop.feature.virtualshop.rotation.data.RotationItemData;
import su.nightexpress.nexshop.user.UserSettings;
import su.nightexpress.nightcore.db.column.Column;
import su.nightexpress.nightcore.db.column.ColumnDataReader;

import java.util.List;
import java.util.UUID;

public class DataColumns {

    // LEGACY - START
    public static final Column<Integer> LEGACY_STOCK_BUY  = Column.intType("buyStock").build();
    public static final Column<Integer> LEGACY_STOCK_SELL = Column.intType("sellStock").build();

    public static final Column<String> LEGACY_SHOP_ID    = Column.stringType("shopId", 256).build();
    public static final Column<String> LEGACY_PRODUCT_ID = Column.stringType("productId", 256).build();
    public static final Column<String> LEGACY_HOLDER_ID  = Column.stringType("holderId", 256).build();
    // LEGACY - END

    public static final Column<Integer> ID = Column.intType("id").primaryKey().autoIncrement().build();

    public static final Column<UUID>    STOCK_PRODUCT_ID   = Column.uuidType("productId").primaryKey().build();
    public static final Column<Integer> STOCK_UNITS        = Column.intType("units").build();
    public static final Column<Long>    STOCK_RESTOCK_DATE = Column.longType("restockDate").build();

    public static final Column<UUID>    LIMIT_PLAYER_ID    = Column.uuidType("playerId").primaryKey().build();
    public static final Column<UUID>    LIMIT_PRODUCT_ID   = Column.uuidType("productId").primaryKey().build();
    public static final Column<Integer> LIMIT_PURCHASES    = Column.intType("purchases").build();
    public static final Column<Integer> LIMIT_SALES        = Column.intType("sales").build();
    public static final Column<Long>    LIMIT_RESTOCK_DATE = Column.longType("restockDate").build();

    public static final Column<UUID>    PRICE_PRODUCT_ID  = Column.uuidType("productId").primaryKey().build();
    public static final Column<Double>  PRICE_BUY_OFFSET  = Column.doubleType("buyOffset").defaultValue(0).build();
    public static final Column<Double>  PRICE_SELL_OFFSET = Column.doubleType("sellOffset").defaultValue(0).build();
    public static final Column<Long>    PRICE_EXPIRE_DATE = Column.longType("expireDate").build();
    public static final Column<Integer> PRICE_PURCHASES   = Column.intType("purchases").build();
    public static final Column<Integer> PRICE_SALES       = Column.intType("sales").build();

    public static final Column<UUID>                   ROTATION_ID            = Column.uuidType("rotationId").primaryKey().build();
    public static final Column<List<RotationItemData>> ROTATION_PRODUCTS      = Column.jsonList("items", DataHandler.GSON, RotationItemData.class).defaultValue("[]").build();
    public static final Column<Long>                   ROTATION_NEXT_ROTATION = Column.longType("nextRotation").build();

    public static final Column<UUID>         USER_ID       = Column.uuidType("uuid").build();
    public static final Column<String>       USER_NAME     = Column.stringType("name", 32).build();
    public static final Column<UserSettings> USER_SETTINGS = Column.json("settings", ColumnDataReader.jsonObject(DataHandler.GSON, UserSettings.class)).build();
}
