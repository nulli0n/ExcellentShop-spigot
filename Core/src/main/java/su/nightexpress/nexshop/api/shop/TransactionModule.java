package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;

public interface TransactionModule extends ShopModule {

    @NotNull TransactionLogger getLogger();
}
