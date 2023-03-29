package su.nightexpress.nexshop.api.shop;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CommandProduct {

    @NotNull List<String> getCommands();

    @NotNull ItemStack getPreview();

    void setPreview(@NotNull ItemStack preview);
}
