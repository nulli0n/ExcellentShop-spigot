package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.currency.ICurrency;
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
    private static final Map<Player, Set<ICurrency>>       CURRENCIES   = new WeakHashMap<>();

    private static final String PLACEHOLDER_CATEGORIES = "%categories%";
    private static final String PLACEHOLDER_CURRENCIES = "%currencies%";
    private static final String PLACEHOLDER_EXPIRED_AMOUNT = "%expired_amount%";
    private static final String PLACEHOLDER_UNCLAIMED_AMOUNT = "%unclaimed_amount%";
    private static final String PLACEHOLDER_LISTING_ORDER = "%listing_order%";

    public AuctionMainMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);

        MenuClick click = (player, type, e) -> {

            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
            else if (type instanceof AuctionItemType type2) {
                switch (type2) {
                    case EXPIRED_LISTINGS -> this.auctionManager.getExpiredMenu().open(player, 1);
                    case SALES_HISTORY -> this.auctionManager.getHistoryMenu().open(player, 1);
                    case UNCLAIMED_ITEMS -> this.auctionManager.getUnclaimedMenu().open(player, 1);
                    case OWN_LISTINGS -> this.auctionManager.getSellingMenu().open(player, 1);
                    case LISTING_ORDER -> {
                        setListingOrder(player, CollectionsUtil.switchEnum(getListingOrder(player)));
                        this.open(player, this.getPage(player));
                    }
                    case CATEGORY_FILTER -> {
                        if (e.isRightClick()) {
                            CATEGORIES.remove(player);
                            this.open(player, this.getPage(player));
                        }
                        else this.auctionManager.getCategoryFilterMenu().open(player, 1);
                    }
                    case CURRENCY_FILTER -> {
                        if (e.isRightClick()) {
                            CURRENCIES.remove(player);
                            this.open(player, this.getPage(player));
                        }
                        else this.auctionManager.getCurrencyFilterMenu().open(player, 1);
                    }
                    default -> { }
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
            MenuItem menuItem = cfg.getMenuItem("Special." + sId, AuctionItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    @NotNull
    public static AuctionSortType getListingOrder(@NotNull Player player) {
        return LISTING_ORDER.computeIfAbsent(player, type -> AuctionSortType.NEWEST);
    }

    public static void setListingOrder(@NotNull Player player, @NotNull AuctionSortType sortType) {
        LISTING_ORDER.put(player, sortType);
    }

    @NotNull
    public static Set<ICurrency> getCurrencies(@NotNull Player player) {
        return CURRENCIES.computeIfAbsent(player, k -> new HashSet<>());
    }

    @NotNull
    public static Set<AuctionCategory> getCategories(@NotNull Player player) {
        return CATEGORIES.computeIfAbsent(player, k -> new HashSet<>());
    }

    @Override
    @NotNull
    protected List<AuctionListing> getObjects(@NotNull Player player) {
        Set<AuctionCategory> categories = getCategories(player);
        Set<ICurrency> currencies = getCurrencies(player);

        return this.auctionManager.getActiveListings().stream()
            .filter(listing -> categories.isEmpty() || categories.stream().anyMatch(category -> category.isItemOfThis(listing.getItemStack())))
            .filter(listing -> currencies.isEmpty() || currencies.contains(listing.getCurrency()))
            .sorted(getListingOrder(player).getComparator()).toList();
    }

    @Override
    @NotNull
    protected MenuClick getObjectClick(@NotNull Player player, @NotNull AuctionListing item) {
        return (player1, type, e) -> {
            if (e.isShiftClick() && e.isRightClick()) {
                if (item.isOwner(player1) || player1.hasPermission(Perms.AUCTION_LISTING_REMOVE_OTHERS)) {
                    this.auctionManager.takeListing(player1, item);
                    this.open(player1, this.getPage(player1));
                }
                return;
            }
            if (item.isOwner(player1)) return;

            if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY && player1.getInventory().firstEmpty() < 0) {
                plugin.getMessage(Lang.SHOP_PRODUCT_ERROR_FULL_INVENTORY).send(player1);
                return;
            }
            this.auctionManager.getPurchaseConfirmationMenu().open(player1, item);
        };
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        String category = getCategories(player).stream().map(AuctionCategory::getName).collect(Collectors.joining(", "));
        String currency = getCurrencies(player).stream().map(c -> c.getConfig().getName()).collect(Collectors.joining(", "));

        ItemUtil.replace(item, line -> line
            .replace("%tax%", NumberUtil.format(AuctionConfig.LISTINGS_TAX_ON_LISTING_ADD))
            .replace("%expire%", TimeUtil.formatTime(AuctionConfig.LISTINGS_EXPIRE_IN))
            .replace(PLACEHOLDER_LISTING_ORDER, plugin.getLangManager().getEnum(getListingOrder(player)))
            .replace(PLACEHOLDER_CATEGORIES, category)
            .replace(PLACEHOLDER_CURRENCIES, currency)
            .replace(PLACEHOLDER_EXPIRED_AMOUNT, String.valueOf(auctionManager.getExpiredListings(player).size()))
            .replace(PLACEHOLDER_UNCLAIMED_AMOUNT, String.valueOf(auctionManager.getUnclaimedListings(player).size()))
        );
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
            return Long.compare(l2.getExpireDate(), l1.getExpireDate());
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
