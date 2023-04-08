package su.nightexpress.nexshop.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.event.ShopTransactionEvent;

public interface IPurchaseListener {

    void onPurchase(@NotNull ShopTransactionEvent<?> event);
}
