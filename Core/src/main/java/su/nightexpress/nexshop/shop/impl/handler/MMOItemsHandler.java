package su.nightexpress.nexshop.shop.impl.handler;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.impl.packer.MMOItemsPacker;

public class MMOItemsHandler implements ItemHandler {

    @Override
    @NotNull
    public  String getName() {
        return HookId.MMOITEMS;
    }

    @Override
    @NotNull
    public ProductPacker createPacker() {
        return new MMOItemsPacker();
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return MMOItems.getType(item) != null && MMOItems.getID(item) != null;
    }
}
