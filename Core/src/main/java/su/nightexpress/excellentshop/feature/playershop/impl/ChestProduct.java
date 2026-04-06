package su.nightexpress.excellentshop.feature.playershop.impl;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.limit.LimitData;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.excellentshop.api.product.stock.StockData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.nexshop.exception.ProductLoadException;
import su.nightexpress.excellentshop.product.ContentType;
import su.nightexpress.excellentshop.product.ContentTypes;
import su.nightexpress.excellentshop.product.ProductContent;
import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.excellentshop.product.ProductPricing;
import su.nightexpress.excellentshop.feature.playershop.ChestUtils;
import su.nightexpress.excellentshop.product.AbstractProduct;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.currency.CurrencyId;
import su.nightexpress.nightcore.util.placeholder.PlaceholderResolver;

import java.util.UUID;

public class ChestProduct extends AbstractProduct<ChestShop> {

    private final ChestStock stock;

    private long quantity;
    private int  cachedAmount;
    private int  cachedSpace;

    public ChestProduct(@NonNull UUID globalId, @NonNull ChestShop shop) {
        super(globalId, globalId.toString(), shop);
        this.stock = new ChestStock(shop, this);
    }

    @NonNull
    public static ChestProduct load(@NonNull FileConfig config, @NonNull String path, @NonNull UUID id, @NonNull ChestShop shop) throws ProductLoadException {
        String currencyId = CurrencyId.reroute(config.getString(path + ".Currency", CurrencyId.VAULT));

        String itemOld = config.getString(path + ".Reward.Item");
        if (itemOld != null && !itemOld.isBlank()) {
            config.remove(path + ".Reward.Item");
            config.set(path + ".Content.Item", itemOld);
        }

        if (!config.contains(path + ".Type")) {
            String handlerId = config.getString(path + ".Handler", "bukkit_item");
            if (handlerId.equalsIgnoreCase("bukkit_command")) {
                config.set(path + ".Type", ContentType.COMMAND.name());
            }
            else if (handlerId.equalsIgnoreCase("bukkit_item")) {
                config.set(path + ".Type", ContentType.ITEM.name());
            }
        }

        ContentType contentType = config.getEnum(path + ".Type", ContentType.class, ContentType.ITEM);
        ProductContent content = ContentTypes.read(contentType, config, path);
        if (content == null) throw new ProductLoadException("Invalid item data");

        int infQuantity = config.getInt(path + ".InfiniteStorage.Quantity");

        // ---- PATCH FOR SHOPS FROM PRE 4.23 VERSIONS: ----
        // Force enable NBT comparison for ChestShop items
        if (content instanceof ItemContent itemContent) {
            itemContent.setCompareNbt(true);
        }

        ChestProduct product = new ChestProduct(id, shop);
        product.setCurrencyId(currencyId);
        product.setContent(content);
        product.setPricing(ProductPricing.read(config, path + ".Price"));
        product.setQuantity(infQuantity);
        return product;
    }

    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".InfiniteStorage.Quantity", this.quantity);

        config.set(path + ".Type", this.content.type().name());
        this.content.write(config, path);

        config.set(path + ".Currency", this.getCurrency().getInternalId());
        this.getPricing().write(config, path + ".Price");
    }

    @Override
    @NonNull
    public PlaceholderResolver placeholders() {
        return ShopPlaceholders.CHEST_PRODUCT.resolver(this);
    }

    @Override
    public void invalidateData() {
        // Nothing to do here
    }

    @Override
    public void handleSuccessfulTransaction(@NonNull ECompletedTransaction transaction, int units) {
        if (transaction.type() == TradeType.BUY) {
            this.stock.consume(units);
        }
        else {
            this.stock.store(units);
        }

        this.shop.markDirty(); // Save product quantity changes.
    }

    @Override
    @NonNull
    public StockData getStockData() {
        return this.stock;
    }

    @Override
    @NonNull
    public LimitData getLimitData(@NonNull Player player) {
        return LimitData.EMPTY;
    }

    @Override
    @NonNull
    public PriceData getPriceData() {
        return PriceData.EMPTY;
    }

    @Override
    public boolean canTrade(@NonNull Player player) {
        return this.shop.isAccessible();
    }

    @Override
    protected double applyPriceModifiers(@NonNull TradeType type, double currentPrice, @Nullable Player player) {
        return currentPrice;
    }

    @Override
    public int getStock() {
        return this.stock.getStock();
    }

    @Override
    public int getCapacity() {
        return calcCapacity(this.getSpace(), this.getStock());
    }

    @Override
    public int getSpace() {
        if (this.shop.isAdminShop()) return -1;
        if (ChestUtils.isInfiniteStorage()) return -1;
        if (!this.shop.isAccessible()) return this.cachedSpace;

        return this.shop.getInventory().map(inv -> this.countUnits(this.countSpace(inv))).orElse(0);
    }

    @Override
    public int getTradeLimit(@NonNull TradeType type) {
        return -1;
    }

    @Override
    public int getBuyLimit() {
        return -1;
    }

    @Override
    public int getSellLimit() {
        return -1;
    }

    /**
     *
     * @return Product quantity for Infinite Storage system.
     */
    public long getQuantity() {
        return this.quantity;
    }

    /**
     * Sets product's quantity for Infinite Storage system.
     * @param quantity Product quantity.
     */
    public void setQuantity(long quantity) {
        this.quantity = Math.max(0, Math.abs(quantity));
    }

    public void updateStockCache() {
        this.setCachedAmount(this.getStock());
        this.setCachedSpace(this.getSpace());
    }

    /**
     * Get cached product's stock value. Used in shop holograms to bypass async access of tile entities.
     * @return Product's stock value.
     */
    public int getCachedAmount() {
        return this.cachedAmount;
    }

    /**
     * Set cached product's stock value. Used in shop holograms to bypass async access of tile entities.
     * @param cachedAmount Cached product's stock value.
     */
    public void setCachedAmount(int cachedAmount) {
        this.cachedAmount = cachedAmount;
    }

    public int getCachedSpace() {
        return this.cachedSpace;
    }

    public void setCachedSpace(int cachedSpace) {
        this.cachedSpace = cachedSpace;
    }

    public int getCachedCapacity() {
        return calcCapacity(this.cachedSpace, this.cachedAmount);
    }

    private static int calcCapacity(int space, int amount) {
        if (space < 0 || amount < 0) return -1;

        return amount + space;
    }
}
