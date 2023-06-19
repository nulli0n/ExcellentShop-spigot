package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.*;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.auction.AuctionCategory;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.shop.auction.listing.AuctionListing;

import java.util.*;
import java.util.stream.Collectors;

public class AuctionMainMenu extends AbstractAuctionMenu<AuctionListing> {

    private static final Map<Player, AuctionSortType>      LISTING_ORDER = new WeakHashMap<>();
    private static final Map<Player, Set<AuctionCategory>> CATEGORIES    = new WeakHashMap<>();
    private static final Map<Player, Set<Currency>>       CURRENCIES   = new WeakHashMap<>();

    private static final String PLACEHOLDER_CATEGORIES = "%categories%";
    private static final String PLACEHOLDER_CURRENCIES = "%currencies%";
    private static final String PLACEHOLDER_EXPIRED_AMOUNT = "%expired_amount%";
    private static final String PLACEHOLDER_UNCLAIMED_AMOUNT = "%unclaimed_amount%";
    private static final String PLACEHOLDER_LISTING_ORDER = "%listing_order%";

    public AuctionMainMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);

        this.registerHandler(AuctionItemType.class)
            .addClick(AuctionItemType.EXPIRED_LISTINGS, (viewer, event) -> {
                this.auctionManager.getExpiredMenu().openNextTick(viewer, 1);
            })
            .addClick(AuctionItemType.SALES_HISTORY, (viewer, event) -> {
                this.auctionManager.getHistoryMenu().openNextTick(viewer, 1);
            })
            .addClick(AuctionItemType.UNCLAIMED_ITEMS, (viewer, event) -> {
                this.auctionManager.getUnclaimedMenu().openNextTick(viewer, 1);
            })
            .addClick(AuctionItemType.OWN_LISTINGS, (viewer, event) -> {
                this.auctionManager.getSellingMenu().openNextTick(viewer, 1);
            })
            .addClick(AuctionItemType.LISTING_ORDER, (viewer, event) -> {
                Player player = viewer.getPlayer();
                setListingOrder(player, CollectionsUtil.next(getListingOrder(player)));
                this.openNextTick(viewer, viewer.getPage());
            })
            .addClick(AuctionItemType.CATEGORY_FILTER, (viewer, event) -> {
                if (event.isRightClick()) {
                    CATEGORIES.remove(viewer.getPlayer());
                    this.openNextTick(viewer, viewer.getPage());
                }
                else this.auctionManager.getCategoryFilterMenu().openNextTick(viewer, 1);
            })
            .addClick(AuctionItemType.CURRENCY_FILTER, (viewer, event) -> {
                if (event.isRightClick()) {
                    CURRENCIES.remove(viewer.getPlayer());
                    this.openNextTick(viewer, viewer.getPage());
                }
                else this.auctionManager.getCurrencyFilterMenu().openNextTick(viewer, 1);
            });

        this.load();

        this.getItems().forEach(menuItem -> {
            menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                Player player = viewer.getPlayer();
                String categories = getCategories(player).stream().map(AuctionCategory::getName).collect(Collectors.joining(", "));
                String currencies = getCurrencies(player).stream().map(Currency::getName).collect(Collectors.joining(", "));
                if (categories.isEmpty()) categories = LangManager.getPlain(Lang.OTHER_NONE);
                if (currencies.isEmpty()) currencies = LangManager.getPlain(Lang.OTHER_NONE);

                String finalCategories = categories;
                String finalCurrencies = currencies;
                PlaceholderMap placeholderMap = new PlaceholderMap()
                    .add("%tax%", () -> NumberUtil.format(AuctionConfig.LISTINGS_TAX_ON_LISTING_ADD))
                    .add("%expire%", () -> TimeUtil.formatTime(AuctionConfig.LISTINGS_EXPIRE_IN))
                    .add(PLACEHOLDER_LISTING_ORDER, () -> plugin.getLangManager().getEnum(getListingOrder(player)))
                    .add(PLACEHOLDER_CATEGORIES, () -> finalCategories)
                    .add(PLACEHOLDER_CURRENCIES, () -> finalCurrencies)
                    .add(PLACEHOLDER_EXPIRED_AMOUNT, () -> NumberUtil.format(auctionManager.getExpiredListings(player).size()))
                    .add(PLACEHOLDER_UNCLAIMED_AMOUNT, () -> NumberUtil.format(auctionManager.getUnclaimedListings(player).size()))
                    ;

                ItemUtil.replace(item, placeholderMap.replacer());
            });
        });
    }

    @NotNull
    public static AuctionSortType getListingOrder(@NotNull Player player) {
        return LISTING_ORDER.computeIfAbsent(player, type -> AuctionSortType.NEWEST);
    }

    public static void setListingOrder(@NotNull Player player, @NotNull AuctionSortType sortType) {
        LISTING_ORDER.put(player, sortType);
    }

    @NotNull
    public static Set<Currency> getCurrencies(@NotNull Player player) {
        return CURRENCIES.computeIfAbsent(player, k -> new HashSet<>());
    }

    @NotNull
    public static Set<AuctionCategory> getCategories(@NotNull Player player) {
        return CATEGORIES.computeIfAbsent(player, k -> new HashSet<>());
    }

    @Override
    @NotNull
    public List<AuctionListing> getObjects(@NotNull Player player) {
        Set<AuctionCategory> categories = getCategories(player);
        Set<Currency> currencies = getCurrencies(player);

        return this.auctionManager.getActiveListings().stream()
            .filter(listing -> categories.isEmpty() || categories.stream().anyMatch(category -> category.isItemOfThis(listing.getItemStack())))
            .filter(listing -> currencies.isEmpty() || currencies.contains(listing.getCurrency()))
            .sorted(getListingOrder(player).getComparator()).toList();
    }

    @Override
    @NotNull
    public ItemClick getObjectClick(@NotNull AuctionListing item) {
        return (viewer, event) -> {
            Player player = viewer.getPlayer();
            boolean isOwner = item.isOwner(player);
            boolean isBedrock = PlayerUtil.isBedrockPlayer(player);

            if ((event.isShiftClick() && event.isRightClick()) || (isOwner && isBedrock)) {
                if (isOwner|| player.hasPermission(Perms.AUCTION_LISTING_REMOVE_OTHERS)) {
                    this.auctionManager.takeListing(player, item);
                    this.openNextTick(viewer, viewer.getPage());
                }
                return;
            }
            if (isOwner) return;

            if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY.get() && player.getInventory().firstEmpty() < 0) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_FULL_INVENTORY).send(player);
                return;
            }
            this.auctionManager.getPurchaseConfirmationMenu().open(player, item);
        };
    }

    private enum AuctionItemType {
        EXPIRED_LISTINGS,
        SALES_HISTORY,
        UNCLAIMED_ITEMS,
        LISTING_ORDER,
        CATEGORY_FILTER,
        CURRENCY_FILTER,
        OWN_LISTINGS,
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

        private final Comparator<AuctionListing> comparator;

        AuctionSortType(@NotNull Comparator<AuctionListing> comparator) {
            this.comparator = comparator;
        }

        @NotNull
        public Comparator<AuctionListing> getComparator() {
            return this.comparator;
        }
    }
}
