package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.type.ShopClickAction;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

public abstract class Product<
    P extends Product<P, S, T>,
    S extends Shop<S, P>,
    T extends ProductStock<P>> implements Placeholder {

    protected final String id;
    protected final PlaceholderMap placeholderMap;

    protected S             shop;
    protected Currency     currency;
    protected ProductPricer pricer;
    protected T             stock;
    protected boolean       isDiscountAllowed;

    public Product(@NotNull String id, @NotNull Currency currency) {
        this.id = id.toLowerCase();
        this.setCurrency(currency);

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.PRODUCT_DISCOUNT_AMOUNT, () -> NumberUtil.format(this.getShop().getDiscountPlain(this)))
            .add(Placeholders.PRODUCT_CURRENCY, () -> this.getCurrency().getName())
            .add(Placeholders.PRODUCT_PRICE_BUY, () -> NumberUtil.format(this.getPricer().getPriceBuy()))
            .add(Placeholders.PRODUCT_PRICE_BUY_FORMATTED, () -> this.getPricer().getPriceBuy() >= 0 ? getCurrency().format(this.getPricer().getPriceBuy()) : "-")
            .add(Placeholders.PRODUCT_PRICE_SELL, () -> NumberUtil.format(this.getPricer().getPriceSell()))
            .add(Placeholders.PRODUCT_PRICE_SELL_FORMATTED, () -> this.getPricer().getPriceSell() >= 0 ? getCurrency().format(this.getPricer().getPriceSell()) : "-")
            .add(Placeholders.PRODUCT_PRICE_TYPE, () -> getShop().plugin().getLangManager().getEnum(this.getPricer().getType()))
            .add(Placeholders.PRODUCT_DISCOUNT_ALLOWED, () -> LangManager.getBoolean(this.isDiscountAllowed()))
            .add(Placeholders.PRODUCT_PREVIEW_NAME, () -> ItemUtil.getItemName(this.getPreview()))
            .add(Placeholders.PRODUCT_PREVIEW_LORE, () -> String.join("\n", ItemUtil.getLore(this.getPreview())))
        ;
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        PlaceholderMap map = new PlaceholderMap(this.placeholderMap);
        map.getKeys().addAll(this.getPricer().getPlaceholders().getKeys());
        map.getKeys().addAll(this.getStock().getPlaceholders().getKeys());
        return map;
    }

    @NotNull
    public PlaceholderMap getPlaceholders(@NotNull Player player) {
        PlaceholderMap placeholderMap = new PlaceholderMap(this.getPlaceholders());
        placeholderMap.getKeys().addAll(this.getStock().getPlaceholders(player).getKeys());
        placeholderMap
            .add(Placeholders.PRODUCT_PRICE_SELL_ALL, () -> NumberUtil.format(this.getPricer().getPriceSell()))
            .add(Placeholders.PRODUCT_PRICE_SELL_ALL_FORMATTED, () -> {
                return this.getPricer().getPriceSell() >= 0 ? this.getCurrency().format(this.getPricer().getPriceSellAll(player)) : "-";
            })
        ;
        return placeholderMap;
    }

    @NotNull
    protected abstract P get();

    public abstract void clear();

    public void prepareTrade(@NotNull Player player, @NotNull ShopClickAction click) {
        Shop<?, ?> shop = this.getShop();
        TradeType tradeType = click.getTradeType();
        if (!shop.isTransactionEnabled(tradeType)) {
            return;
        }

        if (tradeType == TradeType.BUY) {
            if (!this.isBuyable()) {
                shop.plugin().getMessage(Lang.SHOP_PRODUCT_ERROR_UNBUYABLE).send(player);
                return;
            }
            if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY.get() && !this.hasSpace(player)) {
                this.getShop().plugin().getMessage(Lang.SHOP_PRODUCT_ERROR_FULL_INVENTORY).send(player);
                return;
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

        boolean isSellAll = (click == ShopClickAction.SELL_ALL);
        PreparedProduct<P> prepared = this.getPrepared(tradeType, isSellAll);
        if (click == ShopClickAction.BUY_SINGLE || click == ShopClickAction.SELL_SINGLE || prepared.isAll()) {
            prepared.trade(player);

            MenuViewer viewer = shop.getView().getViewer(player);
            if (viewer != null) {
                shop.open(player, viewer.getPage()); // Update current shop page
            }
            return;
        }
        this.openTrade(player, prepared);
    }

    public void openTrade(@NotNull Player player, @NotNull PreparedProduct<P> prepared) {
        this.getShop().plugin().getCartMenu().open(player, prepared);
    }

    public boolean isBuyable() {
        /*if (this.getStock().getLeftAmount(TradeType.BUY) == 0) {
            return false;
        }*/

        ProductPricer pricer = this.getPricer();
        return pricer.getPriceBuy() >= 0D;
    }

    public boolean isSellable() {
        /*if (this.getStock().getLeftAmount(TradeType.SELL) == 0) {
            return false;
        }*/

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

        return true;
    }

    @NotNull
    public abstract PreparedProduct<P> getPrepared(@NotNull TradeType buyType, boolean all);

    public abstract boolean hasSpace(@NotNull Player player);

    public abstract int getUnitAmount();

    @NotNull
    public abstract ItemStack getPreview();

    public abstract void delivery(@NotNull Player player, int count);

    public abstract void take(@NotNull Player player, int count);

    public abstract int count(@NotNull Player player);

    public int countUnits(@NotNull Player player) {
        return this.count(player) / this.getUnitAmount();
    }

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
    public Currency getCurrency() {
        return this.currency;
    }

    public void setCurrency(@NotNull Currency currency) {
        this.currency = currency;
    }

    public boolean isDiscountAllowed() {
        return this.isDiscountAllowed;
    }

    public void setDiscountAllowed(boolean isAllowed) {
        this.isDiscountAllowed = isAllowed;
    }
}
