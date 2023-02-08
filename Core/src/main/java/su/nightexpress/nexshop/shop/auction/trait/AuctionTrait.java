package su.nightexpress.nexshop.shop.auction.trait;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.shop.auction.AuctionManager;

@TraitName("auction")
public class AuctionTrait extends Trait {

    public AuctionTrait() {
        super("auction");
    }

    @EventHandler
    public void click(NPCRightClickEvent e) {
        AuctionManager auctionManager = ShopAPI.getAuctionManager();
        if (auctionManager == null) return;

        if (e.getNPC() == this.getNPC()) {
            Player player = e.getClicker();
            auctionManager.getMainMenu().open(player, 1);
        }
    }
}
