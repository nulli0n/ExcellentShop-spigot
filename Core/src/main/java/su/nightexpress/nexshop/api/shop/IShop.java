package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.Collection;
import java.util.Map;

public interface IShop extends IEditable, IPlaceholder {

    void save();

    @NotNull ExcellentShop plugin();

    @NotNull String getId();

    @NotNull String getName();

    void setName(@NotNull String name);

    @NotNull AbstractShopView<? extends IShop> getView();

    void setupView();

    void open(@NotNull Player player, int page);

    boolean isPurchaseAllowed(@NotNull TradeType buyType);

    void setPurchaseAllowed(@NotNull TradeType buyType, boolean isAllowed);

    @NotNull IBank getBank();

    /*
    double getShopBalance(@NotNull IShopCurrency currency);

    void takeFromShopBalance(@NotNull IShopCurrency currency, double amount);

    void addToShopBalance(@NotNull IShopCurrency currency, double amount);*/

    @NotNull Collection<IShopDiscount> getDiscounts();

    default boolean isDiscountAvailable() {
        return this.getDiscount() != null;
    }

    /**
     * Find the first available Discount for this shop of the current day time.
     * @return Discount object, NULL if no discount.
     */
    @Nullable
    default IShopDiscount getDiscount() {
        return this.getDiscounts().stream().filter(IShopDiscount::isAvailable).findFirst().orElse(null);
    }

    default double getDiscountModifier() {
        IShopDiscount discount = this.getDiscount();
        return discount != null ? discount.getDiscount() : 1D;
    }

    @NotNull Map<String, ? extends IProduct> getProductMap();

    @NotNull Collection<? extends IProduct> getProducts();

    @Nullable IProduct getProductById(@NotNull String id);

    default boolean isProduct(@NotNull IProduct product) {
        return this.getProducts().contains(product);
    }

    default boolean isProduct(@NotNull ItemStack item) {
        return this.getProducts().stream().anyMatch(product -> product.isItemMatches(item));
    }

    default void deleteProduct(@NotNull IProduct product) {
        this.deleteProduct(product.getId());
    }

    default void deleteProduct(@NotNull String id) {
        this.getProductMap().remove(id);
    }
}

