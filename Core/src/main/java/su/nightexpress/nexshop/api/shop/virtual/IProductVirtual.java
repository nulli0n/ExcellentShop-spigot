package su.nightexpress.nexshop.api.shop.virtual;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.IProduct;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.List;

public interface IProductVirtual extends IProduct {

    @Override
    @NotNull
    IShopVirtual getShop();

    @NotNull List<String> getCommands();

    void setCommands(@NotNull List<String> commands);

    default boolean hasCommands() {
        return !this.getCommands().isEmpty();
    }

    int getSlot();

    void setSlot(int slot);

    int getPage();

    void setPage(int page);

    long getStockResetTime(@NotNull Player player, @NotNull TradeType tradeType);

    default boolean isLimited(@NotNull TradeType tradeType) {
        return this.getLimitAmount(tradeType) >= 0;
    }

    default boolean isLimitExpirable(@NotNull TradeType tradeType) {
        return this.getLimitCooldown(tradeType) >= 0;
    }

    int getLimitAmount(@NotNull TradeType tradeType);

    void setLimitAmount(@NotNull TradeType tradeType, int amount);

    int getLimitCooldown(@NotNull TradeType tradeType);

    void setLimitCooldown(@NotNull TradeType tradeType, int cooldown);
}
