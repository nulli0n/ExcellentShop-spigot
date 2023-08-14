package su.nightexpress.nexshop.shop.virtual.listener;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

public class VirtualShopNPCListener extends AbstractListener<ExcellentShop> {

    private final VirtualShopModule module;

    public VirtualShopNPCListener(@NotNull VirtualShopModule module) {
        super(module.plugin());
        this.module = module;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLeftClick(NPCLeftClickEvent e) {
        this.onClick(e);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRightClick(NPCRightClickEvent e) {
        this.onClick(e);
    }

    private void onClick(@NotNull NPCClickEvent e) {
        int id = e.getNPC().getId();

        VirtualShop<?, ?> shop = this.module.getShops().stream()
            .filter(shop2 -> shop2.getNPCIds().contains(id))
            .findFirst().orElse(null);
        if (shop == null) return;

        Player player = e.getClicker();
        if (!shop.canAccess(player, true)) return;

        shop.open(player, 1);
    }
}
