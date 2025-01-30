package su.nightexpress.nexshop.api.shop.stock;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.TimeUtil;

public class StockValues {

    public static final int UNLIMITED = -1;

    private int buyAmount;
    private int sellAmount;
    private long restockTime;

    public StockValues(int buyAmount, int sellAmount, long restockTime) {
        this.setBuyAmount(buyAmount);
        this.setSellAmount(sellAmount);
        this.setRestockTime(restockTime);
    }

    @NotNull
    public static StockValues unlimited()  {
        return new StockValues(UNLIMITED, UNLIMITED, 0L);
    }

    @NotNull
    public static StockValues read(@NotNull FileConfig config, @NotNull String path) {
        if (!config.contains(path + ".RestockTime")) {
            for (TradeType tradeType : TradeType.values()) {
                String path2 = path + "." + tradeType.name();
                int initialAmount = config.getInt(path2 + ".Initial_Amount", -1);
                long restockTime = config.getLong(path2 + ".Restock_Time", 0);

                String name = tradeType == TradeType.BUY ? "BuyAmount" : "SellAmount";
                config.set(path + "." + name, initialAmount);
                if (restockTime != 0L) {
                    config.set(path + ".RestockTime", restockTime);
                }
                config.remove(path + "." + tradeType.name());
            }
        }

        int buyAmount = config.getInt(path + ".BuyAmount", -1);
        int sellAmount = config.getInt(path + ".SellAmount", -1);
        long restockTime = config.getLong(path + ".RestockTime", 0L);


        return new StockValues(buyAmount, sellAmount, restockTime);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".BuyAmount", this.buyAmount);
        config.set(path + ".SellAmount", this.sellAmount);
        config.set(path + ".RestockTime", this.restockTime);
    }

    public boolean isUnlimited(@NotNull TradeType type) {
        return this.getInitialAmount(type) < 0 || this.getRestockTime() == 0L;
    }

    public boolean isRestockable() {
        return this.restockTime >= 0L;
    }

    public long getRestockTimeMillis() {
        return this.restockTime <= 0L ? this.restockTime : this.restockTime * 1000L + 100L; // 100L for better visuals
    }

    public long generateRestockTimestamp() {
        return TimeUtil.createFutureTimestamp(this.restockTime);
    }

    public int getBuyAmount() {
        return this.buyAmount;
    }

    public void setBuyAmount(int buyAmount) {
        this.buyAmount = buyAmount;
    }

    public int getSellAmount() {
        return this.sellAmount;
    }

    public void setSellAmount(int sellAmount) {
        this.sellAmount = sellAmount;
    }

    public long getRestockTime() {
        return this.restockTime;
    }

    public void setRestockTime(long restockTime) {
        this.restockTime = restockTime;
    }

    public int getInitialAmount(@NotNull TradeType type) {
        return type == TradeType.BUY ? this.buyAmount : this.sellAmount;
    }

    public void setAmount(@NotNull TradeType type, int amount) {
        if (type == TradeType.BUY) {
            this.setBuyAmount(amount);
        }
        else {
            this.setSellAmount(amount);
        }
    }
}
