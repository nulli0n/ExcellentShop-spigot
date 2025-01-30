package su.nightexpress.nexshop.data.legacy;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.type.TradeType;

import java.util.Map;
import java.util.UUID;

public class LegacyStockData {

    private final String shopId;
    private final String productId;
    private final Map<TradeType, LegacyStockAmount>            globalAmounts;
    private final Map<TradeType, Map<UUID, LegacyStockAmount>> playerAmounts;

    public LegacyStockData(@NotNull String shopId,
                           @NotNull String productId,
                           @NotNull Map<TradeType, LegacyStockAmount> globalAmounts,
                           @NotNull Map<TradeType, Map<UUID, LegacyStockAmount>> playerAmounts) {
        this.shopId = shopId.toLowerCase();
        this.productId = productId.toLowerCase();
        this.globalAmounts = globalAmounts;
        this.playerAmounts = playerAmounts;
    }

    public String getShopId() {
        return shopId;
    }

    public String getProductId() {
        return productId;
    }

    @NotNull
    public Map<TradeType, LegacyStockAmount> getGlobalAmounts() {
        return this.globalAmounts;
    }

    @NotNull
    public Map<TradeType, Map<UUID, LegacyStockAmount>> getPlayerAmounts() {
        return this.playerAmounts;
    }
}
