package su.nightexpress.nexshop.shop.virtual.compatibility.citizens;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ILoadable;
import su.nexmedia.engine.hooks.external.citizens.CitizensHook;
import su.nexmedia.engine.hooks.external.citizens.CitizensListener;
import su.nexmedia.engine.utils.ArrayUtil;
import su.nightexpress.nexshop.api.shop.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

import java.util.Optional;

public class NpcShopListener implements CitizensListener, ILoadable {

    private final VirtualShop  virtualShop;
    private final TraitInfo    shopTrait;

    public NpcShopListener(@NotNull VirtualShop virtualShop) {
        this.virtualShop = virtualShop;
        this.shopTrait = TraitInfo.create(ShopTrait.class).withName("exshop");
    }

    @Override
    public void setup() {
        CitizensHook.registerTrait(this.virtualShop.plugin(), this.shopTrait);
        CitizensHook.addListener(this.virtualShop.plugin(), this);
    }

    @Override
    public void shutdown() {
        CitizensHook.unregisterTrait(this.virtualShop.plugin(), this.shopTrait);
        CitizensHook.getListeners(this.virtualShop.plugin()).remove(this);
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
        Optional<IShopVirtual> optShop = this.virtualShop.getShops().stream()
                .filter(shop2 -> ArrayUtil.contains(shop2.getCitizensIds(), id))
                .findFirst();

        IShopVirtual shop = optShop.orElse(null);
        if (shop == null) return;

        Player player = e.getClicker();
        shop.open(player, 1);
    }
}
