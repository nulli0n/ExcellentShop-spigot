package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nexshop.product.price.ProductPricing;
import su.nightexpress.nightcore.manager.ConfigBacked;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class AbstractShop<P extends AbstractProduct<?>> implements Shop, ConfigBacked {

    protected final ShopPlugin     plugin;
    protected final Path           path;
    protected final String         id;
    protected final Map<String, P> products;

    protected boolean dirty;

    public AbstractShop(@NotNull ShopPlugin plugin, @NotNull Path path, @NotNull String id) {
        this.plugin = plugin;
        this.path = path;
        this.id = id;
        this.products = new LinkedHashMap<>();
    }

    @Override
    @NotNull
    public String getId() {
        return this.id;
    }

    @Override
    @NotNull
    public Path getPath() {
        return this.path;
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event) {
        Transaction result = event.getTransaction();
        Product product = result.getProduct();
        ProductPricing pricing = product.getPricing();
        if (pricing.getType() == PriceType.FLAT || pricing.getType() == PriceType.PLAYER_AMOUNT) return;

        this.plugin.dataAccess(dataManager -> {
            PriceData priceData = dataManager.getPriceDataOrCreate(product);
            pricing.onTransaction(event, product, priceData);
            priceData.countTransaction(result.getTradeType(), result.getUnits());
            priceData.setSaveRequired(true);
        });
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
    public void open(@NotNull Player player) {
        this.open(player, 1);
    }

    @Override
    public void open(@NotNull Player player, int page) {
        this.open(player, page, false);
    }

    @Override
    public boolean isTradeAllowed(@NotNull TradeType tradeType) {
        return tradeType == TradeType.BUY ? this.isBuyingAllowed() : this.isSellingAllowed();
    }

    @Override
    public int countProducts() {
        return this.products.size();
    }

    @Override
    public boolean hasProduct(@NotNull Product product) {
        return this.hasProduct(product.getId());
    }

    @Override
    public boolean hasProduct(@NotNull String id) {
        return this.products.containsKey(id);
    }

    public void addProduct(@NotNull P product) {
        this.removeProduct(product.getId());
        this.products.put(product.getId(), product);
    }

    @Override
    public void removeProduct(@NotNull Product product) {
        this.removeProduct(product.getId());
    }

    @Override
    public void removeProduct(@NotNull String id) {
        this.products.remove(id);
    }

    @Override
    @NotNull
    public Map<String, P> getProductMap() {
        return this.products;
    }

    @Override
    @NotNull
    public Set<P> getProducts() {
        return new LinkedHashSet<>(this.products.values());
    }

    @Override
    @NotNull
    public List<P> getValidProducts() {
        return this.getProducts().stream().filter(Product::isValid).toList();
    }

    @Override
    @Nullable
    public P getProductById(@NotNull String id) {
        return this.products.get(id.toLowerCase());
    }
}
