package su.nightexpress.nexshop.shop.chest.menu;

import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.link.Linked;
import su.nexmedia.engine.api.menu.link.ViewLink;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.impl.AbstractShop;

import java.util.*;

import static su.nexmedia.engine.utils.Colors2.*;

public class ShopListMenu extends ConfigMenu<ExcellentShop> implements AutoPaged<ChestShop>, Linked<UUID> {

    public static final String FILE = "shops_list.yml";

    private static final String PLACEHOLDER_ACTION_TELEPORT = "%action_teleport%";
    private static final String PLACEHOLDER_ACTION_EDITOR = "%action_editor%";

    private final ChestShopModule   module;
    private final ViewLink<UUID> link;

    private final int[]  shopSlots;
    private final String shopName;
    private final List<String> shopLoreOwn;
    private final List<String> shopLoreOthers;
    private final List<String> actionTeleportLore;
    private final List<String> actionEditLore;

    public ShopListMenu(@NotNull ExcellentShop plugin, @NotNull ChestShopModule module) {
        super(plugin, JYML.loadOrExtract(plugin, module.getMenusPath(), FILE));
        this.module = module;
        this.link = new ViewLink<>();

        this.shopSlots = cfg.getIntArray("Shop.Slots");
        this.shopName = cfg.getString("Shop.Name", Placeholders.SHOP_NAME);
        this.shopLoreOwn = cfg.getStringList("Shop.Lore.Own");
        this.shopLoreOthers = cfg.getStringList("Shop.Lore.Others");
        this.actionTeleportLore = JOption.create("Shop.Lore.Action_Teleport", Lists.newArrayList(
            LIGHT_YELLOW + "[▶] " + LIGHT_GRAY + "Left-Click to " + LIGHT_YELLOW + "teleport" + LIGHT_GRAY + "."
        )).read(cfg);
        this.actionEditLore = JOption.create("Shop.Lore.Action_Editor", Lists.newArrayList(
            LIGHT_YELLOW + "[▶] " + LIGHT_GRAY + "Right-Click to " + LIGHT_YELLOW + "edit" + LIGHT_GRAY + "."
        )).read(cfg);

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.RETURN, (viewer, event) -> {
                this.module.getBrowseMenu().openNextTick(viewer, 1);
            })
            .addClick(MenuItemType.CLOSE, (viewer, event) -> plugin.runTask(task -> viewer.getPlayer().closeInventory()))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this));

        this.load();
    }

    @NotNull
    @Override
    public ViewLink<UUID> getLink() {
        return link;
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @NotNull
    public UUID getOwnerId(@NotNull Player player) {
        return this.getLink().get(player);
    }

    @Override
    public int[] getObjectSlots() {
        return shopSlots;
    }

    @Override
    @NotNull
    public List<ChestShop> getObjects(@NotNull Player player) {
        UUID ownerId = this.getOwnerId(player);
        return this.module.getShops(ownerId).stream().sorted(Comparator.comparing(AbstractShop::getName)).toList();
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull ChestShop shop) {
        boolean isOwn = shop.isOwner(player);
        boolean canEdit = isOwn || player.hasPermission(ChestPerms.EDIT_OTHERS);
        boolean canTeleport = player.hasPermission(ChestPerms.TELEPORT_OTHERS) || (isOwn && player.hasPermission(ChestPerms.TELEPORT));

        ItemStack item = new ItemStack(shop.getBlockType());
        ItemReplacer.create(item).hideFlags().trimmed()
            .setDisplayName(this.shopName).setLore(isOwn ? this.shopLoreOwn : this.shopLoreOthers)
            .replaceLoreExact(PLACEHOLDER_ACTION_EDITOR, canEdit ? this.actionEditLore : Collections.emptyList())
            .replaceLoreExact(PLACEHOLDER_ACTION_TELEPORT, canTeleport ? this.actionTeleportLore : Collections.emptyList())
            .replace(shop.replacePlaceholders())
            .replace(Colorizer::apply)
            .writeMeta();
        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull ChestShop shop) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            boolean isOwn = shop.isOwner(player);

            if (event.isRightClick()) {
                if (isOwn || player.hasPermission(ChestPerms.EDIT_OTHERS)) {
                    shop.openMenu(player);
                }
                return;
            }

            if (player.hasPermission(ChestPerms.TELEPORT_OTHERS) || (isOwn && player.hasPermission(ChestPerms.TELEPORT))) {
                shop.teleport(player);
            }
        };
    }
}
