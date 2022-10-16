package su.nightexpress.nexshop.api.currency;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public interface ICurrencyConfig {

    boolean isEnabled();

    @NotNull String getId();

    @NotNull String getName();

    @NotNull String getFormat();

    @NotNull DecimalFormat getNumberFormat();

    @NotNull ItemStack getIcon();
}
