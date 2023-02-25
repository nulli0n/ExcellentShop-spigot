package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.AbstractEditorMenu;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class EditorShopMain extends AbstractEditorMenu<ExcellentShop, VirtualShop> {

    private EditorShopDiscounts   editorDiscounts;
    private EditorShopViewDesign  editorViewDesign;
    private EditorShopProductList editorProductList;

    public EditorShopMain(@NotNull ExcellentShop plugin, @NotNull VirtualShop shop) {
        super(plugin, shop, Placeholders.EDITOR_VIRTUAL_TITLE, 45);

        EditorInput<VirtualShop, VirtualEditorType> input = (player, shop2, type, e) -> {
            String msg = StringUtil.color(e.getMessage());
            switch (type) {
                case SHOP_CHANGE_NAME -> shop2.setName(msg);
                case SHOP_CHANGE_DESCRIPTION -> shop2.getDescription().add(msg);
                case SHOP_CHANGE_TITLE -> shop2.getView().setTitle(msg);
                case SHOP_CHANGE_CITIZENS_ID -> {
                    msg = StringUtil.colorOff(msg);
                    int inputN = StringUtil.getInteger(msg, -1);
                    if (inputN < 0) {
                        EditorManager.error(player, plugin.getMessage(Lang.EDITOR_ERROR_NUMBER_NOT_INT).getLocalized());
                        return false;
                    }

                    List<Integer> current = new ArrayList<>(IntStream.of(shop2.getCitizensIds()).boxed().toList());
                    if (current.contains(inputN)) break;
                    current.add(inputN);

                    shop2.setCitizensIds(current.stream().mapToInt(i -> i).toArray());
                }
                default -> {}
            }

            shop.save();
            return true;
        };

        MenuClick click = (player, type, e) -> {
            VirtualShopModule virtualShop = plugin.getVirtualShop();
            if (virtualShop == null) return;

            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    virtualShop.getEditor().open(player, 1);
                }
            }
            else if (type instanceof VirtualEditorType type2) {
                switch (type2) {
                    case SHOP_CHANGE_NAME -> {
                        EditorManager.tip(player, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_NAME).getLocalized());
                        EditorManager.startEdit(player, shop, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case SHOP_CHANGE_DESCRIPTION -> {
                        if (e.isRightClick()) {
                            shop.getDescription().clear();
                            break;
                        }
                        EditorManager.tip(player, plugin.getMessage(VirtualLang.EDITOR_ENTER_DESCRIPTION).getLocalized());
                        EditorManager.startEdit(player, shop, type2, input);
                        player.closeInventory();
                        return;
                    }
                    case SHOP_CHANGE_ICON -> {
                        ItemStack cursor = e.getCursor();
                        if (cursor == null || cursor.getType().isAir()) return;

                        shop.setIcon(cursor);
                        e.getView().setCursor(null);
                    }
                    case SHOP_CHANGE_CITIZENS_ID -> {
                        if (!Hooks.hasCitizens()) return;

                        if (e.isLeftClick()) {
                            EditorManager.startEdit(player, shop, type2, input);
                            EditorManager.tip(player, plugin.getMessage(VirtualLang.EDITOR_ENTER_NPC_ID).getLocalized());
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
                                EditorManager.tip(player, plugin.getMessage(VirtualLang.EDITOR_ENTER_TITLE).getLocalized());
                                EditorManager.startEdit(player, shop, VirtualEditorType.SHOP_CHANGE_TITLE, input);
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
                            shop.setTransactionEnabled(TradeType.BUY, !shop.isTransactionEnabled(TradeType.BUY));
                        }
                        else if (e.isRightClick()) {
                            shop.setTransactionEnabled(TradeType.SELL, !shop.isTransactionEnabled(TradeType.SELL));
                        }
                    }
                    default -> {return;}
                }

                this.object.save();
                this.open(player, 1);
            }
        };

        this.loadItems(click);
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

    @NotNull
    public EditorShopDiscounts getEditorDiscounts() {
        if (this.editorDiscounts == null) {
            this.editorDiscounts = new EditorShopDiscounts(this.object);
        }
        return this.editorDiscounts;
    }

    @NotNull
    public EditorShopViewDesign getEditorViewDesign() {
        if (this.editorViewDesign == null) {
            this.editorViewDesign = new EditorShopViewDesign(this.plugin, this.object);
        }
        return this.editorViewDesign;
    }

    @NotNull
    public EditorShopProductList getEditorProducts() {
        if (this.editorProductList == null) {
            this.editorProductList = new EditorShopProductList(this.plugin, this.object);
        }
        return this.editorProductList;
    }

    @Override
    public void setTypes(@NotNull Map<EditorButtonType, Integer> map) {
        map.put(MenuItemType.RETURN, 40);
        map.put(VirtualEditorType.SHOP_CHANGE_PERMISSION, 4);

        map.put(VirtualEditorType.SHOP_CHANGE_NAME, 11);
        map.put(VirtualEditorType.SHOP_CHANGE_DESCRIPTION, 12);
        map.put(VirtualEditorType.SHOP_CHANGE_ICON, 13);
        map.put(VirtualEditorType.SHOP_CHANGE_PAGES, 14);
        map.put(VirtualEditorType.SHOP_CHANGE_TRANSACTIONS, 15);

        map.put(VirtualEditorType.SHOP_CHANGE_CITIZENS_ID, 8);

        map.put(VirtualEditorType.SHOP_CHANGE_DISCOUNTS, 24);
        map.put(VirtualEditorType.SHOP_CHANGE_PRODUCTS, 20);
        map.put(VirtualEditorType.SHOP_CHANGE_VIEW_DESIGN, 22);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        if (menuItem.getType() instanceof VirtualEditorType type) {
            if (type == VirtualEditorType.SHOP_CHANGE_PERMISSION) {
                item.setType(this.object.isPermissionRequired() ? Material.REDSTONE : Material.GUNPOWDER);
            }
            else if (type == VirtualEditorType.SHOP_CHANGE_ICON) {
                item.setType(this.object.getIcon().getType());
            }
        }
        ItemUtil.replace(item, this.object.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return slotType != SlotType.PLAYER && slotType != SlotType.EMPTY_PLAYER;
    }
}
