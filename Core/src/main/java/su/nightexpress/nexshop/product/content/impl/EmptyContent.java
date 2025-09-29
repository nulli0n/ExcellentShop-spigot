package su.nightexpress.nexshop.product.content.impl;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.product.content.ContentType;
import su.nightexpress.nexshop.product.content.ProductContent;
import su.nightexpress.nightcore.config.FileConfig;

public class EmptyContent extends ProductContent {

    public static final EmptyContent VALUE = new EmptyContent();

    public EmptyContent() {
        super(ContentType.EMPTY);
    }

    @Override
    @NotNull
    public String getName() {
        return "empty";
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return new ItemStack(Material.STRUCTURE_VOID);
    }

    @Override
    public boolean hasSpace(@NotNull Inventory inventory) {
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
        return 0;
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {

    }
}
