package su.nightexpress.nexshop.api.currency;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;

import java.util.function.UnaryOperator;

public interface IShopCurrency extends IPlaceholder {

    String PLACEHOLDER_PRICE   = "%price%";
    String PLACEHOLDER_BALANCE = "%balance%";
    String PLACEHOLDER_NAME    = "%currency_name%";
    String PLACEHOLDER_ID = "%currency_id%";

    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return str -> str
                .replace(PLACEHOLDER_NAME, this.getName())
                .replace(PLACEHOLDER_ID, this.getId())
                ;
    }

    boolean hasOfflineSupport();

    @NotNull
    String getId();

    @NotNull
    String getName();

    @NotNull
    String format(double price);

    double getBalance(@NotNull OfflinePlayer player);

    void give(@NotNull OfflinePlayer player, double amount);

    void take(@NotNull OfflinePlayer player, double amount);
}
