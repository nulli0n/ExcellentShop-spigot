package su.nightexpress.nexshop.shop.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

public abstract class AbstractProductPacker implements ProductPacker {

    protected final PlaceholderMap placeholderMap;

    protected ItemStack preview;

    public AbstractProductPacker(@NotNull ItemStack preview) {
        this.setPreview(preview);

        this.placeholderMap = new PlaceholderMap();
    }

    @Override
    public void write(@NotNull FileConfig cfg, @NotNull String path) {
        cfg.remove("Content");
        cfg.setItemEncoded(path + ".Content.Preview", this.getPreview());
        this.writeAdditional(cfg, path);
    }

    protected abstract void writeAdditional(@NotNull FileConfig cfg, @NotNull String path);

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return new ItemStack(this.preview);
    }

    @Override
    public void setPreview(@NotNull ItemStack preview) {
        this.preview = new ItemStack(preview);
    }
}
