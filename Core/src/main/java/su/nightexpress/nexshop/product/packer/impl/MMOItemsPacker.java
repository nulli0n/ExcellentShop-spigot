package su.nightexpress.nexshop.product.packer.impl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.product.handler.impl.MMOItemsHandler;

public class MMOItemsPacker extends UniversalPluginItemPacker<MMOItemsHandler> {

    public MMOItemsPacker(MMOItemsHandler handler, @NotNull String itemId, int amount) {
        super(handler, itemId, amount);
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return this.handler.getIcon(this.itemId);
    }
}
