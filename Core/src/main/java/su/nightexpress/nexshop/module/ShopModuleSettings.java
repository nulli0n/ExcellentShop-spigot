package su.nightexpress.nexshop.module;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeStatus;
import su.nightexpress.excellentshop.product.click.ProductClickSettings;

import java.util.List;

public interface ShopModuleSettings {

    @NonNull List<String> getProductDisplayMasterInfo(@NonNull TradeStatus status);

    @NonNull ProductClickSettings getProductClickSettings();

    boolean isBuyingMenuCloseAfterPurchase();
}
