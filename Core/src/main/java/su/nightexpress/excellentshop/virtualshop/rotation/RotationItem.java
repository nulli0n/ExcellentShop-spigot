package su.nightexpress.excellentshop.virtualshop.rotation;

import org.jspecify.annotations.NonNull;

public class RotationItem {

    private String productId;
    private double weight;

    public RotationItem(@NonNull String productId, double weight) {
        this.setProductId(productId);
        this.setWeight(weight);
    }

    @NonNull
    public String getProductId() {
        return this.productId;
    }

    public void setProductId(@NonNull String productId) {
        this.productId = productId.toLowerCase();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
