package su.nightexpress.nexshop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.listing.AbstractListing;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.api.AutoFilled;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Plugins;

import java.util.*;

public abstract class AbstractAuctionMenu<A extends AbstractListing> extends ConfigMenu<ShopPlugin> implements AutoFilled<A>, Linked<UUID> {

    protected final AuctionManager auctionManager;
    protected final ViewLink<UUID> link;

    protected final ItemHandler returnHandler;
    protected final ItemHandler expiredHandler;
    protected final ItemHandler historyHandler;
    protected final ItemHandler unclaimedHandler;
    protected final ItemHandler listingsHandler;

    protected String       itemName;
    protected List<String> itemLore;
    protected int[]        itemSlots;

    public AbstractAuctionMenu(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager, @NotNull String fileName) {
        super(plugin, FileConfig.loadOrExtract(plugin, auctionManager.getMenusPath(), fileName));
        this.auctionManager = auctionManager;
        this.link = new ViewLink<>();

        this.addHandler(this.returnHandler = ItemHandler.forReturn(this, (viewer, event) -> {
            this.runNextTick(() -> this.auctionManager.openAuction(viewer.getPlayer()));
        }));

        this.addHandler(this.expiredHandler = new ItemHandler("expired_listings", (viewer, event) -> {
            this.runNextTick(() -> this.auctionManager.openExpiedListings(viewer.getPlayer()));
        }));

        this.addHandler(this.historyHandler = new ItemHandler("sales_history", (viewer, event) -> {
            this.runNextTick(() -> this.auctionManager.openSalesHistory(viewer.getPlayer()));
        }));

        this.addHandler(this.unclaimedHandler = new ItemHandler("unclaimed_items", (viewer, event) -> {
            this.runNextTick(() -> this.auctionManager.openUnclaimedListings(viewer.getPlayer()));
        }));

        this.addHandler(this.listingsHandler = new ItemHandler("own_listings", (viewer, event) -> {
            this.runNextTick(() -> this.auctionManager.openPlayerListings(viewer.getPlayer()));
        }));
    }

    @NotNull
    @Override
    public ViewLink<UUID> getLink() {
        return link;
    }

    @Override
    public void load() {
        super.load();

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            if (Config.GUI_PLACEHOLDER_API.get() && Plugins.hasPlaceholderAPI()) {
                ItemReplacer.create(item).readMeta().replacePlaceholderAPI(viewer.getPlayer()).writeMeta();
            }
        }));
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        this.autoFill(viewer);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<A> autoFill) {
        Player player = viewer.getPlayer();

        autoFill.setSlots(this.itemSlots);
        autoFill.setItemCreator(aucItem -> {
            ItemStack item = new ItemStack(aucItem.getItemStack());

            AuctionUtils.hideListingAttributes(item);

            ItemReplacer.create(item).trimmed()
                .setDisplayName(this.itemName)
                .setLore(this.itemLore)
                .replace(aucItem.replacePlaceholders())
                .replacePlaceholderAPI(player)
                .writeMeta();
            return item;
        });
    }
}
