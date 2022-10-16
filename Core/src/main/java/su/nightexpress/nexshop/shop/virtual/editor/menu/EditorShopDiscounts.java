package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.IShopDiscount;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.ShopDiscount;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopConfig;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EditorShopDiscounts extends AbstractMenuAuto<ExcellentShop, IShopDiscount> {

    private final String       objectName;
    private final List<String> objectLore;
    private final int[] objectSlots;

    private final IShopVirtual shop;

    public EditorShopDiscounts(@NotNull ExcellentShop plugin, @NotNull IShopVirtual shop) {
        super(plugin, VirtualShopConfig.SHOP_DISCOUNTS_YML, "");
        this.shop = shop;

        this.objectName = StringUtil.color(cfg.getString("Object.Name", "&eDiscount"));
        this.objectLore = StringUtil.color(cfg.getStringList("Object.Lore"));
        this.objectSlots = cfg.getIntArray("Object.Slots");

        IMenuClick click = (player, type, e) -> {
            if (type == null) return;

            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    shop.getEditor().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof VirtualEditorType type2) {
                if (type2 == VirtualEditorType.DISCOUNT_CREATE) {
                    ShopDiscount discount = new ShopDiscount(new HashSet<>(), new HashSet<>(), 0D);
                    this.shop.getDiscounts().add(discount);
                    this.shop.save();
                    this.open(player, this.getPage(player));
                }
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
            IMenuItem guiItem = cfg.getMenuItem("Editor." + sId, VirtualEditorType.class);

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addItem(guiItem);
        }
    }

    @Override
    @NotNull
    protected List<IShopDiscount> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.shop.getDiscounts());
    }

    @Override
    public int[] getObjectSlots() {
        return objectSlots;
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull IShopDiscount discount) {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.objectName);
        meta.setLore(this.objectLore);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);

        ItemUtil.replace(item, discount.replacePlaceholders());
        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IShopDiscount discount) {

        EditorInput<IShopDiscount, VirtualEditorType> input = (player2, discount2, type, e) -> {
            VirtualShop virtualShop = plugin.getVirtualShop();
            if (virtualShop == null) return true;

            String msg = StringUtil.color(e.getMessage());
            switch (type) {
                case DISCOUNT_CHANGE_DISCOUNT -> {
                    double value = StringUtil.getDouble(StringUtil.colorOff(msg), 0);
                    discount2.setDiscount(value);
                }
                case DISCOUNT_CHANGE_DAY -> {
                    DayOfWeek day = CollectionsUtil.getEnum(msg, DayOfWeek.class);
                    if (day == null) {
                        EditorManager.error(player2, EditorManager.ERROR_ENUM);
                        return false;
                    }
                    discount2.getDays().add(day);
                }
                case DISCOUNT_CHANGE_TIME -> {
                    String[] raw = msg.split(" ");
                    LocalTime[] times = new LocalTime[raw.length];

                    for (int count = 0; count < raw.length; count++) {
                        String[] split = raw[count].split(":");
                        int hour = StringUtil.getInteger(split[0], 0);
                        int minute = StringUtil.getInteger(split.length >= 2 ? split[1] : "0", 0);
                        times[count] = LocalTime.of(hour, minute);
                    }
                    if (times.length < 2) return false;

                    discount2.getTimes().add(times);
                }
                default -> {}
            }

            // Find that shop discount comparing by memory address
            IShopVirtual shop = virtualShop.getShops().stream().filter(shop2 -> {
                return shop2.getDiscounts().stream().anyMatch(d -> d == discount);
            }).findFirst().orElse(null);
            if (shop == null) return true;

            shop.save();
            return true;
        };
        
        return (player1, type, e) -> {
            VirtualShop virtualShop = plugin.getVirtualShop();
            if (virtualShop == null) return;

            if (e.isShiftClick()) {
                if (e.isLeftClick()) {
                    EditorManager.tip(player1, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Day).getLocalized());
                    EditorManager.startEdit(player1, discount, VirtualEditorType.DISCOUNT_CHANGE_DAY, input);
                    EditorManager.suggestValues(player1, CollectionsUtil.getEnumsList(DayOfWeek.class), true);
                }
                if (e.isRightClick()) {
                    EditorManager.tip(player1, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Time_Full).getLocalized());
                    EditorManager.startEdit(player1, discount, VirtualEditorType.DISCOUNT_CHANGE_TIME, input);
                }
                player1.closeInventory();
                return;
            }

            if (e.isRightClick()) {
                this.shop.getDiscounts().remove(discount);
                this.shop.save();
                this.open(player1, this.getPage(player1));
            }
            else {
                EditorManager.tip(player1, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Amount).getLocalized());
                EditorManager.startEdit(player1, discount, VirtualEditorType.DISCOUNT_CHANGE_DISCOUNT, input);
                player1.closeInventory();
            }
        };
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
