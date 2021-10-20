package su.nightexpress.nexshop.api.virtual;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.List;
import java.util.function.UnaryOperator;

public interface IShopVirtualProduct extends IShopProduct {

    String PLACEHOLDER_COMMANDS = "%product_commands%";
    String PLACEHOLDER_LIMIT_BUY_RESET  = "%product_limit_buy_reset%";
    String PLACEHOLDER_LIMIT_SELL_RESET = "%product_limit_sell_reset%";
    String PLACEHOLDER_LIMIT_BUY_AMOUNT = "%product_limit_buy_amount%";
    String PLACEHOLDER_LIMIT_SELL_AMOUNT = "%product_limit_sell_amount%";
    String PLACEHOLDER_LIMIT_BUY_COOLDOWN = "%product_limit_buy_cooldown%";
    String PLACEHOLDER_LIMIT_SELL_COOLDOWN = "%product_limit_sell_cooldown%";

    @Override
    @NotNull
    IShopVirtual getShop();

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> IShopProduct.super.replacePlaceholders().apply(str
                .replace(PLACEHOLDER_COMMANDS, String.join(DELIMITER_DEFAULT, this.getCommands()))
                .replace(PLACEHOLDER_LIMIT_BUY_AMOUNT, String.valueOf(this.getLimitAmount(TradeType.BUY)))
                .replace(PLACEHOLDER_LIMIT_BUY_COOLDOWN, TimeUT.formatTime(this.getLimitCooldown(TradeType.BUY) * 1000L))
                .replace(PLACEHOLDER_LIMIT_SELL_AMOUNT, String.valueOf(this.getLimitAmount(TradeType.SELL)))
                .replace(PLACEHOLDER_LIMIT_SELL_COOLDOWN, TimeUT.formatTime(this.getLimitCooldown(TradeType.SELL) * 1000L))
        );
    }

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders(@NotNull Player player) {
        long resBuy = this.getStockResetTime(player, TradeType.BUY);
        long resSell = this.getStockResetTime(player, TradeType.SELL);

        if (resBuy == 0) resBuy = this.getLimitCooldown(TradeType.BUY) * 1000L;
        if (resSell == 0) resSell = this.getLimitCooldown(TradeType.SELL) * 1000L;

        String never = this.getShop().plugin().lang().Other_Never.getMsg();
        String restockBuy = resBuy < 0 ? never : TimeUT.formatTime(resBuy);
        String restocSell = resSell < 0 ? never : TimeUT.formatTime(resSell);

        return str -> IShopProduct.super.replacePlaceholders(player).apply(str
                .replace(PLACEHOLDER_LIMIT_BUY_RESET, restockBuy)
                .replace(PLACEHOLDER_LIMIT_SELL_RESET, restocSell)
        );
    }

    @Override
    default boolean isBuyable() {
        if (this.getLimitAmount(TradeType.BUY) == 0) {
            return false;
        }
        return IShopProduct.super.isBuyable();
    }

    @Override
    default boolean isSellable() {
        if (this.getLimitAmount(TradeType.SELL) == 0) {
            return false;
        }
        return IShopProduct.super.isSellable();
    }

    @Override
    default boolean isEmpty() {
        return IShopProduct.super.isEmpty() && !this.hasCommands();
    }

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
