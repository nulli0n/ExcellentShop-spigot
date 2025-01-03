package su.nightexpress.nexshop.shop.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.manager.AbstractFileData;

import java.io.File;
import java.util.*;

public abstract class AbstractShop<P extends AbstractProduct<?>> extends AbstractFileData<ShopPlugin> implements Shop {

    protected final ShopDataPricer          pricer;
    protected final Map<TradeType, Boolean> transactions;
    protected final Map<String, P>          products;

    protected String name;

    public AbstractShop(@NotNull ShopPlugin plugin, @NotNull File file, @NotNull String id) {
        super(plugin, file, id);
        this.pricer = new ShopDataPricer(plugin, this);
        this.transactions = new HashMap<>();
        this.products = new LinkedHashMap<>();
    }

    @Override
    public boolean isLoaded() {
        return this.plugin.getShopManager().getProductDataManager().isLoaded();
    }

    @NotNull
    @Override
    public ShopDataPricer getPricer() {
        return pricer;
    }

    @Override
    @NotNull
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Override
    public boolean isTransactionEnabled(@NotNull TradeType tradeType) {
        return this.transactions.getOrDefault(tradeType, true);
    }

    @Override
    public void setTransactionEnabled(@NotNull TradeType tradeType, boolean enabled) {
        this.transactions.put(tradeType, enabled);
    }

    protected void addProduct(@NotNull P product) {
        this.removeProduct(product.getId());
        this.products.put(product.getId(), product);
    }

    @Override
    public final void removeProduct(@NotNull Product product) {
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
    public Collection<P> getProducts() {
        return this.products.values();
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
