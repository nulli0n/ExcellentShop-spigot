package su.nightexpress.nexshop.product.packer.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.packer.CommandPacker;
import su.nightexpress.nexshop.product.packer.AbstractProductPacker;
import su.nightexpress.nexshop.product.handler.impl.BukkitCommandHandler;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class BukkitCommandPacker extends AbstractProductPacker<BukkitCommandHandler> implements CommandPacker {

    private ItemStack    preview;
    private List<String> commands;

    public BukkitCommandPacker(@NotNull BukkitCommandHandler handler) {
        this(handler, new ItemStack(Material.COMMAND_BLOCK), new ArrayList<>());
    }

    public BukkitCommandPacker(@NotNull BukkitCommandHandler handler, @NotNull ItemStack preview, @NotNull List<String> commands) {
        super(handler);
        this.setCommands(commands);
        this.setPreview(preview);
    }

    @Override
    protected void writeAdditional(@NotNull FileConfig config, @NotNull String path) {
        config.setItemEncoded(path + ".Content.Preview", this.getPreview());
        config.set(path + ".Content.Commands", this.getCommands());
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.BUKKIT_COMMAND_PACKER.replacer(this);
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

    @Override
    @NotNull
    public List<String> getCommands() {
        return this.commands;
    }

    @Override
    public void setCommands(@NotNull List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void delivery(@NotNull Inventory inventory, int count) {
        if (!(inventory.getHolder() instanceof Player player)) return;

        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        for (int i = 0; i < count; i++) {
            this.getCommands().forEach(command -> Bukkit.dispatchCommand(sender, Placeholders.forPlayer(player).apply(command)));
        }
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

    @Override
    public int countSpace(@NotNull Inventory inventory) {
        return -1;
    }
}
