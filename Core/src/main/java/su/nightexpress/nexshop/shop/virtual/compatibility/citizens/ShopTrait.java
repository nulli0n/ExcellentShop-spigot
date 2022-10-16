package su.nightexpress.nexshop.shop.virtual.compatibility.citizens;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

@TraitName("exshop")
public class ShopTrait extends Trait {

    public ShopTrait() {
        super("exshop");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        VirtualShop virtualShop = ShopAPI.getVirtualShop();
        if (virtualShop == null) return;

        if (e.getNPC() == this.getNPC()) {
            Player player = e.getClicker();
            virtualShop.openMainMenu(player);
        }
    }
}