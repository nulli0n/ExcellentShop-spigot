package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.module.ShopModule;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.Collection;
import java.util.Map;
import java.util.function.UnaryOperator;

public interface Shop {

    @NotNull UnaryOperator<String> replacePlaceholders();

    void save();

    void save(boolean force);

    void save(@NotNull FileConfig config, boolean force);

    void printBadProducts();

    boolean canAccess(@NotNull Player player, boolean notify);

    void onTransaction(@NotNull ShopTransactionEvent event);

    void update();

    void updatePrices(boolean force);

    boolean isSaveRequired();

    void setSaveRequired(boolean saveRequired);

    void open(@NotNull Player player);

    void open(@NotNull Player player, int page);

    void open(@NotNull Player player, int page, boolean force);

    @NotNull ShopModule getModule();

    @NotNull String getId();

    @NotNull String getName();

    void setName(@NotNull String name);

    boolean isBuyingAllowed();

    void setBuyingAllowed(boolean buyingAllowed);

    boolean isSellingAllowed();

    void setSellingAllowed(boolean sellingAllowed);

    boolean isTradeAllowed(@NotNull TradeType tradeType);

    boolean hasProduct(@NotNull String id);

    void removeProduct(@NotNull Product product);

    void removeProduct(@NotNull String id);

    @NotNull Map<String, ? extends Product> getProductMap();

    @NotNull Collection<? extends Product> getProducts();

    @NotNull Collection<? extends Product> getValidProducts();

    @Nullable Product getProductById(@NotNull String id);

    int countProducts();

    boolean hasProduct(@NotNull Product product);
}
