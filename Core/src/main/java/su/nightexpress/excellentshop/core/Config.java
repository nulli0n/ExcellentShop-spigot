package su.nightexpress.excellentshop.core;

import su.nightexpress.nightcore.config.ConfigValue;

public class Config {

    public static final String DIR_MENU = "/menu/";

    public static final ConfigValue<Boolean> CURRENCY_NEED_PERMISSION = ConfigValue.create("General.Currency_Need_Permission",
        false,
        "Controls whether players must have '" + Perms.PREFIX_CURRENCY + "[name]' permission to use specific currency for their shops and listings.",
        "[*] Useful only for ChestShop and Auction modules.",
        "[*] Currencies set as default are always allowed to use."
    );

    @Deprecated
    public static final ConfigValue<Boolean> GENERAL_BUY_WITH_FULL_INVENTORY = ConfigValue.create("General.Buy_With_Full_Inventory",
        false,
        "Sets wheter players can purchase items from shop with full inventory."
    );

    public static final ConfigValue<Integer> DATA_SAVE_INTERVAL = ConfigValue.create("Data.SaveInterval",
        5,
        "Sets how often (in seconds) modified product & shop datas will be saved to the database.",
        "Data including:",
        "- Product's price data (float, dynamic).",
        "- Product's stock data (global, player).",
        "- Shop's rotation data.",
        "[*] Data is also saved on server reboot and plugin reload.",
        "[*] You can disable it if you're on SQLite or don't care about syncing across multiple servers."
    );

    public static final ConfigValue<String> DATA_LEGACY_PRICE_TABLE = ConfigValue.create("Data.PriceTable",
        "price_data"
    );

    public static final ConfigValue<String> DATA_LEGACY_STOCKS_TABLE = ConfigValue.create("Data.StocksTable",
        "stocks"
    );

    public static final ConfigValue<String> DATA_PRICE_TABLE = ConfigValue.create("Data.Table.ProductPrice",
        "product_prices"
    );

    public static final ConfigValue<String> DATA_GLOBAL_STOCK_TABLE = ConfigValue.create("Data.Table.GlobalStock",
        "global_stock"
    );

    public static final ConfigValue<String> DATA_PLAYER_LIMIT_TABLE = ConfigValue.create("Data.Table.PlayerLimits",
        "player_limits"
    );

    public static final ConfigValue<String> DATA_ROTATIONS_TABLE = ConfigValue.create("Data.Table.Rotations",
        "shop_rotations"
    );

    public static final ConfigValue<String> DATA_LEGACY_ROTATIONS_TABLE = ConfigValue.create("Data.RotationsTable",
        "rotations"
    );
}
