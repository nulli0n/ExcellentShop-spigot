package su.nightexpress.excellentshop.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.shop.TransactionLogger;

public interface ShopModule extends Module {

    @NotNull TransactionLogger getLogger();
}
