package su.nightexpress.excellentshop.feature.virtualshop.product;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.util.TimeUtil;

public class LimitOptions implements Writeable {

    private boolean enabled;
    private int     buyLimit;
    private int     sellLimit;
    private long    restockTime;

    public LimitOptions(boolean enabled, int buyLimit, int sellLimit, long restockTime) {
        this.setEnabled(enabled);
        this.setBuyLimit(buyLimit);
        this.setSellLimit(sellLimit);
        this.setRestockTime(restockTime);
    }

    @NotNull
    public static LimitOptions read(@NotNull FileConfig config, @NotNull String path) {
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

        config.set(path + ".Enabled", config.getInt(path + ".BuyAmount") >= 0 || config.getInt(path + ".SellAmount") >= 0);

        if (config.contains(path + ".BuyAmount")) {
            int old = config.getInt(path + ".BuyAmount");
            config.set(path + ".BuyLimit", old);
            config.remove(path + ".BuyAmount");
        }

        if (config.contains(path + ".SellAmount")) {
            int old = config.getInt(path + ".SellAmount");
            config.set(path + ".SellLimit", old);
            config.remove(path + ".SellAmount");
        }

        boolean enabled = config.get(ConfigTypes.BOOLEAN, path + ".Enabled", false);
        int buyLimit = config.getInt(path + ".BuyLimit", -1);
        int sellLimit = config.getInt(path + ".SellLimit", -1);
        long restockTime = config.getLong(path + ".RestockTime", 0L);


        return new LimitOptions(enabled, buyLimit, sellLimit, restockTime);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Enabled", this.enabled);
        config.set(path + ".BuyLimit", this.buyLimit);
        config.set(path + ".SellLimit", this.sellLimit);
        config.set(path + ".RestockTime", this.restockTime);
    }

    public boolean isUnlimited(@NotNull TradeType type) {
        return this.getLimit(type) < 0;
    }

    public boolean hasResetTime() {
        return this.restockTime != 0L;
    }

    public boolean hasLimits() {
        return this.hasBuyLimit() || this.hasSellLimit();
    }

    public boolean hasBuyLimit() {
        return this.buyLimit >= 0;
    }

    public boolean hasSellLimit() {
        return this.sellLimit >= 0;
    }

    public long generateRestockTimestamp() {
        return TimeUtil.createFutureTimestamp(this.restockTime);
    }

    public int getLimit(@NotNull TradeType type) {
        return switch (type) {
            case BUY -> this.buyLimit;
            case SELL -> this.sellLimit;
        };
    }

    public void setLimit(@NotNull TradeType type, int amount) {
        switch (type) {
            case BUY -> this.setBuyLimit(amount);
            case SELL -> this.setSellLimit(amount);
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getBuyLimit() {
        return this.buyLimit;
    }

    public void setBuyLimit(int buyLimit) {
        this.buyLimit = buyLimit;
    }

    public int getSellLimit() {
        return this.sellLimit;
    }

    public void setSellLimit(int sellLimit) {
        this.sellLimit = sellLimit;
    }

    public long getRestockTime() {
        return this.restockTime;
    }

    public void setRestockTime(long restockTime) {
        this.restockTime = restockTime;
    }
}
