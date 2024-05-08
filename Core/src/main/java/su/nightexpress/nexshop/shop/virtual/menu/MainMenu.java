package su.nightexpress.nexshop.shop.virtual.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.ItemOptions;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static su.nightexpress.nightcore.util.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class MainMenu extends ConfigMenu<ShopPlugin> {

    public static final String FILE_NAME = "main.menu.yml";

    private final VirtualShopModule module;

    private Map<String, Integer> shopSlotMap;

    public MainMenu(@NotNull ShopPlugin plugin, @NotNull VirtualShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getLocalPath(), FILE_NAME));
        this.module = module;

        this.load();

        if (Config.GUI_PLACEHOLDER_API.get()) {
            this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                ItemReplacer.replacePlaceholderAPI(item, viewer.getPlayer());
            }));
        }
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.shopSlotMap.forEach((shopId, slot) -> {
            VirtualShop shop = this.module.getShopById(shopId);
            if (shop == null) {
                this.module.error("Invalid shop in the main menu: '" + shopId + "' !");
                return;
            }

            ItemStack icon = shop.getIcon();
            ItemReplacer.create(icon).trimmed().hideFlags()
                .setDisplayName(VirtualConfig.SHOP_FORMAT_NAME.get())
                .setLore(VirtualConfig.SHOP_FORMAT_LORE.get())
                .replace(shop.getPlaceholders())
                .writeMeta();

            MenuItem menuItem = new MenuItem(icon);
            menuItem.setSlots(slot);
            menuItem.setPriority(Integer.MAX_VALUE);
            menuItem.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
            if (VirtualConfig.MAIN_MENU_HIDE_NO_PERM_SHOPS.get()) {
                menuItem.getOptions().setVisibilityPolicy(viewer1 -> shop.canAccess(viewer1.getPlayer(), false));
            }
            menuItem.setHandler((viewer1, event) -> {
                this.runNextTick(() -> shop.open(viewer1.getPlayer()));
            });
            this.addItem(menuItem);
        });
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose(BOLD.enclose("Shop")), MenuSize.CHEST_45);
    }

    @Override
    protected void loadAdditional() {
        this.shopSlotMap = ConfigValue.forMap("Shops",
            (cfg, path, id) -> cfg.getInt(path + "." + id, -1),
            (cfg, path, map) -> map.forEach((id, slot) -> cfg.set(path + "." + id, slot)),
            () -> {
                Map<String, Integer> map = new HashMap<>();
                map.put("tools", 10);
                map.put("weapons", 11);
                map.put("ingredients", 12);
                map.put("wool", 13);
                map.put("blocks", 14);
                map.put("peaceful_loot", 15);
                map.put("hostile_loot", 16);
                map.put("fish_market", 21);
                map.put("food", 22);
                map.put("farmers_market", 23);
                return map;
            }
        ).read(cfg);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack exitItem = ItemUtil.getSkinHead(SKIN_WRONG_MARK);
        ItemUtil.editMeta(exitItem, meta -> {
            meta.setDisplayName(LIGHT_RED.enclose(BOLD.enclose("Exit")));
        });
        list.add(new MenuItem(exitItem).setSlots(40).setPriority(10).setHandler(ItemHandler.forClose(this)));

        return list;
    }
}
