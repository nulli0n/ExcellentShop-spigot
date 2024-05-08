package su.nightexpress.nexshop.shop.impl.handler;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.impl.packer.OraxenItemPacker;

public class OraxenItemHandler implements ItemHandler {

    @Override
    @NotNull
    public String getName() {
        return HookId.ORAXEN;
    }

    @Override
    @NotNull
    public OraxenItemPacker createPacker() {
        return new OraxenItemPacker();
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return OraxenItems.exists(item);
    }
}
