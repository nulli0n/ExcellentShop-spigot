package su.nightexpress.nexshop.api.shop.product;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;

import java.util.function.UnaryOperator;

public interface Product {

    @NotNull UnaryOperator<String> replacePlaceholders();

    @NotNull UnaryOperator<String> replacePlaceholders(@Nullable Player player);

    boolean isValid();

    /**
     * Performs a check to determine if product is available for buying/selling (e.g. present in the shop, is in rotation).
     * @param player Player who about to buy/sell product.
     * @return Whether product is available for buying/selling.
     */
    boolean isAvailable(@NotNull Player player);

    int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType);

    double getPriceBuy(@NotNull Player player);

    double getPriceSell(@NotNull Player player);

    double getPriceSellAll(@NotNull Player player);

    double getPrice(@NotNull TradeType tradeType);

    double getPrice(@NotNull TradeType tradeType, @Nullable Player player);

    void setPrice(@NotNull TradeType tradeType, double price);

    int getUnitAmount();

    void delivery(@NotNull Player player, int count);

    void delivery(@NotNull Inventory inventory, int count);

    void take(@NotNull Player player, int count);

    void take(@NotNull Inventory inventory, int count);

    int count(@NotNull Player player);

    int countUnits(@NotNull Player player);

    int countUnits(@NotNull Inventory inventory);

    int count(@NotNull Inventory inventory);

    int countSpace(@NotNull Player player);

    int countSpace(@NotNull Inventory inventory);

    boolean hasSpace(@NotNull Player player);

    boolean hasSpace(@NotNull Inventory inventory);

    @NotNull PreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all);



    boolean isTradeable(@NotNull TradeType tradeType);

    boolean isBuyable();

    boolean isSellable();

    @NotNull String getId();

    @NotNull Shop getShop();

    @NotNull
    ProductTyping getType();

    void setType(@NotNull ProductTyping type);

    @NotNull ItemStack getPreview();

    @NotNull Currency getCurrency();

    void setCurrency(@NotNull Currency currency);

//    @NotNull ProductHandler getHandler();
//
//    void setHandler(@NotNull ProductHandler handler, @NotNull ProductPacker packer);
//
//    @NotNull ProductPacker getPacker();

    @NotNull AbstractProductPricer getPricer();

    void setPricer(@NotNull AbstractProductPricer pricer);
}
