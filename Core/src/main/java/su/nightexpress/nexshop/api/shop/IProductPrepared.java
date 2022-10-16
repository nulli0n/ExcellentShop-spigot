package su.nightexpress.nexshop.api.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nightexpress.nexshop.api.type.TradeType;

public interface IProductPrepared extends IPlaceholder {

    @NotNull
    default IShop getShop() {
        return this.getProduct().getShop();
    }

    @NotNull IProduct getProduct();

    @NotNull TradeType getTradeType();

    int getAmount();

    void setAmount(int amount);

    double getPrice();

    boolean trade(@NotNull Player player, boolean isAll);

    boolean buy(@NotNull Player player);

    boolean sell(@NotNull Player player, boolean isAll);
}
