package su.nightexpress.nexshop.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.function.UnaryOperator;

public interface IProductPrepared extends IPlaceholder {

    String PLACEHOLDER_ITEM   = "%item%";
    String PLACEHOLDER_AMOUNT = "%amount%";
    String PLACEHOLDER_TYPE   = "%type%";

    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> str
                .replace(PLACEHOLDER_ITEM, ItemUT.getItemName(this.getShopProduct().getPreview()))
                .replace(PLACEHOLDER_AMOUNT, String.valueOf(this.getAmount()))
                .replace(PLACEHOLDER_TYPE, this.getShop().plugin().lang().getEnum(this.getTradeType()))
                .replace(IShopCurrency.PLACEHOLDER_PRICE, this.getShopProduct().getCurrency().format(this.getPrice()))
                ;
    }

    @NotNull
    default IShop getShop() {
        return this.getShopProduct().getShop();
    }

    @NotNull
    IShopProduct getShopProduct();

    @NotNull
    TradeType getTradeType();

    int getAmount();

    void setAmount(int amount);

    default double getPrice() {
        IShopProduct product = this.getShopProduct();
        double price = product.getPricer().getPrice(this.getTradeType(), true);
        return price * this.getAmount();
    }

    default boolean trade(@NotNull Player player, boolean isAll) {
        return this.getTradeType() == TradeType.BUY ? this.buy(player) : this.sell(player, isAll);
    }

    boolean buy(@NotNull Player player);

    boolean sell(@NotNull Player player, boolean isAll);
}
