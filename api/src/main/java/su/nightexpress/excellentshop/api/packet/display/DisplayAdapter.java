package su.nightexpress.excellentshop.api.packet.display;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public interface DisplayAdapter {

    @NonNull DisplaySettings getSettings();

    void broadcastDestroyPacket(@NonNull Set<Integer> idSet);

    void sendDestroyPacket(@NonNull Player player, @NonNull Set<Integer> idSet);

    void sendItemPackets(@NonNull Player player, @NonNull FakeEntity entity, boolean needSpawn, @NonNull ItemStack item);

    void sendShowcasePackets(@NonNull Player player, @NonNull FakeEntity entity, boolean needSpawn, @NonNull ItemStack item);

    void sendHologramPackets(@NonNull Player player, @NonNull FakeEntity entity, boolean needSpawn, @NonNull String textLine);
}
