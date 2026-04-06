package su.nightexpress.excellentshop.product;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeStatus;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.price.PriceData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.product.content.EmptyContent;
import su.nightexpress.excellentshop.product.price.FlatPricing;
import su.nightexpress.excellentshop.shop.AbstractShop;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractProduct<S extends AbstractShop<?>> implements Product {

    protected final UUID globalId;
    protected final String id;

    protected S              shop;
    protected ProductContent content;
    protected String         currencyId;
    protected ProductPricing pricing;

    protected double buyPrice;
    protected double sellPrice;

    protected boolean buyMenuAllowed;
    protected boolean sellMenuAllowed;

    public AbstractProduct(@NonNull UUID globalId, @NonNull String id, @NonNull S shop) {
        this.globalId = globalId;
        this.id = id.toLowerCase();
        this.shop = shop;
        this.setCurrencyId("null");
        this.setPricing(FlatPricing.of(-1D, -1D));
        this.setContent(EmptyContent.VALUE);
        this.setBuyMenuAllowed(true);
        this.setSellMenuAllowed(true);
    }

    @Override
    public boolean isValid() {
        return this.content.isValid();
    }

    @Override
    @NonNull
    public TradeStatus getTradeStatus() {
        boolean buyable = this.isBuyable();
        boolean sellable = this.isSellable();

        if (buyable && sellable) return TradeStatus.BUYABLE_AND_SELLABLE;
        if (buyable) return TradeStatus.BUYABLE;
        if (sellable) return TradeStatus.SELLABLE;

        return TradeStatus.UNAVAILABLE;
    }

    @Override
    public int getMaxAffordableUnitAmount(@NonNull Player player) {
        Currency currency = this.getCurrency();
        double balance = currency.queryBalance(player);

        double unitPrice = this.getFinalBuyPrice(player, 1);

        return (int) Math.floor(balance / unitPrice);
    }

    @Override
    public int getMaxBuyableUnitAmount(@NonNull Player player, @NonNull Inventory inventory) {
        if (!this.canTrade(player)) return 0;
        if (!this.isBuyable()) return 0;

        int maxPhysical = UnitUtils.amountToUnits(this, this.countSpace(inventory));
        int stock = this.getStock();

        int buyLimit = this.getBuyLimit();
        if (buyLimit >= 0) {
            int purchases = this.getLimitData(player).getPurchases();
            int left = Math.max(0, buyLimit - purchases);
            if (left == 0) return 0;

            if (stock < 0 || left <= stock) {
                return left;
            }
        }

        int maxUnits = stock >= 0 ? Math.min(stock, maxPhysical) : maxPhysical;
        if (maxUnits <= 0) return maxUnits;

        int maxByWealth = this.getMaxAffordableUnitAmount(player);

        return Math.min(maxByWealth, maxUnits);
    }

    @Override
    public int getMaxSellableUnitAmount(@NonNull Player player, @NonNull Inventory inventory) {
        if (!this.canTrade(player)) return 0;
        if (!this.isSellable()) return 0;

        int maxPhysicalUnits = this.countUnits(inventory);
        int maxLimitUnits = -1;

        int sellLimit = this.getSellLimit();
        if (sellLimit >= 0) {
            int sales = this.getLimitData(player).getSales();
            int left = Math.max(0, sellLimit - sales);
            if (left == 0) return 0;

            maxLimitUnits = left;
        }

        int maxSpaceUnits = this.getSpace();

        return UnitUtils.findSmallestPositive(maxPhysicalUnits, maxLimitUnits, maxSpaceUnits).orElse(0);
    }

    @Override
    public void onSuccessfulTransaction(@NonNull ECompletedTransaction transaction, int units) {
        TradeType tradeType = transaction.type();
        Inventory inventory = transaction.userInventory();

        if (tradeType == TradeType.BUY) {
            this.delivery(inventory, units);
        }
        else {
            this.take(inventory, units);
        }

        this.handleSuccessfulTransaction(transaction, units);

        PriceData priceData = this.getPriceData();
        this.pricing.handleTransaction(transaction, this, units, priceData);
        priceData.countTransaction(tradeType, units);
        priceData.markDirty();
    }

    protected abstract void handleSuccessfulTransaction(@NonNull ECompletedTransaction transaction, int units);

    @Override
    public void updatePrice(boolean force) {
        PriceData priceData = this.getPriceData();
        if (force) {
            priceData.setExpired();
            priceData.markDirty();
        }
        this.pricing.updatePrice(this, priceData);
    }

    @Override
    public double getPrice(@NonNull TradeType type) {
        return switch (type) {
            case BUY -> this.getBuyPrice();
            case SELL -> this.getSellPrice();
        };
    }

    @Override
    public void setPrice(@NonNull TradeType type, double price) {
        double floored = price == ProductPricing.DISABLED ? price : this.currency().map(currency -> currency.floorIfNeeded(price)).orElse(price);
        switch (type) {
            case BUY -> this.setBuyPrice(floored);
            case SELL -> this.setSellPrice(floored);
        }
    }

    @Override
    public double getPriceTrending(@NonNull TradeType tradeType) {
        double price = this.getPrice(tradeType);
        double average = this.pricing.getAveragePrice(tradeType);

        return (price / average) - 1D;
    }

    @Override
    public double getBuyPrice() {
        return this.buyPrice;
    }

    @Override
    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    @Override
    public double getSellPrice() {
        return this.sellPrice;
    }

    @Override
    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    @Override
    public double getFinalBuyPrice(@NonNull Player player) {
        return this.getFinalPrice(TradeType.BUY, player);
    }

    @Override
    public double getFinalBuyPrice(@NonNull Player player, int units) {
        return this.getFinalPrice(TradeType.BUY, units, player);
    }

    @Override
    public double getFinalSellPrice(@NonNull Player player) {
        return this.getFinalPrice(TradeType.SELL, player);
    }

    @Override
    public double getFinalSellPrice(@NonNull Player player, int units) {
        return this.getFinalPrice(TradeType.SELL, units, player);
    }

    @Override
    public double getFinalSellAllPrice(@NonNull Player player) {
        int maxSellableUnitAmount = this.getMaxSellableUnitAmount(player, player.getInventory());
        if (maxSellableUnitAmount == 0) return 0D;

        return maxSellableUnitAmount * this.getFinalSellPrice(player);
    }

    @Override
    public double getFinalPrice(@NonNull TradeType tradeType) {
        return this.getFinalPrice(tradeType, null);
    }

    @Override
    public double getFinalPrice(@NonNull TradeType tradeType, int units) {
        return this.getFinalPrice(tradeType, units, null);
    }

    @Override
    public double getFinalPrice(@NonNull TradeType tradeType, @Nullable Player player) {
        return this.getFinalPrice(tradeType, 1, player);
    }

    @Override
    public double getFinalPrice(@NonNull TradeType tradeType, int units, @Nullable Player player) {
        double price = this.getPrice(tradeType);
        double boostedPrice = this.applyPriceModifiers(tradeType, price, player);

        return this.currency().map(currency -> currency.floorIfNeeded(boostedPrice)).orElse(boostedPrice) * units;
    }

    protected abstract double applyPriceModifiers(@NonNull TradeType type, double currentPrice, @Nullable Player player);


    @Override
    public boolean isTradeable(@NonNull TradeType tradeType) {
        return tradeType == TradeType.BUY ? this.isBuyable() : this.isSellable();
    }

    @Override
    public boolean isBuyable() {
        return this.shop.isBuyingAllowed() && this.hasBuyPrice();
    }

    @Override
    public boolean isSellable() {
        if (!this.content.isPhysical()) return false;
        if (!this.shop.isSellingAllowed()) return false;
        if (!this.hasSellPrice()) return false;

        // Don't allow to sell items with sell price greater than buy one.
        if (this.isBuyable()) {
            return this.getSellPrice() <= this.getBuyPrice(); // TODO On log error in problem collector
        }

        return true;
    }

    @Override
    public boolean hasBuyPrice() {
        return this.getBuyPrice() > 0D;
    }

    @Override
    public boolean hasSellPrice() {
        return this.getSellPrice() >= 0D;
    }


    @Override
    public int getUnitSize() {
        return this.content.getUnitAmount();
    }

    @Override
    public int getMaxStackSize() {
        return this.content.getMaxStackSize();
    }

    @Override
    public void delivery(@NonNull Player player, int count) {
        this.delivery(player.getInventory(), count);
    }

    @Override
    public void delivery(@NonNull Inventory inventory, int count) {
        this.content.delivery(inventory, count);
    }

    @Override
    public void take(@NonNull Player player, int count) {
        this.take(player.getInventory(), count);
    }

    @Override
    public void take(@NonNull Inventory inventory, int count) {
        this.content.take(inventory, count);
    }

    @Override
    public int count(@NonNull Player player) {
        return this.count(player.getInventory());
    }

    @Override
    public int countUnits(@NonNull Player player) {
        return this.countUnits(player.getInventory());
    }

    @Override
    public int countUnits(@NonNull Inventory inventory) {
        return this.countUnits(this.count(inventory));
    }

    @Override
    public int countUnits(int amount) {
        return UnitUtils.amountToUnits(this, amount);
    }

    @Override
    public int count(@NonNull Inventory inventory) {
        return this.content.count(inventory);
    }

    @Override
    public int countSpace(@NonNull Player player) {
        return this.countSpace(player.getInventory());
    }

    @Override
    public int countSpace(@NonNull Inventory inventory) {
        return this.content.countSpace(inventory);
    }

    @Override
    public boolean hasSpace(@NonNull Player player) {
        return this.hasSpace(player.getInventory());
    }

    @Override
    public boolean hasSpace(@NonNull Inventory inventory) {
        return this.content.hasSpace(inventory);
    }


    @Override
    @NonNull
    public UUID getGlobalId() {
        return this.globalId;
    }

    @Override
    @NonNull
    public S getShop() {
        return this.shop;
    }

    @Override
    @NonNull
    public String getId() {
        return this.id;
    }

    @NonNull
    @Override
    public ProductContent getContent() {
        return this.content;
    }

    @Override
    public void setContent(@NonNull ProductContent content) {
        this.content = content;
    }

    @Override
    @NonNull
    public ItemStack getPreview() {
        return this.content.getPreview();
    }

    @Override
    @NonNull
    public ItemStack getEffectivePreview() {
        return this.isValid() ? this.getPreview() : ShopUtils.getInvalidProductPlaceholder();
    }

    @Override
    @NonNull
    public ProductPricing getPricing() {
        return this.pricing;
    }

    @Override
    @NonNull
    public PriceType getPricingType() {
        return this.pricing.getType();
    }

    @Override
    public void setPricing(@NonNull ProductPricing pricing) {
        this.pricing = pricing;
        this.updatePrice(false);
    }

    @Override
    @NonNull
    public Optional<Currency> currency() {
        return Optional.ofNullable(EconomyBridge.api().getCurrency(this.currencyId));
    }

    @Override
    @NonNull
    public Currency getCurrency() {
        return EconomyBridge.api().getCurrencyOrDummy(this.currencyId);
    }

    @Override
    @NonNull
    public String getCurrencyId() {
        return this.currencyId;
    }

    @Override
    public void setCurrencyId(@NonNull String currencyId) {
        this.currencyId = currencyId;
    }

    // TODO Editor Impl

    @Override
    public boolean isBuyMenuAllowed() {
        return this.buyMenuAllowed;
    }

    @Override
    public void setBuyMenuAllowed(boolean buyMenuAllowed) {
        this.buyMenuAllowed = buyMenuAllowed;
    }

    @Override
    public boolean isSellMenuAllowed() {
        return this.sellMenuAllowed;
    }

    @Override
    public void setSellMenuAllowed(boolean sellMenuAllowed) {
        this.sellMenuAllowed = sellMenuAllowed;
    }
}
