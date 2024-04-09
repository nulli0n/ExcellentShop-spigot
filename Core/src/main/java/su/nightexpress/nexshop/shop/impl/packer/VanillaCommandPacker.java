package su.nightexpress.nexshop.shop.impl.packer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.nexshop.api.shop.packer.CommandPacker;
import su.nightexpress.nexshop.shop.impl.AbstractProductPacker;
import su.nightexpress.nexshop.shop.virtual.Placeholders;

import java.util.ArrayList;
import java.util.List;

public class VanillaCommandPacker extends AbstractProductPacker implements CommandPacker {

    private List<String> commands;

    public VanillaCommandPacker() {
        this(new ItemStack(Material.COMMAND_BLOCK), new ArrayList<>());
    }

    public VanillaCommandPacker(@NotNull ItemStack preview, @NotNull List<String> commands) {
        super(preview);
        this.setCommands(commands);

        this.placeholderMap
            .add(Placeholders.PRODUCT_COMMANDS, () -> String.join("\n", this.getCommands()));
    }

    @Override
    public boolean load(@NotNull JYML cfg, @NotNull String path) {
        ItemStack preview = cfg.getItemEncoded(path + ".Content.Preview");
        if (preview == null) preview = new ItemStack(Material.COMMAND_BLOCK);

        List<String> commands = cfg.getStringList(path + ".Content.Commands");

        this.setPreview(preview);
        this.setCommands(commands);
        return true;
    }

    @Override
    protected void writeAdditional(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Content.Commands", this.getCommands());
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

    /*@Override
    public boolean hasSpace(@NotNull Inventory inventory) {
        return true;
    }*/

    @Override
    public int countSpace(@NotNull Inventory inventory) {
        return -1;
    }
}
