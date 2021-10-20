package su.nightexpress.nexshop.shop.virtual.compatibility.citizens;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ILoadable;
import su.nexmedia.engine.hooks.external.citizens.CitizensHK;
import su.nexmedia.engine.hooks.external.citizens.CitizensListener;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

import java.util.Optional;

public class NpcShopListener implements CitizensListener, ILoadable {

    private final VirtualShop virtualShop;
    private final CitizensHK citizens;
    private final TraitInfo  shopTrait;

    public NpcShopListener(@NotNull VirtualShop virtualShop, @NotNull CitizensHK citizens) {
        this.virtualShop = virtualShop;
        this.citizens = citizens;
        this.shopTrait = TraitInfo.create(ShopTrait.class).withName("exshop");
    }

    @Override
    public void setup() {
        this.citizens.registerTrait(this.virtualShop.plugin(), this.shopTrait);
        this.citizens.addListener(this.virtualShop.plugin(), this);
    }

    @Override
    public void shutdown() {
        this.citizens.unregisterTrait(this.virtualShop.plugin(), this.shopTrait);
        this.citizens.removeListener(this);
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
                .filter(shop2 -> ArrayUtils.contains(shop2.getCitizensIds(), id))
                .findFirst();

        IShopVirtual shop = optShop.orElse(null);
        if (shop == null) return;

        Player player = e.getClicker();
        shop.open(player, 1);
    }
}
