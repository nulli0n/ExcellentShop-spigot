package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.util.TransactionResult;

public abstract class PreparedProduct<P extends Product<P, ?, ?>> implements Placeholder {

    protected final ExcellentShop  plugin;
    private final   Player         player;
    private final   P              product;
    private final   TradeType      buyType;
    private final   boolean        all;
    private final   PlaceholderMap placeholderMap;

    private Inventory inventory;
    private int units;

    public PreparedProduct(@NotNull Player player, @NotNull P product, @NotNull TradeType buyType, boolean all) {
        this.plugin = product.shop.plugin();
        this.player = player;
        this.product = product;
        this.buyType = buyType;
        this.all = all;

        this.setInventory(player.getInventory());
        this.setUnits(1);

        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.GENERIC_ITEM, () -> ItemUtil.getItemName(this.getProduct().getPreview()))
            .add(Placeholders.GENERIC_AMOUNT, () -> String.valueOf(this.getAmount()))
            .add(Placeholders.GENERIC_UNITS, () -> String.valueOf(this.getUnits()))
            .add(Placeholders.GENERIC_TYPE, () -> plugin.getLangManager().getEnum(this.getTradeType()))
            .add(Placeholders.GENERIC_PRICE, () -> this.getProduct().getCurrency().format(this.getPrice()))
        ;
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
    public Shop<?, P> getShop() {
        return this.getProduct().getShop();
    }

    @NotNull
    public P getProduct() {
        return this.product;
    }

    @NotNull
    public TradeType getTradeType() {
        return this.buyType;
    }

    public boolean isAll() {
        return all;
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
        Product<P, ?, ?> product = this.getProduct();
        double price = product.getPricer().getPrice(this.getPlayer(), this.getTradeType());
        return price * this.getUnits();
    }

    @NotNull
    public TransactionResult trade() {
        return this.getTradeType() == TradeType.BUY ? this.buy() : this.sell();
    }

    @NotNull
    protected abstract TransactionResult buy();

    @NotNull
    protected abstract TransactionResult sell();
}
