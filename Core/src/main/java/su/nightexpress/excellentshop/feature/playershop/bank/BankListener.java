package su.nightexpress.excellentshop.feature.playershop.bank;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.nightcore.manager.AbstractListener;

public class BankListener extends AbstractListener<ShopPlugin> {

    private final BankManager manager;

    public BankListener(@NonNull ShopPlugin plugin, @NonNull BankManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        this.manager.handlePlayerJoin(event);
    }
}
