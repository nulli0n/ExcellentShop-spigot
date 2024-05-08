package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.impl.AbstractShop;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;

import java.util.*;
import java.util.stream.IntStream;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.nexshop.shop.chest.Placeholders.*;

public class ShopListMenu extends ConfigMenu<ShopPlugin> implements AutoFilled<ChestShop>, Linked<String> {

    public static final String FILE_NAME = "shops_list.yml";

    private static final String PLACEHOLDER_ACTION_TELEPORT = "%action_teleport%";
    private static final String PLACEHOLDER_ACTION_EDITOR   = "%action_editor%";

    private final ChestShopModule  module;
    private final ViewLink<String> link;
    private final ItemHandler      returnHandler;

    private int[]        shopSlots;
    private String       shopName;
    private List<String> shopLoreOwn;
    private List<String> shopLoreOthers;
    private List<String> actionTeleportLore;
    private List<String> actionEditLore;

    public ShopListMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE_NAME));
        this.module = module;
        this.link = new ViewLink<>();

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            this.runNextTick(() -> this.module.browseShops(viewer.getPlayer()));
        }));

        this.load();
    }

    @NotNull
    @Override
    public ViewLink<String> getLink() {
        return link;
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<ChestShop> autoFill) {
        Player player = viewer.getPlayer();
        String ownerName = this.getLink(player);

        autoFill.setSlots(this.shopSlots);
        autoFill.setItems(this.module.getShops(ownerName).stream().sorted(Comparator.comparing(AbstractShop::getName)).toList());
        autoFill.setItemCreator(shop -> {
            boolean isOwn = shop.isOwner(player);
            boolean canEdit = isOwn || player.hasPermission(ChestPerms.EDIT_OTHERS);
            boolean canTeleport = player.hasPermission(ChestPerms.TELEPORT_OTHERS) || (isOwn && player.hasPermission(ChestPerms.TELEPORT));

            ItemStack item = new ItemStack(shop.getBlockType());
            ItemReplacer.create(item).hideFlags().trimmed()
                .setDisplayName(this.shopName).setLore(isOwn ? this.shopLoreOwn : this.shopLoreOthers)
                .replaceLoreExact(PLACEHOLDER_ACTION_EDITOR, canEdit ? this.actionEditLore : Collections.emptyList())
                .replaceLoreExact(PLACEHOLDER_ACTION_TELEPORT, canTeleport ? this.actionTeleportLore : Collections.emptyList())
                .replace(shop.replacePlaceholders())
                .writeMeta();
            return item;
        });
        autoFill.setClickAction(shop -> (viewer1, event) -> {
            boolean isOwn = shop.isOwner(player);

            if (event.isRightClick()) {
                if (isOwn || player.hasPermission(ChestPerms.EDIT_OTHERS)) {
                    this.module.openShopSettings(player, shop);
                }
                return;
            }

            if (player.hasPermission(ChestPerms.TELEPORT_OTHERS) || (isOwn && player.hasPermission(ChestPerms.TELEPORT))) {
                shop.teleport(player);
            }
        });
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Shops"), MenuSize.CHEST_45);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack backItem = ItemUtil.getSkinHead(SKIN_ARROW_DOWN);
        ItemUtil.editMeta(backItem, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_RETURN.getDefaultName());
        });
        list.add(new MenuItem(backItem).setSlots(40).setPriority(10).setHandler(this.returnHandler));

        ItemStack prevPage = ItemUtil.getSkinHead(SKIN_ARROW_LEFT);
        ItemUtil.editMeta(prevPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_PREVIOUS_PAGE.getDefaultName());
        });
        list.add(new MenuItem(prevPage).setSlots(39).setPriority(10).setHandler(ItemHandler.forPreviousPage(this)));

        ItemStack nextPage = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_NEXT_PAGE.getDefaultName());
        });
        list.add(new MenuItem(nextPage).setSlots(41).setPriority(10).setHandler(ItemHandler.forNextPage(this)));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.shopSlots = ConfigValue.create("Shop.Slots", IntStream.range(0, 36).toArray()).read(cfg);

        this.shopName = ConfigValue.create("Shop.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(SHOP_NAME))
        ).read(cfg);

        this.shopLoreOwn = ConfigValue.create("Shop.Lore.Own", Lists.newList(
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Location:") + " " + SHOP_LOCATION_X + LIGHT_GRAY.enclose(", ") + SHOP_LOCATION_Y + LIGHT_GRAY.enclose(", ") + SHOP_LOCATION_Z + LIGHT_GRAY.enclose(" in ") + SHOP_LOCATION_WORLD),
            "",
            PLACEHOLDER_ACTION_TELEPORT,
            PLACEHOLDER_ACTION_EDITOR
        )).read(cfg);

        this.shopLoreOthers = ConfigValue.create("Shop.Lore.Others", Lists.newList(
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Owner:") + " " + SHOP_OWNER),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Location:") + " " + SHOP_LOCATION_X + LIGHT_GRAY.enclose(", ") + SHOP_LOCATION_Y + LIGHT_GRAY.enclose(", ") + SHOP_LOCATION_Z + LIGHT_GRAY.enclose(" in ") + SHOP_LOCATION_WORLD),
            "",
            PLACEHOLDER_ACTION_TELEPORT,
            PLACEHOLDER_ACTION_EDITOR
        )).read(cfg);

        this.actionTeleportLore = ConfigValue.create("Shop.Lore.Action_Teleport", Lists.newList(
            LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("teleport") + ".")
        )).read(cfg);

        this.actionEditLore = ConfigValue.create("Shop.Lore.Action_Editor", Lists.newList(
            LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Right-Click to " + LIGHT_YELLOW.enclose("edit") + ".")
        )).read(cfg);
    }
}
