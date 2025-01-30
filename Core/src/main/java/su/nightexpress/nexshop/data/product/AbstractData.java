package su.nightexpress.nexshop.data.product;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.data.Saveable;

public abstract class AbstractData implements Saveable {

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

    @Override
    public boolean isSaveRequired() {
        return this.saveRequired;
    }

    @Override
    public void setSaveRequired(boolean saveRequired) {
        this.saveRequired = saveRequired;
    }
}
