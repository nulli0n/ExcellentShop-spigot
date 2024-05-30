package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;

public interface TransactionModule extends ShopModule {

    @NotNull String getDefaultCartUI();

    @NotNull TransactionLogger getLogger();
}
