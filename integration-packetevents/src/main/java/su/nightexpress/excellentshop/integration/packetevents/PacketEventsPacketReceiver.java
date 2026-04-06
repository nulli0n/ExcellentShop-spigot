package su.nightexpress.excellentshop.integration.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import su.nightexpress.excellentshop.api.packet.PacketReceiver;

public class PacketEventsPacketReceiver implements PacketListener, PacketReceiver {

    protected PacketListenerCommon backend;

    @Override
    public void register() {
        this.backend = PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
    }

    @Override
    public void unregister() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this.backend);
    }
}
