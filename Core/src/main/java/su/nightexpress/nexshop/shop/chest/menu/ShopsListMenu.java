package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestPerms;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ShopsListMenu extends AbstractMenuAuto<ExcellentShop, ChestShop> {

    private static final String PLACEHOLDER_GLOBAL = "%global%";

    private final ChestShopModule module;
    private final Map<Player, String> others;

    private final int[]  shopSlots;
    private final String shopName;
    private final List<String> shopLoreOwn;
    private final List<String> shopLoreOthers;

    public ShopsListMenu(@NotNull ChestShopModule module) {
        super(module.plugin(), JYML.loadOrExtract(module.plugin(), module.getPath() + "menu/shops_list.yml"), "");
        this.module = module;
        this.others = new WeakHashMap<>();

        this.shopSlots = cfg.getIntArray("Shop.Slots");
        this.shopName = Colorizer.apply(cfg.getString("Shop.Name", Placeholders.SHOP_NAME));
        this.shopLoreOwn = Colorizer.apply(cfg.getStringList("Shop.Lore.Own"));
        this.shopLoreOthers = Colorizer.apply(cfg.getStringList("Shop.Lore.Others"));

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
            else if (type instanceof Type type2) {
                if (type2 == Type.GLOBAL_MODE) {
                    if (this.others.containsKey(player)) {
                        this.others.remove(player);
                        this.module.getListMenu().open(player, 1);
                    }
                    else {
                        this.module.getListMenu().open(player, Placeholders.WILDCARD, 1);
                    }
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
            MenuItem menuItem = cfg.getMenuItem("Special." + sId, Type.class);
            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
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
    protected List<ChestShop> getObjects(@NotNull Player player) {
        String user = this.others.get(player);
        if (user == null) return this.module.getShops(player);

        boolean isGlobal = this.isGlobalMode(player);
        return this.module.getShops().stream()
            .filter(shop -> isGlobal || shop.getOwnerName().equalsIgnoreCase(user)).toList();
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull ChestShop shop) {
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
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull ChestShop shop) {
        return (player1, type, e) -> {
            if (e.isRightClick()) {
                if (shop.isOwner(player1) || player1.hasPermission(Perms.PLUGIN)) {
                    shop.getEditor().open(player1, 1);
                }
                return;
            }

            if ((shop.isOwner(player1) && !player1.hasPermission(ChestPerms.TELEPORT))
                || (!shop.isOwner(player1) && !player1.hasPermission(ChestPerms.TELEPORT_OTHERS))) {
                plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player1);
                return;
            }

            shop.teleport(player1);
        };
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);
        if (menuItem.getType() == Type.GLOBAL_MODE) {
            ItemUtil.replace(item, str -> str.replace(PLACEHOLDER_GLOBAL, LangManager.getBoolean(this.isGlobalMode(player))));
        }
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        this.others.remove(player);
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
