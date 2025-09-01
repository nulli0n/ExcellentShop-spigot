package su.nightexpress.nexshop.product.type.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.product.ProductType;
import su.nightexpress.nexshop.api.shop.product.typing.CommandTyping;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.util.ErrorHandler;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemNbt;
import su.nightexpress.nightcore.util.ItemTag;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class CommandProductType implements CommandTyping {

    private ItemStack    preview;
    private List<String> commands;

    public CommandProductType() {
        this(new ItemStack(Material.COMMAND_BLOCK), new ArrayList<>());
    }

    public CommandProductType(@NotNull ItemStack preview, @NotNull List<String> commands) {
        this.setCommands(commands);
        this.setPreview(preview);
    }

    @NotNull
    public static CommandProductType read(@NotNull FileConfig config, @NotNull String path) {
        if (config.contains(path + ".Content.Preview")) {
            String tagString = String.valueOf(config.getString(path + ".Content.Preview"));

            ItemTag tag = new ItemTag(tagString, -1);
            ItemStack itemStack = ItemNbt.fromTag(tag);
            if (itemStack == null) {
                ErrorHandler.configError("Could not update itemstack '" + tagString + "'!", config, path);
            }

            config.remove(path + ".Content.Preview");
            config.set(path + ".PreviewTag", new ItemTag(tagString, Version.getCurrent().getDataVersion()));
        }

        ItemTag tag = ItemTag.read(config, path + ".PreviewTag");
        List<String> commands = config.getStringList(path + ".Content.Commands");

        ItemStack preview = ItemNbt.fromTag(tag);
        if (preview == null) preview = new ItemStack(Material.COMMAND_BLOCK);

        return new CommandProductType(preview, commands);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".PreviewTag", ItemNbt.getTag(this.preview));
        config.set(path + ".Content.Commands", this.commands);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return Placeholders.COMMAND_TYPING.replacer(this);
    }

    @Override
    @NotNull
    public ProductType type() {
        return ProductType.COMMAND;
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
