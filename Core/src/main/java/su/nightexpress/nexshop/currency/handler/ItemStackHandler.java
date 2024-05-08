package su.nightexpress.nexshop.currency.handler;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.currency.CurrencyHandler;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Players;

public class ItemStackHandler implements CurrencyHandler {

    private ItemStack item;

    public ItemStackHandler(@NotNull ItemStack item) {
        this.setItem(item);
    }

    @Nullable
    public static ItemStackHandler read(@NotNull FileConfig config, @NotNull String path) {
        if (!path.endsWith(".") && !path.isEmpty()) path += ".";

        ItemStack item;
        if (config.contains(path + "Item.Material")) {
            item = config.getItem(path + "Item");
        }
        else {
            item = config.getItemEncoded(path + "Item");
        }

        return item == null ? null : new ItemStackHandler(item);
    }

    public void write(@NotNull FileConfig cfg, @NotNull String path) {
        cfg.remove(path + ".Item");
        if (this.getItem().hasItemMeta()) {
            cfg.setItemEncoded(path + ".Item", this.getItem());
        }
        else cfg.setItem(path + ".Item", this.getItem());
    }

    @Override
    @NotNull
    public String getDefaultName() {
        return ItemUtil.getItemName(this.getItem());
    }

    @Override
    @NotNull
    public ItemStack getDefaultIcon() {
        return this.getItem();
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
        return Players.countItem(player, this.getItem());
    }

    @Override
    public void give(@NotNull Player player, double amount) {
        Players.addItem(player, this.getItem(), (int) amount);
    }

    @Override
    public void take(@NotNull Player player, double amount) {
        Players.takeItem(player, this.getItem(), (int) amount);
    }
}
