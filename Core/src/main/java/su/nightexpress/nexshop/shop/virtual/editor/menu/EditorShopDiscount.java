package su.nightexpress.nexshop.shop.virtual.editor.menu;

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
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.IScheduled;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualDiscount;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class EditorShopDiscount extends AbstractEditorMenu<ExcellentShop, VirtualDiscount> {

    private final VirtualShop shop;

    public EditorShopDiscount(@NotNull VirtualShop shop, @NotNull VirtualDiscount discount) {
        super(shop.plugin(), discount, Placeholders.EDITOR_VIRTUAL_TITLE, 45);
        this.shop = shop;

        EditorInput<VirtualDiscount, VirtualEditorType> input = (player2, discount2, type, e) -> {
            String msg = Colorizer.apply(e.getMessage());
            switch (type) {
                case DISCOUNT_CHANGE_DISCOUNT -> {
                    double value = StringUtil.getDouble(Colorizer.strip(msg), 0);
                    discount2.setDiscount(value);
                }
                case DISCOUNT_CHANGE_DURATION -> {
                    int value = StringUtil.getInteger(Colorizer.strip(msg), 0);
                    discount2.setDuration(value);
                }
                case DISCOUNT_CHANGE_DAY -> {
                    DayOfWeek day = CollectionsUtil.getEnum(msg, DayOfWeek.class);
                    if (day == null) {
                        EditorManager.error(player2, plugin.getMessage(Lang.EDITOR_ERROR_ENUM).getLocalized());
                        return false;
                    }
                    discount2.getDays().add(day);
                    discount2.stopScheduler();
                    discount2.startScheduler();
                }
                case DISCOUNT_CHANGE_TIME -> {
                    try {
                        discount2.getTimes().add(LocalTime.parse(msg, IScheduled.TIME_FORMATTER));
                        discount2.stopScheduler();
                        discount2.startScheduler();
                    }
                    catch (DateTimeParseException ex) {
                        return false;
                    }
                }
            }

            shop.save();
            return true;
        };

        MenuClick click = (player1, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    this.shop.getEditor().getEditorDiscounts().open(player1, 1);
                }
                else this.onItemClickDefault(player1, type2);
            }
            else if (type instanceof VirtualEditorType type2) {
                switch (type2) {
                    case DISCOUNT_CHANGE_DISCOUNT -> {
                        EditorManager.prompt(player1, plugin.getMessage(VirtualLang.EDITOR_ENTER_AMOUNT).getLocalized());
                        EditorManager.startEdit(player1, discount, type2, input);
                        player1.closeInventory();
                        return;
                    }
                    case DISCOUNT_CHANGE_DURATION -> {
                        EditorManager.prompt(player1, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_SECONDS).getLocalized());
                        EditorManager.startEdit(player1, discount, type2, input);
                        player1.closeInventory();
                        return;
                    }
                    case DISCOUNT_CHANGE_DAY -> {
                        if (e.isLeftClick()) {
                            EditorManager.prompt(player1, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_DAY).getLocalized());
                            EditorManager.startEdit(player1, discount, type2, input);
                            EditorManager.suggestValues(player1, CollectionsUtil.getEnumsList(DayOfWeek.class), true);
                            player1.closeInventory();
                            return;
                        }
                        else if (e.isRightClick()) {
                            discount.getDays().clear();
                        }
                    }
                    case DISCOUNT_CHANGE_TIME -> {
                        if (e.isLeftClick()) {
                            EditorManager.prompt(player1, plugin.getMessage(Lang.EDITOR_GENERIC_ENTER_TIME).getLocalized());
                            EditorManager.startEdit(player1, discount, type2, input);
                            player1.closeInventory();
                            return;
                        }
                        else if (e.isRightClick()) {
                            discount.getTimes().clear();
                        }
                    }
                }
                this.shop.save();
                this.open(player1, 1);
            }
        };

        this.loadItems(click);
    }

    @Override
    public void setTypes(@NotNull Map<EditorButtonType, Integer> map) {
        map.put(MenuItemType.RETURN, 40);
        map.put(VirtualEditorType.DISCOUNT_CHANGE_DISCOUNT, 10);
        map.put(VirtualEditorType.DISCOUNT_CHANGE_DURATION, 12);
        map.put(VirtualEditorType.DISCOUNT_CHANGE_DAY, 14);
        map.put(VirtualEditorType.DISCOUNT_CHANGE_TIME, 16);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        ItemUtil.replace(item, this.object.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent inventoryClickEvent, @NotNull SlotType slotType) {
        return true;
    }
}
