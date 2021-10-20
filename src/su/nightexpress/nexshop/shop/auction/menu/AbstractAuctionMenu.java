package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.object.AbstractAuctionItem;

import java.util.*;

public abstract class AbstractAuctionMenu<A extends AbstractAuctionItem> extends AbstractMenuAuto<ExcellentShop, A> {

    protected AuctionManager auctionManager;

    protected String       itemName;
    protected List<String> itemLore;

    protected Map<Player, UUID> seeOthers;

    public AbstractAuctionMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager.plugin(), cfg, "");
        this.auctionManager = auctionManager;
        this.seeOthers = new HashMap<>();

        this.itemName = StringUT.color(cfg.getString("Items.Name", AbstractAuctionItem.PLACEHOLDER_ITEM_NAME));
        this.itemLore = StringUT.color(cfg.getStringList("Items.Lore"));
        this.objectSlots = cfg.getIntArray("Items.Slots");
    }

    public void open(@NotNull Player player, int page, @NotNull UUID id) {
        if (!id.equals(player.getUniqueId())) {
            this.seeOthers.put(player, id);
        }
        this.open(player, page);
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull A aucItem) {
        ItemStack item = new ItemStack(aucItem.getItemStack());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(this.itemName);
        meta.setLore(this.itemLore);
        item.setItemMeta(meta);

        ItemUT.replace(item, aucItem.replacePlaceholders());
        return item;
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        this.seeOthers.remove(player);
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
