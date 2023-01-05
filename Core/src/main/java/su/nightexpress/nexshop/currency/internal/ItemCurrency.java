package su.nightexpress.nexshop.currency.internal;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.api.currency.AbstractCurrency;
import su.nightexpress.nexshop.api.currency.MultiCurrency;
import su.nightexpress.nexshop.currency.config.CurrencyItemConfig;

public class ItemCurrency extends AbstractCurrency implements MultiCurrency {

    public ItemCurrency(@NotNull CurrencyItemConfig config) {
        super(config);
    }

    @Override
    @NotNull
    public CurrencyItemConfig getConfig() {
        return (CurrencyItemConfig) super.getConfig();
    }

    @NotNull
    public ItemStack getItem() {
        return this.getConfig().getItem();
    }

    @Override
    public double getBalance(@NotNull Player player) {
        return Math.floor(PlayerUtil.countItem(player, this.getItem()));
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        PlayerUtil.addItem(player, this.getItem(), (int) amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        PlayerUtil.takeItem(player, this.getItem(), (int) amount);
    }
}
