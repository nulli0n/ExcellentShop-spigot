package su.nightexpress.nexshop.product.handler.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.product.ProductHandlerRegistry;
import su.nightexpress.nexshop.product.handler.AbstractProductHandler;
import su.nightexpress.nexshop.product.packer.impl.BukkitItemPacker;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemNbt;

public class BukkitItemHandler extends AbstractProductHandler implements ItemHandler {

    public static final String NAME = "bukkit_item";

    public BukkitItemHandler(@NotNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    @NotNull
    public ItemPacker readPacker(@NotNull FileConfig config, @NotNull String path) {
        String serialized;

        if (config.contains(path + ".Content")) {
            String tagString = config.getString(path + ".Content.Item", "null");
            boolean respectMeta = config.getBoolean(path + ".Item_Meta_Enabled");

            serialized = tagString + DELIMITER + respectMeta;
        }
        else {
            serialized = config.getString(path + ".Data", "null");
        }

        ItemPacker packer = this.deserialize(serialized);
        if (packer.isDummy()) {
            this.plugin.error("[" + NAME + "] Invalid item data string '" + serialized + "'. Caused by '" + config.getFile().getAbsolutePath() + "' -> '" + path + "'.");
        }

        return packer;
    }

    @Override
    @NotNull
    public ItemPacker deserialize(@NotNull String str) {
        String[] split = str.split(DELIMITER);
        String tagString = split[0];
        boolean respectMeta = split.length >= 2 && Boolean.parseBoolean(split[1]);

        ItemStack itemStack = tagString.contains("{") ? ItemNbt.fromTagString(tagString) : ItemNbt.decompress(tagString);
        if (itemStack == null || itemStack.getType().isAir()) return ProductHandlerRegistry.getDummyHandler().createPacker();

        return new BukkitItemPacker(this, itemStack, respectMeta);
    }

    @Override
    @NotNull
    public BukkitItemPacker createPacker(@NotNull ItemStack itemStack) {
        return new BukkitItemPacker(this, itemStack, itemStack.hasItemMeta());
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return !item.getType().isAir();
    }
}
