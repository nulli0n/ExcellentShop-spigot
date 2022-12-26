package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.AbstractEditorMenuAuto;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualDiscount;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class EditorShopDiscounts extends AbstractEditorMenuAuto<ExcellentShop, VirtualShop, VirtualDiscount> {

    public EditorShopDiscounts(@NotNull VirtualShop shop) {
        super(shop.plugin(), shop, Placeholders.EDITOR_VIRTUAL_TITLE, 45);

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    shop.getEditor().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof VirtualEditorType type2) {
                if (type2 == VirtualEditorType.DISCOUNT_CREATE) {
                    this.parent.addDiscountConfig(new VirtualDiscount());
                    this.parent.save();
                    this.open(player, this.getPage(player));
                }
            }
        };

        this.loadItems(click);
    }

    @Override
    public void setTypes(@NotNull Map<EditorButtonType, Integer> map) {
        map.put(VirtualEditorType.DISCOUNT_CREATE, 41);
        map.put(MenuItemType.RETURN, 39);
        map.put(MenuItemType.PAGE_NEXT, 44);
        map.put(MenuItemType.PAGE_PREVIOUS, 36);
    }

    @Override
    @NotNull
    protected List<VirtualDiscount> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.parent.getDiscountConfigs());
    }

    @Override
    public int[] getObjectSlots() {
        return IntStream.range(0, 36).toArray();
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull VirtualDiscount discount) {
        ItemStack item = VirtualEditorType.DISCOUNT_OBJECT.getItem();
        ItemUtil.replace(item, discount.replacePlaceholders());
        return item;
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull VirtualDiscount discount) {
        return (player1, type, e) -> {
            if (e.isShiftClick()) {
                if (e.isRightClick()) {
                    this.parent.removeDiscountConfig(discount);
                    this.parent.save();
                    this.open(player1, this.getPage(player1));
                }
            }
            else {
                discount.getEditor().open(player1, 1);
            }
        };
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
