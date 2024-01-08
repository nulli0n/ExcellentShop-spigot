package su.nightexpress.nexshop.api.shop.product;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualPreparedProduct;

public interface VirtualProduct extends Product {

    @NotNull PlaceholderMap getPlaceholders(@NotNull Player player);

    boolean hasAccess(@NotNull Player player);

    @NotNull VirtualShop getShop();

    @NotNull StockValues getStockValues();

    @NotNull StockValues getLimitValues();

    boolean isDiscountAllowed();

    void setDiscountAllowed(boolean isAllowed);

    @NotNull VirtualPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all);
}
