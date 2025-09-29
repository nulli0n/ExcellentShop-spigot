package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.product.PreparedProduct;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.util.UnitUtils;

import java.util.function.UnaryOperator;

public abstract class AbstractPreparedProduct<P extends Product> implements PreparedProduct {

    protected final Player         player;
    protected final P              product;
    protected final TradeType      tradeType;
    protected final boolean        all;

    private Inventory inventory;
    private int units;
    private double multiplier;
    private boolean silent;

    public AbstractPreparedProduct(@NotNull Player player, @NotNull P product, @NotNull TradeType tradeType, boolean all) {
        this.player = player;
        this.product = product;
        this.tradeType = tradeType;
        this.multiplier = 1D;
        this.all = all;

        this.setInventory(player.getInventory());
        this.setUnits(1);
        this.setSilent(false);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.PREPARED_PRODUCT.replacer(this);
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Shop getShop() {
        return this.getProduct().getShop();
    }

    @NotNull
    public P getProduct() {
        return this.product;
    }

    @NotNull
    public TradeType getTradeType() {
        return this.tradeType;
    }

    public boolean isAll() {
        return all;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }

    @Override
    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public int getUnits() {
        return this.units;
    }

    public void setUnits(int units) {
        this.units = Math.max(units, 1);
    }

    public int getAmount() {
        return UnitUtils.unitsToAmount(this.product, this.units);
    }

    public double getPrice() {
        Product product = this.getProduct();
        double price = product.getFinalPrice(this.tradeType, this.player) * this.multiplier;
        return price * this.units;
    }

    @NotNull
    public Transaction trade() {
        return this.getTradeType() == TradeType.BUY ? this.buy() : this.sell();
    }

    @NotNull
    protected abstract Transaction buy();

    @NotNull
    protected abstract Transaction sell();
}
