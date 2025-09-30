package su.nightexpress.nexshop.product.content.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.product.content.ContentType;
import su.nightexpress.nexshop.product.content.ProductContent;
import su.nightexpress.nexshop.util.ErrorHandler;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemTag;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.Version;

import java.util.ArrayList;
import java.util.List;

public class CommandContent extends ProductContent {

    private ItemStack    preview;
    private List<String> commands;

    public CommandContent() {
        this(new ItemStack(Material.COMMAND_BLOCK), new ArrayList<>());
    }

    public CommandContent(@NotNull ItemStack preview, @NotNull List<String> commands) {
        super(ContentType.COMMAND);
        this.setCommands(commands);
        this.setPreview(preview);
    }

    @NotNull
    public static CommandContent read(@NotNull FileConfig config, @NotNull String path) {
        if (config.contains(path + ".Content.Preview")) {
            String tagString = String.valueOf(config.getString(path + ".Content.Preview"));

            ItemTag tag = new ItemTag(tagString, -1);
            ItemStack itemStack = tag.getItemStack();
            if (itemStack == null) {
                ErrorHandler.configError("Could not update itemstack '" + tagString + "'!", config, path);
            }

            config.remove(path + ".Content.Preview");
            config.set(path + ".PreviewTag", new ItemTag(tagString, Version.getCurrent().getDataVersion()));
        }

        ItemTag tag = ItemTag.read(config, path + ".PreviewTag");
        List<String> commands = config.getStringList(path + ".Content.Commands");

        ItemStack preview = tag.getItemStack();
        if (preview == null) preview = new ItemStack(Material.COMMAND_BLOCK);

        return new CommandContent(preview, commands);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".PreviewTag", ItemTag.of(this.preview));
        config.set(path + ".Content.Commands", this.commands);
    }

    @Override
    @NotNull
    public String getName() {
        return Lang.PRODUCT_TYPES.getLocalized(this.type());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return new ItemStack(this.preview);
    }

    public void setPreview(@NotNull ItemStack preview) {
        this.preview = new ItemStack(preview);
    }

    public boolean hasCommands() {
        return !this.commands.isEmpty();
    }

    @NotNull
    public List<String> getCommands() {
        return this.commands;
    }

    public void setCommands(@NotNull List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void delivery(@NotNull Inventory inventory, int count) {
        if (!(inventory.getHolder() instanceof Player player)) return;

        this.getCommands().forEach(command -> {
            if (command.contains(Placeholders.GENERIC_AMOUNT)) {
                command = command.replace(Placeholders.GENERIC_AMOUNT, String.valueOf(count));
                Players.dispatchCommand(player, command);
            }
            else {
                for (int i = 0; i < count; i++) {
                    Players.dispatchCommand(player, command);
                }
            }
        });
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

    @Override
    public boolean hasSpace(@NotNull Inventory inventory) {
        return true;
    }
}
