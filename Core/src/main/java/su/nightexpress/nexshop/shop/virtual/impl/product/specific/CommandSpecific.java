package su.nightexpress.nexshop.shop.virtual.impl.product.specific;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.CommandProduct;

import java.util.ArrayList;
import java.util.List;

public class CommandSpecific implements ProductSpecific, CommandProduct {

    private   ItemStack    preview;
    private final List<String> commands;

    private final PlaceholderMap placeholderMap;

    public CommandSpecific(@NotNull ItemStack preview, @NotNull List<String> commands) {
        this.setPreview(preview);
        this.commands = new ArrayList<>(commands);

        this.placeholderMap = new PlaceholderMap()
            .add(su.nightexpress.nexshop.shop.virtual.util.Placeholders.PRODUCT_COMMANDS, () -> String.join("\n", this.getCommands()));
    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    @Override
    public void delivery(@NotNull Player player, int count) {
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        for (int i = 0; i < count; i++) {
            this.getCommands().forEach(command -> Bukkit.dispatchCommand(sender, Placeholders.forPlayer(player).apply(command)));
        }
    }

    @Override
    public void take(@NotNull Player player, int count) {

    }

    @Override
    public int count(@NotNull Player player) {
        return 0;
    }

    @Override
    public int getUnitAmount() {
        return 1;
    }

    @NotNull
    @Override
    public List<String> getCommands() {
        return this.commands;
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return new ItemStack(preview);
    }

    @Override
    public void setPreview(@NotNull ItemStack preview) {
        this.preview = new ItemStack(preview);
    }

    @Override
    public boolean hasSpace(@NotNull Player player) {
        return true;
    }
}
