package su.nightexpress.excellentshop.data.legacy;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.data.RotationItemData;

import java.util.List;

public class LegacyRotationData {

    private final String                 shopId;
    private final String                 rotationId;
    private final List<RotationItemData> products;
    private final long                   nextRotationDate;

    private boolean removed;

    public LegacyRotationData(@NonNull String shopId, @NonNull String rotationId, long nextRotationDate, @NonNull List<RotationItemData> products) {
        this.shopId = shopId.toLowerCase();
        this.rotationId = rotationId.toLowerCase();
        this.products = products;
        this.nextRotationDate = nextRotationDate;
    }

    @NonNull
    public String getShopId() {
        return this.shopId;
    }

    @NonNull
    public String getRotationId() {
        return this.rotationId;
    }

    public long getNextRotationDate() {
        return this.nextRotationDate;
    }

    @NonNull
    public List<RotationItemData> getProducts() {
        return this.products;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
