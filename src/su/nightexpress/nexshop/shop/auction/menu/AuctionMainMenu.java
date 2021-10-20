package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.auction.AuctionConfig;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.object.AuctionCategory;
import su.nightexpress.nexshop.shop.auction.object.AuctionListing;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class AuctionMainMenu extends AbstractAuctionMenu<AuctionListing> {

    private final Map<Player, AuctionSortType> sortTypes;
    private final Map<Player, Integer>         categories;


    public AuctionMainMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);
        this.sortTypes = new WeakHashMap<>();
        this.categories = new WeakHashMap<>();

        IMenuClick click = (player, type, e) -> {

            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
            else if (type instanceof AuctionItemType type2) {
                switch (type2) {
                    case AUCTION_EXPIRED -> this.auctionManager.getAuctionExpiredMenu().open(player, 1);
                    case AUCTION_HISTORY -> this.auctionManager.getAuctionHistoryMenu().open(player, 1);
                    case AUCTION_SORTING -> {
                        this.setSortType(player, CollectionsUT.toggleEnum(this.getSortType(player)));
                        this.open(player, this.getPage(player));
                    }
                    case AUCTION_CATEGORY -> {
                        int index = this.categories.getOrDefault(player, 0);
                        if (index < 0) return;
                        if ((index + 1) >= AuctionConfig.CATEGORIES.size()) index = -1;

                        this.setCategory(player, index + 1);
                        this.open(player, this.getPage(player));
                    }
                    default -> { }
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("Special")) {
            IMenuItem menuItem = cfg.getMenuItem("Special." + sId, AuctionItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    @NotNull
    private AuctionSortType getSortType(@NotNull Player player) {
        return this.sortTypes.computeIfAbsent(player, type -> AuctionSortType.NEWEST);
    }

    private void setSortType(@NotNull Player player, @NotNull AuctionSortType sortType) {
        this.sortTypes.put(player, sortType);
    }

    @Nullable
    private AuctionCategory getCategory(@NotNull Player player) {
        if (AuctionConfig.CATEGORIES.isEmpty()) return null;

        return AuctionConfig.CATEGORIES.get(this.categories.computeIfAbsent(player, cat -> 0));
    }

    private void setCategory(@NotNull Player player, int index) {
        this.categories.put(player, index);
    }

    @Override
    @NotNull
    protected List<AuctionListing> getObjects(@NotNull Player player) {
        AuctionCategory category = this.getCategory(player);

        return this.auctionManager.getListings().stream()
                .filter(listing -> category == null || category.isItemOfThis(listing.getItemStack()))
                .sorted(this.getSortType(player).getComparator()).toList();
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull AuctionListing item) {
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
                plugin.lang().Shop_Product_Error_FullInventory.send(player1);
                return;
            }
            this.auctionManager.openAuctionConfirm(player1, item);
        };
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        AuctionCategory category = this.getCategory(player);

        ItemUT.replace(item, line -> line
                .replace("%tax%", NumberUT.format(AuctionConfig.LISTINGS_PRICE_TAX))
                .replace("%expire%", TimeUT.formatTime(AuctionConfig.STORAGE_EXPIRE_IN))
                .replace("%sort-type%", plugin.lang().getEnum(this.getSortType(player)))
                .replace("%category-name%", category != null ? category.getName() : "-")
        );
    }

    private enum AuctionItemType {
        AUCTION_EXPIRED,
        AUCTION_HISTORY,
        AUCTION_SORTING,
        AUCTION_CATEGORY,
    }

    public enum AuctionSortType {

        NAME((l1, l2) -> {
            String name1 = ItemUT.getItemName(l1.getItemStack());
            String name2 = ItemUT.getItemName(l2.getItemStack());
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
