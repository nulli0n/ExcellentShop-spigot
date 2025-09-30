package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.exception.ProductLoadException;
import su.nightexpress.nexshop.product.content.ContentType;
import su.nightexpress.nexshop.product.content.ContentTypes;
import su.nightexpress.nexshop.product.content.ProductContent;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nexshop.product.price.ProductPricing;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.currency.CurrencyId;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class ChestProduct extends AbstractProduct<ChestShop> {

    private long quantity;
    private int  cachedAmount;
    private int  cachedSpace;

    public ChestProduct(@NotNull String id, @NotNull ChestShop shop) {
        super(id, shop);
    }

    @NotNull
    public static ChestProduct load(@NotNull FileConfig config, @NotNull String path, @NotNull String id, @NotNull ChestShop shop) throws ProductLoadException {
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

        ChestProduct product = new ChestProduct(id, shop);
        product.setCurrencyId(currencyId);
        product.setContent(content);
        product.setPricing(ProductPricing.read(config, path + ".Price"));
        product.setQuantity(infQuantity);
        return product;
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".InfiniteStorage.Quantity", this.quantity);

        config.set(path + ".Type", this.content.type().name());
        this.content.write(config, path);

        config.set(path + ".Currency", this.getCurrency().getInternalId());
        this.getPricing().write(config, path + ".Price");
    }

    @Override
    @NotNull
    protected UnaryOperator<String> replaceExplicitPlaceholders(@Nullable Player player) {
        return Placeholders.forChestProduct(this, player);
    }

    @Override
    @NotNull
    public ChestPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all) {
        return new ChestPreparedProduct(player, this, buyType, all);
    }

    @Override
    public int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType) {
        return this.countStock(tradeType, null);
    }

    @Override
    public boolean isAvailable(@NotNull Player player) {
        return this.shop.isActive() && (this.isBuyable() || this.isSellable());
    }

    @Override
    protected double applyPriceModifiers(@NotNull TradeType type, double currentPrice, @Nullable Player player) {
        return currentPrice;
    }

    public int countUnitCapacity() {
        return calcCapacity(this.countUnitSpace(), this.countUnitAmount());
    }

    public int countUnitAmount() {
        if (this.shop.isAdminShop()) return StockValues.UNLIMITED;
        if (ChestUtils.isInfiniteStorage()) return this.countUnits((int) this.quantity);
        if (!this.shop.isChunkLoaded()) return this.cachedAmount;

        Inventory inventory = this.shop.inventory();
        if (inventory == null) return 0; // Shop container is not valid anymore.

        return this.countUnits(inventory);
    }

    public int countUnitSpace() {
        if (this.shop.isAdminShop()) return StockValues.UNLIMITED;
        if (ChestUtils.isInfiniteStorage()) return StockValues.UNLIMITED;
        if (!this.shop.isChunkLoaded()) return this.cachedSpace;

        Inventory inventory = this.shop.inventory();
        if (inventory == null) return 0; // Shop container is not valid anymore.

        return this.countUnits(this.countSpace(inventory));
    }

    @Override
    public int countStock(@NotNull TradeType type, @Nullable UUID playerId) {
        return type == TradeType.BUY ? this.countUnitAmount() : this.countUnitSpace();
    }

    @Override
    public boolean consumeStock(@NotNull TradeType type, int amount, @Nullable UUID playerId) {
        if (this.shop.isInactive()) return false;
        if (!(this.getContent() instanceof ItemContent typing)) return false;

        amount = Math.abs(amount * this.getUnitAmount());

        if (ChestUtils.isInfiniteStorage()) {
            this.setQuantity(this.quantity - amount);
            this.updateStockCache();
            return true;
        }

        Inventory inventory = this.shop.inventory();
        if (inventory == null) return false; // Shop container is not valid anymore.

        ShopUtils.takeItem(inventory, typing::isItemMatches, amount);
        this.updateStockCache();
        return true;
    }

    @Override
    public boolean storeStock(@NotNull TradeType type, int units, @Nullable UUID playerId) {
        if (this.shop.isInactive()) return false;
        if (!this.isValid()) return false;
        if (!(this.getContent() instanceof ItemContent itemContent)) return false;

        int amount = Math.abs(units * this.getUnitAmount());

        if (ChestUtils.isInfiniteStorage()) {
            this.setQuantity(this.quantity + amount);
            this.updateStockCache();
            return true;
        }

        Inventory inventory = this.shop.inventory();
        if (inventory == null) return false; // Shop container is not valid anymore.

        if (ShopUtils.addItem(inventory, itemContent.getItem(), amount)) {
            this.updateStockCache();
            return true;
        }

        return false;
    }

    @Override
    public boolean restock(@NotNull TradeType type, boolean force, @Nullable UUID playerId) {
        return false;
    }

    @Override
    public long getRestockDate(@Nullable UUID playerId) {
        return 0L;
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
        this.setCachedAmount(this.countUnitAmount());
        this.setCachedSpace(this.countUnitSpace());
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
        return calcCapacity(this.getCachedSpace(), this.getCachedAmount());
    }

    private static int calcCapacity(int space, int amount) {
        if (space <= StockValues.UNLIMITED || amount <= StockValues.UNLIMITED) return StockValues.UNLIMITED;

        return amount + space;
    }
}
