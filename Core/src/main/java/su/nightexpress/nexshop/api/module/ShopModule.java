package su.nightexpress.nexshop.api.module;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.TransactionLogger;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public interface ShopModule extends Module {

    @NotNull String getDefaultCartUI();

    @NotNull default String getDefaultCartUI(@NotNull TradeType type) {
        return this.getDefaultCartUI();
    }

    @NotNull TransactionLogger getLogger();
}
