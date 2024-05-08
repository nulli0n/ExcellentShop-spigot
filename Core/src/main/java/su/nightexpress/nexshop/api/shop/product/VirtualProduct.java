package su.nightexpress.nexshop.api.shop.product;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualPreparedProduct;

import java.util.Set;

public interface VirtualProduct extends Product {

    boolean hasAccess(@NotNull Player player);

    @NotNull VirtualShop getShop();

    @NotNull StockValues getStockValues();

    @NotNull StockValues getLimitValues();

    boolean isDiscountAllowed();

    void setDiscountAllowed(boolean isAllowed);

    @NotNull Set<String> getAllowedRanks();

    void setAllowedRanks(@NotNull Set<String> allowedRanks);

    @NotNull Set<String> getRequiredPermissions();

    void setRequiredPermissions(@NotNull Set<String> requiredPermissions);

    @NotNull VirtualPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all);
}
