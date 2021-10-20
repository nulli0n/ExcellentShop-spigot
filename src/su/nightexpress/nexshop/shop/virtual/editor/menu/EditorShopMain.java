package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorHandler;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

public class EditorShopMain extends AbstractMenu<ExcellentShop> {

    private final IShopVirtual shop;

    private EditorShopDiscounts   editorDiscounts;
    private EditorShopViewDesign  editorViewDesign;
    private EditorShopProductList editorProductList;

    public EditorShopMain(@NotNull ExcellentShop plugin, @NotNull IShopVirtual shop) {
        super(plugin, VirtualEditorHandler.SHOP_MAIN_YML, "");
        this.shop = shop;

        IMenuClick click = (player, type, e) -> {
            VirtualShop virtualShop = plugin.getVirtualShop();
            if (virtualShop == null) return;

            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    virtualShop.getEditor().open(player, 1);
                }
            }
            else if (type instanceof VirtualEditorType type2) {
                switch (type2) {
                    case SHOP_DELETE -> {
                        if (!e.isShiftClick()) return;

                        if (virtualShop.delete(shop)) {
                            virtualShop.getEditor().open(player, 1);
                        }
                        return;
                    }
                    case SHOP_CHANGE_ICON -> {
                        ItemStack cursor = e.getCursor();
                        if (cursor == null || ItemUT.isAir(cursor)) return;

                        shop.setIcon(cursor);
                        e.getView().setCursor(null);
                    }
                    case SHOP_CHANGE_CITIZENS_ID -> {
                        if (!Hooks.hasPlugin(Hooks.CITIZENS)) return;

                        if (e.isLeftClick()) {
                            virtualShop.getEditorHandler().startEdit(player, shop, type2);
                            EditorUtils.tipCustom(player, plugin.lang().Virtual_Shop_Editor_Enter_NpcId.getMsg());
                            player.closeInventory();
                            return;
                        }
                        else if (e.isRightClick()) {
                            shop.setCitizensIds(new int[]{});
                        }
                    }
                    case SHOP_CHANGE_PERMISSION -> shop.setPermissionRequired(!shop.isPermissionRequired());
                    case SHOP_CHANGE_PRODUCTS -> {
                        this.getEditorProducts().open(player, 1);
                        return;
                    }
                    case SHOP_CHANGE_VIEW_DESIGN -> {
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                EditorUtils.tipCustom(player, plugin.lang().Virtual_Shop_Editor_Enter_Title.getMsg());
                                virtualShop.getEditorHandler().startEdit(player, shop, VirtualEditorType.SHOP_CHANGE_TITLE);
                                player.closeInventory();
                                return;
                            }
                            int size = shop.getView().getSize();
                            if (size == 54) size = 0;
                            shop.getView().setSize(size + 9);
                            shop.save();
                            shop.setupView();
                            this.open(player, 1);
                            return;
                        }
                        this.getEditorViewDesign().open(player, 1);
                        return;
                    }
                    case SHOP_CHANGE_DISCOUNTS -> {
                        this.getEditorDiscounts().open(player, 1);
                        return;
                    }
                    case SHOP_CHANGE_PAGES -> {
                        if (e.isLeftClick()) {
                            shop.setPages(shop.getPages() + 1);
                        }
                        else if (e.isRightClick()) {
                            shop.setPages(Math.max(1, shop.getPages() - 1));
                        }
                        shop.save();
                        shop.setupView();
                        this.open(player, 1);
                        return;
                    }
                    case SHOP_CHANGE_TRANSACTIONS -> {
                        if (e.isLeftClick()) {
                            shop.setPurchaseAllowed(TradeType.BUY, !shop.isPurchaseAllowed(TradeType.BUY));
                        }
                        else if (e.isRightClick()) {
                            shop.setPurchaseAllowed(TradeType.SELL, !shop.isPurchaseAllowed(TradeType.SELL));
                        }
                    }
                    default -> {return;}
                }

                this.shop.save();
                this.open(player, 1);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Editor")) {
            IMenuItem menuItem = cfg.getMenuItem("Editor." + sId, VirtualEditorType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    public void clear() {
        super.clear();
        if (this.editorDiscounts != null) {
            this.editorDiscounts.clear();
            this.editorDiscounts = null;
        }
        if (this.editorViewDesign != null) {
            this.editorViewDesign.clear();
            this.editorViewDesign = null;
        }
        if (this.editorProductList != null) {
            this.editorProductList.clear();
            this.editorProductList = null;
        }
    }

    /*@Deprecated
    public void rebuild() {
        this.editorViewDesign = new EditorShopViewDesign(this.plugin, this.shop);
        this.editorProductList = new EditorShopProductList(this.plugin, this.shop);
    }*/

    @NotNull
    public EditorShopDiscounts getEditorDiscounts() {
        if (this.editorDiscounts == null) {
            this.editorDiscounts = new EditorShopDiscounts(this.plugin, this.shop);
        }
        return this.editorDiscounts;
    }

    @NotNull
    public EditorShopViewDesign getEditorViewDesign() {
        if (this.editorViewDesign == null) {
            //this.rebuild();
            this.editorViewDesign = new EditorShopViewDesign(this.plugin, this.shop);
        }
        return this.editorViewDesign;
    }

    @NotNull
    public EditorShopProductList getEditorProducts() {
        if (this.editorProductList == null) {
            //this.rebuild();
            this.editorProductList = new EditorShopProductList(this.plugin, this.shop);
        }
        return this.editorProductList;
    }

    @Override
    @Nullable
    public MenuItemDisplay onItemDisplayPrepare(@NotNull Player player, @NotNull IMenuItem menuItem) {
        if (menuItem.getType() instanceof VirtualEditorType type) {
            if (type == VirtualEditorType.SHOP_CHANGE_PERMISSION) {
                return menuItem.getDisplay(String.valueOf(shop.isPermissionRequired() ? 1 : 0));
            }
        }
        return super.onItemDisplayPrepare(player, menuItem);
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        if (menuItem.getType() == VirtualEditorType.SHOP_CHANGE_ICON) {
            item.setType(this.shop.getIcon().getType());
        }

        ItemUT.replace(item, this.shop.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }
}
