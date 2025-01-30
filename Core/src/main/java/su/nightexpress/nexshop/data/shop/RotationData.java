package su.nightexpress.nexshop.data.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.data.Saveable;
import su.nightexpress.nightcore.util.TimeUtil;

import java.util.*;

public class RotationData implements Saveable {

    private final String shopId;
    private final String rotationId;

    private Map<Integer, List<String>> products;

    private long    nextRotationDate;
    private boolean saveRequired;

    public RotationData(@NotNull String shopId, @NotNull String rotationId) {
        this(shopId, rotationId, 0L, new HashMap<>());
    }

    public RotationData(@NotNull String shopId, @NotNull String rotationId, long nextRotationDate, @NotNull Map<Integer, List<String>> products) {
        this.shopId = shopId.toLowerCase();
        this.rotationId = rotationId.toLowerCase();
        this.setProducts(products);
        this.setNextRotationDate(nextRotationDate);
    }

    @Override
    public boolean isSaveRequired() {
        return this.saveRequired;
    }

    @Override
    public void setSaveRequired(boolean saveRequired) {
        this.saveRequired = saveRequired;
    }

    public void reset() {
        this.products.clear();
        this.setNextRotationDate(0L);
    }

    public boolean isRotationTime() {
        return TimeUtil.isPassed(this.nextRotationDate);
    }

    public boolean containsProduct(@NotNull String productId) {
        return this.products.values().stream().anyMatch(productIds -> productIds.contains(productId));
    }

    @NotNull
    public String getShopId() {
        return this.shopId;
    }

    @NotNull
    public String getRotationId() {
        return this.rotationId;
    }

    public long getNextRotationDate() {
        return this.nextRotationDate;
    }

    public void setNextRotationDate(long nextRotationDate) {
        this.nextRotationDate = nextRotationDate;
    }

    @NotNull
    public Map<Integer, List<String>> getProducts() {
        return this.products;
    }

    public void setProducts(@NotNull Map<Integer, List<String>> products) {
        this.products = new HashMap<>(products);
    }
}
