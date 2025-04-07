package su.nightexpress.nexshop.auction;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.Comparator;

public enum SortType {

    NAME((l1, l2) -> {
        String name1 = NightMessage.stripTags(ItemUtil.getSerializedName(l1.getItemStack()));
        String name2 = NightMessage.stripTags(ItemUtil.getSerializedName(l2.getItemStack()));
        return name1.compareTo(name2);
    }),
    MATERIAL((l1, l2) -> {
        String type1 = LangAssets.get(l1.getItemStack().getType());
        String type2 = LangAssets.get(l2.getItemStack().getType());
        return type1.compareTo(type2);
    }),
    SELLER(Comparator.comparing(AbstractListing::getOwnerName)),
    NEWEST(Comparator.comparingLong((ActiveListing listing) -> listing.getCreationDate()).reversed()),
    OLDEST(Comparator.comparingLong(ActiveListing::getExpireDate)),
    MOST_EXPENSIVE(Comparator.comparingDouble((ActiveListing listing) -> listing.getPrice()).reversed()),
    LEAST_EXPENSIVE(Comparator.comparingDouble(AbstractListing::getPrice)),
    ;

    private final Comparator<ActiveListing> comparator;

    SortType(@NotNull Comparator<ActiveListing> comparator) {
        this.comparator = comparator;
    }

    @NotNull
    public Comparator<ActiveListing> getComparator() {
        return this.comparator;
    }
}
