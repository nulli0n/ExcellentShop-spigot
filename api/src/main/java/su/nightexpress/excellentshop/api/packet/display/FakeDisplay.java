package su.nightexpress.excellentshop.api.packet.display;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class FakeDisplay {

    private final Set<UUID>                 humanViewers;
    private final Map<FakeType, FakeEntity> entityMap;

    private int  productIndex;
    private long tickCount;

    public FakeDisplay() {
        this.humanViewers = new HashSet<>();
        this.entityMap = new HashMap<>();
        this.productIndex = -1;
    }

    public void addViewer(@NonNull Player player) {
        this.humanViewers.add(player.getUniqueId());
    }

    public void removeViewer(@NonNull Player player) {
        this.humanViewers.remove(player.getUniqueId());
    }

    public boolean isViewer(@NonNull Player player) {
        return this.humanViewers.contains(player.getUniqueId());
    }

    public void addFakeEntity(@NonNull FakeType type, @NonNull Location location) {
        this.entityMap.put(type, FakeEntity.create(location));
    }
    
    @NonNull
    public Optional<FakeEntity> fakeEntity(@NonNull FakeType type) {
        return Optional.ofNullable(this.getFakeEntity(type));
    }

    @Nullable
    public FakeEntity getFakeEntity(@NonNull FakeType type) {
        return this.entityMap.get(type);
    }

    @NonNull
    public Set<FakeEntity> getAll() {
        return Set.copyOf(this.entityMap.values());
    }

    @NonNull
    public Set<Integer> getIDs() {
        return this.getAll().stream().mapToInt(FakeEntity::getId).boxed().collect(Collectors.toSet());
    }

    public long getTickCount() {
        return this.tickCount;
    }

    public void tick() {
        this.tickCount++;
    }

    public int nextProductIndex() {
        return ++this.productIndex;
    }

    public int getProductIndex() {
        return this.productIndex;
    }

    public int resetProductIndex() {
        this.productIndex = 0;
        return this.productIndex;
    }

    @Override
    public String toString() {
        return "FakeDisplay{" +
            "humanViewers=" + humanViewers +
            ", entityMap=" + entityMap +
            ", productIndex=" + productIndex +
            ", tickCount=" + tickCount +
            '}';
    }
}
