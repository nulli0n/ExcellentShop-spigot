package su.nightexpress.nexshop.product.data;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractData {

    protected final String shopId;
    protected final String productId;

    private boolean saveRequired;

    public AbstractData(@NotNull String shopId, @NotNull String productId) {
        this.shopId = shopId.toLowerCase();
        this.productId = productId.toLowerCase();
    }

    @NotNull
    public String getShopId() {
        return this.shopId;
    }

    @NotNull
    public String getProductId() {
        return this.productId;
    }

    public boolean isSaveRequired() {
        return saveRequired;
    }

    public void setSaveRequired(boolean saveRequired) {
        this.saveRequired = saveRequired;
    }
}
