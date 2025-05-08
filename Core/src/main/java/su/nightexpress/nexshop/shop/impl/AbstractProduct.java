package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.data.product.PriceData;
import su.nightexpress.nexshop.product.price.AbstractProductPricer;
import su.nightexpress.nexshop.product.price.impl.DynamicPricer;
import su.nightexpress.nexshop.product.price.impl.FlatPricer;
import su.nightexpress.nexshop.product.price.impl.FloatPricer;
import su.nightexpress.nexshop.util.UnitUtils;

import java.util.function.UnaryOperator;

public abstract class AbstractProduct<S extends AbstractShop<?>> implements Product {

    protected final ShopPlugin plugin;
    protected final String     id;

    protected S                     shop;
    protected ProductTyping         type;
    protected Currency              currency;
    protected AbstractProductPricer pricer;

    public AbstractProduct(@NotNull ShopPlugin plugin,
                           @NotNull String id,
                           @NotNull S shop,
                           @NotNull Currency currency,
                           @NotNull ProductTyping type) {
        this.plugin = plugin;
        this.id = id.toLowerCase();
        this.shop = shop;
        this.setCurrency(currency);
        this.setPricer(new FlatPricer());
        this.setType(type);
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
        var explicit = this.replaceExplicitPlaceholders(player);
        var packer = this.type.replacePlaceholders();
        var pricer = this.pricer.replacePlaceholders();

        return str -> {
            str = explicit.apply(str);
            str = packer.apply(str);
            str = pricer.apply(str);
            return str;
        };
    }

    @Override
    public boolean isValid() {
        return this.type.isValid();
    }

    @Override
    public void updatePrice() {
        this.updatePrice(false);
    }

    @Override
    public void updatePrice(boolean force) {
        if (this.pricer.getType() == PriceType.FLAT) return;
        if (this.pricer.getType() == PriceType.PLAYER_AMOUNT) return;

        PriceData priceData = this.plugin.getDataManager().getPriceDataOrCreate(this);

        if (priceData.isExpired() || force) {
            //plugin.debug("Flush prices for " + this.getId() + " product of " + shop.getId() + " shop.");
            double buyPrice = priceData.getLatestBuyPrice();
            double sellPrice = priceData.getLatestSellPrice();
            long expireDate = priceData.getExpireDate();

            if (this.pricer instanceof FloatPricer floatPricer) {
                buyPrice = floatPricer.rollPrice(TradeType.BUY);
                sellPrice = floatPricer.rollPrice(TradeType.SELL);
                expireDate = floatPricer.getClosestTimestamp();
            }
            else if (this.pricer instanceof DynamicPricer dynamicPricer) {
                double difference = priceData.getPurchases() - priceData.getSales();
                buyPrice = dynamicPricer.getAdjustedPrice(TradeType.BUY, difference);
                sellPrice = dynamicPricer.getAdjustedPrice(TradeType.SELL, difference);
                expireDate = -1L;
            }

            if (sellPrice > buyPrice && buyPrice >= 0) {
                sellPrice = buyPrice;
            }

            priceData.setLatestBuyPrice(buyPrice);
            priceData.setLatestSellPrice(sellPrice);
            priceData.setLatestUpdateDate(System.currentTimeMillis());
            priceData.setExpireDate(expireDate);
            priceData.setSaveRequired(true);
        }

        this.setPrice(TradeType.BUY, priceData.getLatestBuyPrice());
        this.setPrice(TradeType.SELL, priceData.getLatestSellPrice());
    }

    @Override
    public double getPriceBuy(@NotNull Player player) {
        return this.getPrice(TradeType.BUY, player);
    }

    @Override
    public double getPriceSell(@NotNull Player player) {
        return this.getPrice(TradeType.SELL, player);
    }

    @Override
    public double getPriceSellAll(@NotNull Player player) {
        int amountHas = this.countUnits(player);
        int amountCan = this.getAvailableAmount(player, TradeType.SELL);

        int balance = Math.min((amountCan < 0 ? amountHas : amountCan), amountHas);
        double price = balance * this.getPriceSell(player);

        return Math.max(price, 0);
    }

    @Override
    public double getPrice(@NotNull TradeType tradeType) {
        return this.getPrice(tradeType, null);
    }

    @Override
    public double getPrice(@NotNull TradeType tradeType, @Nullable Player player) {
        double price = this.pricer.getPrice(tradeType);

        price = this.applyPriceModifiers(tradeType, price, player);

        return this.currency.fineValue(price);
    }

    protected abstract double applyPriceModifiers(@NotNull TradeType type, double currentPrice, @Nullable Player player);

    @Override
    public void setPrice(@NotNull TradeType tradeType, double price) {
        this.pricer.setPrice(tradeType, this.currency.fineValue(price));
    }


    @Override
    public boolean isTradeable(@NotNull TradeType tradeType) {
        return tradeType == TradeType.BUY ? this.isBuyable() : this.isSellable();
    }

    @Override
    public boolean isBuyable() {
        return this.shop.isBuyingAllowed() && this.pricer.getBuyPrice() >= 0D;
    }

    @Override
    public boolean isSellable() {
        if (!(this.type instanceof PhysicalTyping)) return false;
        if (!this.shop.isSellingAllowed()) return false;

        double sellPrice = this.pricer.getSellPrice();
        if (sellPrice < 0D) {
            return false;
        }

        // Don't allow to sell items with sell price greater than buy one.
        if (this.isBuyable()) {
            return sellPrice <= this.pricer.getBuyPrice();
        }

        return true;
    }


    @Override
    public int getUnitAmount() {
        return this.type.getUnitAmount();
    }

    @Override
    public void delivery(@NotNull Player player, int count) {
        this.delivery(player.getInventory(), count);
    }

    @Override
    public void delivery(@NotNull Inventory inventory, int count) {
        this.type.delivery(inventory, count);
    }

    @Override
    public void take(@NotNull Player player, int count) {
        this.take(player.getInventory(), count);
    }

    @Override
    public void take(@NotNull Inventory inventory, int count) {
        this.type.take(inventory, count);
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
        return this.countUnits(this.count(inventory));// / this.getUnitAmount();
    }

    @Override
    public int countUnits(int amount) {
        return UnitUtils.amountToUnits(this, amount);
        //return amount / this.getUnitAmount();
    }

    @Override
    public int count(@NotNull Inventory inventory) {
        return this.type.count(inventory);
    }

    @Override
    public int countSpace(@NotNull Player player) {
        return this.countSpace(player.getInventory());
    }

    @Override
    public int countSpace(@NotNull Inventory inventory) {
        return this.type.countSpace(inventory);
    }

    @Override
    public boolean hasSpace(@NotNull Player player) {
        return this.hasSpace(player.getInventory());
    }

    @Override
    public boolean hasSpace(@NotNull Inventory inventory) {
        return this.type.hasSpace(inventory);
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
    public ProductTyping getType() {
        return this.type;
    }

    @Override
    public void setType(@NotNull ProductTyping type) {
        this.type = type;
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return this.type.getPreview();
    }

    @Override
    @NotNull
    public AbstractProductPricer getPricer() {
        return this.pricer;
    }

    @Override
    public void setPricer(@NotNull AbstractProductPricer pricer) {
        this.pricer = pricer;
    }

    @Override
    @NotNull
    public Currency getCurrency() {
        return this.currency;
    }

    @Override
    public void setCurrency(@NotNull Currency currency) {
        this.currency = currency;
    }
}
