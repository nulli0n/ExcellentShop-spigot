package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.data.object.StockData;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.shop.virtual.editor.menu.ProductMainEditor;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualPreparedProduct;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractVirtualProduct<S extends AbstractVirtualShop<?>> extends AbstractProduct<S> implements VirtualProduct {

    protected StockValues stockValues;
    protected StockValues limitValues;
    protected Set<String> allowedRanks;
    protected boolean     discountAllowed;

    protected ProductMainEditor editor;

    public AbstractVirtualProduct(@NotNull String id, @NotNull S shop, @NotNull Currency currency,
                                  @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        super(shop.plugin(), id, shop, currency, handler, packer);
        this.allowedRanks = new HashSet<>();
        this.stockValues = new StockValues();
        this.limitValues = new StockValues();

        this.placeholderMap.add(this.getGenericPlaceholders());
        this.placeholderMap.add(this.getShop().getStock().getPlaceholders(this));
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders(@NotNull Player player) {
        return super.getPlaceholders(player)
            .add(this.getGenericPlaceholders())
            .add(this.getShop().getStock().getPlaceholders(player, this));
    }

    @NotNull
    private PlaceholderMap getGenericPlaceholders() {
        return new PlaceholderMap()
            .add(Placeholders.PRODUCT_DISCOUNT_AMOUNT, () -> NumberUtil.format(this.getShop().getDiscountPlain(this)))
            .add(Placeholders.PRODUCT_DISCOUNT_ALLOWED, () -> LangManager.getBoolean(this.isDiscountAllowed()))
            .add(Placeholders.PRODUCT_ALLOWED_RANKS, () -> String.join("\n", this.getAllowedRanks()));
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Handler", this.getHandler().getName());
        this.getPacker().write(cfg, path);
        cfg.set(path + ".Allowed_Ranks", this.getAllowedRanks());
        if (this.getCurrency() != CurrencyManager.DUMMY) {
            cfg.set(path + ".Currency", this.getCurrency().getId());
        }
        this.getPricer().write(cfg, path + ".Price");
        this.getStockValues().write(cfg, path + ".Stock.GLOBAL");
        this.getLimitValues().write(cfg, path + ".Stock.PLAYER");
        this.writeAdditional(cfg, path);
    }

    protected abstract void loadAdditional(@NotNull JYML cfg, @NotNull String path);

    protected abstract void writeAdditional(@NotNull JYML cfg, @NotNull String path);

    public void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
    }

    @NotNull
    public ProductMainEditor getEditor() {
        if (this.editor == null) {
            this.editor = new ProductMainEditor(this.getShop().plugin(), this);
        }
        return this.editor;
    }

    @Override
    @NotNull
    public VirtualPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all) {
        return new VirtualPreparedProduct(this.plugin, player, this, buyType, all);
    }

    @Override
    public boolean hasAccess(@NotNull Player player) {
        if (this.getAllowedRanks().isEmpty()) return true;

        Set<String> ranks = PlayerUtil.getPermissionGroups(player);
        return ranks.stream().anyMatch(rank -> this.getAllowedRanks().contains(rank));
    }

    @Override
    public int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType) {
        ShopUser user = this.plugin.getUserManager().getUserData(player);
        StockData data = user.getProductLimit(this, tradeType);

        int inStock = this.getShop().getStock().count(this, tradeType);
        int inLimit = data == null ? -1 : data.getItemsLeft();

        // If both, stock & limit, are present, return minimal of them.
        if (inStock >= 0 && inLimit >= 0) return Math.min(inStock, inLimit);

        // In other case return the one that is not unlimited.
        return inStock < 0 ? inLimit : inStock;
    }

    @NotNull
    public StockValues getStockValues() {
        return stockValues;
    }

    public void setStockValues(@NotNull StockValues stockValues) {
        this.stockValues = stockValues;
    }

    @NotNull
    public StockValues getLimitValues() {
        return limitValues;
    }

    public void setLimitValues(@NotNull StockValues limitValues) {
        this.limitValues = limitValues;
    }

    @NotNull
    public Set<String> getAllowedRanks() {
        return allowedRanks;
    }

    public void setAllowedRanks(@NotNull Set<String> allowedRanks) {
        this.allowedRanks = allowedRanks;
    }

    @Override
    public boolean isDiscountAllowed() {
        return discountAllowed;
    }

    @Override
    public void setDiscountAllowed(boolean discountAllowed) {
        this.discountAllowed = discountAllowed;
    }
}
