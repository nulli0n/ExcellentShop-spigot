package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.impl.Discount;
import su.nightexpress.nexshop.shop.virtual.type.ShopType;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VirtualShop extends Shop {

    @NotNull File getFile();

    boolean load();

    boolean hasPermission(@NotNull Player player);

    @NotNull ShopType getType();

    @NotNull Set<Discount> getDiscounts();

    @NotNull ItemStack getIcon();

    void setIcon(@NotNull ItemStack icon);

    @NotNull String getLayoutName();

    void setLayoutName(@NotNull String layoutName);

    @NotNull List<String> getDescription();

    void setDescription(@NotNull List<String> description);

    boolean isPermissionRequired();

    void setPermissionRequired(boolean permissionRequired);

    @NotNull Set<Integer> getNPCIds();

    default boolean hasDiscount() {
        return this.getDiscountPlain() != 0D;
    }

    default boolean hasDiscount(@NotNull VirtualProduct product) {
        return this.getDiscountPlain(product) != 0D;
    }

    default double getDiscountModifier() {
        return 1D - this.getDiscountPlain() / 100D;
    }

    default double getDiscountModifier(@NotNull VirtualProduct product) {
        return 1D - this.getDiscountPlain(product) / 100D;
    }

    default double getDiscountPlain() {
        return Math.min(100D, this.getDiscounts().stream().mapToDouble(Discount::getDiscountPlain).sum());
    }

    default double getDiscountPlain(@NotNull VirtualProduct product) {
        return product.isDiscountAllowed() ? this.getDiscountPlain() : 0D;
    }

    @NotNull Map<String, ? extends VirtualProduct> getProductMap();

    @NotNull Collection<? extends VirtualProduct> getProducts();

    @Nullable VirtualProduct getProductById(@NotNull String id);

    @Nullable default VirtualProduct getBestProduct(@NotNull ItemStack item, @NotNull TradeType tradeType) {
        return this.getBestProduct(item, tradeType, null);
    }

    @Nullable VirtualProduct getBestProduct(@NotNull ItemStack item, @NotNull TradeType tradeType, @Nullable Player player);
}
