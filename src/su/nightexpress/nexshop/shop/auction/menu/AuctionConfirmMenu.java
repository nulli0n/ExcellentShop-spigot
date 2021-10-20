package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.object.AuctionListing;

import java.util.Map;
import java.util.WeakHashMap;

public class AuctionConfirmMenu extends AbstractMenu<ExcellentShop> {

    private final AuctionManager auctionManager;
    private final int            itemSlot;

    private final Map<Player, AuctionListing> cache;

    public AuctionConfirmMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager.plugin(), cfg, "");
        this.auctionManager = auctionManager;
        this.itemSlot = cfg.getInt("Item_Slot");
        this.cache = new WeakHashMap<>();

        IMenuClick click = (player, type, e) -> {

            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.CONFIRMATION_ACCEPT) {
                    AuctionListing listing = this.cache.get(player);
                    if (listing != null) {
                        this.auctionManager.buy(player, listing);
                    }

                    this.auctionManager.openAuction(player);
                }
                else if (type2 == MenuItemType.CONFIRMATION_DECLINE) {
                    this.auctionManager.openAuction(player);
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    public void open(@NotNull Player player, @NotNull AuctionListing listing) {
        ItemStack item = new ItemStack(listing.getItemStack()); // Copy to prevent modifying

        this.addItem(player, item, this.itemSlot);
        this.cache.put(player, listing);
        this.open(player, 1);
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        AuctionListing listing = this.cache.get(player);
        if (listing == null) return;

        ItemUT.replace(item, listing.replacePlaceholders());
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        this.cache.remove(player);
    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
