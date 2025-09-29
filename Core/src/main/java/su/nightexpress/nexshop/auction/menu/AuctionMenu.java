package su.nightexpress.nexshop.auction.menu;

import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.ListingCategory;
import su.nightexpress.nexshop.auction.SortType;
import su.nightexpress.nexshop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.auction.config.AuctionLang;
import su.nightexpress.nexshop.auction.config.AuctionPerms;
import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;
import su.nightexpress.nightcore.util.placeholder.Replacer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class AuctionMenu extends AbstractAuctionMenu<ActiveListing> {

    public static final String FILE_NAME = "main.yml";

    private static final Map<Player, SortType>        LISTING_ORDER = new WeakHashMap<>();
    private static final Map<Player, ListingCategory> CATEGORY      = new WeakHashMap<>();
    private static final Map<Player, Currency>        CURRENCY      = new WeakHashMap<>();

    private static final String PLACEHOLDER_LORE_FORMAT      = "%lore_format%";
    private static final String PLACEHOLDER_CATEGORIES       = "%categories%";
    private static final String PLACEHOLDER_CURRENCIES       = "%currencies%";
    private static final String PLACEHOLDER_ORDERS           = "%orders%";
    private static final String PLACEHOLDER_EXPIRED_AMOUNT   = "%expired_amount%";
    private static final String PLACEHOLDER_HISTORY_AMOUNT   = "%history_amount%";
    private static final String PLACEHOLDER_UNCLAIMED_AMOUNT = "%unclaimed_amount%";
    private static final String PLACEHOLDER_LISTING_ORDER    = "%listing_order%";
    private static final String PLACEHOLDER_ACTION_PREVIEW   = "%action_preview%";

    private final ItemHandler orderHandler;
    private final ItemHandler categoryHandler;
    private final ItemHandler currencyHandler;

    private List<String> itemLoreOwn;
    private List<String> itemLorePlayer;
    private List<String> itemLoreAdmin;

    private List<String> lorePreview;
    private String loreListUnselected;
    private String loreListSelected;

    public AuctionMenu(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager) {
        super(plugin, auctionManager, FILE_NAME);

        this.addHandler(this.orderHandler = new ItemHandler("listing_order", (viewer, event) -> {
            Player player = viewer.getPlayer();
            SortType type = getListingOrder(player);
            setListingOrder(player, event.isLeftClick() ? Lists.next(type) : Lists.previous(type));
            this.runNextTick(() -> this.flush(viewer));
        }));

        this.addHandler(this.categoryHandler = new ItemHandler("category_filter", (viewer, event) -> {
            List<ListingCategory> categories = new ArrayList<>(this.auctionManager.getCategories());
            ListingCategory current = this.getCategory(viewer.getPlayer());
            ListingCategory selected = Lists.shifted(categories, categories.indexOf(current), event.isLeftClick() ? 1 : -1);
            CATEGORY.put(viewer.getPlayer(), selected);
            this.runNextTick(() -> this.flush(viewer));
        }));

        this.addHandler(this.currencyHandler = new ItemHandler("currency_filter", (viewer, event) -> {
            if (event.isShiftClick() && event.isRightClick()) {
                CURRENCY.remove(viewer.getPlayer());
                this.runNextTick(() -> this.flush(viewer));
                return;
            }

            List<Currency> currencies = new ArrayList<>(this.auctionManager.getAvailableCurrencies(viewer.getPlayer()));
            Currency current = this.getCurrency(viewer.getPlayer());
            Currency selected = Lists.shifted(currencies, currencies.indexOf(current), event.isLeftClick() ? 1 : -1);
            CURRENCY.put(viewer.getPlayer(), selected);
            this.runNextTick(() -> this.flush(viewer));
        }));

        this.load();

        this.getItems().forEach(menuItem -> {
            if (menuItem.getHandler() == this.categoryHandler) {
                menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                    Player player = viewer.getPlayer();
                    ListingCategory userCategory = this.getCategory(player);
                    String list = this.auctionManager.getCategories().stream().map(category -> {
                        String entry = userCategory == category ? this.loreListSelected : this.loreListUnselected;
                        return entry.replace(GENERIC_NAME, category.getName());
                    }).collect(Collectors.joining("\n"));

                    ItemReplacer.replace(itemStack, str -> str.replace(PLACEHOLDER_CATEGORIES, list));
                });
            }
            else if (menuItem.getHandler() == this.currencyHandler) {
                menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                    Player player = viewer.getPlayer();
                    Currency userCurrency = this.getCurrency(player);
                    String list = this.auctionManager.getAvailableCurrencies(player).stream().map(currency -> {
                        String entry = userCurrency == currency ? this.loreListSelected : this.loreListUnselected;
                        return entry.replace(GENERIC_NAME, currency.getName());
                    }).collect(Collectors.joining("\n"));

                    ItemReplacer.replace(itemStack, str -> str.replace(PLACEHOLDER_CURRENCIES, list));
                });
            }
            else if (menuItem.getHandler() == this.orderHandler) {
                menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                    SortType type = getListingOrder(viewer.getPlayer());
                    String list = Stream.of(SortType.values()).map(sortType -> {
                        String entry = type == sortType ? this.loreListSelected : this.loreListUnselected;
                        return entry.replace(GENERIC_NAME, AuctionLang.SORT_TYPE.getLocalized(sortType));
                    }).collect(Collectors.joining("\n"));

                    ItemReplacer.replace(itemStack, str -> str
                        .replace(PLACEHOLDER_ORDERS, list)
                        .replace(PLACEHOLDER_LISTING_ORDER, AuctionLang.SORT_TYPE.getLocalized(type))
                    );
                });
            }
            else if (menuItem.getHandler() == this.expiredHandler) {
                menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                    ItemReplacer.replace(itemStack, str -> str.replace(PLACEHOLDER_EXPIRED_AMOUNT, NumberUtil.format(auctionManager.getListings().getExpired(viewer.getPlayer()).size())));
                });
            }
            else if (menuItem.getHandler() == this.historyHandler) {
                menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                    ItemReplacer.replace(itemStack, str -> str.replace(PLACEHOLDER_HISTORY_AMOUNT, NumberUtil.format(auctionManager.getListings().getClaimed(viewer.getPlayer()).size())));
                });
            }
            else if (menuItem.getHandler() == this.unclaimedHandler) {
                menuItem.getOptions().addDisplayModifier((viewer, itemStack) -> {
                    ItemReplacer.replace(itemStack, str -> str.replace(PLACEHOLDER_UNCLAIMED_AMOUNT, NumberUtil.format(auctionManager.getListings().getUnclaimed(viewer.getPlayer()).size())));
                });
            }
            else {
                menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                    PlaceholderMap placeholderMap = new PlaceholderMap()
                        .add(GENERIC_TAX, () -> NumberUtil.format(AuctionConfig.LISTINGS_SELL_TAX.get()))
                        .add(GENERIC_EXPIRE, () -> TimeUtil.formatTime(AuctionConfig.LISTINGS_EXPIRE_TIME.get() * 1000L));
                    ItemReplacer.replace(item, placeholderMap.replacer());
                });
            }
        });
    }

    public boolean isContainer(@NotNull ActiveListing listing) {
        ItemStack item = listing.getItemStack();
        if (item.getItemMeta() instanceof BlockStateMeta meta) {
            return meta.getBlockState() instanceof Container;
        }
        return false;
    }

    @NotNull
    public static SortType getListingOrder(@NotNull Player player) {
        return LISTING_ORDER.computeIfAbsent(player, type -> SortType.NEWEST);
    }

    public static void setListingOrder(@NotNull Player player, @NotNull SortType sortType) {
        LISTING_ORDER.put(player, sortType);
    }

    @Nullable
    public Currency getCurrency(@NotNull Player player) {
        return CURRENCY.get(player);
    }

    @NotNull
    public ListingCategory getCategory(@NotNull Player player) {
        return CATEGORY.getOrDefault(player, this.auctionManager.getDefaultCategory());
    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<ActiveListing> autoFill) {
        super.onAutoFill(viewer, autoFill);

        Player player = viewer.getPlayer();
        ListingCategory category = this.getCategory(player);
        Currency currencies = this.getCurrency(player);

        autoFill.setItemCreator(listing -> {
            ItemStack item = new ItemStack(listing.getItemStack());

            List<String> previewLore = new ArrayList<>();

            if (AuctionConfig.MENU_CONTAINER_PREVIEW_ENABLED.get() && this.isContainer(listing)) {
                previewLore.addAll(this.lorePreview);
            }

            AuctionUtils.hideListingAttributes(item);

            return NightItem.fromItemStack(item)
                .setDisplayName(this.itemName)
                .setLore(this.itemLore)
                .replacement(replacer -> replacer
                    .replace(PLACEHOLDER_LORE_FORMAT, Replacer.create().replace(PLACEHOLDER_ACTION_PREVIEW, previewLore).apply(this.getLoreFormat(player, listing)))
                    //.replace(PLACEHOLDER_ACTION_PREVIEW, previewLore)
                    .replace(listing.replacePlaceholders())
                )
                .getItemStack();

//            ItemReplacer.create(item).trimmed()
//                .setDisplayName(this.itemName)
//                .setLore(this.itemLore)
//                .injectLore(PLACEHOLDER_LORE_FORMAT, this.getLoreFormat(player, listing))
//                .replaceLoreExact(PLACEHOLDER_ACTION_PREVIEW, previewLore)
//                .replace(listing.replacePlaceholders())
//                .replacePlaceholderAPI(player)
//                .writeMeta();
//
//            return item;
        });

        autoFill.setItems(this.auctionManager.getListings().getActive().stream()
            .filter(AbstractListing::isValid)
            .filter(listing -> category.isItemOfThis(listing.getItemStack()))
            .filter(listing -> currencies == null || currencies == listing.getCurrency())
            .sorted(getListingOrder(player).getComparator()).toList()
        );

        autoFill.setClickAction(listing -> (viewer1, event) -> {
            boolean isOwner = listing.isOwner(player);
            boolean isBedrock = Players.isBedrock(player);

            if ((event.isShiftClick() && event.isRightClick()) || (isOwner && isBedrock)) {
                if (isOwner|| player.hasPermission(AuctionPerms.LISTING_REMOVE_OTHERS)) {
                    this.auctionManager.takeListing(player, listing);
                    this.runNextTick(() -> this.flush(viewer));
                }
                return;
            }

            if (AuctionConfig.MENU_CONTAINER_PREVIEW_ENABLED.get() && event.isRightClick()) {
                ItemStack item = listing.getItemStack();
                if (item.getItemMeta() instanceof BlockStateMeta meta) {
                    if (meta.getBlockState() instanceof Container container) {
                        ContainerPreview preview = new ContainerPreview(this.plugin, this.auctionManager, container, viewer1.getPage());
                        this.runNextTick(() -> preview.open(viewer));
                        return;
                    }
                }
            }

            if (isOwner) return;

            if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY.get() && player.getInventory().firstEmpty() < 0) {
                Lang.SHOP_PRODUCT_ERROR_FULL_INVENTORY.message().send(player);
                return;
            }

            this.runNextTick(() -> this.auctionManager.openPurchaseConfirmation(player, listing));
        });
    }

    @NotNull
    protected List<String> getLoreFormat(@NotNull Player player, @NotNull ActiveListing aucItem) {
        List<String> format = this.itemLorePlayer;

        if (player.hasPermission(AuctionPerms.LISTING_REMOVE_OTHERS)) format = this.itemLoreAdmin;
        else if (aucItem.isOwner(player)) format = this.itemLoreOwn;

        return new ArrayList<>(format);
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        MenuOptions options = new MenuOptions(BLACK.enclose("Auction House"), MenuSize.CHEST_54);
        options.setAutoRefresh(1);
        return options;
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        // TODO
        //ItemStack backGround = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        //list.add(new MenuItem(backGround).setSlots(IntStream.range(36, 54).toArray()).setPriority(0));

        // TODO
        /*ItemStack prevPage = ItemUtil.getSkinHead(SKIN_ARROW_LEFT);
        ItemUtil.editMeta(prevPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_PREVIOUS_PAGE.getDefaultName());
        });
        list.add(new MenuItem(prevPage).setSlots(36).setPriority(10).setHandler(ItemHandler.forPreviousPage(this)));

        ItemStack nextPage = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_NEXT_PAGE.getDefaultName());
        });
        list.add(new MenuItem(nextPage).setSlots(44).setPriority(10).setHandler(ItemHandler.forNextPage(this)));*/



        ItemStack orderItem = new ItemStack(Material.NAME_TAG);
        ItemUtil.editMeta(orderItem, meta -> {
            meta.setDisplayName(LIGHT_BLUE.enclose(BOLD.enclose("Items Order")));
            meta.setLore(Lists.newList(
                PLACEHOLDER_ORDERS,
                "",
                LIGHT_GRAY.enclose(LIGHT_BLUE.enclose("[▶]") + " Click to " + LIGHT_BLUE.enclose("toggle") + ".")
            ));
        });
        list.add(new MenuItem(orderItem).setSlots(45).setPriority(10).setHandler(this.orderHandler));

        ItemStack categoryItem = new ItemStack(Material.COMPASS);
        ItemUtil.editMeta(categoryItem, meta -> {
            meta.setDisplayName(LIGHT_BLUE.enclose(BOLD.enclose("Category")));
            meta.setLore(Lists.newList(
                PLACEHOLDER_CATEGORIES,
                "",
                LIGHT_GRAY.enclose(LIGHT_BLUE.enclose("[▶]") + " Click to " + LIGHT_BLUE.enclose("toggle") + ".")
            ));
        });
        list.add(new MenuItem(categoryItem).setSlots(46).setPriority(10).setHandler(this.categoryHandler));

        ItemStack currencyItem = new ItemStack(Material.EMERALD);
        ItemUtil.editMeta(currencyItem, meta -> {
            meta.setDisplayName(LIGHT_BLUE.enclose(BOLD.enclose("Currency Filter")));
            meta.setLore(Lists.newList(
                PLACEHOLDER_CURRENCIES,
                "",
                LIGHT_GRAY.enclose(LIGHT_BLUE.enclose("[▶]") + " Click to " + LIGHT_BLUE.enclose("toggle") + "."),
                LIGHT_GRAY.enclose(LIGHT_BLUE.enclose("[▶]") + " Shift-Right to " + LIGHT_BLUE.enclose("reset") + ".")
            ));
        });
        list.add(new MenuItem(currencyItem).setSlots(47).setPriority(10).setHandler(this.currencyHandler));



        ItemStack listingsItem = new ItemStack(Material.BOOK);
        ItemUtil.editMeta(listingsItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("My Items")));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Your active listings."),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("navigate") + ".")
            ));
        });
        list.add(new MenuItem(listingsItem).setSlots(49).setPriority(10).setHandler(this.listingsHandler));

        ItemStack unclaimedItem = new ItemStack(Material.GOLD_NUGGET);
        ItemUtil.editMeta(unclaimedItem, meta -> {
            meta.setDisplayName(LIGHT_ORANGE.enclose(BOLD.enclose("Unclaimed Items")) + " " + LIGHT_GRAY.enclose("[" + WHITE.enclose(PLACEHOLDER_UNCLAIMED_AMOUNT) + "]"));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Items that were sold, but not claimed and"),
                LIGHT_GRAY.enclose("awaiting a final removal if not claimed."),
                "",
                LIGHT_GRAY.enclose(LIGHT_ORANGE.enclose("[▶]") + " Click to " + LIGHT_ORANGE.enclose("navigate") + ".")
            ));
        });
        list.add(new MenuItem(unclaimedItem).setSlots(51).setPriority(10).setHandler(this.unclaimedHandler));

        ItemStack expiredItem = new ItemStack(Material.HOPPER);
        ItemUtil.editMeta(expiredItem, meta -> {
            meta.setDisplayName(LIGHT_RED.enclose(BOLD.enclose("Expired Items")) + " " + LIGHT_GRAY.enclose("[" + WHITE.enclose(PLACEHOLDER_EXPIRED_AMOUNT) + "]"));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("Items that weren't sold in a time and"),
                LIGHT_GRAY.enclose("awaiting a final removal if not taken."),
                "",
                LIGHT_GRAY.enclose(LIGHT_RED.enclose("[▶]") + " Click to " + LIGHT_RED.enclose("navigate") + ".")
            ));
        });
        list.add(new MenuItem(expiredItem).setSlots(52).setPriority(10).setHandler(this.expiredHandler));

        ItemStack historyItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemUtil.editMeta(historyItem, meta -> {
            meta.setDisplayName(LIGHT_GREEN.enclose(BOLD.enclose("Sales History")) + " " + LIGHT_GRAY.enclose("[" + WHITE.enclose(PLACEHOLDER_HISTORY_AMOUNT) + "]"));
            meta.setLore(Lists.newList(
                LIGHT_GRAY.enclose("List of sold items."),
                "",
                LIGHT_GRAY.enclose(LIGHT_GREEN.enclose("[▶]") + " Click to " + LIGHT_GREEN.enclose("navigate") + ".")
            ));
        });
        list.add(new MenuItem(historyItem).setSlots(53).setPriority(10).setHandler(this.historyHandler));



        ItemStack infoItem = new ItemStack(Material.LECTERN);
        ItemUtil.editMeta(infoItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Info")));
            meta.setLore(Lists.newList(
                LIGHT_YELLOW.enclose(BOLD.enclose("Selling Items:")),
                LIGHT_YELLOW.enclose("▸ " + LIGHT_GRAY.enclose("Use command") + " /ah sell <price>"),
                "",
                LIGHT_YELLOW.enclose(BOLD.enclose("Item Expiration:")),
                LIGHT_GRAY.enclose("Unsold and unclaimed items"),
                LIGHT_GRAY.enclose("after " + LIGHT_ORANGE.enclose(GENERIC_EXPIRE) + " will be"),
                LIGHT_GRAY.enclose("purged out from the auction."),
                "",
                LIGHT_YELLOW.enclose(BOLD.enclose("Taxes:")),
                LIGHT_GRAY.enclose("There is a " + LIGHT_YELLOW.enclose(GENERIC_TAX + "%") + " tax of"),
                LIGHT_GRAY.enclose("the item price to adding it"),
                LIGHT_GRAY.enclose("to the auction.")
            ));
        });
        list.add(new MenuItem(infoItem).setSlots(40).setPriority(10));
        
        return list;
    }

    @Override
    protected void loadAdditional() {
        this.itemName = ConfigValue.create("Items.Name",
            LIGHT_YELLOW.enclose(BOLD.enclose(LISTING_ITEM_NAME))
        ).read(cfg);

        this.itemLore = ConfigValue.create("Items.Lore", Lists.newList(
            PLACEHOLDER_LORE_FORMAT
        )).read(cfg);

        this.itemSlots = ConfigValue.create("Items.Slots", IntStream.range(0, 36).toArray()).read(cfg);



        this.lorePreview = ConfigValue.create("Lore_Format.Action_Preview", Lists.newList(
            LIGHT_GRAY.enclose(LIGHT_GREEN.enclose("[▶]") + " Right-Click to " + LIGHT_GREEN.enclose("preview") + ".")
            ),
            "Sets preview action format for container listings.",
            "Use placeholder '" + PLACEHOLDER_ACTION_PREVIEW + "' in listing lore format to insert it."
        ).read(cfg);

        this.itemLoreOwn = ConfigValue.create("Lore_Format.OWNER", Lists.newList(
            DARK_GRAY.enclose(ITALIC.enclose("(This is your listing)")),
            "",
            LISTING_ITEM_LORE,
            "",
            LIGHT_YELLOW.enclose(BOLD.enclose("Info:")),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Price: ") + LISTING_PRICE),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Expires in: ") + LISTING_EXPIRES_IN),
            "",
            LIGHT_GRAY.enclose(LIGHT_GREEN.enclose("[▶]") + " Shift-Right to " + LIGHT_GREEN.enclose("remove") + "."),
            PLACEHOLDER_ACTION_PREVIEW
        )).read(cfg);

        this.itemLorePlayer = ConfigValue.create("Lore_Format.PLAYER", Lists.newList(
            LISTING_ITEM_LORE,
            "",
            LIGHT_YELLOW.enclose(BOLD.enclose("Info:")),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Price: ") + LISTING_PRICE),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Seller: ") + LISTING_SELLER),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Expires in: ") + LISTING_EXPIRES_IN),
            "",
            LIGHT_GRAY.enclose(LIGHT_GREEN.enclose("[▶]") + " Left-Click to " + LIGHT_GREEN.enclose("purchase") + "."),
            PLACEHOLDER_ACTION_PREVIEW
        )).read(cfg);

        this.itemLoreAdmin = ConfigValue.create("Lore_Format.ADMIN", Lists.newList(
            LISTING_ITEM_LORE,
            "",
            LIGHT_YELLOW.enclose(BOLD.enclose("Info:")),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Price: ") + LISTING_PRICE),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Seller: ") + LISTING_SELLER),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Added: ") + LISTING_DATE_CREATION),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Expires in: ") + LISTING_EXPIRES_IN),
            "",
            LIGHT_GRAY.enclose(LIGHT_GREEN.enclose("[▶]") + " Left-Click to " + LIGHT_GREEN.enclose("purchase") + "."),
            LIGHT_GRAY.enclose(LIGHT_GREEN.enclose("[▶]") + " Shift-Right to " + LIGHT_GREEN.enclose("remove") + "."),
            PLACEHOLDER_ACTION_PREVIEW
        )).read(cfg);



        this.loreListSelected = ConfigValue.create("Lore_Format.List.Selected",
            LIGHT_GREEN.enclose("✔ " + GENERIC_NAME)
        ).read(cfg);

        this.loreListUnselected = ConfigValue.create("Lore_Format.List.Unselected",
            LIGHT_GRAY.enclose("   " + GENERIC_NAME)
        ).read(cfg);
    }

}
