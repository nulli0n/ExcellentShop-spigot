package su.nightexpress.nexshop.shop.impl.handler;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.impl.packer.OraxenItemPacker;

public class OraxenItemHandler extends AbstractItemHandler {

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
    protected void loadEditorAdditional(@NotNull EditorMenu<ExcellentShop, ? extends Product> menu, @NotNull Product product) {

    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return OraxenItems.exists(item);
    }
}
