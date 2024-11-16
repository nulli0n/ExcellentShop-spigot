package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.type.ShopType;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.EditorMenu;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.StringUtil;

import java.util.Comparator;
import java.util.stream.IntStream;

public class ShopListEditor extends EditorMenu<ShopPlugin, VirtualShopModule> implements AutoFilled<VirtualShop> {

    private final VirtualShopModule module;

    public ShopListEditor(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, VirtualLang.EDITOR_TITLE_SHOP_LIST.getString(), MenuSize.CHEST_45);
        this.module = module;

        this.addExit(39);
        this.addNextPage(44);
        this.addPreviousPage(36);

        this.addCreation(VirtualLocales.SHOP_CREATE, 41, (viewer, event, module1) -> {
            this.handleInput(viewer, VirtualLang.EDITOR_ENTER_SHOP_ID, (dialog, input) -> {
                ShopType type = event.isLeftClick() ? ShopType.STATIC : ShopType.ROTATING;
                if (!module1.createShop(StringUtil.lowerCaseUnderscore(input.getTextRaw()), type)) {
                    dialog.error(VirtualLang.EDITOR_SHOP_CREATE_ERROR_EXIST.getMessage());
                    return false;
                }
                return true;
            });
        });
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<VirtualShop> autoFill) {
        autoFill.setSlots(IntStream.range(0, 36).toArray());
        autoFill.setItems(this.module.getShops().stream().sorted(Comparator.comparing(VirtualShop::getId)).toList());
        autoFill.setItemCreator(shop -> {
            ItemStack item = new ItemStack(shop.getIcon());
            ItemReplacer.create(item)
                .readLocale(VirtualLocales.SHOP_OBJECT).hideFlags()
                .replacement(replacer -> replacer.replace(Placeholders.forVirtualShopEditor(shop)))
                .writeMeta();
            return item;
        });
        autoFill.setClickAction(shop -> (viewer1, event) -> {
            if (event.isShiftClick() && event.isRightClick()) {
                this.module.delete(shop);
                this.runNextTick(() -> this.flush(viewer));
                return;
            }
            this.runNextTick(() -> this.module.openShopEditor(viewer.getPlayer(), shop));
        });
    }
}
