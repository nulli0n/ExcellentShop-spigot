package su.nightexpress.nexshop.shop.chest.editor;

public enum ChestEditorType {

    SHOP_CHANGE_NAME,
    SHOP_CHANGE_ADMIN,
    SHOP_CHANGE_TRANSACTIONS,
    SHOP_CHANGE_PRODUCTS,
    SHOP_DELETE,

    //PRODUCT_CHANGE_COMMANDS,
    PRODUCT_CHANGE_CURRENCY,

    PRODUCT_CHANGE_PRICE_BUY,
    PRODUCT_CHANGE_PRICE_BUY_MIN,
    PRODUCT_CHANGE_PRICE_BUY_MAX,
    PRODUCT_CHANGE_PRICE_SELL,
    PRODUCT_CHANGE_PRICE_SELL_MIN,
    PRODUCT_CHANGE_PRICE_SELL_MAX,

    PRODUCT_CHANGE_PRICE_RND,
    PRODUCT_CHANGE_PRICE_RND_TOGGLE,
    PRODUCT_CHANGE_PRICE_RND_TIME_DAY,
    PRODUCT_CHANGE_PRICE_RND_TIME_TIME,
}
