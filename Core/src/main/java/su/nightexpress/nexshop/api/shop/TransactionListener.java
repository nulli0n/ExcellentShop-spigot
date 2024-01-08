package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;

public interface TransactionListener {

    void onTransaction(@NotNull ShopTransactionEvent event);
}
