package su.nightexpress.nexshop.shop.virtual.impl;

import org.jetbrains.annotations.NotNull;

public class RotationItem {

    private String productId;
    private double weight;

    public RotationItem(@NotNull String productId, double weight) {
        this.setProductId(productId);
        this.setWeight(weight);
    }

    @NotNull
    public String getProductId() {
        return this.productId;
    }

    public void setProductId(@NotNull String productId) {
        this.productId = productId.toLowerCase();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
