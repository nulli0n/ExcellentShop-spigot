package su.nightexpress.nexshop.shop.virtual.compat.citizens;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

@TraitName("virtualshop")
public class VirtualShopTrait extends Trait {

    public VirtualShopTrait() {
        super("virtualshop");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        VirtualShopModule virtualShop = ShopAPI.getVirtualShop();
        if (virtualShop == null) return;

        if (e.getNPC() == this.getNPC()) {
            Player player = e.getClicker();
            virtualShop.openMainMenu(player);
        }
    }
}