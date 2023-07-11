package su.nightexpress.nexshop.shop.virtual.impl.product;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.CommandProduct;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VirtualCommandProduct extends VirtualProduct implements CommandProduct {

    private       ItemStack    preview;
    private final List<String> commands;

    public VirtualCommandProduct(@NotNull ItemStack preview, @NotNull Currency currency) {
        this(UUID.randomUUID().toString(), preview, new ArrayList<>(), currency);
    }

    public VirtualCommandProduct(@NotNull String id, @NotNull ItemStack preview, @NotNull List<String> commands, @NotNull Currency currency) {
        super(id, currency);
        this.setPreview(preview);
        this.commands = new ArrayList<>(commands);
        this.commands.replaceAll(cmd -> cmd.replace("[CONSOLE]", "").trim());

        this.placeholderMap
            .add(Placeholders.PRODUCT_VIRTUAL_COMMANDS, () -> String.join("\n", this.getCommands()))
            ;
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

    @Override
    public boolean isSellable() {
        return false;
    }
}
