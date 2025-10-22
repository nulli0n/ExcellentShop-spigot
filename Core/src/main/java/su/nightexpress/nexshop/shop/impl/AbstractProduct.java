package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nexshop.product.content.ProductContent;
import su.nightexpress.nexshop.product.content.impl.EmptyContent;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nexshop.product.price.ProductPricing;
import su.nightexpress.nexshop.product.price.impl.FlatPricing;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;

import java.util.Optional;
import java.util.function.UnaryOperator;

public abstract class AbstractProduct<S extends AbstractShop<?>> implements Product {

    protected final String id;

    protected S              shop;
    protected ProductContent content;
    protected String         currencyId;
    protected ProductPricing pricing;

    protected double buyPrice;
    protected double sellPrice;

    public AbstractProduct(@NotNull String id, @NotNull S shop) {
        this.id = id.toLowerCase();
        this.shop = shop;
        this.setCurrencyId("null");
        this.setPricing(FlatPricing.of(-1D, -1D));
        this.setContent(EmptyContent.VALUE);
        this.updatePrice(false);
    }

    @NotNull
    protected abstract UnaryOperator<String> replaceExplicitPlaceholders(@Nullable Player player);

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return this.replacePlaceholders(null);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(@Nullable Player player) {
        return this.replaceExplicitPlaceholders(player);
    }

    @Override
    public boolean isValid() {
        return this.content.isValid();
    }

    @Override
    public void updatePrice(boolean force) {
        ShopAPI.dataAccess(dataManager -> {
            PriceData priceData = dataManager.getPriceDataOrCreate(this);
            if (force) {
                priceData.setExpired();
            }

            this.pricing.updatePrice(this, priceData);
        });
    }

    @Override
    public double getPrice(@NotNull TradeType type) {
        return switch (type) {
            case BUY -> this.getBuyPrice();
            case SELL -> this.getSellPrice();
        };
    }

    @Override
    public void setPrice(@NotNull TradeType type, double price) {
        double floored = this.currency().map(currency -> currency.floorIfNeeded(price)).orElse(price);
        switch (type) {
            case BUY -> this.setBuyPrice(floored);
            case SELL -> this.setSellPrice(floored);
        }
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
    public double getFinalBuyPrice(@NotNull Player player) {
        return this.getFinalPrice(TradeType.BUY, player);
    }

    @Override
    public double getFinalBuyPrice(@NotNull Player player, int amount) {
        return this.getFinalPrice(TradeType.BUY, amount, player);
    }

    @Override
    public double getFinalSellPrice(@NotNull Player player) {
        return this.getFinalPrice(TradeType.SELL, player);
    }

    @Override
    public double getFinalSellPrice(@NotNull Player player, int amount) {
        return this.getFinalPrice(TradeType.SELL, amount, player);
    }

    @Override
    public double getFinalSellAllPrice(@NotNull Player player) {
        int amountHas = this.countUnits(player);
        int amountCan = this.getAvailableAmount(player, TradeType.SELL);

        int balance = Math.min((amountCan < 0 ? amountHas : amountCan), amountHas);
        double price = balance * this.getFinalSellPrice(player);

        return Math.max(price, 0);
    }

    @Override
    public double getFinalPrice(@NotNull TradeType tradeType) {
        return this.getFinalPrice(tradeType, null);
    }

    @Override
    public double getFinalPrice(@NotNull TradeType tradeType, int amount) {
        return this.getFinalPrice(tradeType, amount, null);
    }

    @Override
    public double getFinalPrice(@NotNull TradeType tradeType, @Nullable Player player) {
        return this.getFinalPrice(tradeType, 1, player);
    }

    @Override
    public double getFinalPrice(@NotNull TradeType tradeType, int amount, @Nullable Player player) {
        double price = this.getPrice(tradeType);
        double boostedPrice = this.applyPriceModifiers(tradeType, price, player);

        return this.currency().map(currency -> currency.floorIfNeeded(boostedPrice)).orElse(boostedPrice) * amount;
    }

    protected abstract double applyPriceModifiers(@NotNull TradeType type, double currentPrice, @Nullable Player player);


    @Override
    public boolean isTradeable(@NotNull TradeType tradeType) {
        return tradeType == TradeType.BUY ? this.isBuyable() : this.isSellable();
    }

    @Override
    public boolean isBuyable() {
        return this.shop.isBuyingAllowed() && this.hasBuyPrice();
    }

    @Override
    public boolean isSellable() {
        if (!(this.content instanceof ItemContent)) return false;
        if (!this.shop.isSellingAllowed()) return false;
        if (!this.hasSellPrice()) return false;

        // Don't allow to sell items with sell price greater than buy one.
        if (this.isBuyable()) {
            return this.getSellPrice() <= this.getBuyPrice();
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
    public int getUnitAmount() {
        return this.content.getUnitAmount();
    }

    @Override
    public void delivery(@NotNull Player player, int count) {
        this.delivery(player.getInventory(), count);
    }

    @Override
    public void delivery(@NotNull Inventory inventory, int count) {
        this.content.delivery(inventory, count);
    }

    @Override
    public void take(@NotNull Player player, int count) {
        this.take(player.getInventory(), count);
    }

    @Override
    public void take(@NotNull Inventory inventory, int count) {
        this.content.take(inventory, count);
    }

    @Override
    public int count(@NotNull Player player) {
        return this.count(player.getInventory());
    }

    @Override
    public int countUnits(@NotNull Player player) {
        return this.countUnits(player.getInventory());
    }

    @Override
    public int countUnits(@NotNull Inventory inventory) {
        return this.countUnits(this.count(inventory));
    }

    @Override
    public int countUnits(int amount) {
        return UnitUtils.amountToUnits(this, amount);
    }

    @Override
    public int count(@NotNull Inventory inventory) {
        return this.content.count(inventory);
    }

    @Override
    public int countSpace(@NotNull Player player) {
        return this.countSpace(player.getInventory());
    }

    @Override
    public int countSpace(@NotNull Inventory inventory) {
        return this.content.countSpace(inventory);
    }

    @Override
    public boolean hasSpace(@NotNull Player player) {
        return this.hasSpace(player.getInventory());
    }

    @Override
    public boolean hasSpace(@NotNull Inventory inventory) {
        return this.content.hasSpace(inventory);
    }



    @Override
    @NotNull
    public S getShop() {
        return this.shop;
    }

    @Override
    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    @Override
    public ProductContent getContent() {
        return this.content;
    }

    @Override
    public void setContent(@NotNull ProductContent content) {
        this.content = content;
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return this.content.getPreview();
    }

    @Override
    @NotNull
    public ItemStack getPreviewOrPlaceholder() {
        return this.isValid() ? this.getPreview() : ShopUtils.getInvalidProductPlaceholder();
    }

    @Override
    @NotNull
    public ProductPricing getPricing() {
        return this.pricing;
    }

    @Override
    @NotNull
    public PriceType getPricingType() {
        return this.pricing.getType();
    }

    @Override
    public void setPricing(@NotNull ProductPricing pricing) {
        this.pricing = pricing;
    }

    @Override
    @NotNull
    public Optional<Currency> currency() {
        return Optional.ofNullable(EconomyBridge.getCurrency(this.currencyId));
    }

    @Override
    @NotNull
    public Currency getCurrency() {
        return EconomyBridge.getCurrencyOrDummy(this.currencyId);
    }

    @Override
    @NotNull
    public String getCurrencyId() {
        return this.currencyId;
    }

    @Override
    public void setCurrencyId(@NotNull String currencyId) {
        this.currencyId = currencyId;
    }
}
