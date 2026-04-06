package su.nightexpress.excellentshop.feature.virtualshop.product;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.configuration.ConfigType;
import su.nightexpress.nightcore.util.Randomizer;
import su.nightexpress.nightcore.util.TimeUtil;

public class StockOptions implements Writeable {

    public static final ConfigType<StockOptions> CONFIG_TYPE = ConfigType.of(StockOptions::read, FileConfig::set);

    private boolean enabled;
    private int     capacity;
    private int     restockMinAmount;
    private int     restockMaxAmount;
    private long    restockTime;

    public StockOptions(boolean enabled, int capacity, int restockMinAmount, int restockMaxAmount, long restockTime) {
        this.setEnabled(enabled);
        this.setCapacity(capacity);
        this.setRestockMinAmount(restockMinAmount);
        this.setRestockMaxAmount(restockMaxAmount);
        this.setRestockTime(restockTime);
    }

    @NotNull
    public static StockOptions read(@NotNull FileConfig config, @NotNull String path) {
        if (config.contains(path + ".BuyAmount")) {
            int oldAmount = config.getInt(path + ".BuyAmount", -1);
            config.set(path + ".Enabled", oldAmount >= 0);
            config.set(path + ".Capacity", oldAmount);
            config.set(path + ".Restock-Min-Amount", oldAmount);
            config.set(path + ".Restock-Max-Amount", oldAmount);
            config.remove(path + ".BuyAmount");
        }

        boolean enabled = config.getBoolean(path + ".Enabled", false);
        int capacity = config.getInt(path + ".Capacity", -1);
        int restockMinAmount = config.getInt(path + ".Restock-Min-Amount", 0);
        int restsockMaxAmount = config.getInt(path + ".Restock-Max-Amount", 0);
        long restockTime = config.getLong(path + ".RestockTime", 0L);

        return new StockOptions(enabled, capacity, restockMinAmount, restsockMaxAmount, restockTime);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Enabled", this.enabled);
        config.set(path + ".Capacity", this.capacity);
        config.set(path + ".Restock-Min-Amount", this.restockMinAmount);
        config.set(path + ".Restock-Max-Amount", this.restockMaxAmount);
        config.set(path + ".RestockTime", this.restockTime);
    }

    public boolean hasRestockTime() {
        return this.restockTime != 0L;
    }

    public long generateRestockTimestamp() {
        return TimeUtil.createFutureTimestamp(this.restockTime);
    }

    public int generateInitialAmount() {
        if (this.restockMinAmount < 0 || this.restockMaxAmount < 0) return -1;
        if (this.restockMaxAmount <= this.restockMinAmount) return this.restockMinAmount;

        return Randomizer.nextInt(this.restockMinAmount, this.restockMaxAmount);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(0, capacity);
    }

    public int getRestockMinAmount() {
        return this.restockMinAmount;
    }

    public void setRestockMinAmount(int restockMinAmount) {
        this.restockMinAmount = Math.max(0, restockMinAmount);
    }

    public int getRestockMaxAmount() {
        return this.restockMaxAmount;
    }

    public void setRestockMaxAmount(int restockMaxAmount) {
        this.restockMaxAmount = Math.max(0, restockMaxAmount);
    }

    public long getRestockTime() {
        return this.restockTime;
    }

    public void setRestockTime(long restockTime) {
        this.restockTime = restockTime;
    }
}
