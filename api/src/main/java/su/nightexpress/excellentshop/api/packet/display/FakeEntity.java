package su.nightexpress.excellentshop.api.packet.display;

import org.bukkit.Location;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.EntityUtil;

public class FakeEntity {

    private final int      id;
    private final Location location;

    public FakeEntity(int id, @NonNull Location location) {
        this.id = id;
        this.location = location;
    }

    @NonNull
    public static FakeEntity create(@NonNull Location location) {
        return new FakeEntity(EntityUtil.nextEntityId(), location);
    }

    public int getId() {
        return this.id;
    }

    @NonNull
    public Location getLocation() {
        return this.location.clone();
    }
}
