package su.nightexpress.nexshop.product.handler.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.product.handler.AbstractProductHandler;
import su.nightexpress.nexshop.product.packer.impl.BukkitCommandPacker;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.List;

public class BukkitCommandHandler extends AbstractProductHandler {

    public static final String NAME = "bukkit_command";

    public BukkitCommandHandler(@NotNull ShopPlugin plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @NotNull
    public BukkitCommandPacker createPacker() {
        return new BukkitCommandPacker(this);
    }

    @Override
    @NotNull
    public BukkitCommandPacker createPacker(@NotNull FileConfig config, @NotNull String path) {
        ItemStack preview = config.getItemEncoded(path + ".Content.Preview");
        if (preview == null) preview = new ItemStack(Material.COMMAND_BLOCK);

        List<String> commands = config.getStringList(path + ".Content.Commands");

        return new BukkitCommandPacker(this, preview, commands);
    }
}
