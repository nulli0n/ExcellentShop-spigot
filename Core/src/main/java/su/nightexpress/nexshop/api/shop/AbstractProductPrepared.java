package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.function.UnaryOperator;

public abstract class AbstractProductPrepared<S extends IProduct> implements IProductPrepared {

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
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(this.getProduct().getPreview()))
            .replace(Placeholders.GENERIC_AMOUNT, String.valueOf(this.getAmount()))
            .replace(Placeholders.GENERIC_TYPE, this.getShop().plugin().getLangManager().getEnum(this.getTradeType()))
            .replace(Placeholders.GENERIC_PRICE, this.getProduct().getCurrency().format(this.getPrice()))
            ;
    }

    @Override
    @NotNull
    public S getProduct() {
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

    @Override
    public double getPrice() {
        IProduct product = this.getProduct();
        double price = product.getPricer().getPrice(this.getTradeType());
        return price * this.getAmount();
    }

    @Override
    public boolean trade(@NotNull Player player, boolean isAll) {
        return this.getTradeType() == TradeType.BUY ? this.buy(player) : this.sell(player, isAll);
    }
}
