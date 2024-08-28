package su.nightexpress.nexshop.product.data.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.data.AbstractData;
import su.nightexpress.nexshop.product.stock.StockAmount;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StockData extends AbstractData {

    private final Map<TradeType, StockAmount>            globalAmounts;
    private final Map<TradeType, Map<UUID, StockAmount>> playerAmounts;

    public StockData(@NotNull VirtualProduct product) {
        this(product.getShop().getId(), product.getId(), new HashMap<>(), new HashMap<>());
    }

    public StockData(@NotNull String shopId,
                     @NotNull String productId,
                     @NotNull Map<TradeType, StockAmount> globalAmounts,
                     @NotNull Map<TradeType, Map<UUID, StockAmount>> playerAmounts) {
        super(shopId, productId);
        this.globalAmounts = globalAmounts;
        this.playerAmounts = playerAmounts;
    }

    public boolean isEmpty() {
        return this.globalAmounts.isEmpty() && this.playerAmounts.isEmpty();
    }

    public void cleanUp() {
        for (TradeType type : TradeType.values()) {
            this.globalAmounts.values().removeIf(StockAmount::isRestockTime);
            this.playerAmounts.values().removeIf(map -> map.values().removeIf(StockAmount::isRestockTime) && map.isEmpty());
        }
    }

//    public void restock(@NotNull StockValues globalStock, @NotNull StockValues playerLimits) {
//        for (TradeType type : TradeType.values()) {
//            this.globalAmounts.computeIfAbsent(type, k -> new StockAmounts()).restock(globalStock, type);
//            this.playerAmounts.computeIfAbsent(type, k -> new HashMap<>()).values().forEach(stockAmounts -> stockAmounts.restock(playerLimits, type));
//        }
//    }
//
//    public void updateRestockDate(@NotNull StockValues globalStock, @NotNull StockValues playerLimits) {
//        for (TradeType type : TradeType.values()) {
//            this.globalAmounts.computeIfAbsent(type, k -> new StockAmounts()).updateRestockDate(globalStock, type);
//            this.playerAmounts.computeIfAbsent(type, k -> new HashMap<>()).values().forEach(stockAmounts -> stockAmounts.updateRestockDate(playerLimits, type));
//        }
//    }

    @NotNull
    public StockAmount getGlobalAmount(@NotNull TradeType type) {
        return this.globalAmounts.computeIfAbsent(type, k -> new StockAmount());
    }

    @NotNull
    public StockAmount getPlayerAmount(@NotNull TradeType type, @NotNull UUID playerId) {
        return this.playerAmounts.computeIfAbsent(type, k -> new HashMap<>()).computeIfAbsent(playerId, k -> new StockAmount());
    }

    @NotNull
    public Map<TradeType, StockAmount> getGlobalAmounts() {
        return globalAmounts;
    }

    @NotNull
    public Map<TradeType, Map<UUID, StockAmount>> getPlayerAmounts() {
        return playerAmounts;
    }
}
