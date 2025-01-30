package su.nightexpress.nexshop.data.product;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.util.TimeUtil;

import java.util.UUID;

public class StockData extends AbstractData {

    private final String holder;

    private int buyStock;
    private int sellStock;
    private long restockDate;

    public StockData(@NotNull String shopId,
                     @NotNull String productId,
                     @NotNull String holder,
                     int buyStock,
                     int sellStock,
                     long restockDate) {
        super(shopId, productId);
        this.holder = holder.toLowerCase();
        this.buyStock = buyStock;
        this.sellStock = sellStock;
        this.restockDate = restockDate;
    }

    @NotNull
    public static StockData create(@NotNull Product product, @NotNull StockValues values, @Nullable UUID playerId) {
        String shopId = product.getShop().getId();
        String productId = product.getId();
        String holder = playerId == null ? shopId : playerId.toString();

        int buyStock = values.getBuyAmount();
        int sellStock = values.getSellAmount();
        long restockDate = 0L;

        return new StockData(shopId, productId, holder, buyStock, sellStock, restockDate);
    }

    public boolean isRestockTime() {
        return !this.isAwaiting() && TimeUtil.isPassed(this.restockDate);
        //return this.restockDate > 0 && System.currentTimeMillis() >= this.restockDate;
    }

    public boolean isAwaiting() {
        return this.restockDate == 0L;
    }

    public void setExpired() {
        this.setRestockDate(System.currentTimeMillis() - 1000L);
    }

//    public void restockIfReady(@NotNull StockValues values) {
//        if (this.isRestockTime()) {
//            this.restock(values);
//        }
//    }

    public void restock(@NotNull StockValues values) {
        this.setBuyStock(values.getBuyAmount());
        this.setSellStock(values.getSellAmount());
        this.restockDate = 0L;
    }

    public void startRestockIfAbsent(@NotNull StockValues values) {
        if (!this.isAwaiting()) return;

        this.updateRestockDate(values);
    }

    public void updateRestockDate(@NotNull StockValues values) {
        if (values.isRestockable()) {
            this.restockDate = values.generateRestockTimestamp();
        }
        else {
            this.restockDate = -1L;
        }
    }

    public int countStock(@NotNull TradeType type) {
        return type == TradeType.BUY ? this.buyStock : this.sellStock;
    }

    public void consumeStock(@NotNull TradeType type, int amount) {
        if (type == TradeType.BUY) {
            this.setBuyStock(this.buyStock - amount);
        }
        else {
            this.setSellStock(this.sellStock - amount);
        }
    }

    public void fillStock(@NotNull TradeType type, int amount) {
        if (type == TradeType.BUY) {
            this.setBuyStock(this.buyStock + amount);
        }
        else {
            this.setSellStock(this.sellStock + amount);
        }
    }

    @NotNull
    public String getHolder() {
        return this.holder;
    }

    public int getBuyStock() {
        return this.buyStock;
    }

    public void setBuyStock(int buyStock) {
        this.buyStock = Math.max(0, buyStock);
    }

    public int getSellStock() {
        return this.sellStock;
    }

    public void setSellStock(int sellStock) {
        this.sellStock = Math.max(0, sellStock);
    }

    public long getRestockDate() {
        return this.restockDate;
    }

    public void setRestockDate(long restockDate) {
        this.restockDate = restockDate;
    }
}
