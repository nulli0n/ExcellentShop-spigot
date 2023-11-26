package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.EditorMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualShop;
import su.nightexpress.nexshop.shop.virtual.type.ShopType;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class ShopListEditor extends EditorMenu<ExcellentShop, VirtualShopModule> implements AutoPaged<VirtualShop> {

    public ShopListEditor(@NotNull VirtualShopModule module) {
        super(module.plugin(), module, Placeholders.EDITOR_VIRTUAL_TITLE, 45);

        this.addExit(39);
        this.addNextPage(44);
        this.addPreviousPage(36);

        this.addCreation(VirtualLocales.SHOP_CREATE, 41).setClick((viewer, event) -> {
            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_SHOP_ID, wrapper -> {
                ShopType type = event.isLeftClick() ? ShopType.STATIC : ShopType.ROTATING;
                if (!module.createShop(StringUtil.lowerCaseUnderscore(wrapper.getTextRaw()), type)) {
                    EditorManager.error(viewer.getPlayer(), plugin.getMessage(VirtualLang.EDITOR_SHOP_CREATE_ERROR_EXIST).getLocalized());
                    return false;
                }
                return true;
            });
        });
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @Override
    @NotNull
    public List<VirtualShop> getObjects(@NotNull Player player) {
        return this.object.getShops().stream().sorted(Comparator.comparing(VirtualShop::getId)).toList();
    }

    @Override
    public int[] getObjectSlots() {
        return IntStream.range(0, 36).toArray();
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull VirtualShop shop) {
        ItemStack item = new ItemStack(shop.getIcon());
        ItemReplacer.create(item).readLocale(VirtualLocales.SHOP_OBJECT).hideFlags().trimmed()
            .replace(shop.replacePlaceholders())
            .writeMeta();
        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull VirtualShop shop) {
        return (viewer, event) -> {
            if (event.isShiftClick() && event.isRightClick()) {
                this.object.delete(shop);
                this.openNextTick(viewer, viewer.getPage());
                return;
            }
            AbstractVirtualShop<?> virtualShop = (AbstractVirtualShop<?>) shop;
            virtualShop.getEditor().openNextTick(viewer, 1);
        };
    }
}
