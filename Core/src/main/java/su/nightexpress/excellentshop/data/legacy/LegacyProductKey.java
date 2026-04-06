package su.nightexpress.excellentshop.data.legacy;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.shop.Shop;

import java.util.Objects;
import java.util.UUID;

public class LegacyProductKey {

    private final String shopId;
    private final String productId;
    private final String holderId;

    public LegacyProductKey(@NonNull String shopId, @NonNull String productId, @NonNull String holderId) {
        this.shopId = shopId.toLowerCase();
        this.productId = productId.toLowerCase();
        this.holderId = holderId.toLowerCase();
    }

    @NonNull
    public static LegacyProductKey global(@NonNull Product product) {
        String shopId = product.getShop().getId();
        String productId = product.getId();

        return new LegacyProductKey(shopId, productId, shopId);
    }

    @NonNull
    public static LegacyProductKey personal(@NonNull Product product, @NonNull UUID playerId) {
        String shopId = product.getShop().getId();
        String productId = product.getId();

        return new LegacyProductKey(shopId, productId, playerId.toString());
    }

    @NonNull
    public static LegacyProductKey globalOrPerosnal(@NonNull Product product, @Nullable UUID playerId) {
        return playerId == null ? global(product) : personal(product, playerId);
    }

    public boolean isProduct(@NonNull Product product) {
        return this.isShop(product.getShop()) && this.productId.equalsIgnoreCase(product.getId());
    }

    public boolean isShop(@NonNull Shop shop) {
        return this.isShop(shop.getId());
    }

    public boolean isShop(@NonNull String shopId) {
        return this.shopId.equalsIgnoreCase(shopId);
    }

    public boolean isHolder(@NonNull UUID playerId) {
        return this.holderId.equalsIgnoreCase(playerId.toString());
    }

    @NonNull
    public String getShopId() {
        return this.shopId;
    }

    @NonNull
    public String getProductId() {
        return this.productId;
    }

    @NonNull
    public String getHolderId() {
        return this.holderId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof LegacyProductKey key)) return false;
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
