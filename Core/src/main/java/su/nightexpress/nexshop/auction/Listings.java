package su.nightexpress.nexshop.auction;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class Listings {

    private final Map<UUID, ActiveListing>         listings;
    private final Map<UUID, CompletedListing>      completedListings;
    private final Map<UUID, Set<ActiveListing>>    listingsByOwnerId;
    private final Map<UUID, Set<CompletedListing>> completedByOwnerId;

    private static final Comparator<AbstractListing> SORT_BY_CREATION = Comparator.comparingLong(AbstractListing::getCreationDate).reversed();

    public Listings() {
        this.listings = new ConcurrentHashMap<>();
        this.completedListings = new ConcurrentHashMap<>();
        this.listingsByOwnerId = new ConcurrentHashMap<>();
        this.completedByOwnerId = new ConcurrentHashMap<>();
    }

    public void clear() {
        this.listings.clear();
        this.completedListings.clear();
        this.listingsByOwnerId.clear();
        this.completedByOwnerId.clear();
    }

    @NotNull
    public static <T extends AbstractListing> List<T> sorted(@NotNull List<T> listings) {
        listings.sort(SORT_BY_CREATION);
        return listings;
    }

    public void add(@NotNull ActiveListing listing) {
        this.listings.put(listing.getId(), listing);
        this.listingsByOwnerId.computeIfAbsent(listing.getOwner(), k -> new HashSet<>()).add(listing);
    }

    public void remove(@NotNull ActiveListing listing) {
        this.listings.remove(listing.getId());
        this.listingsByOwnerId.getOrDefault(listing.getOwner(), Collections.emptySet()).remove(listing);
    }

    public void addCompleted(@NotNull CompletedListing listing) {
        this.completedListings.put(listing.getId(), listing);
        this.completedByOwnerId.computeIfAbsent(listing.getOwner(), k -> new HashSet<>()).add(listing);
    }

    public void removeCompleted(@NotNull CompletedListing listing) {
        this.completedListings.remove(listing.getId());
        this.completedByOwnerId.getOrDefault(listing.getOwner(), Collections.emptySet()).remove(listing);
    }

    public boolean hasListing(@NotNull UUID uuid) {
        return this.getById(uuid) != null;
    }

    @Nullable
    public ActiveListing getById(@NotNull UUID uuid) {
        return this.listings.getOrDefault(uuid, null);
    }



    @NotNull
    public List<ActiveListing> getAll() {
        return new ArrayList<>(this.listings.values());
    }

    @NotNull
    public List<ActiveListing> getAll(@NotNull Player player) {
        return this.getAll(player.getUniqueId());
    }

    @NotNull
    public List<ActiveListing> getAll(@NotNull UUID id) {
        return new ArrayList<>(this.listingsByOwnerId.getOrDefault(id, Collections.emptySet()));
    }



    @NotNull
    public List<CompletedListing> getCompleted() {
        return new ArrayList<>(this.completedListings.values());
    }

    @NotNull
    public List<CompletedListing> getCompleted(@NotNull Player player) {
        return this.getCompleted(player.getUniqueId());
    }

    @NotNull
    public List<CompletedListing> getCompleted(@NotNull UUID id) {
        return new ArrayList<>(this.completedByOwnerId.getOrDefault(id, Collections.emptySet()));
    }



    @NotNull
    public List<ActiveListing> getActive() {
        return new ArrayList<>(this.listings.values().stream().filter(Predicate.not(ActiveListing::isExpired)).toList());
    }

    @NotNull
    public List<ActiveListing> getActive(@NotNull Player owner) {
        return this.getActive(owner.getUniqueId());
    }

    @NotNull
    public List<ActiveListing> getActive(@NotNull UUID owner) {
        return new ArrayList<>(this.getAll(owner).stream().filter(Predicate.not(ActiveListing::isExpired)).toList());
    }



    @NotNull
    public List<ActiveListing> getExpired() {
        return new ArrayList<>(this.listings.values());
    }

    @NotNull
    public List<ActiveListing> getExpired(@NotNull Player player) {
        return this.getExpired(player.getUniqueId());
    }

    @NotNull
    public List<ActiveListing> getExpired(@NotNull UUID id) {
        return new ArrayList<>(this.getAll(id).stream().filter(ActiveListing::isExpired).toList());
    }



    @NotNull
    public List<CompletedListing> getClaimed() {
        return new ArrayList<>(this.completedListings.values());
    }

    @NotNull
    public List<CompletedListing> getClaimed(@NotNull Player player) {
        return this.getClaimed(player.getUniqueId());
    }

    @NotNull
    public List<CompletedListing> getClaimed(@NotNull UUID id) {
        return new ArrayList<>(this.getCompleted(id).stream().filter(CompletedListing::isClaimed).toList());
    }



    @NotNull
    public List<CompletedListing> getUnclaimed() {
        return new ArrayList<>(this.completedListings.values());
    }

    @NotNull
    public List<CompletedListing> getUnclaimed(@NotNull Player player) {
        return this.getUnclaimed(player.getUniqueId());
    }

    @NotNull
    public List<CompletedListing> getUnclaimed(@NotNull UUID id) {
        return new ArrayList<>(this.getCompleted(id).stream().filter(Predicate.not(CompletedListing::isClaimed)).toList());
    }
}
