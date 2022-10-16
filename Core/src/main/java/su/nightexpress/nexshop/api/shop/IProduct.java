package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IEditable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.type.ShopClickType;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.function.UnaryOperator;

public interface IProduct extends ICleanable, IEditable, IPlaceholder {

    @NotNull IShop getShop();

    @NotNull String getId();

    @NotNull UnaryOperator<String> replacePlaceholdersView();

    @NotNull UnaryOperator<String> replacePlaceholders(@NotNull Player player);

    void prepareTrade(@NotNull Player player, @NotNull ShopClickType click);

    void openTrade(@NotNull Player player, @NotNull IProductPrepared prepared);

    boolean isEmpty();

    boolean isBuyable();

    boolean isSellable();

    @NotNull IProductPrepared getPrepared(@NotNull TradeType tradeType);

    @NotNull IProductPricer getPricer();

    @NotNull ICurrency getCurrency();

    void setCurrency(@NotNull ICurrency currency);

    int getStockAmountLeft(@NotNull Player player, @NotNull TradeType tradeType);

    boolean isDiscountAllowed();

    void setDiscountAllowed(boolean isAllowed);

    boolean isItemMetaEnabled();

    void setItemMetaEnabled(boolean isEnabled);

    @NotNull ItemStack getPreview();

    void setPreview(@NotNull ItemStack preview);

    @NotNull
    ItemStack getItem();

    void setItem(@NotNull ItemStack item);

    default boolean hasItem() {
        return !this.getItem().getType().isAir();
    }

    default boolean isItemMatches(@NotNull ItemStack item) {
        return this.isItemMetaEnabled() ? this.getItem().isSimilar(item) : this.getItem().getType() == item.getType();
    }

    default int getItemAmount(@NotNull Player player) {
        if (!this.hasItem()) return 0;

        return PlayerUtil.countItem(player, this::isItemMatches);
    }

    default void takeItemAmount(@NotNull Player player, int amount) {
        if (!this.hasItem()) return;

        PlayerUtil.takeItem(player, this::isItemMatches, amount);
    }
}
