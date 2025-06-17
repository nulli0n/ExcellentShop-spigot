package su.nightexpress.nexshop.shop.chest.display.impl;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class FakeDisplay {

    private final Set<UUID>                       humanViewers;
    private final Map<FakeType, List<FakeEntity>> entityMap;

    private int productIndex;
    private long tickCount;

    public FakeDisplay() {
        this.humanViewers = new HashSet<>();
        this.entityMap = new HashMap<>();
        this.productIndex = -1;
    }

    public void addViewer(@NotNull Player player) {
        this.humanViewers.add(player.getUniqueId());
    }

    public void removeViewer(@NotNull Player player) {
        this.humanViewers.remove(player.getUniqueId());
    }

    public boolean isViewer(@NotNull Player player) {
        return this.humanViewers.contains(player.getUniqueId());
    }

    public void addFakeEntity(@NotNull FakeType type, @NotNull Location location) {
        this.entityMap.computeIfAbsent(type, k -> new ArrayList<>()).add(FakeEntity.create(location));
    }

    @NotNull
    public List<FakeEntity> getFakeEntities(@NotNull FakeType type) {
        return this.entityMap.getOrDefault(type, Collections.emptyList());
    }

    @NotNull
    public Set<FakeEntity> getAll() {
        return this.entityMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @NotNull
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
}
