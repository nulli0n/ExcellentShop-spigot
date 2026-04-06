package su.nightexpress.excellentshop.integration.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.packet.PacketReceiver;

public abstract class ProtocolLibPacketReceiver extends PacketAdapter implements PacketReceiver {

    public ProtocolLibPacketReceiver(@NonNull Plugin plugin, @NonNull PacketType... types) {
        super(plugin, types);
    }

    @Override
    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListener(this);
    }
}
