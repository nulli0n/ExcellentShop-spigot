package su.nightexpress.nexshop.api.shop;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public interface TransactionModule extends ShopModule {

    @NotNull String getDefaultCartUI();

    @NotNull default String getDefaultCartUI(@NotNull TradeType type) {
        return this.getDefaultCartUI();
    }

    @NotNull TransactionLogger getLogger();
}
