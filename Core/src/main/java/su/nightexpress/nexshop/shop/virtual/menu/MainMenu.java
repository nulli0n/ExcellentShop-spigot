package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;

public class MainMenu extends ConfigMenu<ExcellentShop> {

    private final VirtualShopModule module;

    public MainMenu(@NotNull VirtualShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getLocalPath(), "main.menu.yml"));
        this.module = module;

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, ClickHandler.forClose(this));

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
            menuItem.getOptions().addDisplayModifier((viewer, item) -> ItemReplacer.replace(item, shop.replacePlaceholders()));
            menuItem.setClick((viewer, event) -> {
                if (shop.canAccess(viewer.getPlayer(), true)) {
                    shop.openNextTick(viewer.getPlayer(), 1);
                }
            });
            this.addItem(menuItem);
        });

        if (Config.GUI_PLACEHOLDER_API.get()) {
            this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                ItemReplacer.create(item).readMeta().replacePlaceholderAPI(viewer.getPlayer()).writeMeta();
            }));
        }
    }
}
