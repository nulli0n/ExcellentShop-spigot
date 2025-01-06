package su.nightexpress.nexshop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.AuctionUtils;
import su.nightexpress.nexshop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.impl.ConfigMenu;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.ItemOptions;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.menu.link.Linked;
import su.nightexpress.nightcore.menu.link.ViewLink;
import su.nightexpress.nightcore.util.ItemReplacer;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static su.nightexpress.nexshop.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class PurchaseConfirmMenu extends ConfigMenu<ShopPlugin> implements Linked<ActiveListing> {

    public static final String FILE_NAME = "purchase_confirm.yml";

    private final AuctionManager auctionManager;
    private final ViewLink<ActiveListing> link;

    private final ItemHandler acceptHandler;
    private final ItemHandler declineHandler;

    private int itemSlot;

    public PurchaseConfirmMenu(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager) {
        super(plugin, FileConfig.loadOrExtract(plugin, auctionManager.getMenusPath(), FILE_NAME));
        this.auctionManager = auctionManager;
        this.link = new ViewLink<>();

        this.addHandler(this.acceptHandler = new ItemHandler("confirmation_accept", (viewer, event) -> {
            Player player = viewer.getPlayer();
            ActiveListing listing = this.getLink(player);
            this.auctionManager.buy(player, listing);
            this.runNextTick(player::closeInventory);
        }));

        this.addHandler(this.declineHandler = new ItemHandler("confirmation_decline", (viewer, event) -> {
            this.runNextTick(() -> this.auctionManager.openAuction(viewer.getPlayer()));
        }));

        this.load();

        this.getItems().forEach(menuItem -> {
            menuItem.getOptions().addDisplayModifier((viewer, item) -> {
                ActiveListing listing = this.getLink(viewer);

                ItemReplacer.create(item).readMeta()
                    .replace(listing.replacePlaceholders())
                    .replace(GENERIC_BALANCE, () -> listing.getCurrency().format(listing.getCurrency().getBalance(viewer.getPlayer())))
                    .replacePlaceholderAPI(viewer.getPlayer())
                    .writeMeta();
            });
        });
    }

    @NotNull
    @Override
    public ViewLink<ActiveListing> getLink() {
        return link;
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        ActiveListing listing = this.getLink(viewer);

        ItemStack item = new ItemStack(listing.getItemStack()); // Copy to prevent modifying

        AuctionUtils.hideListingAttributes(item);

        MenuItem menuItem = new MenuItem(item);
        menuItem.setSlots(this.itemSlot);
        menuItem.setOptions(ItemOptions.personalWeak(viewer.getPlayer()));
        this.addItem(menuItem);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    @Override
    protected @NotNull MenuOptions createDefaultOptions() {
        return new MenuOptions(BLACK.enclose("Confirm Purchase"), MenuSize.CHEST_9);
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack acceptItem = ItemUtil.getSkinHead(SKIN_CHECK_MARK);
        ItemUtil.editMeta(acceptItem, meta -> {
            meta.setDisplayName(LIGHT_GREEN.enclose(BOLD.enclose("Accept")));
            meta.setLore(Lists.newList(
                LIGHT_GREEN.enclose("▪ " + LIGHT_GRAY.enclose("Price: ") + LISTING_PRICE),
                LIGHT_GREEN.enclose("▪ " + LIGHT_GRAY.enclose("Balance: ") + GENERIC_BALANCE),
                LIGHT_GREEN.enclose("▪ " + LIGHT_GRAY.enclose("Seller: ") + LISTING_SELLER)
            ));
        });
        list.add(new MenuItem(acceptItem).setSlots(8).setPriority(10).setHandler(this.acceptHandler));

        ItemStack cancelItem = ItemUtil.getSkinHead(SKIN_WRONG_MARK);
        ItemUtil.editMeta(cancelItem, meta -> {
            meta.setDisplayName(LIGHT_RED.enclose(BOLD.enclose("Cancel")));
        });
        list.add(new MenuItem(cancelItem).setSlots(0).setPriority(10).setHandler(this.declineHandler));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.itemSlot = ConfigValue.create("Item_Slot", 4).read(cfg);
    }
}
