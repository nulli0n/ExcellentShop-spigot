package su.nightexpress.nexshop.auction.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.Listings;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.menu.MenuOptions;
import su.nightexpress.nightcore.menu.MenuSize;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.AutoFill;
import su.nightexpress.nightcore.menu.item.ItemHandler;
import su.nightexpress.nightcore.menu.item.MenuItem;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static su.nightexpress.nexshop.auction.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class ExpiredListingsMenu extends AbstractAuctionMenu<ActiveListing> {

    public static final String FILE_NAME = "expired.yml";

    private final ItemHandler takeAllHandler;

    public ExpiredListingsMenu(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager) {
        super(plugin, auctionManager, FILE_NAME);

        this.addHandler(this.takeAllHandler = new ItemHandler("take_all", (viewer, event) -> {
            Player player = viewer.getPlayer();
            this.auctionManager.getListings().getExpired(player).forEach(listing -> {
                this.auctionManager.takeListing(player, listing);
            });
            this.runNextTick(() -> this.flush(player));
        }));

        this.load();
    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<ActiveListing> autoFill) {
        super.onAutoFill(viewer, autoFill);
        autoFill.setItems(Listings.sorted(this.auctionManager.getListings().getExpired(this.getLink(viewer))));
        autoFill.setClickAction(item -> (viewer1, event) -> {
            Player player = viewer1.getPlayer();
            this.auctionManager.takeListing(player, item);
            this.runNextTick(() -> this.flush(viewer));
        });
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        MenuOptions options = new MenuOptions(BLACK.enclose("Expired Listings"), MenuSize.CHEST_54);
        options.setAutoRefresh(1);
        return options;
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

        ItemStack takeAllItem = new ItemStack(Material.HOPPER);
        ItemUtil.editMeta(takeAllItem, meta -> {
            meta.setDisplayName(LIGHT_YELLOW.enclose(BOLD.enclose("Take All")));
        });
        list.add(new MenuItem(takeAllItem).setSlots(51).setPriority(10).setHandler(this.takeAllHandler));

        ItemStack backItem = ItemUtil.getSkinHead(SKIN_ARROW_DOWN);
        ItemUtil.editMeta(backItem, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_RETURN.getDefaultName());
        });
        list.add(new MenuItem(backItem).setSlots(49).setPriority(10).setHandler(this.returnHandler));

        ItemStack prevPage = ItemUtil.getSkinHead(SKIN_ARROW_LEFT);
        ItemUtil.editMeta(prevPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_PREVIOUS_PAGE.getDefaultName());
        });
        list.add(new MenuItem(prevPage).setSlots(45).setPriority(10).setHandler(ItemHandler.forPreviousPage(this)));

        ItemStack nextPage = ItemUtil.getSkinHead(SKIN_ARROW_RIGHT);
        ItemUtil.editMeta(nextPage, meta -> {
            meta.setDisplayName(Lang.EDITOR_ITEM_NEXT_PAGE.getDefaultName());
        });
        list.add(new MenuItem(nextPage).setSlots(53).setPriority(10).setHandler(ItemHandler.forNextPage(this)));

        return list;
    }

    @Override
    protected void loadAdditional() {
        this.itemName = ConfigValue.create("Items.Name", 
            LIGHT_YELLOW.enclose(BOLD.enclose(LISTING_ITEM_NAME))
        ).read(cfg);

        this.itemLore = ConfigValue.create("Items.Lore", Lists.newList(
            LISTING_ITEM_LORE,
                "",
                LIGHT_YELLOW.enclose(BOLD.enclose("Info:")),
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Price: ") + LISTING_PRICE),
                LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Added: ") + LISTING_DATE_CREATION),
                "",
                LIGHT_GRAY.enclose(LIGHT_RED.enclose("[❗]") + " Deletes in: " + LIGHT_RED.enclose(LISTING_DELETES_IN)),
                "",
                LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("take") + ".")
        )).read(cfg);

        this.itemSlots = ConfigValue.create("Items.Slots", IntStream.range(0, 36).toArray()).read(cfg);
    }
}
