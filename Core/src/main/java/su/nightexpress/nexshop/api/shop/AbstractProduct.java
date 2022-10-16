package su.nightexpress.nexshop.api.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.chest.IShopChest;
import su.nightexpress.nexshop.api.type.ShopClickType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

public abstract class AbstractProduct<S extends IShop> implements IProduct {

    protected final S      shop;
    protected final String id;

    protected ItemStack shopPreview;
    protected ItemStack rewardItem;

    protected       ICurrency      currency;
    protected final IProductPricer pricer;

    protected boolean isDiscountAllowed;
    protected boolean isItemMetaEnabled;

    public AbstractProduct(@NotNull S shop, @NotNull String id,
                           @NotNull ItemStack itemPreview, @Nullable ItemStack itemReward,
                           @NotNull ICurrency currency, @NotNull IProductPricer pricer,
                           boolean isDiscountAllowed, boolean isItemMetaEnabled) {
        this.shop = shop;
        this.id = id.toLowerCase();
        this.pricer = pricer;
        this.pricer.setProduct(this);

        this.setPreview(itemPreview);
        this.setItem(itemReward);
        this.setCurrency(currency);
        this.setDiscountAllowed(isDiscountAllowed);
        this.setItemMetaEnabled(isItemMetaEnabled);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        IProductPricer pricer = this.getPricer();
        ItemStack buyItem = this.getItem();
        String itemName = !buyItem.getType().isAir() ? ItemUtil.getItemName(buyItem) : "null";
        DateTimeFormatter timeFormat = DateTimeFormatter.ISO_LOCAL_TIME;

        return str -> this.replacePlaceholdersView().apply(str)
            .replace(Placeholders.PRODUCT_PRICE_BUY_MIN, String.valueOf(pricer.getPriceMin(TradeType.BUY)))
            .replace(Placeholders.PRODUCT_PRICE_BUY_MAX, String.valueOf(pricer.getPriceMax(TradeType.BUY)))
            .replace(Placeholders.PRODUCT_PRICE_SELL_MIN, String.valueOf(pricer.getPriceMin(TradeType.SELL)))
            .replace(Placeholders.PRODUCT_PRICE_SELL_MAX, String.valueOf(pricer.getPriceMax(TradeType.SELL)))
            .replace(Placeholders.PRODUCT_DISCOUNT_ALLOWED, LangManager.getBoolean(this.isDiscountAllowed()))
            .replace(Placeholders.PRODUCT_PRICE_RANDOM_ENABLED, LangManager.getBoolean(pricer.isRandomizerEnabled()))
            .replace(Placeholders.PRODUCT_PRICE_RANDOM_DAYS, String.join(DELIMITER_DEFAULT, pricer.getDays()
                .stream().map(DayOfWeek::name).toList()))
            .replace(Placeholders.PRODUCT_PRICE_RANDOM_TIMES, String.join(DELIMITER_DEFAULT, pricer.getTimes()
                .stream().map(arr -> timeFormat.format(arr[0]) + "-" + timeFormat.format(arr[1])).toList()))
            .replace(Placeholders.PRODUCT_ITEM_META_ENABLED, LangManager.getBoolean(this.isItemMetaEnabled()))
            .replace(Placeholders.PRODUCT_ITEM_NAME, itemName)
            .replace(Placeholders.PRODUCT_ITEM_LORE, String.join("\n", ItemUtil.getLore(buyItem)))
            .replace(Placeholders.PRODUCT_PREVIEW_NAME, ItemUtil.getItemName(this.getPreview()))
            .replace(Placeholders.PRODUCT_PREVIEW_LORE, String.join("\n", ItemUtil.getLore(this.getPreview())))
            ;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholdersView() {
        IProductPricer pricer = this.getPricer();
        ICurrency currency = this.getCurrency();
        double priceBuy = this.getPricer().getPriceBuy();
        double priceSell = this.getPricer().getPriceSell();

        return str -> str
            .replace(Placeholders.PRODUCT_PRICE_BUY, NumberUtil.format(priceBuy))
            .replace(Placeholders.PRODUCT_PRICE_BUY_FORMATTED, priceBuy >= 0 ? currency.format(priceBuy) : "-")
            .replace(Placeholders.PRODUCT_PRICE_SELL, NumberUtil.format(priceSell))
            .replace(Placeholders.PRODUCT_PRICE_SELL_FORMATTED, priceSell >= 0 ? currency.format(priceSell) : "-")
            ;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(@NotNull Player player) {
        double priceSell = this.getPricer().getPriceSell();
        double priceSellAll = this.getPricer().getPriceSellAll(player);

        ICurrency currency = this.getCurrency();
        int limBuy = this.getStockAmountLeft(player, TradeType.BUY);
        String limitBuy = limBuy >= 0 ? String.valueOf(limBuy) : getShop().plugin().getMessage(Lang.OTHER_INFINITY).getLocalized();

        int limSell = this.getStockAmountLeft(player, TradeType.SELL);
        String limitSell = limSell >= 0 ? String.valueOf(limSell) : getShop().plugin().getMessage(Lang.OTHER_INFINITY).getLocalized();

        return str -> this.replacePlaceholdersView().apply(str
            .replace(Placeholders.PRODUCT_PRICE_SELL_ALL, NumberUtil.format(priceSellAll))
            .replace(Placeholders.PRODUCT_PRICE_SELL_ALL_FORMATTED, priceSell >= 0 ? currency.format(priceSellAll) : "-")
            .replace(Placeholders.PRODUCT_LIMIT_BUY_AVAILABLE, limitBuy)
            .replace(Placeholders.PRODUCT_LIMIT_SELL_AVAILABLE, limitSell)
        );
    }

    @Override
    public void prepareTrade(@NotNull Player player, @NotNull ShopClickType click) {
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
                shop.plugin().getMessage(Lang.Shop_Product_Error_Unbuyable).send(player);
                return;
            }
            if (this.hasItem()) {
                if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY && PlayerUtil.countItemSpace(player, this.getItem()) == 0) {
                    this.getShop().plugin().getMessage(Lang.Shop_Product_Error_FullInventory).send(player);
                    return;
                }
            }
        }
        else if (tradeType == TradeType.SELL) {
            if (!this.isSellable()) {
                shop.plugin().getMessage(Lang.Shop_Product_Error_Unsellable).send(player);
                return;
            }
        }

        // Для Virtual Shop вернет остаточное кол-во предметов от лимита БД (если есть).
        // Для Chest Shop вернет количество свободного места или количество предмета в сундуке.
        int canPurchase = this.getStockAmountLeft(player, tradeType);
        if (canPurchase == 0) {
            LangMessage msgStock;
            if (tradeType == TradeType.BUY) {
                msgStock = shop.plugin().getMessage(Lang.Shop_Product_Error_OutOfStock);
            }
            else {
                if (shop instanceof IShopChest shopChest) {
                    msgStock = shop.plugin().getMessage(Lang.Shop_Product_Error_OutOfSpace);
                }
                else msgStock = shop.plugin().getMessage(Lang.Shop_Product_Error_FullStock);
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

    @Override
    public void openTrade(@NotNull Player player, @NotNull IProductPrepared prepared) {
        Config.getCartMenu(prepared.getTradeType()).open(player, prepared);
    }

    @Override
    public boolean isBuyable() {
        /*if (this.getLimitAmount(TradeType.BUY) == 0) {
            return false;
        }*/

        IProductPricer pricer = this.getPricer();
        if (pricer.getPriceBuy() < 0D) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isSellable() {
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
            if (priceSell > pricer.getPriceBuy()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEmpty() {
        return !this.hasItem();
    }

    @Override
    @NotNull
    public abstract IProductPrepared getPrepared(@NotNull TradeType buyType);

    @Override
    @NotNull
    public S getShop() {
        return this.shop;
    }

    @Override
    @NotNull
    public String getId() {
        return this.id;
    }

    @Override
    @NotNull
    public IProductPricer getPricer() {
        return this.pricer;
    }

    @Override
    @NotNull
    public ICurrency getCurrency() {
        return this.currency;
    }

    @Override
    public void setCurrency(@NotNull ICurrency currency) {
        this.currency = currency;
    }

    @Override
    public boolean isDiscountAllowed() {
        return this.isDiscountAllowed;
    }

    @Override
    public void setDiscountAllowed(boolean isAllowed) {
        this.isDiscountAllowed = isAllowed;
    }

    @Override
    public boolean isItemMetaEnabled() {
        return this.isItemMetaEnabled;
    }

    @Override
    public void setItemMetaEnabled(boolean isEnabled) {
        this.isItemMetaEnabled = isEnabled;
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return new ItemStack(this.shopPreview);
    }

    @Override
    public void setPreview(@NotNull ItemStack preview) {
        this.shopPreview = new ItemStack(preview);
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        return this.rewardItem;
    }

    @Override
    public void setItem(@Nullable ItemStack item) {
        this.rewardItem = item == null ? new ItemStack(Material.AIR) : new ItemStack(item);
        this.rewardItem.setAmount(1);
    }
}
