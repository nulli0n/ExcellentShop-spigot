package su.nightexpress.nexshop.shop.chest.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.impl.AbstractProduct;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ChestProduct extends AbstractProduct<ChestShop> {

    private long quantity;
    private int cachedStock;

    public ChestProduct(@NotNull ShopPlugin plugin,
                        @NotNull String id,
                        @NotNull ChestShop shop,
                        @NotNull Currency currency,
                        @NotNull ProductTyping typing) {
        super(plugin, id, shop, currency, typing);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        this.writeQuantity(config, path);

        config.set(path + ".Type", this.type.type().name());
        this.type.write(config, path);

        config.set(path + ".Currency", this.getCurrency().getInternalId());
        this.getPricer().write(config, path + ".Price");
    }

    public void writeQuantity(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".InfiniteStorage.Quantity", this.quantity);
    }

    @Override
    @NotNull
    protected UnaryOperator<String> replaceExplicitPlaceholders(@Nullable Player player) {
        return Placeholders.forChestProduct(this, player);
    }

    @Override
    @NotNull
    public ChestPreparedProduct getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all) {
        return new ChestPreparedProduct(this.plugin, player, this, buyType, all);
    }

    @Override
    public int getAvailableAmount(@NotNull Player player, @NotNull TradeType tradeType) {
        //return this.shop.getStock().count(this, tradeType, player);
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

    @Override
    public int countStock(@NotNull TradeType type, @Nullable UUID playerId) {
        if (this.shop.isInactive()) return 0;
        if (this.shop.isAdminShop()) return StockValues.UNLIMITED;
        if (!(this.getType() instanceof PhysicalTyping typing)) return StockValues.UNLIMITED;

        double unitAmount = this.getUnitAmount();

        if (ChestUtils.isInfiniteStorage()) {
            return type == TradeType.SELL ? StockValues.UNLIMITED : (int) Math.floor(this.getQuantity() / unitAmount);
        }

        Inventory inventory = this.shop.getInventory();
        ItemStack[] contents = inventory.getContents();
        double totalAmount;

        // For buying (from player's perspective) return product unit amount based on similar inventory slots only.
        if (type == TradeType.BUY) {
            totalAmount = Stream.of(contents).mapToInt(content -> content != null && typing.isItemMatches(content) ? content.getAmount() : 0).sum();
        }
        // For selling (from player's perspective) return product unit amount based on free or similar inventory slots.
        else {
            ItemStack item = typing.getItem();
            totalAmount = Stream.of(contents).mapToInt(content -> {
                if (content == null || content.getType().isAir()) return item.getMaxStackSize();
                if (typing.isItemMatches(content)) return Math.max(0, content.getMaxStackSize() - content.getAmount());

                return 0;
            }).sum();
        }

        int result = (int) Math.floor(totalAmount / unitAmount);
        this.setCachedStock(result);

        return result;
    }

    @Override
    public boolean consumeStock(@NotNull TradeType type, int amount, @Nullable UUID playerId) {
        if (this.shop.isInactive()) return false;
        if (!(this.getType() instanceof PhysicalTyping typing)) return false;

        amount = Math.abs(amount * this.getUnitAmount());

        if (ChestUtils.isInfiniteStorage()) {
            this.setQuantity(this.getQuantity() - amount);
            return true;
        }

        Inventory inventory = this.shop.getInventory();
        ShopUtils.takeItem(inventory, typing::isItemMatches, amount);
        this.updateStockCache();
        return true;
    }

    @Override
    public boolean storeStock(@NotNull TradeType type, int amount, @Nullable UUID playerId) {
        if (this.shop.isInactive()) return false;
        if (!(this.getType() instanceof PhysicalTyping typing)) return false;

        amount = Math.abs(amount * this.getUnitAmount());

        if (ChestUtils.isInfiniteStorage()) {
            this.setQuantity(this.getQuantity() + amount);
            return true;
        }

        Inventory inventory = this.shop.getInventory();
        if (ShopUtils.addItem(inventory, typing.getItem(), amount)) {
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
        this.setCachedStock(this.countStock(TradeType.BUY, null));
    }

    /**
     * Get cached product's stock value. Used in shop holograms to bypass async access of tile entities.
     * @return Product's stock value.
     */
    public int getCachedStock() {
        return this.cachedStock;
    }

    /**
     * Set cached product's stock value. Used in shop holograms to bypass async access of tile entities.
     * @param cachedStock Cached product's stock value.
     */
    public void setCachedStock(int cachedStock) {
        this.cachedStock = cachedStock;
    }
}
