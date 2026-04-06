package su.nightexpress.excellentshop.integration.protocollib;

import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.menu.SellingMenuAdapter;
import su.nightexpress.excellentshop.api.menu.SellingMenuProvider;
import su.nightexpress.excellentshop.api.packet.PacketLibrary;
import su.nightexpress.excellentshop.api.packet.display.DisplaySettings;
import su.nightexpress.excellentshop.integration.protocollib.adapter.ProtocolLibDisplayAdapter;
import su.nightexpress.excellentshop.integration.protocollib.adapter.ProtocolLibSellingMenuAdapter;

public class ProtocolLibHook implements PacketLibrary {

    @Override
    @NonNull
    public String getName() {
        return "ProtocolLib";
    }

    @Override
    @NonNull
    public SellingMenuAdapter createSellingMenuAdapter(@NonNull JavaPlugin plugin, @NonNull SellingMenuProvider provider) {
        return new ProtocolLibSellingMenuAdapter(plugin, provider);
    }

    @Override
    @NonNull
    public ProtocolLibDisplayAdapter createDisplayAdapter(@NonNull DisplaySettings settings) {
        return new ProtocolLibDisplayAdapter(settings);
    }
}
