package su.nightexpress.nexshop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.Listings;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
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

public class UnclaimedListingsMenu extends AbstractAuctionMenu<CompletedListing> {

    public static final String FILE_NAME = "unclaimed.yml";

    public UnclaimedListingsMenu(@NotNull ShopPlugin plugin, @NotNull AuctionManager auctionManager) {
        super(plugin, auctionManager, FILE_NAME);

        this.load();
    }

    @Override
    public void onAutoFill(@NotNull MenuViewer viewer, @NotNull AutoFill<CompletedListing> autoFill) {
        super.onAutoFill(viewer, autoFill);
        autoFill.setItems(Listings.sorted(this.auctionManager.getListings().getUnclaimed(this.getLink(viewer))));
        autoFill.setClickAction(listing -> (viewer1, event) -> {
            Player player = viewer.getPlayer();
            this.auctionManager.claimRewards(player, listing);
            this.runNextTick(() -> this.flush(viewer));
        });
    }

    @Override
    @NotNull
    protected MenuOptions createDefaultOptions() {
        MenuOptions options = new MenuOptions(BLACK.enclose("Unclaimed Listings"), MenuSize.CHEST_54);
        options.setAutoRefresh(1);
        return options;
    }

    @Override
    @NotNull
    protected List<MenuItem> createDefaultItems() {
        List<MenuItem> list = new ArrayList<>();

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
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Buyer: ") + LISTING_BUYER),
            LIGHT_YELLOW.enclose("▪ " + LIGHT_GRAY.enclose("Date: ") + LISTING_BUY_DATE),
            "",
            LIGHT_GRAY.enclose(LIGHT_RED.enclose("[❗]") + " Deletes in: " + LIGHT_RED.enclose(LISTING_DELETES_IN)),
            "",
            LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose("[▶]") + " Click to " + LIGHT_YELLOW.enclose("claim " + LISTING_PRICE) + ".")
        )).read(cfg);

        this.itemSlots = ConfigValue.create("Items.Slots", IntStream.range(0, 36).toArray()).read(cfg);
    }
}
