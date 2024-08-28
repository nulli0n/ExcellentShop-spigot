package su.nightexpress.nexshop.product.handler.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.product.handler.AbstractProductHandler;
import su.nightexpress.nexshop.product.packer.impl.DummyPacker;
import su.nightexpress.nightcore.config.FileConfig;

public class DummyHandler extends AbstractProductHandler implements ItemHandler {

    public DummyHandler(@NotNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getName() {
        return "dummy";
    }

    @Override
    @NotNull
    public ProductPacker createPacker(@NotNull FileConfig config, @NotNull String path) {
        return new DummyPacker(this);
    }

    @Override
    @Nullable
    public ItemPacker createPacker(@NotNull ItemStack itemStack) {
        return new DummyPacker(this);
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return false;
    }
}
