package su.nightexpress.nexshop.shop.virtual.editor.menu;

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
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopConfig;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;
import su.nightexpress.nexshop.shop.virtual.object.ShopVirtual;

import java.util.ArrayList;
import java.util.List;

public class EditorShopList extends AbstractMenuAuto<ExcellentShop, IShopVirtual> {

    private final VirtualShop virtualShop;

    private final String       objectName;
    private final List<String> objectLore;
    private final int[] objectSlots;

    public EditorShopList(@NotNull VirtualShop virtualShop) {
        super(virtualShop.plugin(), VirtualShopConfig.SHOP_LIST_YML, "");
        this.virtualShop = virtualShop;

        this.objectName = StringUtil.color(cfg.getString("Object.Name", "%title%"));
        this.objectLore = StringUtil.color(cfg.getStringList("Object.Lore"));
        this.objectSlots = cfg.getIntArray("Object.Slots");

        EditorInput<VirtualShop, VirtualEditorType> input = (player, vshop, type, e) -> {
            if (type == VirtualEditorType.SHOP_CREATE) {
                String id = EditorManager.fineId(e.getMessage());
                if (vshop.getShopById(id) != null) {
                    EditorManager.error(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Create_Error_Exist).getLocalized());
                    return false;
                }
                IShopVirtual shop = new ShopVirtual(vshop, vshop.getFullPath() + VirtualShop.DIR_SHOPS + id + "/" + id + ".yml");
                vshop.getShopsMap().put(shop.getId(), shop);
            }
            return true;
        };

        IMenuClick click = (player, type, e) -> {
            if (type == null) return;

            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
            else if (type instanceof VirtualEditorType type2) {
                if (type2 == VirtualEditorType.SHOP_CREATE) {
                    EditorManager.startEdit(player, virtualShop, type2, input);
                    EditorManager.tip(player, plugin.getMessage(Lang.Virtual_Shop_Editor_Enter_Id).getLocalized());
                    player.closeInventory();
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
            IMenuItem menuItem = cfg.getMenuItem("Editor." + sId, VirtualEditorType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    @NotNull
    protected List<IShopVirtual> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.virtualShop.getShops());
    }

    @Override
    public int[] getObjectSlots() {
        return objectSlots;
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull IShopVirtual shop) {
        ItemStack item = new ItemStack(shop.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.objectName);
        meta.setLore(this.objectLore);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);

        ItemUtil.replace(item, shop.replacePlaceholders());
        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IShopVirtual shop) {
        return (player1, type, e) -> {
            shop.getEditor().open(player1, 1);
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
