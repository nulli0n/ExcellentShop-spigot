package su.nightexpress.nexshop.auction;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class Listings {

    private final Map<UUID, ActiveListing>         activeById;
    private final Map<UUID, CompletedListing>      completedById;
    private final Map<UUID, Set<ActiveListing>>    activeByOwnerId;
    private final Map<UUID, Set<CompletedListing>> completedByOwnerId;

    private static final Comparator<AbstractListing> SORT_BY_CREATION = Comparator.comparingLong(AbstractListing::getCreationDate).reversed();

    public Listings() {
        this.activeById = new ConcurrentHashMap<>();
        this.completedById = new ConcurrentHashMap<>();
        this.activeByOwnerId = new ConcurrentHashMap<>();
        this.completedByOwnerId = new ConcurrentHashMap<>();
    }

    public void clear() {
        this.activeById.clear();
        this.completedById.clear();
        this.activeByOwnerId.clear();
        this.completedByOwnerId.clear();
    }

    @NonNull
    public static <T extends AbstractListing> List<T> sortAndValidate(@NonNull List<T> listings) {
        listings.removeIf(Predicate.not(AbstractListing::isValid));
        listings.sort(SORT_BY_CREATION);
        return listings;
    }

    public void add(@NonNull ActiveListing listing) {
        this.activeById.put(listing.getId(), listing);
        this.activeByOwnerId.computeIfAbsent(listing.getOwner(), k -> new HashSet<>()).add(listing);
    }

    public void remove(@NonNull ActiveListing listing) {
        this.activeById.remove(listing.getId());
        this.activeByOwnerId.getOrDefault(listing.getOwner(), Collections.emptySet()).remove(listing);
    }

    public void addCompleted(@NonNull CompletedListing listing) {
        this.completedById.put(listing.getId(), listing);
        this.completedByOwnerId.computeIfAbsent(listing.getOwner(), k -> new HashSet<>()).add(listing);
    }

    public void removeCompleted(@NonNull CompletedListing listing) {
        this.completedById.remove(listing.getId());
        this.completedByOwnerId.getOrDefault(listing.getOwner(), Collections.emptySet()).remove(listing);
    }

    public void removeInvalidActive() {
        this.activeById.values().removeIf(AbstractListing::isDeletionTime);
        this.activeByOwnerId.values().forEach(set -> set.removeIf(AbstractListing::isDeletionTime));
    }

    public void removeInvalidCompleted() {
        this.completedById.values().removeIf(AbstractListing::isDeletionTime);
        this.completedByOwnerId.values().forEach(set -> set.removeIf(AbstractListing::isDeletionTime));
    }

    public boolean hasListing(@NonNull UUID uuid) {
        return this.getById(uuid) != null;
    }

    @Nullable
    public ActiveListing getById(@NonNull UUID uuid) {
        this.removeInvalidActive();

        return this.activeById.getOrDefault(uuid, null);
    }



    @NonNull
    public List<ActiveListing> getAll() {
        this.removeInvalidActive();

        return new ArrayList<>(this.activeById.values());
    }

    @NonNull
    public List<ActiveListing> getAll(@NonNull Player player) {
        return this.getAll(player.getUniqueId());
    }

    @NonNull
    public List<ActiveListing> getAll(@NonNull UUID id) {
        this.removeInvalidActive();

        return new ArrayList<>(this.activeByOwnerId.getOrDefault(id, Collections.emptySet()));
    }



    @NonNull
    public List<CompletedListing> getCompleted() {
        this.removeInvalidCompleted();

        return new ArrayList<>(this.completedById.values());
    }

    @NonNull
    public List<CompletedListing> getCompleted(@NonNull Player player) {
        return this.getCompleted(player.getUniqueId());
    }

    @NonNull
    public List<CompletedListing> getCompleted(@NonNull UUID id) {
        this.removeInvalidCompleted();

        return new ArrayList<>(this.completedByOwnerId.getOrDefault(id, Collections.emptySet()));
    }



    @NonNull
    public List<ActiveListing> getActive() {
        this.removeInvalidActive();

        return new ArrayList<>(this.activeById.values().stream().filter(Predicate.not(ActiveListing::isExpired)).toList());
    }

    @NonNull
    public List<ActiveListing> getActive(@NonNull Player owner) {
        return this.getActive(owner.getUniqueId());
    }

    @NonNull
    public List<ActiveListing> getActive(@NonNull UUID owner) {
        return new ArrayList<>(this.getAll(owner).stream().filter(Predicate.not(ActiveListing::isExpired)).toList());
    }



    /*@NonNull
    public List<ActiveListing> getExpired() {
        return new ArrayList<>(this.listings.values());
    }*/

    @NonNull
    public List<ActiveListing> getExpired(@NonNull Player player) {
        return this.getExpired(player.getUniqueId());
    }

    @NonNull
    public List<ActiveListing> getExpired(@NonNull UUID id) {
        return new ArrayList<>(this.getAll(id).stream().filter(ActiveListing::isExpired).toList());
    }



    /*@NonNull
    public List<CompletedListing> getClaimed() {
        return new ArrayList<>(this.completedListings.values());
    }*/

    @NonNull
    public List<CompletedListing> getClaimed(@NonNull Player player) {
        return this.getClaimed(player.getUniqueId());
    }

    @NonNull
    public List<CompletedListing> getClaimed(@NonNull UUID id) {
        return new ArrayList<>(this.getCompleted(id).stream().filter(CompletedListing::isClaimed).toList());
    }



    /*@NonNull
    public List<CompletedListing> getUnclaimed() {
        return new ArrayList<>(this.completedListings.values());
    }*/

    @NonNull
    public List<CompletedListing> getUnclaimed(@NonNull Player player) {
        return this.getUnclaimed(player.getUniqueId());
    }

    @NonNull
    public List<CompletedListing> getUnclaimed(@NonNull UUID id) {
        return new ArrayList<>(this.getCompleted(id).stream().filter(Predicate.not(CompletedListing::isClaimed)).toList());
    }
}
