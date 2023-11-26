package su.nightexpress.nexshop.shop.impl.handler;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.impl.packer.ItemsAdderPacker;

public class ItemsAdderHandler extends AbstractItemHandler {

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return CustomStack.byItemStack(item) != null;
    }

    @Override
    @NotNull
    public String getName() {
        return HookId.ITEMS_ADDER;
    }

    @Override
    @NotNull
    public ItemsAdderPacker createPacker() {
        return new ItemsAdderPacker();
    }

    @Override
    protected void loadEditorAdditional(@NotNull EditorMenu<ExcellentShop, ? extends Product> menu, @NotNull Product product) {

    }
}
