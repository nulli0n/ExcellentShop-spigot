package su.nightexpress.nexshop.shop.impl.handler;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.shop.impl.packer.VanillaItemPacker;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;

import java.util.function.Predicate;

public class VanillaItemHandler extends AbstractItemHandler {

    public static final String NAME = "bukkit_item";

    private static final String TEXTURE_META = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmMzODU1MGI1ZGFjOWVmYWZhODg5OTQ1YWVjM2JjZDE3OGNiODgwODA2ZWI3ZTcxMmQxYmQ5NmE2MWRmMjNmNiJ9fX0=";

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    @NotNull
    public VanillaItemPacker createPacker() {
        return new VanillaItemPacker();
    }

    @Override
    public boolean canHandle(@NotNull ItemStack item) {
        return !item.getType().isAir();
    }

    @Override
    protected void loadEditorAdditional(@NotNull EditorMenu<ExcellentShop, ? extends Product> menu, @NotNull Product product) {
        Predicate<MenuViewer> predicate = viewer -> product.getHandler() == this;

        menu.addItem(ItemUtil.createCustomHead(TEXTURE_META), VirtualLocales.PRODUCT_RESPECT_ITEM_META, 19).setClick((viewer, event) -> {
            if (!(product.getPacker() instanceof VanillaItemPacker packer)) return;

            packer.setRespectItemMeta(!packer.isRespectItemMeta());
            product.getShop().saveProducts();
            menu.openNextTick(viewer, viewer.getPage());
        }).getOptions().setVisibilityPolicy(predicate);
    }
}
