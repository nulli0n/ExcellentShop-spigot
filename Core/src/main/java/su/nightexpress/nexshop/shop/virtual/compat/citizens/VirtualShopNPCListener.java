package su.nightexpress.nexshop.shop.virtual.compat.citizens;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.external.citizens.CitizensListener;
import su.nexmedia.engine.utils.ArrayUtil;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;

public class VirtualShopNPCListener implements CitizensListener {

    private final VirtualShopModule module;

    public VirtualShopNPCListener(@NotNull VirtualShopModule module) {
        this.module = module;
    }

    @Override
    public void onLeftClick(NPCLeftClickEvent e) {
        this.onClick(e);
    }

    @Override
    public void onRightClick(NPCRightClickEvent e) {
        this.onClick(e);
    }

    private void onClick(@NotNull NPCClickEvent e) {
        int id = e.getNPC().getId();
        VirtualShop shop = this.module.getShops().stream()
            .filter(shop2 -> ArrayUtil.contains(shop2.getCitizensIds(), id))
            .findFirst().orElse(null);
        if (shop == null) return;

        Player player = e.getClicker();
        shop.open(player, 1);
    }
}
