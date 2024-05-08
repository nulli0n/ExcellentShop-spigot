package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.stock.Stock;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public abstract class AbstractStock<S extends Shop, P extends Product> implements Stock {

    public static final int UNLIMITED = -1;

    protected final ShopPlugin plugin;
    protected final S          shop;

    public AbstractStock(@NotNull ShopPlugin plugin, @NotNull S shop) {
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
    public int count(@NotNull Product product, @NotNull TradeType type, @Nullable Player player) {
        P origin = this.findProduct(product);
        return origin == null ? 0 : this.countItem(origin, type, player);
    }

    public abstract int countItem(@NotNull P product, @NotNull TradeType type, @Nullable Player player);

    @Override
    public boolean consume(@NotNull Product product, int amount, @NotNull TradeType type, @Nullable Player player) {
        P origin = this.findProduct(product);
        return origin != null && this.consumeItem(origin, amount, type, player);
    }

    public abstract boolean consumeItem(@NotNull P product, int amount, @NotNull TradeType type, @Nullable Player player);

    @Override
    public boolean store(@NotNull Product product, int amount, @NotNull TradeType type, @Nullable Player player) {
        P origin = this.findProduct(product);
        return origin != null && this.storeItem(origin, amount, type, player);
    }

    public abstract boolean storeItem(@NotNull P product, int amount, @NotNull TradeType type, @Nullable Player player);

    @Override
    public boolean restock(@NotNull Product product, @NotNull TradeType type, boolean force, @Nullable Player player) {
        P origin = this.findProduct(product);
        return origin != null && this.restockItem(origin, type, force, player);
    }

    public abstract boolean restockItem(@NotNull P product, @NotNull TradeType type, boolean force, @Nullable Player player);
}
