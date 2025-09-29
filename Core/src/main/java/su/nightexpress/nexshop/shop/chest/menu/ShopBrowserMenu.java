package su.nightexpress.nexshop.shop.chest.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.ChestUtils;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.language.entry.LangEnum;
import su.nightexpress.nightcore.ui.dialog.Dialog;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.item.ItemOptions;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ShopBrowserMenu extends LinkedMenu<ShopPlugin, ShopBrowserMenu.Data> implements Filled<ChestShop>, ConfigBased {

    private static final LangEnum<SortType> SORT_LOCALE = LangEnum.of("Shop.SortLocale", SortType.class);

    private final ChestShopModule  module;

    private String titleOwn;
    private String titlePlayer;
    private String titleItem;

    private boolean      shopUseProductIcon;
    private String       shopName;
    private List<String> shopLoreOwn;
    private List<String> shopLoreOthers;
    private int[]        shopSlots;

    private int          productLimit;
    private List<String> productEntry;

    public record Data(@NotNull SortType sortType, String player, String itemSearch, @Nullable ChestShop source){}

    private enum SortType {

        OWNER_NAME(Comparator.comparing(ChestShop::getOwnerName)),
        SHOP_NAME(Comparator.comparing(shop -> NightMessage.stripTags(shop.getName())));

        private final Comparator<ChestShop> comparator;

        SortType(@NotNull Comparator<ChestShop> comparator) {
            this.comparator = comparator;
        }
    }

    public ShopBrowserMenu(@NotNull ShopPlugin plugin, @NotNull ChestShopModule module) {
        super(plugin, MenuType.GENERIC_9X6, BLACK.wrap("Shop Browser"));
        this.module = module;
    }

    public void open(@NotNull Player player) {
        this.open(player, null, null, null);
    }

    public void openByPlayer(@NotNull Player player, @NotNull String ownerName) {
        this.open(player, ownerName, null, null);
    }

    public void openByItem(@NotNull Player player, @NotNull String itemSearch) {
        this.open(player, null, itemSearch, null);
    }

    public void openFromShop(@NotNull Player player, @NotNull ChestShop shop) {
        this.open(player, shop.getOwnerName(), null, shop);
    }

    private void open(@NotNull Player player, @Nullable String ownerName, @Nullable String itemSearch, @Nullable ChestShop source) {
        this.open(player, SortType.SHOP_NAME, ownerName, itemSearch, source);
    }

    private void open(@NotNull Player player, @NotNull SortType sortType, @Nullable String ownerName, @Nullable String itemSearch, @Nullable ChestShop source) {
        this.open(player, new Data(sortType, ownerName, itemSearch, source));
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        if (data.player != null) return data.player.equalsIgnoreCase(player.getName()) ? this.titleOwn : this.titlePlayer.replace(PLAYER_NAME, data.player);
        if (data.itemSearch != null) return this.titleItem.replace(GENERIC_ITEM, data.itemSearch);

        return super.getTitle(viewer);
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        if (viewer.hasItem(menuItem)) return;

        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        item.replacement(replacer -> replacer.replace(Placeholders.GENERIC_TYPE, SORT_LOCALE.getLocalized(data.sortType)));
    }

    @Override
    @NotNull
    public MenuFiller<ChestShop> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        String ownerName = data.player;
        String itemName = data.itemSearch;

        List<ChestShop> shops = this.module.lookup().getAll().stream()
            .filter(ChestShop::isActive)
            .filter(ChestShop::hasProducts)
            .filter(shop -> {
                if (ownerName != null && !shop.getOwnerName().equalsIgnoreCase(ownerName)) return false;
                if (itemName != null && !ChestUtils.containsItem(shop, itemName)) return false;
                return true;
            })
            .sorted(data.sortType.comparator)
            .toList();

        return MenuFiller.builder(this)
            .setSlots(this.shopSlots)
            .setItems(shops)
            .setItemCreator(shop -> {
                List<ChestProduct> products = shop.getValidProducts();
                List<String> productInfo = new ArrayList<>();
                int productCount = 0;
                for (ChestProduct product : products) {
                    if (productCount >= this.productLimit) break;
                    for (String line : this.productEntry) {
                        productInfo.add(product.replacePlaceholders().apply(line));
                    }
                    productCount++;
                }

                NightItem item;
                if (this.shopUseProductIcon) {
                    item = NightItem.fromItemStack(products.getFirst().getPreview());
                }
                else {
                    item = NightItem.fromType(shop.isChunkLoaded() ? shop.location().getBlockType() : Material.CHEST);
                }

                return item
                    .hideAllComponents()
                    .setDisplayName(this.shopName)
                    .setLore(shop.isOwner(player) ? this.shopLoreOwn : this.shopLoreOthers)
                    .replacement(replacer -> replacer
                        .replace(GENERIC_PRODUCTS, productInfo)
                        .replace(shop.replacePlaceholders())
                    );
            })
            .setItemClick(shop -> (viewer1, event) -> {
                Permission permission = shop.isOwner(player) ? ChestPerms.TELEPORT : ChestPerms.TELEPORT_OTHERS;
                if (!player.hasPermission(permission)) {
                    CoreLang.ERROR_NO_PERMISSION.message().send(player);
                    return;
                }

                this.module.teleportToShop(player, shop);
            })
            .build();
    }

    private void handleSearch(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);

        this.handleInput(Dialog.builder(player, input -> {
            // .runNextTick hack to override dialog GUI restoration.
            this.runNextTick(() -> {
                player.closeInventory();
                this.open(player, data.sortType, data.player, input.getTextRaw(), data.source);
            });
            return true;
        }));

        ChestLang.SEARCH_PROMPT_ITEM_NAME.message().send(player);
    }

    private void handleSorting(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        SortType sortType = Lists.next(data.sortType);

        this.runNextTick(() -> this.open(player, sortType, data.player, data.itemSearch, data.source));
    }

    private void handleReturn(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        Data data = this.getLink(player);
        if (data.source != null) {
            this.runNextTick(() -> this.module.openShopSettings(player, data.source));
        }
        else if (data.player != null) {
            this.runNextTick(() -> this.module.browseShopOwners(player));
        }
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        SORT_LOCALE.load(config);

        this.titleOwn = ConfigValue.create("Title.Own", BLACK.wrap("My Shops")).read(config);
        this.titlePlayer = ConfigValue.create("Title.Player", BLACK.wrap(PLAYER_NAME + "'s Shops")).read(config);
        this.titleItem = ConfigValue.create("Title.ItemSearch", BLACK.wrap("Shops found with '" + GENERIC_ITEM + "'")).read(config);

        this.shopUseProductIcon = ConfigValue.create("Shop.UseProductIcon", true).read(config);

        this.shopSlots = ConfigValue.create("Shop.Slots", new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34}).read(config);

        this.shopName = ConfigValue.create("Shop.Name",
            SHOP_NAME
        ).read(config);

        this.shopLoreOwn = ConfigValue.create("Shop.Lore.Own", Lists.newList(
            GRAY.wrap(LIGHT_YELLOW.wrap("➥ ") + "Location: " + LIGHT_YELLOW.wrap(CHEST_SHOP_X) + ", " + LIGHT_YELLOW.wrap(CHEST_SHOP_Y) + ", " + LIGHT_YELLOW.wrap(CHEST_SHOP_Z) + " in " + LIGHT_YELLOW.wrap(CHEST_SHOP_WORLD)),
            GRAY.wrap(LIGHT_YELLOW.wrap("➥ ") + "Owner: " + LIGHT_YELLOW.wrap(CHEST_SHOP_OWNER)) + " " + LIGHT_GREEN.wrap("(You)"),
            EMPTY_IF_BELOW,
            GENERIC_PRODUCTS,
            EMPTY_IF_ABOVE,
            LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to teleport"))
        )).read(config);

        this.shopLoreOthers = ConfigValue.create("Shop.Lore.Others", Lists.newList(
            GRAY.wrap(LIGHT_YELLOW.wrap("➥ ") + "Location: " + LIGHT_YELLOW.wrap(CHEST_SHOP_X) + ", " + LIGHT_YELLOW.wrap(CHEST_SHOP_Y) + ", " + LIGHT_YELLOW.wrap(CHEST_SHOP_Z) + " in " + LIGHT_YELLOW.wrap(CHEST_SHOP_WORLD)),
            GRAY.wrap(LIGHT_YELLOW.wrap("➥ ") + "Owner: " + LIGHT_YELLOW.wrap(CHEST_SHOP_OWNER)),
            EMPTY_IF_BELOW,
            GENERIC_PRODUCTS,
            EMPTY_IF_ABOVE,
            LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to teleport"))
        )).read(config);

        this.productLimit = ConfigValue.create("Shop.ProductInfo.DisplayLimit", 3).read(config);

        this.productEntry = ConfigValue.create("Shop.ProductInfo.Entry", Lists.newList(
            GRAY.wrap(WHITE.wrap(PRODUCT_UNIT_AMOUNT + "x " + PRODUCT_PREVIEW_NAME) + " (" + GREEN.wrap(PRODUCT_PRICE_FORMATTED.apply(TradeType.BUY)) + " : " + RED.wrap(PRODUCT_PRICE_FORMATTED.apply(TradeType.SELL)) + ")")
        )).read(config);

        loader.addDefaultItem(NightItem.fromType(Material.COMPASS)
            .setDisplayName(LIGHT_YELLOW.wrap(BOLD.wrap("Search Item")))
            .setLore(Lists.newList(
                GRAY.wrap("Search for shops contains"),
                GRAY.wrap("specific item(s)."),
                "",
                LIGHT_YELLOW.wrap("→ " + UNDERLINED.wrap("Click to search"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(46)
            .setHandler(new ItemHandler("search_item", (viewer, event) -> this.handleSearch(viewer)))
        );

        loader.addDefaultItem(NightItem.fromType(Material.COMPARATOR)
            .setDisplayName(LIGHT_RED.wrap(BOLD.wrap("Sorting Mode")))
            .setLore(Lists.newList(
                LIGHT_RED.wrap("➥ " + GRAY.wrap("Current: ") + GENERIC_TYPE),
                "",
                GRAY.wrap("Changes shop display order."),
                "",
                LIGHT_RED.wrap("→ " + UNDERLINED.wrap("Click to toggle"))
            ))
            .hideAllComponents()
            .toMenuItem()
            .setPriority(10)
            .setSlots(52)
            .setHandler(new ItemHandler("sorting", (viewer, event) -> this.handleSorting(viewer)))
        );

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE).setHideTooltip(true).toMenuItem().setSlots(IntStream.range(45, 54).toArray()));

        loader.addDefaultItem(MenuItem.buildReturn(this, 49, (viewer, event) -> this.handleReturn(viewer),
            ItemOptions.builder()
                .setVisibilityPolicy(viewer -> this.getLink(viewer).source != null || this.getLink(viewer).player != null)
                .build()
        ).setPriority(10));

        loader.addDefaultItem(MenuItem.buildExit(this, 49).setPriority(1));
        loader.addDefaultItem(MenuItem.buildNextPage(this, 26));
        loader.addDefaultItem(MenuItem.buildPreviousPage(this, 18));
    }
}
