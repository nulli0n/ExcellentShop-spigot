package su.nightexpress.nexshop.product.handler.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
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
    public DummyPacker readPacker(@NotNull FileConfig config, @NotNull String path) {
        return this.createPacker();
    }

    @NotNull
    public DummyPacker createPacker() {
        return new DummyPacker(this);
    }

    @Override
    @NotNull
    public DummyPacker createPacker(@NotNull ItemStack itemStack) {
        return this.createPacker();
    }

    @Override
    @NotNull
    public DummyPacker deserialize(@NotNull String str) {
        return this.createPacker();
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return false;
    }
}
