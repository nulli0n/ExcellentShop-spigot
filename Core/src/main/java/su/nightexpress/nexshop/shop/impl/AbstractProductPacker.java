package su.nightexpress.nexshop.shop.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;

public abstract class AbstractProductPacker implements ProductPacker {

    protected final PlaceholderMap placeholderMap;

    protected ItemStack preview;

    public AbstractProductPacker(@NotNull ItemStack preview) {
        this.setPreview(preview);

        this.placeholderMap = new PlaceholderMap();
    }

    @Override
    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.remove("Content");
        cfg.setItemEncoded(path + ".Content.Preview", this.getPreview());
        this.writeAdditional(cfg, path);
    }

    protected abstract void writeAdditional(@NotNull JYML cfg, @NotNull String path);

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
