package su.nightexpress.nexshop.currency.handler;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;

public class ItemStackHandler implements CurrencyHandler {

    private ItemStack item;

    public ItemStackHandler(@NotNull ItemStack item) {
        this.setItem(item);
    }

    public void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
    }

    @NotNull
    public ItemStack getItem() {
        return item;
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
