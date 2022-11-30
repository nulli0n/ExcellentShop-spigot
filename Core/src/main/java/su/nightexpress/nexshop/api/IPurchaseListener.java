package su.nightexpress.nexshop.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.event.ShopPurchaseEvent;

public interface IPurchaseListener {

    void onPurchase(@NotNull ShopPurchaseEvent<?> event);
}
