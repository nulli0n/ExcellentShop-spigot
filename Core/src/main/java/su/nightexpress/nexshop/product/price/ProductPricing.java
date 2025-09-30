package su.nightexpress.nexshop.product.price;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nexshop.product.price.impl.DynamicPricing;
import su.nightexpress.nexshop.product.price.impl.FlatPricing;
import su.nightexpress.nexshop.product.price.impl.FloatPricing;
import su.nightexpress.nexshop.product.price.impl.PlayersPricing;
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

    public abstract void onTransaction(@NotNull ShopTransactionEvent event, @NotNull Product product, @NotNull PriceData priceData);

    public abstract void updatePrice(@NotNull Product product, @NotNull PriceData priceData);

    public abstract double getAveragePrice(@NotNull TradeType type);

    @NotNull
    public PriceType getType() {
        return this.type;
    }
}
