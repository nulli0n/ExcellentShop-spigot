package su.nightexpress.excellentshop.data.legacy;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.virtualshop.rotation.Rotation;

import java.util.Objects;

public class LegacyRotationKey {

    private final String shopId;
    private final String rotationId;

    public LegacyRotationKey(@NonNull String shopId, @NonNull String rotationId) {
        this.shopId = shopId;
        this.rotationId = rotationId;
    }

    @NonNull
    public static LegacyRotationKey from(@NonNull Rotation rotation) {
        return new LegacyRotationKey(rotation.getShop().getId(), rotation.getId());
    }

    public boolean isShop(@NonNull Shop shop) {
        return this.shopId.equalsIgnoreCase(shop.getId());
    }

    @NonNull
    public String getShopId() {
        return this.shopId;
    }

    @NonNull
    public String getRotationId() {
        return this.rotationId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof LegacyRotationKey that)) return false;
        return Objects.equals(shopId, that.shopId) && Objects.equals(rotationId, that.rotationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shopId, rotationId);
    }
}
