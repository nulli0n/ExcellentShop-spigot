package su.nightexpress.excellentshop.virtualshop.rotation.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import su.nightexpress.excellentshop.api.data.state.StatefulData;
import su.nightexpress.nightcore.util.TimeUtil;

public class RotationData extends StatefulData {

    private final UUID rotationId;

    private List<RotationItemData> products;
    private long                   nextRotationDate;

    public RotationData(@NonNull UUID rotationId, long nextRotationDate, @NonNull List<RotationItemData> products) {
        this.rotationId = rotationId;
        this.products = new ArrayList<>(products);
        this.nextRotationDate = nextRotationDate;
    }

    public boolean isRotationTime() {
        return TimeUtil.isPassed(this.nextRotationDate);
    }

    public boolean isExpirable() {
        return this.nextRotationDate >= 0L;
    }

    public boolean containsProduct(@NonNull String productId) {
        return this.products.stream().anyMatch(itemData -> itemData.productId().equalsIgnoreCase(productId));
    }

    @NonNull
    public UUID getRotationId() {
        return this.rotationId;
    }

    public long getNextRotationDate() {
        return this.nextRotationDate;
    }

    public void setNextRotationDate(long nextRotationDate) {
        this.nextRotationDate = nextRotationDate;
    }

    @NonNull
    public List<RotationItemData> getProducts() {
        return this.products;
    }

    public void setProducts(@NonNull List<RotationItemData> products) {
        this.products = new ArrayList<>(products);
    }
}
