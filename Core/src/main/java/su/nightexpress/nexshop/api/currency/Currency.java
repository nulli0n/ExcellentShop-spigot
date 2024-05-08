package su.nightexpress.nexshop.api.currency;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.currency.handler.ItemStackHandler;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.placeholder.Placeholder;

public interface Currency extends Placeholder {

    @NotNull
    default String formatValue(double price) {
        if (!this.decimalsAllowed()) {
            price = Math.floor(price);
        }
        return NumberUtil.format(price);
    }

    @NotNull
    default String format(double price) {
        /*if (EngineUtils.hasPlugin("nightcore")) {
            format = NightMessage.asLegacy(format);
        }*/
        return (this.replacePlaceholders().apply(this.getFormat()
            .replace(Placeholders.GENERIC_AMOUNT, this.formatValue(price))
            .replace(Placeholders.GENERIC_PRICE, this.formatValue(price))
            .replace(Placeholders.GENERIC_NAME, this.getName())
        ));
    }

    /*default double getBalance(@NotNull Player player) {
        return this.getHandler().getBalance(player);
    }

    default void give(@NotNull Player player, double amount) {
        this.getHandler().give(player, amount);
    }

    default void take(@NotNull Player player, double amount) {
        this.getHandler().take(player, amount);
    }*/

    @Nullable
    default CurrencyOfflineHandler getOfflineHandler() {
        if (this instanceof CurrencyOfflineHandler handler) return handler;
        if (this.getHandler() instanceof CurrencyOfflineHandler handler) return handler;

        return null;
    }

    default boolean decimalsAllowed() {
        return !(this.getHandler() instanceof ItemStackHandler);
    }

    @NotNull CurrencyHandler getHandler();

    @NotNull String getId();

    @NotNull String getName();

    @NotNull String getFormat();

    @NotNull ItemStack getIcon();
}
