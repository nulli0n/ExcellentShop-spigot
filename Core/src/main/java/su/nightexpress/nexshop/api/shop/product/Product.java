package su.nightexpress.nexshop.api.shop.product;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.product.content.ProductContent;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.product.price.ProductPricing;

import java.util.Optional;
import java.util.UUID;
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


    int countStock(@NotNull TradeType type, @Nullable UUID playerId);

    boolean consumeStock(@NotNull TradeType type, int amount, @Nullable UUID playerId);

    boolean storeStock(@NotNull TradeType type, int amount, @Nullable UUID playerId);

    boolean restock(@NotNull TradeType type, boolean force, @Nullable UUID playerId);

    long getRestockDate(@Nullable UUID playerId);



    void updatePrice(boolean force);

    double getPrice(@NotNull TradeType type);

    void setPrice(@NotNull TradeType type, double price);

    double getBuyPrice();

    void setBuyPrice(double buyPrice);

    double getSellPrice();

    void setSellPrice(double sellPrice);

    double getFinalBuyPrice(@NotNull Player player);

    double getFinalBuyPrice(@NotNull Player player, int amount);

    double getFinalSellPrice(@NotNull Player player);

    double getFinalSellPrice(@NotNull Player player, int amount);

    double getFinalSellAllPrice(@NotNull Player player);

    double getFinalPrice(@NotNull TradeType tradeType);

    double getFinalPrice(@NotNull TradeType tradeType, int amount);

    double getFinalPrice(@NotNull TradeType tradeType, @Nullable Player player);

    double getFinalPrice(@NotNull TradeType tradeType, int amount, @Nullable Player player);



    int getUnitAmount();

    void delivery(@NotNull Player player, int count);

    void delivery(@NotNull Inventory inventory, int count);

    void take(@NotNull Player player, int count);

    void take(@NotNull Inventory inventory, int count);

    int count(@NotNull Player player);

    int countUnits(@NotNull Player player);

    int countUnits(@NotNull Inventory inventory);

    int countUnits(int amount);

    int count(@NotNull Inventory inventory);

    int countSpace(@NotNull Player player);

    int countSpace(@NotNull Inventory inventory);

    boolean hasSpace(@NotNull Player player);

    boolean hasSpace(@NotNull Inventory inventory);

    @NotNull PreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all);



    boolean isTradeable(@NotNull TradeType tradeType);

    boolean isBuyable();

    boolean isSellable();

    boolean hasBuyPrice();

    boolean hasSellPrice();

    @NotNull String getId();

    @NotNull Shop getShop();

    @NotNull ProductContent getContent();

    void setContent(@NotNull ProductContent content);

    @NotNull ItemStack getPreview();

    @NotNull ItemStack getPreviewOrPlaceholder();

    @NotNull Currency getCurrency();

    @NotNull Optional<Currency> currency();

    @NotNull String getCurrencyId();

    void setCurrencyId(@NotNull String currencyId);

    @NotNull ProductPricing getPricing();

    @NotNull PriceType getPricingType();

    void setPricing(@NotNull ProductPricing pricing);
}
