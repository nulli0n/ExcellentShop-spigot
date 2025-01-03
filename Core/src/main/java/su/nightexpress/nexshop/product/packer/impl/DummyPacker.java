package su.nightexpress.nexshop.product.packer.impl;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.product.handler.impl.DummyHandler;
import su.nightexpress.nexshop.product.packer.AbstractItemPacker;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.function.UnaryOperator;

public class DummyPacker extends AbstractItemPacker<DummyHandler> {

    public DummyPacker(@NotNull DummyHandler handler) {
        super(handler);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        // Do not save/override anything in the config.
    }

    @Override
    public boolean isDummy() {
        return true;
    }

    @Override
    @Nullable
    public String serialize() {
        return "{0}";
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return s -> s;
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        return new ItemStack(INVALID_ITEM);
    }

    @Override
    public boolean isItemMatches(@NotNull ItemStack item) {
        return false;
    }

    @Override
    public int countSpace(@NotNull Inventory inventory) {
        return 0;
    }

    @Override
    public void delivery(@NotNull Inventory inventory, int count) {

    }

    @Override
    public void take(@NotNull Inventory inventory, int count) {

    }

    @Override
    public int count(@NotNull Inventory inventory) {
        return 0;
    }

    @Override
    public int getUnitAmount() {
        return 1;
    }
}
