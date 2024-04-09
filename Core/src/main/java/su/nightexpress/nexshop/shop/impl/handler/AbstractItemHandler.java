package su.nightexpress.nexshop.shop.impl.handler;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.handler.ItemHandler;
import su.nightexpress.nexshop.api.shop.handler.ProductHandler;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.shop.ProductHandlerRegistry;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;

import java.util.function.Predicate;

public abstract class AbstractItemHandler implements ItemHandler {

    @Override
    public void loadEditor(@NotNull EditorMenu<ExcellentShop, ? extends Product> menu, @NotNull Product product) {
        Predicate<MenuViewer> predicate = viewer -> product.getHandler() == this;

        int slot = product.getPacker() instanceof ItemPacker itemPacker && itemPacker.isUsePreview() ? 5 : 4;

        menu.addItem(new ItemStack(Material.ITEM_FRAME), slot).setClick((viewer, event) -> {
            if (!(product.getPacker() instanceof ItemPacker itemPacker)) return;

            if (event.isRightClick()) {
                PlayerUtil.addItem(viewer.getPlayer(), itemPacker.getItem());
                return;
            }

            ItemStack cursor = event.getCursor();
            if (cursor == null || cursor.getType().isAir()) return;

            ProductHandler handler;
            if (event.isShiftClick()) {
                handler = ProductHandlerRegistry.BUKKIT_ITEM;
            }
            else handler = ProductHandlerRegistry.getHandler(cursor);

            product.setHandler(handler);
            if (product.getPacker() instanceof ItemPacker itemPacker1) {
                itemPacker1.load(cursor);
            }

            event.getView().setCursor(null);
            product.getShop().saveProducts();
            menu.openNextTick(viewer, viewer.getPage());
        }).getOptions().setVisibilityPolicy(predicate).setDisplayModifier((viewer, item) -> {
            if (!(product.getPacker() instanceof ItemPacker itemPacker)) return;

            ItemStack original = itemPacker.getItem();
            item.setType(original.getType());
            item.setAmount(original.getAmount());
            item.setItemMeta(original.getItemMeta());
            ItemReplacer.create(item).readLocale(VirtualLocales.PRODUCT_ITEM).hideFlags().writeMeta();
        });

        this.loadEditorAdditional(menu, product);
    }

    protected abstract void loadEditorAdditional(@NotNull EditorMenu<ExcellentShop, ? extends Product> menu, @NotNull Product product);

}
