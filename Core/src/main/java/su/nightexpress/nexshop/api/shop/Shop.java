package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.stock.Stock;
import su.nightexpress.nexshop.api.shop.type.TradeType;

import java.util.Collection;
import java.util.Map;
import java.util.function.UnaryOperator;

public interface Shop {

    @NotNull UnaryOperator<String> replacePlaceholders();

    void save();

    void saveSettings();

    void saveProducts();

    void saveProduct(@NotNull Product product);

    boolean isLoaded();

    boolean canAccess(@NotNull Player player, boolean notify);

    default void open(@NotNull Player player) {
        this.open(player, 1);
    }

    default void open(@NotNull Player player, int page) {
        this.open(player, page, false);
    }

    void open(@NotNull Player player, int page, boolean force);

    @NotNull TransactionModule getModule();

    @NotNull ShopPricer getPricer();

    @NotNull String getId();

    @NotNull Stock getStock();

    @NotNull String getName();

    void setName(@NotNull String name);

    boolean isTransactionEnabled(@NotNull TradeType tradeType);

    void setTransactionEnabled(@NotNull TradeType tradeType, boolean enabled);

    void addProduct(@NotNull Product product);

    void removeProduct(@NotNull Product product);

    void removeProduct(@NotNull String id);

    @NotNull Map<String, ? extends Product> getProductMap();

    @NotNull Collection<? extends Product> getProducts();

    @NotNull Collection<? extends Product> getValidProducts();

    @Nullable Product getProductById(@NotNull String id);

    default int countProducts() {
        return this.getProducts().size();
    }

    default boolean isProduct(@NotNull Product product) {
        return this.getProductMap().containsKey(product.getId());
    }
}
