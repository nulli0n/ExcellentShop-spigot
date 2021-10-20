package su.nightexpress.nexshop.shop.virtual.editor.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorUtils;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IShopDiscount;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.ShopDiscount;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorHandler;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EditorShopDiscounts extends AbstractMenuAuto<ExcellentShop, IShopDiscount> {

    private final String       objectName;
    private final List<String> objectLore;

    private final IShopVirtual shop;

    public EditorShopDiscounts(@NotNull ExcellentShop plugin, @NotNull IShopVirtual shop) {
        super(plugin, VirtualEditorHandler.SHOP_DISCOUNTS_YML, "");
        this.shop = shop;

        this.objectName = StringUT.color(cfg.getString("Object.Name", "&eDiscount"));
        this.objectLore = StringUT.color(cfg.getStringList("Object.Lore"));

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
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull IShopDiscount discount) {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.objectName);
        meta.setLore(this.objectLore);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);

        ItemUT.replace(item, discount.replacePlaceholders());
        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IShopDiscount discount) {
        return (player1, type, e) -> {
            if (e.getClick() == ClickType.MIDDLE) {
                this.shop.getDiscounts().remove(discount);
                this.shop.save();
                this.open(player1, this.getPage(player1));
                return;
            }

            VirtualShop virtualShop = plugin.getVirtualShop();
            if (virtualShop == null) return;

            if (e.isShiftClick()) {
                if (e.isLeftClick()) {
                    EditorUtils.tipCustom(player1, plugin.lang().Virtual_Shop_Editor_Enter_Day.getMsg());
                    virtualShop.getEditorHandler().startEdit(player1, discount, VirtualEditorType.DISCOUNT_CHANGE_DAY);
                    EditorUtils.sendClickableTips(player1, CollectionsUT.getEnumsList(DayOfWeek.class));
                }
                if (e.isRightClick()) {
                    EditorUtils.tipCustom(player1, plugin.lang().Virtual_Shop_Editor_Enter_Time_Full.getMsg());
                    virtualShop.getEditorHandler().startEdit(player1, discount, VirtualEditorType.DISCOUNT_CHANGE_TIME);
                }
                player1.closeInventory();
                return;
            }
            EditorUtils.tipCustom(player1, plugin.lang().Virtual_Shop_Editor_Enter_Amount.getMsg());
            virtualShop.getEditorHandler().startEdit(player1, discount, VirtualEditorType.DISCOUNT_CHANGE_DISCOUNT);
            player1.closeInventory();
        };
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
