package su.nightexpress.nexshop.api;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.type.TradeType;

public abstract class AbstractProductPrepared<S extends IShopProduct> implements IProductPrepared {

    private final S         product;
    private final TradeType buyType;

    private int amount;

    public AbstractProductPrepared(@NotNull S product, @NotNull TradeType buyType) {
        this.product = product;
        this.buyType = buyType;

        this.setAmount(1);
    }

    @Override
    @NotNull
    public S getShopProduct() {
        return this.product;
    }

    @Override
    @NotNull
    public TradeType getTradeType() {
        return this.buyType;
    }

    @Override
    public int getAmount() {
        return this.amount;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = Math.max(amount, 1);
    }
}
