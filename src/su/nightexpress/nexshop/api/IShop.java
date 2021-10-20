package su.nightexpress.nexshop.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface IShop extends IEditable, IPlaceholder {

    String PLACEHOLDER_ID = "%shop_id%";
    String PLACEHOLDER_NAME = "%shop_name%";
    String PLACEHOLDER_BUY_ALLOWED  = "%shop_buy_allowed%";
    String PLACEHOLDER_SELL_ALLOWED = "%shop_sell_allowed%";
    String PLACEHOLDER_DISCOUNT_AVAILABLE = "%shop_discount_available%";
    String PLACEHOLDER_DISCOUNT_AMOUNT    = "%shop_discount_amount%";
    String PLACEHOLDER_DISCOUNT_TIMELEFT  = "%shop_discount_timeleft%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        IShopDiscount discount = this.getDiscount();
        String hhTimeLeft;
        if (discount != null) {
            LocalTime[] times = discount.getCurrentTimes();
            if (times != null) {
                Duration dur = Duration.between(LocalTime.now(), times[1]);
                hhTimeLeft = TimeUT.formatTime(dur.toMillis());
            }
            else hhTimeLeft = "-";
        }
        else hhTimeLeft = "-";

        return str -> str
                .replace(PLACEHOLDER_ID, this.getId())
                .replace(PLACEHOLDER_NAME, this.getName())
                .replace(PLACEHOLDER_BUY_ALLOWED, plugin().lang().getBool(this.isPurchaseAllowed(TradeType.BUY)))
                .replace(PLACEHOLDER_SELL_ALLOWED, plugin().lang().getBool(this.isPurchaseAllowed(TradeType.SELL)))
                .replace(PLACEHOLDER_DISCOUNT_AVAILABLE, plugin().lang().getBool(discount != null))
                .replace(PLACEHOLDER_DISCOUNT_AMOUNT, discount != null ? NumberUT.format(discount.getDiscountRaw()) : "-")
                .replace(PLACEHOLDER_DISCOUNT_TIMELEFT, hhTimeLeft)
                ;
    }

    void save();

    @NotNull
    ExcellentShop plugin();

    @NotNull
    String getId();

    @NotNull
    String getName();

    void setName(@NotNull String name);

    @NotNull
    AbstractShopView<? extends IShop> getView();

    void setupView();

    void open(@NotNull Player player, int page);

    boolean isPurchaseAllowed(@NotNull TradeType buyType);

    void setPurchaseAllowed(@NotNull TradeType buyType, boolean isAllowed);

    /**
     * Returns how much funds a shop has.
     * @param currency Currency to check balance from.
     * @return Shop balance. Returns -1 if balance is unlimited.
     */
    double getShopBalance(@NotNull IShopCurrency currency);

    void takeFromShopBalance(@NotNull IShopCurrency currency, double amount);

    void addToShopBalance(@NotNull IShopCurrency currency, double amount);

    @NotNull
    Collection<IShopDiscount> getDiscounts();

    default boolean isDiscountAvailable() {
        return this.getDiscount() != null;
    }

    /**
     * Find the first available Discount for this shop of the current day time.
     * @return Discount object, NULL if no discount.
     */
    @Nullable
    default IShopDiscount getDiscount() {
        Optional<IShopDiscount> opt = this.getDiscounts().stream().filter(IShopDiscount::isAvailable).findFirst();
        return opt.orElse(null);
    }

    default double getDiscountModifier() {
        IShopDiscount discount = this.getDiscount();
        return discount != null ? discount.getDiscount() : 1D;
    }

    @NotNull
    Map<String, ? extends IShopProduct> getProductMap();

    @NotNull
    default Collection<? extends IShopProduct> getProducts() {
        return this.getProductMap().values();
    }

    @Nullable
    default IShopProduct getProductById(@NotNull String id) {
        return this.getProductMap().get(id);
    }

    default void deleteProduct(@NotNull IShopProduct product) {
        this.deleteProduct(product.getId());
    }

    default void deleteProduct(@NotNull String id) {
        this.getProductMap().remove(id);
    }
}

