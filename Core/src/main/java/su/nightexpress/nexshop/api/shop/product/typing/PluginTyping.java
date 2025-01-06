package su.nightexpress.nexshop.api.shop.product.typing;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.api.item.ItemHandler;

public interface PluginTyping extends PhysicalTyping {

    @NotNull ItemHandler getHandler();

    @NotNull String getItemId();

    int getAmount();
}
