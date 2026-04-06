package su.nightexpress.excellentshop.product;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.product.price.DynamicPricing;
import su.nightexpress.excellentshop.product.price.FlatPricing;
import su.nightexpress.excellentshop.product.price.FloatPricing;
import su.nightexpress.excellentshop.product.price.PlayersPricing;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

public abstract class ProductPricing implements Writeable {

    public static final double DISABLED = -1D;

    protected final PriceType type;

    public ProductPricing(@NotNull PriceType type) {
        this.type = type;
    }

    @NotNull
    public static ProductPricing read(@NotNull FileConfig config, @NotNull String path) {
        PriceType priceType = config.getEnum(path + ".Type", PriceType.class, PriceType.FLAT);

        return switch (priceType) {
            case FLAT -> FlatPricing.read(config, path);
            case FLOAT -> FloatPricing.read(config, path);
            case DYNAMIC -> DynamicPricing.read(config, path);
            case PLAYER_AMOUNT -> PlayersPricing.read(config, path);
        };
    }

    @NotNull
    public static ProductPricing from(@NotNull PriceType priceType) {
        return switch (priceType) {
            case FLAT -> new FlatPricing();
            case FLOAT -> new FloatPricing();
            case DYNAMIC -> new DynamicPricing();
            case PLAYER_AMOUNT -> new PlayersPricing();
        };
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Type", this.type.name());
        this.writeAdditional(config, path);
    }

    protected abstract void writeAdditional(@NotNull FileConfig config, @NotNull String path);

    public abstract boolean shouldResetOnExpire();

    public abstract void handleTransaction(@NonNull ECompletedTransaction transaction, @NonNull Product product, int units, @NonNull PriceData priceData);

    public abstract void updatePrice(@NotNull Product product, @NotNull PriceData priceData);

    public abstract double getAveragePrice(@NotNull TradeType type);

    @NotNull
    public PriceType getType() {
        return this.type;
    }
}
