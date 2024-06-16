package su.nightexpress.nexshop.shop.impl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.product.PreparedProduct;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

public abstract class AbstractPreparedProduct<P extends Product> implements PreparedProduct {

    protected final ShopPlugin     plugin;
    protected final Player         player;
    protected final P              product;
    protected final TradeType      tradeType;
    protected final boolean        all;
    protected final PlaceholderMap placeholderMap;

    private Inventory inventory;
    private int units;
    private boolean silent;

    public AbstractPreparedProduct(@NotNull ShopPlugin plugin, @NotNull Player player, @NotNull P product, @NotNull TradeType tradeType, boolean all) {
        this.plugin = plugin;
        this.player = player;
        this.product = product;
        this.tradeType = tradeType;
        this.all = all;
        this.placeholderMap = Placeholders.forPreparedProduct(this);

        this.setInventory(player.getInventory());
        this.setUnits(1);
        this.setSilent(false);
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
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

    public int getUnits() {
        return this.units;
    }

    public void setUnits(int units) {
        this.units = Math.max(units, 1);
    }

    public int getAmount() {
        return this.getProduct().getUnitAmount() * this.getUnits();
    }

    public double getPrice() {
        Product product = this.getProduct();
        double price = product.getPrice(this.getTradeType(), this.getPlayer());
        return price * this.getUnits();
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
