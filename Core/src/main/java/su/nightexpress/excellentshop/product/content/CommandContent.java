package su.nightexpress.excellentshop.product.content;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.api.product.ContentType;
import su.nightexpress.excellentshop.api.product.ProductContent;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.util.ErrorHandler;
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

    public CommandContent(@NonNull ItemStack preview, @NonNull List<String> commands) {
        super(ContentType.COMMAND);
        this.setCommands(commands);
        this.setPreview(preview);
    }

    @NonNull
    public static CommandContent read(@NonNull FileConfig config, @NonNull String path) {
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
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".PreviewTag", ItemTag.of(this.preview));
        config.set(path + ".Content.Commands", this.commands);
    }

    @Override
    @NonNull
    public String getName() {
        return Lang.CONTENT_TYPE.getLocalized(this.type());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isPhysical() {
        return false;
    }

    @Override
    public boolean isItemMatches(@NonNull ItemStack itemStack) {
        return false;
    }

    @Override
    @NonNull
    public ItemStack getPreview() {
        return new ItemStack(this.preview);
    }

    public void setPreview(@NonNull ItemStack preview) {
        this.preview = new ItemStack(preview);
    }

    public boolean hasCommands() {
        return !this.commands.isEmpty();
    }

    @NonNull
    public List<String> getCommands() {
        return this.commands;
    }

    public void setCommands(@NonNull List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void delivery(@NonNull Inventory inventory, int count) {
        if (!(inventory.getHolder() instanceof Player player)) return;

        this.getCommands().forEach(command -> {
            if (command.contains(ShopPlaceholders.GENERIC_AMOUNT)) {
                command = command.replace(ShopPlaceholders.GENERIC_AMOUNT, String.valueOf(count));
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
    public void take(@NonNull Inventory inventory, int count) {

    }

    @Override
    public int count(@NonNull Inventory inventory) {
        return 0;
    }

    @Override
    public int getUnitAmount() {
        return 1;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int countSpace(@NonNull Inventory inventory) {
        return -1;
    }

    @Override
    public boolean hasSpace(@NonNull Inventory inventory) {
        return true;
    }
}
