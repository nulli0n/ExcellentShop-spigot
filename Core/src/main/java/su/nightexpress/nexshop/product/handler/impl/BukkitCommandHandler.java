package su.nightexpress.nexshop.product.handler.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.product.handler.AbstractProductHandler;
import su.nightexpress.nexshop.product.packer.impl.BukkitCommandPacker;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemNbt;
import su.nightexpress.nightcore.util.Lists;

import java.util.List;

public class BukkitCommandHandler extends AbstractProductHandler {

    public static final String NAME = "bukkit_command";
    public static final String CMD_DELIMITER = " @ ";

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
    public BukkitCommandPacker readPacker(@NotNull FileConfig config, @NotNull String path) {
        String serialized;

        if (config.contains(path + ".Content")) {
            String previewTag = config.getString(path + ".Content.Preview", "null");
            String commands = String.join(CMD_DELIMITER, config.getStringList(path + ".Content.Commands"));

            serialized = previewTag + DELIMITER + commands;
        }
        else {
            serialized = config.getString(path + ".Data", "null");
        }

        return this.deserialize(serialized);
    }

    @Override
    @NotNull
    public BukkitCommandPacker deserialize(@NotNull String str) {
        String[] split = str.split(DELIMITER);

        String previewTag = split[0];
        String[] commandsSplit = split.length >= 2 ? split[1].split(CMD_DELIMITER) : new String[0];

        ItemStack preview = previewTag.contains("{") ? ItemNbt.fromTagString(previewTag) : ItemNbt.decompress(previewTag);
        if (preview == null) preview = new ItemStack(Material.COMMAND_BLOCK);

        List<String> commands = Lists.newList(commandsSplit);

        return new BukkitCommandPacker(this, preview, commands);
    }
}
