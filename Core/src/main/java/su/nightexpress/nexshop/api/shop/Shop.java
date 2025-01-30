package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;

import java.util.Collection;
import java.util.Map;
import java.util.function.UnaryOperator;

public interface Shop {

    @NotNull UnaryOperator<String> replacePlaceholders();

    void save();

    void saveSettings();

    void saveRotations();

    void saveProducts();

    @Deprecated
    void saveProduct(@NotNull Product product);

    boolean canAccess(@NotNull Player player, boolean notify);

    void onTransaction(@NotNull ShopTransactionEvent event);

    void update();

    void updatePrices();

    void updatePrices(boolean force);

    default void open(@NotNull Player player) {
        this.open(player, 1);
    }

    default void open(@NotNull Player player, int page) {
        this.open(player, page, false);
    }

    void open(@NotNull Player player, int page, boolean force);

    @NotNull ShopModule getModule();

    @NotNull String getId();

    @NotNull String getName();

    void setName(@NotNull String name);

    boolean isBuyingAllowed();

    void setBuyingAllowed(boolean buyingAllowed);

    boolean isSellingAllowed();

    void setSellingAllowed(boolean sellingAllowed);

    default boolean isTradeAllowed(@NotNull TradeType tradeType) {
        return tradeType == TradeType.BUY ? this.isBuyingAllowed() : this.isSellingAllowed();
    }

    @Deprecated
    default void setTransactionEnabled(@NotNull TradeType tradeType, boolean enabled) {
        if (tradeType == TradeType.BUY) this.setBuyingAllowed(enabled);
        else this.setSellingAllowed(enabled);
    }

    //void addProduct(@NotNull Product product);

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
