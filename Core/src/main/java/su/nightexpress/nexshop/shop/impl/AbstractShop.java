package su.nightexpress.nexshop.shop.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nightcore.manager.AbstractFileData;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractShop<P extends AbstractProduct<?>> extends AbstractFileData<ShopPlugin> implements Shop {

    protected final Map<String, P> products;

    protected String  name;
    protected boolean buyingAllowed;
    protected boolean sellingAllowed;

    public AbstractShop(@NotNull ShopPlugin plugin, @NotNull File file, @NotNull String id) {
        super(plugin, file, id);
        this.products = new LinkedHashMap<>();
    }

    @Override
    public void onTransaction(@NotNull ShopTransactionEvent event) {
        Transaction result = event.getTransaction();
        Product product = result.getProduct();
        if (product.getPricer().getType() == PriceType.FLAT) return;
        if (product.getPricer().getType() == PriceType.PLAYER_AMOUNT) return;

        PriceData priceData = this.plugin.getDataManager().getPriceDataOrCreate(product);
        priceData.countTransaction(result.getTradeType(), result.getUnits());
        priceData.setSaveRequired(true);

        if (product.getPricer().getType() == PriceType.DYNAMIC) {
            priceData.setExpired(); // To trigger isExpired in update.
        }

        product.updatePrice();
    }

    @Override
    public void update() {
        this.updatePrices();
    }

    @Override
    public void updatePrices() {
        this.updatePrices(false);
    }

    @Override
    public void updatePrices(boolean force) {
        this.getValidProducts().forEach(product -> product.updatePrice(force));
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
    public boolean isBuyingAllowed() {
        return this.buyingAllowed;
    }

    @Override
    public void setBuyingAllowed(boolean buyingAllowed) {
        this.buyingAllowed = buyingAllowed;
    }

    @Override
    public boolean isSellingAllowed() {
        return this.sellingAllowed;
    }

    @Override
    public void setSellingAllowed(boolean sellingAllowed) {
        this.sellingAllowed = sellingAllowed;
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
        // TODO Delete product datas?
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
