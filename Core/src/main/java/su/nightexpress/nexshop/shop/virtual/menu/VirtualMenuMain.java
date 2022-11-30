package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;

public class VirtualMenuMain extends AbstractMenu<ExcellentShop> {

    private final VirtualShopModule module;

    public VirtualMenuMain(@NotNull VirtualShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getPath() + "main.menu.yml"), "");
        this.module = module;

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Shops")) {
            VirtualShop shop = module.getShopById(sId);
            if (shop == null) {
                plugin.error("Invalid shop in the main menu: '" + sId + "' !");
                continue;
            }

            ItemStack icon = shop.getIcon();
            ItemMeta meta = icon.getItemMeta();
            if (meta == null) continue;

            meta.setDisplayName(VirtualConfig.SHOP_FORMAT_NAME.get());
            meta.setLore(VirtualConfig.SHOP_FORMAT_LORE.get());
            meta.addItemFlags(ItemFlag.values());
            icon.setItemMeta(meta);

            int slot = cfg.getInt("Shops." + sId, cfg.getInt("Shops." + sId + ".Slots"));
            IMenuItem menuItem = new MenuItem(shop.getId(), icon, slot);

            menuItem.setClick((player, type, e) -> {
                shop.open(player, 1);
            });

            this.addItem(menuItem);
        }
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

        VirtualShop shop = this.module.getShopById(menuItem.getId());
        if (shop == null) return;

        ItemUtil.replace(item, shop.replacePlaceholders());
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
