package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
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
import su.nexmedia.engine.api.menu.link.Linked;
import su.nexmedia.engine.api.menu.link.ViewLink;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemReplacer;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.impl.AbstractShop;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ShopListMenu extends ConfigMenu<ExcellentShop> implements AutoPaged<ChestShop>, Linked<UUID> {

    public static final String FILE = "shops_list.yml";

    private final ChestShopModule   module;
    private final ViewLink<UUID> link;

    private final int[]  shopSlots;
    private final String shopName;
    private final List<String> shopLoreOwn;
    private final List<String> shopLoreOthers;

    public ShopListMenu(@NotNull ExcellentShop plugin, @NotNull ChestShopModule module) {
        super(plugin, JYML.loadOrExtract(plugin, module.getMenusPath(), FILE));
        this.module = module;
        this.link = new ViewLink<>();

        this.shopSlots = cfg.getIntArray("Shop.Slots");
        this.shopName = Colorizer.apply(cfg.getString("Shop.Name", Placeholders.SHOP_NAME));
        this.shopLoreOwn = Colorizer.apply(cfg.getStringList("Shop.Lore.Own"));
        this.shopLoreOthers = Colorizer.apply(cfg.getStringList("Shop.Lore.Others"));

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
        boolean isOwn = this.getOwnerId(player).equals(player.getUniqueId());

        ItemStack item = new ItemStack(shop.getBlockType());
        ItemReplacer.create(item).hideFlags().trimmed()
            .setDisplayName(this.shopName).setLore(isOwn ? this.shopLoreOwn : this.shopLoreOthers)
            .replace(shop.replacePlaceholders())
            .writeMeta();
        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull ChestShop shop) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            if (event.isRightClick()) {
                if (shop.isOwner(player) || player.hasPermission(ChestPerms.REMOVE_OTHERS)) {
                    shop.openMenu(player);
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
}
