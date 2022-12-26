package su.nightexpress.nexshop.api.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.type.ShopClickType;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.function.UnaryOperator;

public abstract class Product<
    P extends Product<P, S, T>,
    S extends Shop<S, P>,
    T extends ProductStock<P>> implements ICleanable, IEditable, IPlaceholder, JOption.Writer {

    protected final String id;

    protected S             shop;
    protected ItemStack     itemPreivew;
    protected ItemStack     itemReal;
    protected ICurrency     currency;
    protected ProductPricer pricer;
    protected T             stock;
    protected boolean       isDiscountAllowed;
    protected boolean       isItemMetaEnabled;

    public Product(@NotNull String id, @NotNull ItemStack itemPreview, @NotNull ICurrency currency) {
        this.id = id.toLowerCase();
        this.setPreview(itemPreview);
        this.setCurrency(currency);
    }

    @NotNull
    protected abstract P get();

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        ItemStack buyItem = this.getItem();
        String itemName = !buyItem.getType().isAir() ? ItemUtil.getItemName(buyItem) : "null";

        return str -> {
            str = this.replacePlaceholdersView().apply(str);
            str = this.getPricer().replacePlaceholders().apply(str);
            return str
                .replace(Placeholders.PRODUCT_PRICE_TYPE, this.getPricer().getType().name())
                .replace(Placeholders.PRODUCT_DISCOUNT_ALLOWED, LangManager.getBoolean(this.isDiscountAllowed()))
                .replace(Placeholders.PRODUCT_ITEM_META_ENABLED, LangManager.getBoolean(this.isItemMetaEnabled()))
                .replace(Placeholders.PRODUCT_ITEM_NAME, itemName)
                .replace(Placeholders.PRODUCT_ITEM_LORE, String.join("\n", ItemUtil.getLore(buyItem)))
                .replace(Placeholders.PRODUCT_PREVIEW_NAME, ItemUtil.getItemName(this.getPreview()))
                .replace(Placeholders.PRODUCT_PREVIEW_LORE, String.join("\n", ItemUtil.getLore(this.getPreview())))
                ;
        };
    }

    @NotNull
    private UnaryOperator<String> replacePlaceholdersView() {
        ICurrency currency = this.getCurrency();
        double priceBuy = this.getPricer().getPriceBuy();
        double priceSell = this.getPricer().getPriceSell();

        return str -> this.getStock().replacePlaceholders().apply(str)
            .replace(Placeholders.PRODUCT_DISCOUNT_AMOUNT, NumberUtil.format(this.getShop().getDiscountPlain(this)))
            .replace(Placeholders.PRODUCT_CURRENCY, this.getCurrency().getConfig().getName())
            .replace(Placeholders.PRODUCT_PRICE_BUY, NumberUtil.format(priceBuy))
            .replace(Placeholders.PRODUCT_PRICE_BUY_FORMATTED, priceBuy >= 0 ? currency.format(priceBuy) : "-")
            .replace(Placeholders.PRODUCT_PRICE_SELL, NumberUtil.format(priceSell))
            .replace(Placeholders.PRODUCT_PRICE_SELL_FORMATTED, priceSell >= 0 ? currency.format(priceSell) : "-")
            ;
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders(@NotNull Player player) {
        double priceSell = this.getPricer().getPriceSell();
        double priceSellAll = this.getPricer().getPriceSellAll(player);

        ICurrency currency = this.getCurrency();
        return str -> {
            str = this.replacePlaceholdersView().apply(str);
            str = this.getStock().replacePlaceholders(player).apply(str);
            return str
                .replace(Placeholders.PRODUCT_PRICE_SELL_ALL, NumberUtil.format(priceSellAll))
                .replace(Placeholders.PRODUCT_PRICE_SELL_ALL_FORMATTED, priceSell >= 0 ? currency.format(priceSellAll) : "-")
                ;
        };
    }

    public void prepareTrade(@NotNull Player player, @NotNull ShopClickType click) {
        Shop<?, ?> shop = this.getShop();
        TradeType tradeType = click.getBuyType();
        if (!shop.isTransactionEnabled(tradeType)) {
            return;
        }

        if (tradeType == TradeType.BUY) {
            if (!this.isBuyable()) {
                shop.plugin().getMessage(Lang.SHOP_PRODUCT_ERROR_UNBUYABLE).send(player);
                return;
            }
            if (this.hasItem()) {
                if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY && PlayerUtil.countItemSpace(player, this.getItem()) == 0) {
                    this.getShop().plugin().getMessage(Lang.SHOP_PRODUCT_ERROR_FULL_INVENTORY).send(player);
                    return;
                }
            }
        }
        else if (tradeType == TradeType.SELL) {
            if (!this.isSellable()) {
                shop.plugin().getMessage(Lang.SHOP_PRODUCT_ERROR_UNSELLABLE).send(player);
                return;
            }
        }

        // Для Virtual Shop вернет остаточное кол-во предметов от лимита БД (если есть).
        // Для Chest Shop вернет количество свободного места или количество предмета в сундуке.
        int canPurchase = this.getStock().getPossibleAmount(tradeType, player);
        if (canPurchase == 0) {
            LangMessage msgStock;
            if (tradeType == TradeType.BUY) {
                msgStock = shop.plugin().getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_STOCK);
            }
            else {
                if (shop instanceof ChestShop shopChest) {
                    msgStock = shop.plugin().getMessage(Lang.SHOP_PRODUCT_ERROR_OUT_OF_SPACE);
                }
                else msgStock = shop.plugin().getMessage(Lang.SHOP_PRODUCT_ERROR_FULL_STOCK);
            }
            msgStock.send(player);
            return;
        }

        boolean isSellAll = (click == ShopClickType.SELL_ALL);
        PreparedProduct<P> prepared = this.getPrepared(tradeType);
        if (click == ShopClickType.BUY_SINGLE || click == ShopClickType.SELL_SINGLE || isSellAll) {
            prepared.trade(player, isSellAll);
            shop.open(player, shop.getView().getPage(player)); // Update current shop page
            return;
        }
        this.openTrade(player, prepared);
    }

    public void openTrade(@NotNull Player player, @NotNull PreparedProduct<P> prepared) {
        Config.getCartMenu(prepared.getTradeType()).open(player, prepared);
    }

    public boolean isBuyable() {
        if (this.getStock().getInitialAmount(StockType.GLOBAL, TradeType.BUY) == 0) {
            return false;
        }

        ProductPricer pricer = this.getPricer();
        if (pricer.getPriceBuy() < 0D) {
            return false;
        }

        return !this.isEmpty();
    }

    public boolean isSellable() {
        if (!this.hasItem()) {
            return false;
        }
        if (this.getStock().getInitialAmount(StockType.GLOBAL, TradeType.SELL) == 0) {
            return false;
        }

        ProductPricer pricer = this.getPricer();
        double priceSell = pricer.getPriceSell();
        if (priceSell < 0D) {
            return false;
        }

        // Check if this product is buyable, so we can check if sell price is over the buy price
        // to prevent money duplication.
        // If this product can not be purchased, these checks are useless.
        if (this.getShop().isTransactionEnabled(TradeType.BUY) && this.isBuyable()) {
            if (priceSell > pricer.getPriceBuy()) {
                return false;
            }
        }

        return !this.isEmpty();
    }

    public boolean isEmpty() {
        return !this.hasItem();
    }

    @NotNull
    public abstract PreparedProduct<P> getPrepared(@NotNull TradeType buyType);

    @NotNull
    public S getShop() {
        if (this.shop == null) {
            throw new IllegalStateException("Product shop is undefined!");
        }
        return this.shop;
    }

    public void setShop(@NotNull S shop) {
        if (this.shop != null && !this.shop.getId().equalsIgnoreCase(shop.getId())) {
            this.shop.getProductMap().remove(this.getId());
        }
        this.shop = shop;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public ProductPricer getPricer() {
        if (this.pricer == null) {
            throw new IllegalStateException("Product pricer is undefined!");
        }
        return this.pricer;
    }

    public void setPricer(@NotNull ProductPricer pricer) {
        this.pricer = pricer;
        this.pricer.setProduct(this);
    }

    @NotNull
    public T getStock() {
        if (this.stock == null) {
            throw new IllegalStateException("Product stock is undefined!");
        }
        return stock;
    }

    public void setStock(@NotNull T stock) {
        this.stock = stock;
        this.stock.setProduct(this.get());
    }

    @NotNull
    public ICurrency getCurrency() {
        return this.currency;
    }

    public void setCurrency(@NotNull ICurrency currency) {
        this.currency = currency;
    }

    public boolean isDiscountAllowed() {
        return this.isDiscountAllowed;
    }

    public void setDiscountAllowed(boolean isAllowed) {
        this.isDiscountAllowed = isAllowed;
    }

    public boolean isItemMetaEnabled() {
        return this.isItemMetaEnabled;
    }

    public void setItemMetaEnabled(boolean isEnabled) {
        this.isItemMetaEnabled = isEnabled;
    }

    @NotNull
    public ItemStack getPreview() {
        return new ItemStack(this.itemPreivew);
    }

    public void setPreview(@NotNull ItemStack preview) {
        this.itemPreivew = new ItemStack(preview);
    }

    @NotNull
    public ItemStack getItem() {
        return new ItemStack(this.itemReal);
    }

    public void setItem(@Nullable ItemStack item) {
        this.itemReal = item == null ? new ItemStack(Material.AIR) : new ItemStack(item);
        this.itemReal.setAmount(1);
    }

    public boolean hasItem() {
        return !this.getItem().getType().isAir();
    }

    public boolean isItemMatches(@NotNull ItemStack item) {
        return this.isItemMetaEnabled() ? this.getItem().isSimilar(item) : this.getItem().getType() == item.getType();
    }

    public int countItem(@NotNull Player player) {
        if (!this.hasItem()) return 0;

        return PlayerUtil.countItem(player, this::isItemMatches);
    }

    public boolean takeItem(@NotNull Player player, int amount) {
        if (!this.hasItem()) return false;

        return PlayerUtil.takeItem(player, this::isItemMatches, amount);
    }
}
