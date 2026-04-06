package su.nightexpress.excellentshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.ShopModule;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.placeholder.PlaceholderResolvable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Shop extends PlaceholderResolvable {

    @NonNull Path getPath();

    @NonNull FileConfig loadConfig();

    void saveForce();

    void saveIfDirty();

    void write(@NonNull FileConfig config);

    boolean isDirty();

    void markDirty();

    void printBadProducts();

    boolean isAdminShop();

    boolean canAccess(@NonNull Player player, boolean notify);

    void updatePrices(boolean force);

    void open(@NonNull Player player);

    void open(@NonNull Player player, int page);

    void open(@NonNull Player player, int page, boolean force);

    @NonNull CompletableFuture<Double> queryBalance(@NonNull Currency currency);

    @NonNull CompletableFuture<Boolean> depositBalance(@NonNull Currency currency, double amount);

    @NonNull CompletableFuture<Boolean> withdrawBalance(@NonNull Currency currency, double amount);

    @NonNull ShopModule getModule();

    @NonNull String getId();

    @NonNull String getName();

    void setName(@NonNull String name);

    boolean isBuyingAllowed();

    void setBuyingAllowed(boolean buyingAllowed);

    boolean isSellingAllowed();

    void setSellingAllowed(boolean sellingAllowed);

    boolean isTradeAllowed(@NonNull TradeType tradeType);

    boolean hasProduct(@NonNull String id);

    void removeProduct(@NonNull Product product);

    void removeProduct(@NonNull String id);

    @NonNull Map<String, ? extends Product> getProductMap();

    @NonNull Collection<? extends Product> getProducts();

    @NonNull Collection<? extends Product> getValidProducts();

    @Nullable Product getProductById(@NonNull String id);

    @Nullable Product getBestProduct(@NonNull ItemStack item, @NonNull TradeType tradeType);

    @Nullable Product getBestProduct(@NonNull ItemStack itemStack, @NonNull TradeType tradeType, @Nullable Player player);

    int countProducts();

    boolean hasProduct(@NonNull Product product);
}
