package su.nightexpress.nexshop.api.shop.product;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.stock.StockValues;

public interface VirtualProduct extends Product {

    @NotNull PlaceholderMap getPlaceholders(@NotNull Player player);

    boolean hasAccess(@NotNull Player player);

    @NotNull VirtualShop getShop();

    @NotNull StockValues getStockValues();

    @NotNull StockValues getLimitValues();

    boolean isDiscountAllowed();

    void setDiscountAllowed(boolean isAllowed);
}
