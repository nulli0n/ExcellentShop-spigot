package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.shop.virtual.Placeholders;
import su.nightexpress.nexshop.shop.virtual.editor.menu.ProductMainEditor;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualPreparedProduct;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractVirtualProduct<S extends AbstractVirtualShop<?>> extends AbstractProduct<S> implements VirtualProduct {

    protected StockValues stockValues;
    protected StockValues limitValues;
    protected Set<String> allowedRanks;
    protected Set<String> requiredPermissions;
    protected boolean     discountAllowed;

    protected ProductMainEditor editor;

    public AbstractVirtualProduct(@NotNull String id, @NotNull S shop, @NotNull Currency currency,
                                  @NotNull ProductHandler handler, @NotNull ProductPacker packer) {
        super(shop.plugin(), id, shop, currency, handler, packer);
        this.allowedRanks = new HashSet<>();
        this.requiredPermissions = new HashSet<>();
        this.stockValues = new StockValues();
        this.limitValues = new StockValues();

        this.placeholderRelMap.add(Placeholders.forVirtualProduct(this));
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Handler", this.getHandler().getName());
        this.getPacker().write(cfg, path);
        cfg.set(path + ".Allowed_Ranks", this.getAllowedRanks());
        cfg.set(path + ".Required_Permissions", this.getRequiredPermissions());
        if (this.getCurrency() != CurrencyManager.DUMMY_CURRENCY) {
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
        if (!this.getRequiredPermissions().isEmpty()) {
            if (this.getRequiredPermissions().stream().noneMatch(player::hasPermission)) {
                return false;
            }
        }

        if (!this.getAllowedRanks().isEmpty()) {
            Set<String> ranks = PlayerUtil.getPermissionGroups(player);
            return ranks.stream().anyMatch(rank -> this.getAllowedRanks().contains(rank));
        }

        return true;
    }

    @Override
    public int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType) {
        int inStock = this.shop.getStock().count(this, tradeType);
        int inLimit = this.shop.getStock().count(this, tradeType, player);

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

    @NotNull
    public Set<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    public void setRequiredPermissions(@NotNull Set<String> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
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
