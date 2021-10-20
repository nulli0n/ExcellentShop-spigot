package su.nightexpress.nexshop.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.ShopClickType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

public interface IShopProduct extends ICleanable, IEditable, IPlaceholder {

    String PLACEHOLDER_PRICE_BUY                = "%product_price_buy%";
    String PLACEHOLDER_PRICE_BUY_FORMATTED      = "%product_price_buy_formatted%";
    String PLACEHOLDER_PRICE_SELL               = "%product_price_sell%";
    String PLACEHOLDER_PRICE_SELL_FORMATTED     = "%product_price_sell_formatted%";
    String PLACEHOLDER_PRICE_SELL_ALL           = "%product_price_sell_all%";
    String PLACEHOLDER_PRICE_SELL_ALL_FORMATTED = "%product_price_sell_all_formatted%";
    String PLACEHOLDER_LIMIT_BUY_AVAILABLE      = "%product_limit_buy_available%";
    String PLACEHOLDER_LIMIT_SELL_AVAILABLE     = "%product_limit_sell_available%";

    String PLACEHOLDER_PRICE_BUY_MIN = "%product_price_buy_min%";
    String PLACEHOLDER_PRICE_BUY_MAX = "%product_price_buy_max%";
    String PLACEHOLDER_PRICE_SELL_MIN = "%product_price_sell_min%";
    String PLACEHOLDER_PRICE_SELL_MAX = "%product_price_sell_max%";
    String PLACEHOLDER_DISCOUNT_ALLOWED = "%product_discount_allowed%";
    String PLACEHOLDER_PRICE_RANDOM_ENABLED = "%product_price_random_enabled%";
    String PLACEHOLDER_PRICE_RANDOM_DAYS = "%product_price_random_days%";
    String PLACEHOLDER_PRICE_RANDOM_TIMES = "%product_price_random_times%";
    String PLACEHOLDER_ITEM_META_ENABLED = "%product_item_meta_enabled%";
    String PLACEHOLDER_ITEM_NAME = "%product_item_name%";
    String PLACEHOLDER_PREVIEW_NAME = "%product_preview_name%";

    @NotNull
    IShop getShop();

    @NotNull
    String getId();

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        IProductPricer pricer = this.getPricer();
        IShopCurrency currency = this.getCurrency();
        double priceBuy = this.getPricer().getPriceBuy(true);
        double priceSell = this.getPricer().getPriceSell();

        ItemStack buyItem = this.getItem();
        String itemName = !ItemUT.isAir(buyItem) ? ItemUT.getItemName(buyItem) : "null";
        DateTimeFormatter timeFormat = DateTimeFormatter.ISO_LOCAL_TIME;

        return str -> str
                .replace(PLACEHOLDER_PRICE_BUY, NumberUT.format(priceBuy))
                .replace(PLACEHOLDER_PRICE_BUY_FORMATTED, priceBuy >= 0 ? currency.format(priceBuy) : "-")
                .replace(PLACEHOLDER_PRICE_SELL, NumberUT.format(priceSell))
                .replace(PLACEHOLDER_PRICE_SELL_FORMATTED, priceSell >= 0 ? currency.format(priceSell) : "-")
                .replace(PLACEHOLDER_PRICE_BUY_MIN, String.valueOf(pricer.getPriceMin(TradeType.BUY)))
                .replace(PLACEHOLDER_PRICE_BUY_MAX, String.valueOf(pricer.getPriceMax(TradeType.BUY)))
                .replace(PLACEHOLDER_PRICE_SELL_MIN, String.valueOf(pricer.getPriceMin(TradeType.SELL)))
                .replace(PLACEHOLDER_PRICE_SELL_MAX, String.valueOf(pricer.getPriceMax(TradeType.SELL)))
                .replace(PLACEHOLDER_DISCOUNT_ALLOWED, getShop().plugin().lang().getBool(this.isDiscountAllowed()))
                .replace(PLACEHOLDER_PRICE_RANDOM_ENABLED, getShop().plugin().lang().getBool(pricer.isRandomizerEnabled()))
                .replace(PLACEHOLDER_PRICE_RANDOM_DAYS, String.join(DELIMITER_DEFAULT, pricer.getDays()
                        .stream().map(DayOfWeek::name).toList()))
                .replace(PLACEHOLDER_PRICE_RANDOM_TIMES, String.join(DELIMITER_DEFAULT, pricer.getTimes()
                        .stream().map(arr -> timeFormat.format(arr[0]) + "-" + timeFormat.format(arr[1])).toList()))
                .replace(PLACEHOLDER_ITEM_META_ENABLED, getShop().plugin().lang().getBool(this.isItemMetaEnabled()))
                .replace(PLACEHOLDER_ITEM_NAME, itemName)
                .replace(PLACEHOLDER_PREVIEW_NAME, ItemUT.getItemName(this.getPreview()))
                ;
    }

    @NotNull
    default UnaryOperator<String> replacePlaceholders(@NotNull Player player) {
        double priceSell = this.getPricer().getPriceSell();
        double priceSellAll = this.getPricer().getPriceSellAll(player);

        IShopCurrency currency = this.getCurrency();
        int limBuy = this.getStockAmountLeft(player, TradeType.BUY);
        String limitBuy = limBuy >= 0 ? String.valueOf(limBuy) : getShop().plugin().lang().Other_Infinity.getMsg();

        int limSell = this.getStockAmountLeft(player, TradeType.SELL);
        String limitSell = limSell >= 0 ? String.valueOf(limSell) : getShop().plugin().lang().Other_Infinity.getMsg();

        return str -> this.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_PRICE_SELL_ALL, NumberUT.format(priceSellAll))
                .replace(PLACEHOLDER_PRICE_SELL_ALL_FORMATTED, priceSell >= 0 ? currency.format(priceSellAll) : "-")
                .replace(PLACEHOLDER_LIMIT_BUY_AVAILABLE, limitBuy)
                .replace(PLACEHOLDER_LIMIT_SELL_AVAILABLE, limitSell)
        );
    }

    @NotNull
    IProductPricer getPricer();

    @NotNull
    IShopCurrency getCurrency();

    void setCurrency(@NotNull IShopCurrency currency);

    @NotNull
    IProductPrepared getPrepared(@NotNull TradeType tradeType);

    int getStockAmountLeft(@NotNull Player player, @NotNull TradeType tradeType);

    boolean isDiscountAllowed();

    void setDiscountAllowed(boolean isAllowed);

    default void prepareTrade(@NotNull Player player, @NotNull ShopClickType click) {
        if (this.isEmpty()) {
            return;
        }

        IShop shop = this.getShop();
        TradeType tradeType = click.getBuyType();
        if (!shop.isPurchaseAllowed(tradeType)) {
            return;
        }

        if (tradeType == TradeType.BUY) {
            if (!this.isBuyable()) {
                shop.plugin().lang().Shop_Product_Error_Unbuyable.send(player);
                return;
            }
            if (this.hasItem()) {
                if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY && PlayerUT.countItemSpace(player, this.getItem()) == 0) {
                    this.getShop().plugin().lang().Shop_Product_Error_FullInventory.send(player);
                    return;
                }
            }
        }
        else if (tradeType == TradeType.SELL) {
            if (!this.isSellable()) {
                shop.plugin().lang().Shop_Product_Error_Unsellable.send(player);
                return;
            }
        }

        // Для Virtual Shop вернет остаточное кол-во предметов от лимита БД (если есть).
        // Для Chest Shop вернет количество свободного места или количество предмета в сундуке.
        int canPurchase = this.getStockAmountLeft(player, tradeType);
        if (canPurchase == 0) {
            ILangMsg msgStock;
            if (tradeType == TradeType.BUY) {
                msgStock = shop.plugin().lang().Shop_Product_Error_OutOfStock;
            }
            else {
                if (shop instanceof IShopChest shopChest) {
                    msgStock = shop.plugin().lang().Shop_Product_Error_OutOfSpace;
                }
                else msgStock = shop.plugin().lang().Shop_Product_Error_FullStock;
            }
            msgStock.send(player);
            return;
        }

        boolean isSellAll = (click == ShopClickType.SELL_ALL);
        IProductPrepared prepared = this.getPrepared(tradeType);
        if (click == ShopClickType.BUY_SINGLE || click == ShopClickType.SELL_SINGLE || isSellAll) {
            prepared.trade(player, isSellAll);
            shop.open(player, shop.getView().getPage(player)); // Update current shop page
            return;
        }
        this.openTrade(player, prepared);
    }

    default void openTrade(@NotNull Player player, @NotNull IProductPrepared prepared) {
        Config.getCartMenu(prepared.getTradeType()).open(player, prepared);
    }

    default boolean isEmpty() {
        return !this.hasItem();
    }

    boolean isItemMetaEnabled();

    void setItemMetaEnabled(boolean isEnabled);

    @NotNull
    ItemStack getItem();

    void setItem(@NotNull ItemStack item);

    default boolean hasItem() {
        return !ItemUT.isAir(this.getItem());
    }

    default boolean isItemMatches(@NotNull ItemStack item) {
        return this.isItemMetaEnabled() ? this.getItem().isSimilar(item) : this.getItem().getType() == item.getType();
    }

    default int getItemAmount(@NotNull Player player) {
        if (!this.hasItem()) return 0;

        return PlayerUT.countItem(player, this::isItemMatches);
    }

    default void takeItemAmount(@NotNull Player player, int amount) {
        if (!this.hasItem()) return;

        PlayerUT.takeItem(player, this::isItemMatches, amount);
    }

    @NotNull ItemStack getPreview();

    void setPreview(@NotNull ItemStack preview);

    default boolean isBuyable() {
        /*if (this.getLimitAmount(TradeType.BUY) == 0) {
            return false;
        }*/

        IProductPricer pricer = this.getPricer();
        if (pricer.getPriceBuy(false) < 0D || pricer.getPriceBuy(true) < 0D) {
            return false;
        }

        return true;
    }

    default boolean isSellable() {
        if (!this.hasItem()/* || this.getLimitAmount(TradeType.SELL) == 0*/) {
            return false;
        }

        IProductPricer pricer = this.getPricer();
        double priceSell = pricer.getPriceSell();
        if (priceSell < 0D) {
            return false;
        }

        // Check if this product is buyable, so we can check if sell price is over the buy price
        // to prevent money duplication.
        // If this product can not be purchased, these checks are useless.
        if (this.getShop().isPurchaseAllowed(TradeType.BUY) && this.isBuyable()) {
            if (priceSell > pricer.getPriceBuy(false) || priceSell > pricer.getPriceBuy(true)) {
                return false;
            }
        }

        return true;
    }


}
