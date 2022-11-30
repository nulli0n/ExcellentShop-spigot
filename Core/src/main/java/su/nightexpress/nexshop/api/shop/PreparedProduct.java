package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.function.UnaryOperator;

public abstract class PreparedProduct<P extends Product<P, ?, ?>> implements IPlaceholder {

    private final P         product;
    private final TradeType buyType;
    private       int       amount;

    public PreparedProduct(@NotNull P product, @NotNull TradeType buyType) {
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

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(amount, 1);
    }

    public double getPrice() {
        Product<?, ?, ?> product = this.getProduct();
        double price = product.getPricer().getPrice(this.getTradeType());
        return price * this.getAmount();
    }

    public boolean trade(@NotNull Player player, boolean isAll) {
        return this.getTradeType() == TradeType.BUY ? this.buy(player) : this.sell(player, isAll);
    }

    public abstract boolean buy(@NotNull Player player);

    public abstract boolean sell(@NotNull Player player, boolean isAll);
}
