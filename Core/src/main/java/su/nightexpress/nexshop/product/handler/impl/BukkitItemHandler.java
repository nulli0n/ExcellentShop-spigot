package su.nightexpress.nexshop.product.handler.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.product.handler.AbstractProductHandler;
import su.nightexpress.nexshop.product.packer.impl.BukkitItemPacker;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

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
    public BukkitItemPacker createPacker(@NotNull FileConfig config, @NotNull String path) {
        ItemStack item = config.getItemEncoded(path + ".Content.Item");
        if (item == null) {
            item = new ItemStack(Material.AIR);
            this.logBadItem("", config, path);
        }

        ItemStack preview = config.getItemEncoded(path + ".Content.Preview");
        if (preview == null) preview = new ItemStack(item);

        boolean meta = ConfigValue.create(path + ".Item_Meta_Enabled", item.hasItemMeta()).read(config);

        return new BukkitItemPacker(this, item, preview, meta);
    }

    @Override
    @Nullable
    public BukkitItemPacker createPacker(@NotNull ItemStack itemStack) {
        return new BukkitItemPacker(this, itemStack, itemStack, itemStack.hasItemMeta());
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return !item.getType().isAir();
    }
}
