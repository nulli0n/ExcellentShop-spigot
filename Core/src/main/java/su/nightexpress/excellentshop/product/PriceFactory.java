package su.nightexpress.excellentshop.product;

import org.jspecify.annotations.NonNull;

import su.nightexpress.excellentshop.api.product.PriceType;
import su.nightexpress.excellentshop.api.product.ProductPricing;
import su.nightexpress.excellentshop.product.price.DynamicPricing;
import su.nightexpress.excellentshop.product.price.FlatPricing;
import su.nightexpress.excellentshop.product.price.FloatPricing;
import su.nightexpress.excellentshop.product.price.PlayersPricing;
import su.nightexpress.nightcore.config.FileConfig;

public class PriceFactory {

    private PriceFactory() {
    }

    public static @NonNull ProductPricing read(@NonNull FileConfig config, @NonNull String path) {
        PriceType priceType = config.getEnum(path + ".Type", PriceType.class, PriceType.FLAT);

        return switch (priceType) {
            case FLAT -> FlatPricing.read(config, path);
            case FLOAT -> FloatPricing.read(config, path);
            case DYNAMIC -> DynamicPricing.read(config, path);
            case PLAYER_AMOUNT -> PlayersPricing.read(config, path);
        };
    }

    public static @NonNull ProductPricing from(@NonNull PriceType priceType) {
        return switch (priceType) {
            case FLAT -> new FlatPricing();
            case FLOAT -> new FloatPricing();
            case DYNAMIC -> new DynamicPricing();
            case PLAYER_AMOUNT -> new PlayersPricing();
        };
    }


}
