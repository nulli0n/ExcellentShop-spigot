package su.nightexpress.nexshop.data.key;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.shop.virtual.impl.Rotation;

import java.util.Objects;

public class RotationKey {

    private final String shopId;
    private final String rotationId;

    public RotationKey(@NotNull String shopId, @NotNull String rotationId) {
        this.shopId = shopId;
        this.rotationId = rotationId;
    }

    @NotNull
    public static RotationKey from(@NotNull Rotation rotation) {
        return new RotationKey(rotation.getShop().getId(), rotation.getId());
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
        if (!(object instanceof RotationKey that)) return false;
        return Objects.equals(shopId, that.shopId) && Objects.equals(rotationId, that.rotationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shopId, rotationId);
    }
}
