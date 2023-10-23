package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.*;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.shop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.shop.auction.listing.ListingCategory;

import java.util.*;
import java.util.stream.Collectors;

public class AuctionMainMenu extends AbstractAuctionMenu<ActiveListing> {

    private static final Map<Player, AuctionSortType> LISTING_ORDER = new WeakHashMap<>();
    private static final Map<Player, ListingCategory> CATEGORY      = new WeakHashMap<>();
    private static final Map<Player, Currency>        CURRENCY      = new WeakHashMap<>();

    private static final String PLACEHOLDER_CATEGORIES       = "%categories%";
    private static final String PLACEHOLDER_CURRENCIES       = "%currencies%";
    private static final String PLACEHOLDER_EXPIRED_AMOUNT   = "%expired_amount%";
    private static final String PLACEHOLDER_UNCLAIMED_AMOUNT = "%unclaimed_amount%";
    private static final String PLACEHOLDER_LISTING_ORDER    = "%listing_order%";
    private static final String PLACEHOLDER_ACTION_PREVIEW   = "%action_preview%";

    private final List<String> lorePreview;
    private final String loreListUnselected;
    private final String loreListSelected;

    public AuctionMainMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);

        this.lorePreview = JOption.create("Lore_Format.Action_Preview",
            List.of("#a5ff9a▪ #ddeceeRight-Click: #a5ff9aPreview Content"),
            "Sets preview action format for container listings.",
            "Use placeholder '" + PLACEHOLDER_ACTION_PREVIEW + "' in listing lore format to insert it."
        ).mapReader(Colorizer::apply).read(cfg);

        this.loreListSelected = JOption.create("Lore_Format.List.Selected",
            Colors.GREEN + "✔ " + Placeholders.GENERIC_NAME
        ).mapReader(Colorizer::apply).read(cfg);

        this.loreListUnselected = JOption.create("Lore_Format.List.Unselected",
            Colors.GRAY + "   " + Placeholders.GENERIC_NAME
        ).mapReader(Colorizer::apply).read(cfg);

        cfg.saveChanges();

        this.registerHandler(FilterType.class)
            .addClick(FilterType.LISTING_ORDER, (viewer, event) -> {
                Player player = viewer.getPlayer();
                setListingOrder(player, CollectionsUtil.next(getListingOrder(player)));
                this.openNextTick(viewer, viewer.getPage());
            })
            .addClick(FilterType.CATEGORY_FILTER, (viewer, event) -> {
                List<ListingCategory> categories = new ArrayList<>(this.auctionManager.getCategories());
                ListingCategory current = this.getCategory(viewer.getPlayer());
                ListingCategory selected = CollectionsUtil.shifted(categories, categories.indexOf(current), event.isLeftClick() ? 1 : -1);
                CATEGORY.put(viewer.getPlayer(), selected);
                this.openNextTick(viewer, viewer.getPage());
            })
            .addClick(FilterType.CURRENCY_FILTER, (viewer, event) -> {
                if (event.isShiftClick() && event.isRightClick()) {
                    CURRENCY.remove(viewer.getPlayer());
                    this.openNextTick(viewer, viewer.getPage());
                    return;
                }

                List<Currency> currencies = new ArrayList<>(this.auctionManager.getCurrencies(viewer.getPlayer()));
                Currency current = this.getCurrency(viewer.getPlayer());
                Currency selected = CollectionsUtil.shifted(currencies, currencies.indexOf(current), event.isLeftClick() ? 1 : -1);
                CURRENCY.put(viewer.getPlayer(), selected);
                this.openNextTick(viewer, viewer.getPage());
            });

        this.load();

        this.getItems().forEach(menuItem -> {
            menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                Player player = viewer.getPlayer();

                String categoryList = this.auctionManager.getCategories().stream().map(category -> {
                    if (this.getCategory(player) == category) {
                        return this.loreListSelected.replace(Placeholders.GENERIC_NAME, category.getName());
                    }
                    return this.loreListUnselected.replace(Placeholders.GENERIC_NAME, category.getName());
                }).collect(Collectors.joining("\n"));

                String currencyList = this.auctionManager.getCurrencies(player).stream().map(currency -> {
                    if (this.getCurrency(player) == currency) {
                        return this.loreListSelected.replace(Placeholders.GENERIC_NAME, currency.getName());
                    }
                    return this.loreListUnselected.replace(Placeholders.GENERIC_NAME, currency.getName());
                }).collect(Collectors.joining("\n"));

                PlaceholderMap placeholderMap = new PlaceholderMap()
                    .add("%tax%", () -> NumberUtil.format(AuctionConfig.LISTINGS_TAX_ON_LISTING_ADD))
                    .add("%expire%", () -> TimeUtil.formatTime(AuctionConfig.LISTINGS_EXPIRE_IN))
                    .add(PLACEHOLDER_LISTING_ORDER, () -> plugin.getLangManager().getEnum(getListingOrder(player)))
                    .add(PLACEHOLDER_CATEGORIES, () -> categoryList)
                    .add(PLACEHOLDER_CURRENCIES, () -> currencyList)
                    .add(PLACEHOLDER_EXPIRED_AMOUNT, () -> NumberUtil.format(auctionManager.getExpiredListings(player).size()))
                    .add(PLACEHOLDER_UNCLAIMED_AMOUNT, () -> NumberUtil.format(auctionManager.getUnclaimedListings(player).size()))
                    ;

                ItemUtil.replace(item, placeholderMap.replacer());

                if (Config.GUI_PLACEHOLDER_API.get() && EngineUtils.hasPlaceholderAPI()) {
                    ItemUtil.setPlaceholderAPI(viewer.getPlayer(), item);
                }
            });
        });
    }

    public boolean isContainer(@NotNull ActiveListing listing) {
        ItemStack item = listing.getItemStack();
        if (item.getItemMeta() instanceof BlockStateMeta meta) {
            return meta.getBlockState() instanceof Container container;
        }
        return false;
    }

    @NotNull
    public static AuctionSortType getListingOrder(@NotNull Player player) {
        return LISTING_ORDER.computeIfAbsent(player, type -> AuctionSortType.NEWEST);
    }

    public static void setListingOrder(@NotNull Player player, @NotNull AuctionSortType sortType) {
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
    @NotNull
    public List<ActiveListing> getObjects(@NotNull Player player) {
        ListingCategory category = this.getCategory(player);
        Currency currencies = this.getCurrency(player);

        return this.auctionManager.getActiveListings().stream()
            .filter(listing -> category.isItemOfThis(listing.getItemStack()))
            .filter(listing -> currencies == null || currencies == listing.getCurrency())
            .sorted(getListingOrder(player).getComparator()).toList();
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull ActiveListing listing) {
        ItemStack item = super.getObjectStack(player, listing);
        if (AuctionConfig.MENU_CONTAINER_PREVIEW_ENABLED.get()) {
            ItemUtil.mapMeta(item, meta -> {
                List<String> lore = meta.getLore();
                if (lore == null) return;

                List<String> replace = this.isContainer(listing) ? new ArrayList<>(this.lorePreview) : Collections.emptyList();
                lore = StringUtil.replaceInList(lore, PLACEHOLDER_ACTION_PREVIEW, replace);
                meta.setLore(lore);
            });
        }
        return item;
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull ActiveListing listing) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            boolean isOwner = listing.isOwner(player);
            boolean isBedrock = PlayerUtil.isBedrockPlayer(player);

            if ((event.isShiftClick() && event.isRightClick()) || (isOwner && isBedrock)) {
                if (isOwner|| player.hasPermission(Perms.AUCTION_LISTING_REMOVE_OTHERS)) {
                    this.auctionManager.takeListing(player, listing);
                    this.openNextTick(viewer, viewer.getPage());
                }
                return;
            }

            if (AuctionConfig.MENU_CONTAINER_PREVIEW_ENABLED.get() && event.isRightClick()) {
                ItemStack item = listing.getItemStack();
                if (item.getItemMeta() instanceof BlockStateMeta meta) {
                    if (meta.getBlockState() instanceof Container container) {
                        new ContainerPreview(this, container, viewer.getPage()).openNextTick(viewer, 1);
                        return;
                    }
                }
            }

            if (isOwner) return;

            if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY.get() && player.getInventory().firstEmpty() < 0) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_FULL_INVENTORY).send(player);
                return;
            }
            this.auctionManager.getPurchaseConfirmationMenu().open(player, listing);
        };
    }

    private enum FilterType {
        LISTING_ORDER,
        CATEGORY_FILTER,
        CURRENCY_FILTER,
    }

    public enum AuctionSortType {

        NAME((l1, l2) -> {
            String name1 = ItemUtil.getItemName(l1.getItemStack());
            String name2 = ItemUtil.getItemName(l2.getItemStack());
            return name1.compareTo(name2);
        }),
        MATERIAL((l1, l2) -> {
            String type1 = l1.getItemStack().getType().name();
            String type2 = l2.getItemStack().getType().name();
            return type1.compareTo(type2);
        }),
        SELLER((l1, l2) -> {
            return l1.getOwnerName().compareTo(l2.getOwnerName());
        }),
        NEWEST((l1, l2) -> {
            return Long.compare(l2.getDateCreation(), l1.getDateCreation());
        }),
        OLDEST((l1, l2) -> {
            return Long.compare(l1.getExpireDate(), l2.getExpireDate());
        }),
        MOST_EXPENSIVE((l1, l2) -> {
            return Double.compare(l2.getPrice(), l1.getPrice());
        }),
        LEAST_EXPENSIVE((l1, l2) -> {
            return Double.compare(l1.getPrice(), l2.getPrice());
        }),
        ;

        private final Comparator<ActiveListing> comparator;

        AuctionSortType(@NotNull Comparator<ActiveListing> comparator) {
            this.comparator = comparator;
        }

        @NotNull
        public Comparator<ActiveListing> getComparator() {
            return this.comparator;
        }
    }
}
