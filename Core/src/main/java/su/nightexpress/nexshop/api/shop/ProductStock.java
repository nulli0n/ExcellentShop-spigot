package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nightexpress.nexshop.api.IPurchaseListener;
import su.nightexpress.nexshop.api.type.StockType;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.function.UnaryOperator;

public abstract class ProductStock<P extends Product<P, ?, ?>> implements IPlaceholder, IPurchaseListener, JOption.Writer {

    protected P       product;
    protected boolean locked;

    public ProductStock() {

    }

    @NotNull
    public P getProduct() {
        if (this.product == null) {
            throw new IllegalStateException("Product is undefined!");
        }
        return product;
    }

    public void setProduct(@NotNull P product) {
        this.product = product;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void unlock() {
        this.setLocked(false);
    }

    public void lock() {
        this.setLocked(true);
    }

    @NotNull
    public abstract UnaryOperator<String> replacePlaceholders(@NotNull Player player);

    public abstract int getInitialAmount(@NotNull StockType stockType, @NotNull TradeType tradeType);

    public abstract void setInitialAmount(@NotNull StockType stockType, @NotNull TradeType tradeType, int amount);

    public boolean isUnlimited(@NotNull StockType stockType, @NotNull TradeType tradeType) {
        return this.getInitialAmount(stockType, tradeType) < 0;
    }

    public abstract int getRestockCooldown(@NotNull StockType stockType, @NotNull TradeType tradeType);

    public abstract void setRestockCooldown(@NotNull StockType stockType, @NotNull TradeType tradeType, int cooldown);

    public boolean hasRestockCooldown(@NotNull StockType stockType, @NotNull TradeType tradeType) {
        return this.getRestockCooldown(stockType, tradeType) != 0;
    }

    public boolean isRestockPossible(@NotNull StockType stockType, @NotNull TradeType tradeType) {
        return !this.hasRestockCooldown(stockType, tradeType) || this.getRestockCooldown(stockType, tradeType) >= 0;
    }


    public int getLeftAmount(@NotNull TradeType tradeType) {
        return this.getLeftAmount(tradeType, null);
    }

    public void setLeftAmount(@NotNull TradeType tradeType, int amount) {
        this.setLeftAmount(tradeType, amount, null);
    }

    public long getRestockDate(@NotNull TradeType tradeType) {
        return this.getRestockDate(tradeType, null);
    }


    public abstract int getPossibleAmount(@NotNull TradeType tradeType, @NotNull Player player);

    public abstract int getLeftAmount(@NotNull TradeType tradeType, @Nullable Player player);

    public abstract void setLeftAmount(@NotNull TradeType tradeType, int amount, @Nullable Player player);

    public abstract long getRestockDate(@NotNull TradeType tradeType, @Nullable Player player);
}
