package su.nightexpress.nexshop.product.packer.impl;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.product.handler.impl.DummyHandler;
import su.nightexpress.nexshop.product.packer.AbstractItemPacker;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.function.UnaryOperator;

public class DummyPacker extends AbstractItemPacker<DummyHandler> {

    public DummyPacker(@NotNull DummyHandler handler) {
        super(handler);
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {

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
    @NotNull
    public ItemStack getPreview() {
        return this.getItem();
    }

    @Override
    public void setPreview(@NotNull ItemStack preview) {

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
