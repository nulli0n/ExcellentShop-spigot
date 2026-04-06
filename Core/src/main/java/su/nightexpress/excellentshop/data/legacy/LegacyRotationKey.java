package su.nightexpress.excellentshop.data.legacy;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.Rotation;

import java.util.Objects;

public class LegacyRotationKey {

    private final String shopId;
    private final String rotationId;

    public LegacyRotationKey(@NotNull String shopId, @NotNull String rotationId) {
        this.shopId = shopId;
        this.rotationId = rotationId;
    }

    @NotNull
    public static LegacyRotationKey from(@NotNull Rotation rotation) {
        return new LegacyRotationKey(rotation.getShop().getId(), rotation.getId());
    }

    public boolean isShop(@NotNull Shop shop) {
        return this.shopId.equalsIgnoreCase(shop.getId());
    }

    @NotNull
    public String getShopId() {
        return this.shopId;
    }

    @NotNull
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
