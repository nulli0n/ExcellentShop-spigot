package su.nightexpress.excellentshop.api.packet;

import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.menu.SellingMenuAdapter;
import su.nightexpress.excellentshop.api.menu.SellingMenuProvider;
import su.nightexpress.excellentshop.api.packet.display.DisplayAdapter;
import su.nightexpress.excellentshop.api.packet.display.DisplaySettings;

public interface PacketLibrary {

    @NonNull String getName();

    @NonNull SellingMenuAdapter createSellingMenuAdapter(@NonNull JavaPlugin plugin, @NonNull SellingMenuProvider provider);

    @NonNull DisplayAdapter createDisplayAdapter(@NonNull DisplaySettings settings);
}
