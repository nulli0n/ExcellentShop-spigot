package su.nightexpress.nexshop.shop.virtual.listener;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nightcore.manager.AbstractListener;

public class VirtualShopNPCListener extends AbstractListener<ShopPlugin> {

    private final VirtualShopModule module;

    public VirtualShopNPCListener(@NotNull VirtualShopModule module) {
        super(module.plugin());
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeftClick(NPCLeftClickEvent event) {
        this.onClick(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRightClick(NPCRightClickEvent event) {
        this.onClick(event);
    }

    private void onClick(@NotNull NPCClickEvent event) {
        int id = event.getNPC().getId();

        this.module.getShops().stream().filter(shop -> shop.getNPCIds().contains(id)).findFirst().ifPresent(shop -> {
            shop.open(event.getClicker());
        });
    }
}
