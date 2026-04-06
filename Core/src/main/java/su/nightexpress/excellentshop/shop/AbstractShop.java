package su.nightexpress.excellentshop.shop;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.product.AbstractProduct;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractShop<P extends AbstractProduct<?>> implements Shop {

    protected final ShopPlugin     plugin;
    protected final Path           path;
    protected final String         id;
    protected final Map<String, P> products;

    protected boolean dirty;

    public AbstractShop(@NonNull ShopPlugin plugin, @NonNull Path path, @NonNull String id) {
        this.plugin = plugin;
        this.path = path;
        this.id = id;
        this.products = new LinkedHashMap<>();
    }

    @Override
    @NonNull
    public String getId() {
        return this.id;
    }

    @Override
    @NonNull
    public Path getPath() {
        return this.path;
    }

    @Override
    @NonNull
    public FileConfig loadConfig() {
        return FileConfig.load(this.path);
    }

    @Override
    public void printBadProducts() {
        this.getProducts().forEach(product -> {
            if (!product.getContent().isValid()) {
                this.plugin.error("Invalid item data of '" + product.getId() + "' product. Found in '" + this.path + "' shop.");
            }
        });
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public void saveForce() {
        this.markDirty();
        this.saveIfDirty();
    }

    @Override
    public void saveIfDirty() {
        if (!Files.exists(this.path)) return;
        
        if (this.dirty) {
            this.loadConfig().edit(this::write);
            this.dirty = false;
        }
    }

    @Override
    public void updatePrices(boolean force) {
        this.getProducts().forEach(product -> product.updatePrice(force));
    }

    @Override
    public void open(@NonNull Player player) {
        this.open(player, 1);
    }

    @Override
    public void open(@NonNull Player player, int page) {
        this.open(player, page, false);
    }

    @Override
    public boolean isTradeAllowed(@NonNull TradeType tradeType) {
        return tradeType == TradeType.BUY ? this.isBuyingAllowed() : this.isSellingAllowed();
    }

    @Override
    public int countProducts() {
        return this.products.size();
    }

    @Override
    public boolean hasProduct(@NonNull Product product) {
        return this.hasProduct(product.getId());
    }

    @Override
    public boolean hasProduct(@NonNull String id) {
        return this.products.containsKey(id);
    }

    public void addProduct(@NonNull P product) {
        this.removeProduct(product.getId());
        this.products.put(product.getId(), product);
    }

    @Override
    public void removeProduct(@NonNull Product product) {
        this.removeProduct(product.getId());
    }

    @Override
    public void removeProduct(@NonNull String id) {
        P product = this.getProductById(id);
        if (product != null) {
            product.invalidateData();
        }

        this.products.remove(id);
    }

    @Override
    @NonNull
    public Map<String, P> getProductMap() {
        return this.products;
    }

    @Override
    @NonNull
    public List<P> getProducts() {
        return List.copyOf(this.products.values());
    }

    @Override
    @NonNull
    public List<P> getValidProducts() {
        return this.products.values().stream().filter(Product::isValid).toList();
    }

    @Override
    @Nullable
    public P getProductById(@NonNull String id) {
        return this.products.get(id.toLowerCase());
    }
}
