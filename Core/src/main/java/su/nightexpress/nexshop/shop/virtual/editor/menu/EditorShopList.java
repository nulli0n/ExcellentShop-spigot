package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.AbstractEditorMenuAuto;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class EditorShopList extends AbstractEditorMenuAuto<ExcellentShop, VirtualShopModule, VirtualShop> {

    public EditorShopList(@NotNull VirtualShopModule module) {
        super(module.plugin(), module, Placeholders.EDITOR_VIRTUAL_TITLE, 45);

        EditorInput<VirtualShopModule, VirtualEditorType> input = (player, module2, type, e) -> {
            if (type == VirtualEditorType.SHOP_CREATE) {
                String id = EditorManager.fineId(e.getMessage());
                if (!module2.createShop(id)) {
                    EditorManager.error(player, plugin.getMessage(VirtualLang.EDITOR_SHOP_CREATE_ERROR_EXIST).getLocalized());
                    return false;
                }
            }
            return true;
        };

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
            else if (type instanceof VirtualEditorType type2) {
                if (type2 == VirtualEditorType.SHOP_CREATE) {
                    EditorManager.startEdit(player, module, type2, input);
                    EditorManager.tip(player, plugin.getMessage(VirtualLang.EDITOR_ENTER_ID).getLocalized());
                    player.closeInventory();
                }
            }
        };

        this.loadItems(click);
    }

    @Override
    public void setTypes(@NotNull Map<EditorButtonType, Integer> map) {
        map.put(VirtualEditorType.SHOP_CREATE, 41);
        map.put(MenuItemType.CLOSE, 39);
        map.put(MenuItemType.PAGE_NEXT, 44);
        map.put(MenuItemType.PAGE_PREVIOUS, 36);
    }

    @Override
    @NotNull
    protected List<VirtualShop> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.parent.getShops());
    }

    @Override
    public int[] getObjectSlots() {
        return IntStream.range(0, 36).toArray();
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull VirtualShop shop) {
        ItemStack item = new ItemStack(shop.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        ItemStack editor = VirtualEditorType.SHOP_OBJECT.getItem();
        ItemMeta meta2 = editor.getItemMeta();
        if (meta2 == null) return item;

        meta.setDisplayName(meta2.getDisplayName());
        meta.setLore(meta2.getLore());
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);

        ItemUtil.replace(item, shop.replacePlaceholders());
        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull VirtualShop shop) {
        return (player1, type, e) -> {
            if (e.isShiftClick() && e.isRightClick()) {
                this.parent.delete(shop);
                this.open(player, this.getPage(player));
                return;
            }
            shop.getEditor().open(player1, 1);
        };
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
