package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.utils.Colors;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;

import java.util.ArrayList;
import java.util.List;

public class MainMenu extends ConfigMenu<ExcellentShop> {

    public static final String FILE_NAME = "main.menu.yml";

    private final VirtualShopModule module;

    public MainMenu(@NotNull ExcellentShop plugin, @NotNull VirtualShopModule module) {
        super(plugin, JYML.loadOrExtract(plugin, module.getLocalPath(), FILE_NAME));
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
            menuItem.setPriority(Integer.MAX_VALUE);
            menuItem.getOptions().addDisplayModifier((viewer, item) -> ItemReplacer.replace(item, shop.replacePlaceholders()));
            if (VirtualConfig.MAIN_MENU_HIDE_NO_PERM_SHOPS.get()) {
                menuItem.getOptions().setVisibilityPolicy(viewer -> shop.canAccess(viewer.getPlayer(), false));
            }
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

    @Override
    public boolean isCodeCreation() {
        return true;
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions("Shop", 45, InventoryType.CHEST);
    }

    @Override
    protected void loadAdditional() {
        if (!cfg.contains("Shops")) {
            cfg.set("Shops.tools", 10);
            cfg.set("Shops.weapons", 11);
            cfg.set("Shops.ingredients", 12);
            cfg.set("Shops.wool", 13);
            cfg.set("Shops.blocks", 14);
            cfg.set("Shops.peaceful_loot", 15);
            cfg.set("Shops.hostile_loot", 16);
            cfg.set("Shops.fish_market", 21);
            cfg.set("Shops.food", 22);
            cfg.set("Shops.farmers_market", 23);
        }
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack closeItem = ItemUtil.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==");
        ItemUtil.mapMeta(closeItem, meta -> meta.setDisplayName(Colors.RED + "Close"));
        list.add(new MenuItem(closeItem).setPriority(10).setSlots(40).setType(MenuItemType.CLOSE));

        return list;
    }
}
