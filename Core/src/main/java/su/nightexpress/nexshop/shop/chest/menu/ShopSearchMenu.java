package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.shop.chest.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ShopSearchMenu extends ConfigMenu<ShopPlugin> implements AutoFilled<ChestProduct>, Linked<List<ChestProduct>> {

    public static final String FILE_NAME = "shops_search.yml";

    private static final String PLACEHOLDER_ACTION_TELEPORT = "%action_teleport%";

    //private final ChestShopModule              module;
    private final ViewLink<List<ChestProduct>> link;

    private int[]        productSlots;
    private String       productName;
    private List<String> productLore;
    private List<String> actionTeleportLore;

    public ShopSearchMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, FileConfig.loadOrExtract(plugin, module.getMenusPath(), FILE_NAME));
        //this.module = module;
        this.link = new ViewLink<>();

        this.load();
    }

    @NotNull
    @Override
    public ViewLink<List<ChestProduct>> getLink() {
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
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<ChestProduct> autoFill) {
        Player player = viewer.getPlayer();

        autoFill.setSlots(this.productSlots);
        autoFill.setItems(this.getLink(player));
        autoFill.setItemCreator(product -> {
            boolean isOwn = product.getShop().isOwner(player);
            boolean canTeleport = player.hasPermission(ChestPerms.TELEPORT_OTHERS) || (isOwn && player.hasPermission(ChestPerms.TELEPORT));


            ItemStack item = new ItemStack(product.getPreview());
            ItemReplacer.create(item).hideFlags().trimmed()
                .setDisplayName(this.productName)
                .setLore(this.productLore)
                .replaceLoreExact(PLACEHOLDER_ACTION_TELEPORT, canTeleport ? this.actionTeleportLore : Collections.emptyList())
                .replace(product.getPlaceholders())
                .replace(product.getShop().getPlaceholders())
                .writeMeta();
            return item;
        });
        autoFill.setClickAction(product -> (viewer1, event) -> {
            boolean isOwn = product.getShop().isOwner(player);
            boolean canTeleport = player.hasPermission(ChestPerms.TELEPORT_OTHERS) || (isOwn && player.hasPermission(ChestPerms.TELEPORT));

            if (canTeleport) {
                product.getShop().teleport(player);
            }
        });
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Search Result"), MenuSize.CHEST_45);
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
        this.productSlots = ConfigValue.create("Product.Slots", IntStream.range(0, 36).toArray()).read(cfg);

        this.productName = ConfigValue.create("Product.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(PRODUCT_PREVIEW_NAME))
        ).read(cfg);

        this.productLore = ConfigValue.create("Product.Lore", Lists.newList(
            PRODUCT_PREVIEW_LORE,
            "",
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Shop: ") + SHOP_NAME + " " + GRAY.enclose("(by " + SHOP_OWNER + ")")),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Buy for: ") + PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY) + " " + GRAY.enclose("(" + PRODUCT_STOCK_AMOUNT_LEFT.apply(TradeType.BUY) + " left)")),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Sell for: ") + PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL) + " " + GRAY.enclose("(" + PRODUCT_STOCK_AMOUNT_LEFT.apply(TradeType.SELL) + " left)")),
            "",
            PLACEHOLDER_ACTION_TELEPORT
        )).read(cfg);

        this.actionTeleportLore = ConfigValue.create("Product.Action_Teleport", Lists.newList(
            LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Left-Click to " + LIGHT_YELLOW.enclose("teleport") + ".")
        )).read(cfg);
    }
}
