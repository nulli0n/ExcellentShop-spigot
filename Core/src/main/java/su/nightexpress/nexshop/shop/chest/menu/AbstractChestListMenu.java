package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.List;

public abstract class AbstractChestListMenu extends AbstractMenuAuto<ExcellentShop, ChestShop> {

    protected ChestShopModule chestShop;

    protected int[]        objectSlots;
    protected String       shopName;
    protected List<String> shopLore;

    public AbstractChestListMenu(@NotNull ChestShopModule chestShop, @NotNull JYML cfg) {
        super(chestShop.plugin(), cfg, "");
        this.chestShop = chestShop;

        this.objectSlots = cfg.getIntArray("Shop_Icon.Slots");
        this.shopName = StringUtil.color(cfg.getString("Shop_Icon.Name", ""));
        this.shopLore = StringUtil.color(cfg.getStringList("Shop_Icon.Lore"));

        MenuClick click = (player, type, e) -> {

            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
            else if (type instanceof ItemType type2) {
                if (type2 == ItemType.LIST_GLOBAL_TOGGLE) {
                    boolean isGlobal = false;
                    if (this instanceof ChestListOwnMenu) {
                        this.chestShop.getListGlobalMenu().open(player, 1);
                        isGlobal = true;
                    }
                    else if (this instanceof ChestListGlobalMenu) {
                        this.chestShop.getListOwnMenu().open(player, 1);
                    }
                    else return;

                    plugin.getMessage(ChestLang.SHOP_LIST_INFO_SWITCH)
                        .replace("%state%", LangManager.getBoolean(isGlobal))
                        .send(player);
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Special")) {
            MenuItem menuItem = cfg.getMenuItem("Special." + sId, ItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    private enum ItemType {
        LIST_GLOBAL_TOGGLE,
    }

    @Override
    public int[] getObjectSlots() {
        return objectSlots;
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull ChestShop shop) {
        ItemStack item = new ItemStack(shop.getLocation().getBlock().getType());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.shopName);
        meta.setLore(this.shopLore);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);

        ItemUtil.replace(item, shop.replacePlaceholders());
        return item;
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
