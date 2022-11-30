package su.nightexpress.nexshop.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.PreparedProduct;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.api.type.TradeType;

public abstract class ShopPurchaseEvent<P extends Product<P, ?, ?>> extends Event implements Cancellable {

    protected Player             player;
    protected Shop<?, P>         shop;
    protected Product<P, ?, ?>   product;
    protected PreparedProduct<P> prepared;
    protected TradeType          tradeType;
    protected Result             result;

    public ShopPurchaseEvent(@NotNull Player player, @NotNull PreparedProduct<P> prepared) {
        this.player = player;
        this.shop = prepared.getShop();
        this.product = prepared.getProduct();
        this.prepared = prepared;
        this.tradeType = prepared.getTradeType();
        this.result = Result.SUCCESS;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }

    @NotNull
    public Shop<?, P> getShop() {
        return this.shop;
    }

    @NotNull
    public Product<P, ?, ?> getProduct() {
        return this.product;
    }

    @NotNull
    public PreparedProduct<P> getPrepared() {
        return this.prepared;
    }

    @NotNull
    public TradeType getTradeType() {
        return this.tradeType;
    }

    @NotNull
    public Result getResult() {
        return this.result;
    }

    public void setResult(@NotNull Result result) {
        this.result = result;
    }

    @Override
    public boolean isCancelled() {
        return this.getResult() != Result.SUCCESS;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.setResult(Result.FAILURE);
    }

    public enum Result {
        TOO_EXPENSIVE,
        NOT_ENOUGH_ITEMS,
        OUT_OF_STOCK,
        OUT_OF_MONEY,
        OUT_OF_SPACE,
        SUCCESS,
        FAILURE,
    }
}
