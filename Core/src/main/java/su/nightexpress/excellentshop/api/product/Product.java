package su.nightexpress.excellentshop.api.product;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.limit.LimitData;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.excellentshop.api.product.stock.StockData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.product.PriceType;
import su.nightexpress.excellentshop.product.ProductContent;
import su.nightexpress.excellentshop.product.ProductPricing;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.util.placeholder.PlaceholderResolvable;

import java.util.Optional;
import java.util.UUID;

public interface Product extends PlaceholderResolvable {

    void onSuccessfulTransaction(@NonNull ECompletedTransaction transaction, int units);

    /**
     * Performs a check to determine if product is available for buying/selling (e.g. present in the shop, is in rotation).
     * @param player Player who about to buy/sell product.
     * @return Whether product is available for buying/selling.
     */
    boolean canTrade(@NonNull Player player);

    int getMaxBuyableUnitAmount(@NonNull Player player, @NonNull Inventory inventory);

    int getMaxAffordableUnitAmount(@NonNull Player player);

    int getMaxSellableUnitAmount(@NonNull Player player, @NonNull Inventory inventory);

    boolean isValid();

    void invalidateData();



    void updatePrice(boolean force);

    double getPrice(@NonNull TradeType type);

    void setPrice(@NonNull TradeType type, double price);

    double getBuyPrice();

    void setBuyPrice(double buyPrice);

    double getSellPrice();

    void setSellPrice(double sellPrice);

    double getFinalBuyPrice(@NonNull Player player);

    double getFinalBuyPrice(@NonNull Player player, int units);

    double getFinalSellPrice(@NonNull Player player);

    double getFinalSellPrice(@NonNull Player player, int units);

    double getFinalSellAllPrice(@NonNull Player player);

    double getFinalPrice(@NonNull TradeType tradeType);

    double getFinalPrice(@NonNull TradeType tradeType, int units);

    double getFinalPrice(@NonNull TradeType tradeType, @Nullable Player player);

    double getFinalPrice(@NonNull TradeType tradeType, int units, @Nullable Player player);

    double getPriceTrending(@NonNull TradeType tradeType);



    int getUnitSize();

    int getMaxStackSize();

    void delivery(@NonNull Player player, int count);

    void delivery(@NonNull Inventory inventory, int count);

    void take(@NonNull Player player, int count);

    void take(@NonNull Inventory inventory, int count);

    int count(@NonNull Player player);

    int countUnits(@NonNull Player player);

    int countUnits(@NonNull Inventory inventory);

    int countUnits(int amount);

    int count(@NonNull Inventory inventory);

    int countSpace(@NonNull Player player);

    int countSpace(@NonNull Inventory inventory);

    boolean hasSpace(@NonNull Player player);

    boolean hasSpace(@NonNull Inventory inventory);


    int getStock();

    int getCapacity();

    int getSpace();

    int getTradeLimit(@NonNull TradeType type);

    int getBuyLimit();

    int getSellLimit();


    boolean isTradeable(@NonNull TradeType tradeType);

    boolean isBuyable();

    boolean isSellable();

    boolean hasBuyPrice();

    boolean hasSellPrice();

    @NonNull TradeStatus getTradeStatus();

    @NonNull StockData getStockData();

    @NonNull PriceData getPriceData();

    @NonNull LimitData getLimitData(@NonNull Player player);

    @NonNull UUID getGlobalId();

    @NonNull String getId();

    @NonNull Shop getShop();

    @NonNull ProductContent getContent();

    void setContent(@NonNull ProductContent content);

    @NonNull ItemStack getPreview();

    @NonNull ItemStack getEffectivePreview();

    @NonNull Currency getCurrency();

    @NonNull Optional<Currency> currency();

    @NonNull String getCurrencyId();

    void setCurrencyId(@NonNull String currencyId);

    @NonNull ProductPricing getPricing();

    @NonNull PriceType getPricingType();

    void setPricing(@NonNull ProductPricing pricing);

    boolean isBuyMenuAllowed();

    void setBuyMenuAllowed(boolean buyMenuAllowed);

    boolean isSellMenuAllowed();

    void setSellMenuAllowed(boolean sellMenuAllowed);
}
