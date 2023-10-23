package su.nightexpress.nexshop.shop.auction.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.Placeholders;
import su.nightexpress.nexshop.shop.auction.listing.AbstractListing;
import su.nightexpress.nexshop.shop.auction.menu.type.AuctionItemType;

import java.util.*;

public abstract class AbstractAuctionMenu<A extends AbstractListing> extends ConfigMenu<ExcellentShop> implements AutoPaged<A> {

    protected AuctionManager auctionManager;

    protected int[]        objectSlots;
    protected String       itemName;
    protected List<String> itemLore;

    protected Map<FormatType, List<String>> loreFormat;
    protected Map<Player, UUID> seeOthers;

    private static final String PLACEHOLDER_LORE_FORMAT = "%lore_format%";

    public AbstractAuctionMenu(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager.plugin(), cfg);
        this.auctionManager = auctionManager;
        this.seeOthers = new WeakHashMap<>();
        this.loreFormat = new HashMap<>();

        this.itemName = Colorizer.apply(cfg.getString("Items.Name", Placeholders.LISTING_ITEM_NAME));
        this.itemLore = Colorizer.apply(cfg.getStringList("Items.Lore"));
        this.objectSlots = cfg.getIntArray("Items.Slots");
        for (FormatType formatType : FormatType.values()) {
            this.loreFormat.put(formatType, Colorizer.apply(cfg.getStringList("Lore_Format." + formatType.name())));
        }

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, ClickHandler.forClose(this))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this))
            .addClick(MenuItemType.RETURN, (viewer, event) -> this.auctionManager.getMainMenu().openNextTick(viewer, 1));

        this.registerHandler(AuctionItemType.class)
            .addClick(AuctionItemType.EXPIRED_LISTINGS, (viewer, event) -> {
                this.auctionManager.getExpiredMenu().openNextTick(viewer, 1);
            })
            .addClick(AuctionItemType.SALES_HISTORY, (viewer, event) -> {
                this.auctionManager.getHistoryMenu().openNextTick(viewer, 1);
            })
            .addClick(AuctionItemType.UNCLAIMED_ITEMS, (viewer, event) -> {
                this.auctionManager.getUnclaimedMenu().openNextTick(viewer, 1);
            })
            .addClick(AuctionItemType.OWN_LISTINGS, (viewer, event) -> {
                this.auctionManager.getSellingMenu().openNextTick(viewer, 1);
            });
    }

    @Override
    public void load() {
        super.load();

        this.getItems().forEach(menuItem -> menuItem.getOptions().addDisplayModifier((viewer, item) -> {
            if (Config.GUI_PLACEHOLDER_API.get() && EngineUtils.hasPlaceholderAPI()) {
                ItemUtil.setPlaceholderAPI(viewer.getPlayer(), item);
            }
        }));
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    enum FormatType {
        OWNER, PLAYER, ADMIN
    }

    public void open(@NotNull Player player, int page, @NotNull UUID id) {
        if (!id.equals(player.getUniqueId())) {
            this.seeOthers.put(player, id);
        }
        this.open(player, page);
    }

    @Override
    public int[] getObjectSlots() {
        return objectSlots;
    }

    @Override
    @NotNull
    public ItemStack getObjectStack(@NotNull Player player, @NotNull A aucItem) {
        ItemStack item = new ItemStack(aucItem.getItemStack());
        ItemUtil.mapMeta(item, meta -> {
            List<String> lore = StringUtil.replaceInList(this.itemLore, PLACEHOLDER_LORE_FORMAT, this.getLoreFormat(player, aucItem));
            meta.setDisplayName(this.itemName);
            meta.setLore(lore);
            //lore.replaceAll(aucItem.replacePlaceholders());
            //meta.setDisplayName(aucItem.replacePlaceholders().apply(this.itemName));
            //meta.setLore(lore);
            ItemUtil.replace(meta, aucItem.replacePlaceholders());
        });
        return item;
    }

    @NotNull
    protected List<String> getLoreFormat(@NotNull Player player, @NotNull A aucItem) {
        FormatType formatType = FormatType.PLAYER;
        if (player.hasPermission(Perms.AUCTION_LISTING_REMOVE_OTHERS)) formatType = FormatType.ADMIN;
        else if (aucItem.isOwner(player)) formatType = FormatType.OWNER;

        return this.loreFormat.getOrDefault(formatType, Collections.emptyList());
    }

    @Override
    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        super.onClose(viewer, event);
        this.seeOthers.remove(viewer.getPlayer());
    }
}
