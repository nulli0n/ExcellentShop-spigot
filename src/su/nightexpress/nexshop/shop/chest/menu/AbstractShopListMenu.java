package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.ChestShop;

import java.util.List;

public abstract class AbstractShopListMenu extends AbstractMenuAuto<ExcellentShop, IShopChest> {

    protected ChestShop chestShop;

    protected String       shopName;
    protected List<String> shopLore;

    public AbstractShopListMenu(@NotNull ChestShop chestShop, @NotNull JYML cfg) {
        super(chestShop.plugin(), cfg, "");
        this.chestShop = chestShop;

        this.objectSlots = cfg.getIntArray("Shop_Icon.Slots");
        this.shopName = StringUT.color(cfg.getString("Shop_Icon.Name", ""));
        this.shopLore = StringUT.color(cfg.getStringList("Shop_Icon.Lore"));

        IMenuClick click = (player, type, e) -> {

            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
            else if (type instanceof ItemType type2) {
                if (type2 == ItemType.LIST_GLOBAL_TOGGLE) {
                    boolean isGlobal = false;
                    if (this instanceof ShopListOwnMenu) {
                        this.chestShop.getListGlobalGUI().open(player, 1);
                        isGlobal = true;
                    }
                    else if (this instanceof ShopListGlobalMenu) {
                        this.chestShop.getListOwnGUI().open(player, 1);
                    }
                    else return;

                    plugin.lang().Chest_Shop_ShopList_Info_Switch
                            .replace("%state%", plugin.lang().getBool(isGlobal))
                            .send(player);
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

        for (String sId : cfg.getSection("Special")) {
            IMenuItem menuItem = cfg.getMenuItem("Special." + sId, ItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    private enum ItemType {
        LIST_GLOBAL_TOGGLE,
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull IShopChest shop) {
        ItemStack item = new ItemStack(shop.getLocation().getBlock().getType());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.shopName);
        meta.setLore(this.shopLore);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);

        ItemUT.replace(item, shop.replacePlaceholders());
        return item;
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
