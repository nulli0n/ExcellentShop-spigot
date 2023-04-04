package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

public class ShopMainMenu extends ConfigMenu<ExcellentShop> {

    private final VirtualShopModule module;
    //private final Map<String, Integer> shopSlots;

    public ShopMainMenu(@NotNull VirtualShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getPath() + "main.menu.yml"));
        this.module = module;
        //this.shopSlots = new HashMap<>();
        /*this.cfg.getSection("Shops").forEach(shopId -> {
            this.shopSlots.put(shopId.toLowerCase(), cfg.getInt("Shops." + shopId));
        });*/

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, (viewer, event) -> plugin.runTask(task -> viewer.getPlayer().closeInventory()));

        this.load();

        this.cfg.getSection("Shops").forEach(shopId -> {
            VirtualShop shop = module.getShopById(shopId);
            if (shop == null) {
                this.module.error("Invalid shop in the main menu: '" + shopId + "' !");
                return;
            }
            int slot = cfg.getInt("Shops." + shopId);

            ItemStack icon = shop.getIcon();
            ItemUtil.mapMeta(icon, meta -> {
                meta.setDisplayName(VirtualConfig.SHOP_FORMAT_NAME.get());
                meta.setLore(VirtualConfig.SHOP_FORMAT_LORE.get());
                meta.addItemFlags(ItemFlag.values());
            });

            MenuItem menuItem = new MenuItem(icon);
            menuItem.setSlots(slot);
            menuItem.setPriority(100);
            menuItem.getOptions().setDisplayModifier((viewer, item) -> ItemUtil.replace(item, shop.replacePlaceholders()));
            menuItem.setClick((viewer, event) -> {
                this.plugin.runTask( task -> shop.open(viewer.getPlayer(), 1));
            });
            this.addItem(menuItem);
        });
    }
}
