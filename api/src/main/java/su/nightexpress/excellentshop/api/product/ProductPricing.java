package su.nightexpress.excellentshop.api.product;

import org.jspecify.annotations.NonNull;

import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

public abstract class ProductPricing implements Writeable {

    public static final double DISABLED = -1D;

    protected final PriceType type;

    protected ProductPricing(@NonNull PriceType type) {
        this.type = type;
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Type", this.type.name());
        this.writeAdditional(config, path);
    }

    protected abstract void writeAdditional(@NonNull FileConfig config, @NonNull String path);

    public abstract boolean shouldResetOnExpire();

    public abstract void handleTransaction(@NonNull ECompletedTransaction transaction, @NonNull Product product,
                                           int units, @NonNull PriceData priceData);

    public abstract void updatePrice(@NonNull Product product, @NonNull PriceData priceData);

    public abstract double getAveragePrice(@NonNull TradeType type);

    @NonNull
    public PriceType getType() {
        return this.type;
    }
}
