package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.ITimed;
import su.nightexpress.nexshop.api.type.TradeType;

import java.time.LocalTime;

public interface IProductPricer extends ITimed {

    @NotNull IProduct getProduct();

    void setProduct(@NotNull IProduct product);

    void updatePrice();

    double getPriceMin(@NotNull TradeType buyType);

    double getPriceMax(@NotNull TradeType buyType);

    void setPriceMin(@NotNull TradeType buyType, double price);

    void setPriceMax(@NotNull TradeType buyType, double price);

    /**
     * @param buyType
     * @return current product price.
     */
    double getPrice(@NotNull TradeType buyType);

    void setPrice(@NotNull TradeType buyType, double price);

    default double getPriceBuy() {
        return this.getPrice(TradeType.BUY);
    }

    default double getPriceSell() {
        return this.getPrice(TradeType.SELL);
    }

    default double getPriceSellAll(@NotNull Player player) {
        int amountHas = this.getProduct().getItemAmount(player);
        int amountCan = this.getProduct().getStockAmountLeft(player, TradeType.SELL);

        int balance = Math.min((amountCan < 0 ? amountHas : amountCan), amountHas);
        return balance * this.getPriceSell();
    }

    boolean isRandomizerEnabled();

    void setRandomizerEnabled(boolean isEnabled);

    @Nullable LocalTime getLastRandomizedTime();

    void setLastRandomizedTime(@NotNull LocalTime time);

    default void randomizePrices() {
        this.randomizePrices(false);
    }

    boolean randomizePrices(boolean force);
}
