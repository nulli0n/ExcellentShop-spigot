package su.nightexpress.nexshop.api.shop.product.typing;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CommandTyping extends ProductTyping {

    @NotNull List<String> getCommands();

    void setCommands(@NotNull List<String> commands);

    void setPreview(@NotNull ItemStack preview);
}
