package su.nightexpress.nexshop.api.shop.stock;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.api.shop.type.TradeType;

import java.util.HashMap;
import java.util.Map;

public class StockValues {

    private final Map<TradeType, Integer> initialAmountMap;
    private final Map<TradeType, Long>    restockTimeMap;

    public StockValues() {
        this(new HashMap<>(), new HashMap<>());

        for (TradeType tradeType : TradeType.values()) {
            this.getInitialAmountMap().put(tradeType, -1);
            this.getRestockTimeMap().put(tradeType, 0L);
        }
    }

    public StockValues(@NotNull Map<TradeType, Integer> initialAmountMap, @NotNull Map<TradeType, Long> restockTimeMap) {
        this.initialAmountMap = initialAmountMap;
        this.restockTimeMap = restockTimeMap;
    }

    @NotNull
    public static StockValues read(@NotNull JYML cfg, @NotNull String path) {
        Map<TradeType, Integer> initialAmountMap = new HashMap<>();
        Map<TradeType, Long> restockTimeMap = new HashMap<>();

        for (TradeType tradeType : TradeType.values()) {
            String path2 = path + "." + tradeType.name();
            int initialAmount = cfg.getInt(path2 + ".Initial_Amount", -1);
            long restockTime = cfg.getLong(path2 + ".Restock_Time", 0);

            initialAmountMap.put(tradeType, initialAmount);
            restockTimeMap.put(tradeType, restockTime);
        }
        return new StockValues(initialAmountMap, restockTimeMap);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        for (TradeType tradeType : TradeType.values()) {
            cfg.set(path + "." + tradeType.name() + ".Initial_Amount", this.getInitialAmount(tradeType));
            cfg.set(path + "." + tradeType.name() + ".Restock_Time", this.getRestockTime(tradeType));
        }
    }

    public boolean isUnlimited(@NotNull TradeType type) {
        return this.getInitialAmount(type) < 0 || this.getRestockTime(type) == 0L;
    }

    public boolean isRestockable(@NotNull TradeType type) {
        return this.getRestockTime(type) >= 0L;
    }

    @NotNull
    public Map<TradeType, Integer> getInitialAmountMap() {
        return initialAmountMap;
    }

    @NotNull
    public Map<TradeType, Long> getRestockTimeMap() {
        return restockTimeMap;
    }

    public int getInitialAmount(@NotNull TradeType type) {
        return this.getInitialAmountMap().getOrDefault(type, -1);
    }

    public void setInitialAmount(@NotNull TradeType type, int amount) {
        this.getInitialAmountMap().put(type, amount);
    }

    public long getRestockTime(@NotNull TradeType type) {
        return this.getRestockTimeMap().getOrDefault(type, 0L);
    }

    public void setRestockTime(@NotNull TradeType type, long time) {
        this.getRestockTimeMap().put(type, time);
    }
}
