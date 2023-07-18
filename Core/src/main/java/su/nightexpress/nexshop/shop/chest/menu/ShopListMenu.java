package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ShopListMenu extends ConfigMenu<ExcellentShop> implements AutoPaged<ChestShop> {

    private static final String PLACEHOLDER_GLOBAL = "%global%";

    private final ChestShopModule module;
    private final Map<Player, String> others;

    private final int[]  shopSlots;
    private final String shopName;
    private final List<String> shopLoreOwn;
    private final List<String> shopLoreOthers;

    public ShopListMenu(@NotNull ChestShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getLocalPath() + "/menu/", "shops_list.yml"));
        this.module = module;
        this.others = new WeakHashMap<>();

        this.shopSlots = cfg.getIntArray("Shop.Slots");
        this.shopName = Colorizer.apply(cfg.getString("Shop.Name", Placeholders.SHOP_NAME));
        this.shopLoreOwn = Colorizer.apply(cfg.getStringList("Shop.Lore.Own"));
        this.shopLoreOthers = Colorizer.apply(cfg.getStringList("Shop.Lore.Others"));

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, (viewer, event) -> plugin.runTask(task -> viewer.getPlayer().closeInventory()))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this));

        this.registerHandler(Type.class)
            .addClick(Type.GLOBAL_MODE, (viewer, event) -> {
                Player player = viewer.getPlayer();
                if (this.others.containsKey(player)) {
                    this.others.remove(player);
                    this.module.getListMenu().open(player, 1);
                }
                else {
                    this.module.getListMenu().open(player, Placeholders.WILDCARD, 1);
                }
            });

        this.load();

        this.getItems().forEach(menuItem -> {
            if (menuItem.getType() == Type.GLOBAL_MODE) {
                menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                    ItemUtil.replace(item, str -> str.replace(PLACEHOLDER_GLOBAL, LangManager.getBoolean(this.isGlobalMode(viewer.getPlayer()))));
                });
            }
        });
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    private enum Type {
        GLOBAL_MODE,
    }

    private boolean isGlobalMode(@NotNull Player player) {
        String user = this.others.get(player);
        return user != null && user.equalsIgnoreCase(Placeholders.WILDCARD);
    }

    public void open(@NotNull Player player, @NotNull String user, int page) {
        this.others.put(player, user);
        this.open(player, page);
    }

    @Override
    public int[] getObjectSlots() {
        return shopSlots;
    }

    @Override
    @NotNull
    public List<ChestShop> getObjects(@NotNull Player player) {
        String user = this.others.get(player);
        if (user == null) return new ArrayList<>(this.module.getShops(player));

        boolean isGlobal = this.isGlobalMode(player);
        return this.module.getShops().stream()
            .filter(shop -> isGlobal || shop.getOwnerName().equalsIgnoreCase(user)).toList();
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull ChestShop shop) {
        ItemStack item = new ItemStack(shop.getLocation().getBlock().getType());
        ItemUtil.mapMeta(item, meta -> {
            meta.setDisplayName(this.shopName);
            meta.setLore(this.others.containsKey(player) ? this.shopLoreOthers : this.shopLoreOwn);
            meta.addItemFlags(ItemFlag.values());
            ItemUtil.replace(meta, shop.replacePlaceholders());
        });
        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull ChestShop shop) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            if (event.isRightClick()) {
                if (shop.isOwner(player) || player.hasPermission(Perms.PLUGIN)) {
                    shop.getEditor().open(player, 1);
                }
                return;
            }

            if ((shop.isOwner(player) && !player.hasPermission(ChestPerms.TELEPORT))
                || (!shop.isOwner(player) && !player.hasPermission(ChestPerms.TELEPORT_OTHERS))) {
                plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
                return;
            }

            shop.teleport(player);
        };
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        super.onClose(viewer, event);
        this.others.remove(viewer.getPlayer());
    }
}
