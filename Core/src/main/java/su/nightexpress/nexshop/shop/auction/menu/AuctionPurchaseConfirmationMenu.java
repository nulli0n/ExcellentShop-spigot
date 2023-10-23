package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.ItemOptions;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.listing.ActiveListing;

import java.util.Map;
import java.util.WeakHashMap;

public class AuctionPurchaseConfirmationMenu extends ConfigMenu<ExcellentShop> {

    private final AuctionManager auctionManager;
    private final int            itemSlot;

    private final Map<Player, ActiveListing> cache;

    public AuctionPurchaseConfirmationMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager.plugin(), cfg);
        this.auctionManager = auctionManager;
        this.itemSlot = cfg.getInt("Item_Slot");
        this.cache = new WeakHashMap<>();

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CONFIRMATION_ACCEPT, (viewer, event) -> {
                Player player = viewer.getPlayer();
                ActiveListing listing = this.cache.get(player);
                if (listing != null) {
                    this.auctionManager.buy(player, listing);
                }
                player.closeInventory();
            })
            .addClick(MenuItemType.CONFIRMATION_DECLINE, (viewer, event) -> {
                this.auctionManager.getMainMenu().openNextTick(viewer, 1);
            });

        this.load();

        this.getItems().forEach(menuItem -> {
            menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                ActiveListing listing = this.cache.get(viewer.getPlayer());
                if (listing == null) return;

                ItemUtil.replace(item, listing.replacePlaceholders());

                if (Config.GUI_PLACEHOLDER_API.get() && EngineUtils.hasPlaceholderAPI()) {
                    ItemUtil.setPlaceholderAPI(viewer.getPlayer(), item);
                }
            });
        });
    }

    public void open(@NotNull Player player, @NotNull ActiveListing listing) {
        this.cache.put(player, listing);
        this.open(player, 1);
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        ActiveListing listing = this.cache.get(viewer.getPlayer());
        if (listing == null) return;

        ItemStack item = new ItemStack(listing.getItemStack()); // Copy to prevent modifying
        MenuItem menuItem = new MenuItem(item);
        menuItem.setSlots(this.itemSlot);
        menuItem.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
        this.addItem(menuItem);
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        super.onClose(viewer, event);
        this.cache.remove(viewer.getPlayer());
    }
}
