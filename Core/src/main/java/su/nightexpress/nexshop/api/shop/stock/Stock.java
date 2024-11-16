package su.nightexpress.nexshop.api.shop.stock;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.TransactionListener;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public interface Stock extends TransactionListener {

    @NotNull Shop getShop();

    void resetGlobalValues(@NotNull Product product);

    void resetPlayerLimits(@NotNull Product product);

    default int count(@NotNull Product product, @NotNull TradeType type) {
        return this.count(product, type, null);
    }

    default boolean consume(@NotNull Product product, int amount, @NotNull TradeType type) {
        return this.consume(product, amount, type, null);
    }

    default boolean store(@NotNull Product product, int amount, @NotNull TradeType type) {
        return this.store(product, amount, type, null);
    }

    default boolean restock(@NotNull Product product, @NotNull TradeType type, boolean force) {
        return this.restock(product, type, force, null);
    }

    int count(@NotNull Product product, @NotNull TradeType type, @Nullable Player player);

    boolean consume(@NotNull Product product, int amount, @NotNull TradeType type, @Nullable Player player);

    boolean store(@NotNull Product product, int amount, @NotNull TradeType type, @Nullable Player player);

    boolean restock(@NotNull Product product, @NotNull TradeType type, boolean force, @Nullable Player player);

    long getRestockTime(@NotNull Product product, @NotNull TradeType type, @Nullable Player player);
}
