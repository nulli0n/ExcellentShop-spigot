package su.nightexpress.nexshop.api.currency;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.Placeholders;

public interface ICurrency extends Placeholder {

    @NotNull ICurrencyConfig getConfig();

    @NotNull
    default String getId() {
        return this.getConfig().getId();
    }

    @NotNull
    default String getFormat() {
        return this.replacePlaceholders().apply(this.getConfig().getFormat());
    }

    @NotNull
    default String format(double price) {
        return this.getFormat().replace(Placeholders.GENERIC_PRICE, this.getConfig().getNumberFormat().format(price));
    }

    @NotNull
    default ItemStack getIcon() {
        ItemStack icon = this.getConfig().getIcon();
        ItemUtil.replace(icon, this.replacePlaceholders());
        return icon;
    }

    double getBalance(@NotNull Player player);

    void give(@NotNull Player player, double amount);

    void take(@NotNull Player player, double amount);
}
