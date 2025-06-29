package su.nightexpress.nexshop.data.key;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.Product;

import java.util.Objects;
import java.util.UUID;

public class ProductKey {

    private final String shopId;
    private final String productId;
    private final String holderId;

    public ProductKey(@NotNull String shopId, @NotNull String productId, @NotNull String holderId) {
        this.shopId = shopId.toLowerCase();
        this.productId = productId.toLowerCase();
        this.holderId = holderId.toLowerCase();
    }

    @NotNull
    public static ProductKey global(@NotNull Product product) {
        String shopId = product.getShop().getId();
        String productId = product.getId();

        return new ProductKey(shopId, productId, shopId);
    }

    @NotNull
    public static ProductKey personal(@NotNull Product product, @NotNull UUID playerId) {
        String shopId = product.getShop().getId();
        String productId = product.getId();

        return new ProductKey(shopId, productId, playerId.toString());
    }

    @NotNull
    public static ProductKey globalOrPerosnal(@NotNull Product product, @Nullable UUID playerId) {
        return playerId == null ? global(product) : personal(product, playerId);
    }

    public boolean isProduct(@NotNull Product product) {
        return this.isShop(product.getShop()) && this.productId.equalsIgnoreCase(product.getId());
    }

    public boolean isShop(@NotNull Shop shop) {
        return this.isShop(shop.getId());
    }

    public boolean isShop(@NotNull String shopId) {
        return this.shopId.equalsIgnoreCase(shopId);
    }

    public boolean isHolder(@NotNull UUID playerId) {
        return this.holderId.equalsIgnoreCase(playerId.toString());
    }

    @NotNull
    public String getShopId() {
        return this.shopId;
    }

    @NotNull
    public String getProductId() {
        return this.productId;
    }

    @NotNull
    public String getHolderId() {
        return this.holderId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ProductKey key)) return false;
        return Objects.equals(shopId, key.shopId) && Objects.equals(productId, key.productId) && Objects.equals(holderId, key.holderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shopId, productId, holderId);
    }

    @Override
    public String toString() {
        return "ProductKey{" +
            "shopId='" + shopId + '\'' +
            ", productId='" + productId + '\'' +
            ", holderId='" + holderId + '\'' +
            '}';
    }
}
