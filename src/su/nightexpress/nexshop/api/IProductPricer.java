package su.nightexpress.nexshop.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.nexshop.api.type.TradeType;

import java.time.LocalTime;

public interface IProductPricer extends ITimed {

    @NotNull
    IShopProduct getProduct();

    void setProduct(@NotNull IShopProduct product);

    default void updatePrice() {
        if (this.randomizePrices(false)) {
            return;
        }

        double buyMin = this.getPriceMin(TradeType.BUY);
        double sellMin = this.getPriceMin(TradeType.SELL);
        this.setPrice(TradeType.BUY, buyMin);
        this.setPrice(TradeType.SELL, sellMin);
    }

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

    /**
     * @param buyType
     * @param allowDiscount
     * @return Returns current product price with or without discount modifier.
     */
    default double getPrice(@NotNull TradeType buyType, boolean allowDiscount) {
        double price = this.getPrice(buyType);

        if (buyType == TradeType.BUY && price > 0 && allowDiscount) {
            IShop shop = this.getProduct().getShop();
            IShopDiscount discount = shop.getDiscount();
            if (discount != null) price *= discount.getDiscount();
        }
        return price;
    }

    default double getPriceBuy(boolean allowDiscount) {
        return this.getPrice(TradeType.BUY, allowDiscount);
    }

    default double getPriceSell() {
        return this.getPrice(TradeType.SELL, false);
    }

    default double getPriceSellAll(@NotNull Player player) {
        int amountHas = this.getProduct().getItemAmount(player);
        int amountCan = this.getProduct().getStockAmountLeft(player, TradeType.SELL);

        int balance = Math.min((amountCan < 0 ? amountHas : amountCan), amountHas);
        return balance * this.getPriceSell();
    }

    boolean isRandomizerEnabled();

    void setRandomizerEnabled(boolean isEnabled);

    @Nullable
    LocalTime getLastRandomizedTime();

    void setLastRandomizedTime(@NotNull LocalTime time);

    default void randomizePrices() {
        this.randomizePrices(false);
    }

    default boolean randomizePrices(boolean force) {
        if (!this.isRandomizerEnabled()) return false;

        LocalTime[] times = this.getCurrentTimes();
        if (!force) {
            if (times == null) return false;

            LocalTime lastTime = this.getLastRandomizedTime();
            if (times[1].equals(lastTime)) return false;
        }

        double buyPrice = Rnd.getDouble(this.getPriceMin(TradeType.BUY), this.getPriceMax(TradeType.BUY));
        double sellPrice = Rnd.getDouble(this.getPriceMin(TradeType.SELL), this.getPriceMax(TradeType.SELL));
        if (sellPrice > buyPrice && buyPrice >= 0) {
            sellPrice = buyPrice;
        }

        this.setPrice(TradeType.BUY, buyPrice);
        this.setPrice(TradeType.SELL, sellPrice);

        if (times == null) return true;

        this.setLastRandomizedTime(times[1]);
        return true;
    }
}
