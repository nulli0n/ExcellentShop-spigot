package su.nightexpress.nexshop.shop.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.stock.Stock;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public abstract class AbstractStock<S extends Shop, P extends Product> implements Stock {

    protected final ExcellentShop plugin;
    protected final S shop;

    public AbstractStock(@NotNull ExcellentShop plugin, @NotNull S shop) {
        this.plugin = plugin;
        this.shop = shop;
    }

    @NotNull
    @Override
    public S getShop() {
        return shop;
    }

    @Nullable
    protected abstract P findProduct(@NotNull Product product);

    @Override
    public int count(@NotNull Product product, @NotNull TradeType type) {
        P origin = this.findProduct(product);
        return origin == null ? 0 : this.countItem(origin, type);
    }

    public abstract int countItem(@NotNull P product, @NotNull TradeType type);

    @Override
    public boolean consume(@NotNull Product product, int amount, @NotNull TradeType type) {
        P origin = this.findProduct(product);
        return origin != null && this.consumeItem(origin, amount, type);
    }

    public abstract boolean consumeItem(@NotNull P product, int amount, @NotNull TradeType type);

    @Override
    public boolean store(@NotNull Product product, int amount, @NotNull TradeType type) {
        P origin = this.findProduct(product);
        return origin != null && this.storeItem(origin, amount, type);
    }

    public abstract boolean storeItem(@NotNull P product, int amount, @NotNull TradeType type);

    @Override
    public boolean restock(@NotNull Product product, @NotNull TradeType type, boolean force) {
        P origin = this.findProduct(product);
        return origin != null && this.restockItem(origin, type, force);
    }

    public abstract boolean restockItem(@NotNull P product, @NotNull TradeType type, boolean force);
}
