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

    protected static final int UNLIMITED = -1;

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

    @Deprecated
    public int countItem(@NotNull P product, @NotNull TradeType type, @Nullable Player player) {
        return this.count(product, type, player);
    }

    @Deprecated
    public boolean consumeItem(@NotNull P product, int amount, @NotNull TradeType type, @Nullable Player player) {
        return this.consume(product, amount, type, player);
    }

    @Deprecated
    public boolean storeItem(@NotNull P product, int amount, @NotNull TradeType type, @Nullable Player player) {
        return this.store(product, amount, type, player);
    }

    @Deprecated
    public boolean restockItem(@NotNull P product, @NotNull TradeType type, boolean force, @Nullable Player player) {
        return this.restock(product, type, force, player);
    }

    @Deprecated
    public long getRestockDate(@NotNull P product, @NotNull TradeType type, @Nullable Player player) {
        return this.getRestockTime(product, type, player);
    }
}
