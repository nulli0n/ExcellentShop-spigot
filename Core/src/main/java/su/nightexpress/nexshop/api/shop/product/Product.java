package su.nightexpress.nexshop.api.shop.product;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.handler.PluginItemHandler;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nightcore.util.placeholder.Placeholder;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

public interface Product extends Placeholder {

    @Override
    @NotNull
    default PlaceholderMap getPlaceholders() {
        return this.getPlaceholders(null);
    }

    default boolean isValid() {
        if (this.getPacker() instanceof PluginItemPacker packer && this.getHandler() instanceof PluginItemHandler handler) {
            return handler.isValidId(packer.getItemId());
        }
        return true;

        //return !(this.getHandler() instanceof AbstractPluginItemHandler packer) || packer.isValidId(packer.getItemId());
    }

    @NotNull PlaceholderMap getPlaceholders(@Nullable Player player);

//    @Deprecated
//    void prepareTrade(@NotNull Player player, @NotNull ShopClickAction click);

    default double getPrice(@NotNull TradeType tradeType) {
        return this.getPrice(tradeType, null);
    }

    double getPrice(@NotNull TradeType tradeType, @Nullable Player player);

    void setPrice(@NotNull TradeType tradeType, double price);

    default double getPriceBuy(@NotNull Player player) {
        return this.getPrice(TradeType.BUY, player);
    }

    default double getPriceSell(@NotNull Player player) {
        return this.getPrice(TradeType.SELL, player);
    }

    double getPriceSellAll(@NotNull Player player);

    default int getUnitAmount() {
        return this.getPacker().getUnitAmount();
    }

    default void delivery(@NotNull Player player, int count) {
        this.delivery(player.getInventory(), count);
    }

    default void delivery(@NotNull Inventory inventory, int count) {
        this.getPacker().delivery(inventory, count);
    }

    default void take(@NotNull Player player, int count) {
        this.take(player.getInventory(), count);
    }

    default void take(@NotNull Inventory inventory, int count) {
        this.getPacker().take(inventory, count);
    }

    default int count(@NotNull Player player) {
        return this.count(player.getInventory());
    }

    default int countUnits(@NotNull Player player) {
        return this.countUnits(player.getInventory());
    }

    default int countUnits(@NotNull Inventory inventory) {
        return this.count(inventory) / this.getUnitAmount();
    }

    default int count(@NotNull Inventory inventory) {
        return this.getPacker().count(inventory);
    }

    default int countSpace(@NotNull Player player) {
        return this.countSpace(player.getInventory());
    }

    default int countSpace(@NotNull Inventory inventory) {
        return this.getPacker().countSpace(inventory);
    }

    default boolean hasSpace(@NotNull Player player) {
        return this.hasSpace(player.getInventory());
    }

    default boolean hasSpace(@NotNull Inventory inventory) {
        return this.getPacker().hasSpace(inventory);
    }

    @NotNull PreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all);

    int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType);

    default boolean isTradeable(@NotNull TradeType tradeType) {
        return tradeType == TradeType.BUY ? this.isBuyable() : this.isSellable();
    }

    boolean isBuyable();

    boolean isSellable();

    @NotNull String getId();

    @NotNull Shop getShop();

    @NotNull Currency getCurrency();

    void setCurrency(@NotNull Currency currency);

    @NotNull ProductHandler getHandler();

    void setHandler(@NotNull ProductHandler handler, @NotNull ProductPacker packer);

    @NotNull ProductPacker getPacker();

    @NotNull AbstractProductPricer getPricer();

    void setPricer(@NotNull AbstractProductPricer pricer);

    @NotNull default ItemStack getPreview() {
        return this.getPacker().getPreview();
    }
}
