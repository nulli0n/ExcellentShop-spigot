package su.nightexpress.excellentshop.api;

import org.jspecify.annotations.NonNull;

public interface ShopModule extends Module {

    @NonNull
    TransactionLogger getLogger();
}
