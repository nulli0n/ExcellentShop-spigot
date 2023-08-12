package su.nightexpress.nexshop.data.rotation;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ShopRotationData {

    private final String shopId;

    private  Set<String> products;
    private       long        latestRotation;

    public ShopRotationData(@NotNull String shopId) {
        this(shopId, 0L, new HashSet<>());
    }

    public ShopRotationData(@NotNull String shopId, long latestRotation, @NotNull Set<String> products) {
        this.shopId = shopId.toLowerCase();
        this.setProducts(products);
        this.setLatestRotation(latestRotation);
    }

    @NotNull
    public String getShopId() {
        return shopId;
    }

    public long getLatestRotation() {
        return latestRotation;
    }

    public void setLatestRotation(long latestRotation) {
        this.latestRotation = latestRotation;
    }

    @NotNull
    public Set<String> getProducts() {
        return products;
    }

    public void setProducts(@NotNull Set<String> products) {
        this.products = new HashSet<>(products);
    }
}
