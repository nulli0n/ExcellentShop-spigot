package su.nightexpress.nexshop.api.shop.stock;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.TransactionListener;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.type.TradeType;

public interface Stock extends TransactionListener {

    @NotNull Shop getShop();

    void load();

    int count(@NotNull Product product, @NotNull TradeType type);

    boolean consume(@NotNull Product product, int amount, @NotNull TradeType type);

    boolean store(@NotNull Product product, int amount, @NotNull TradeType type);

    boolean restock(@NotNull Product product, @NotNull TradeType type, boolean force);

}
