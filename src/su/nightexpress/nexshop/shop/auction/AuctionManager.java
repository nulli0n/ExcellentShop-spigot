package su.nightexpress.nexshop.shop.auction;

import com.google.common.collect.Lists;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.data.StorageType;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.VaultHK;
import su.nexmedia.engine.utils.ClickText;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.hooks.EHook;
import su.nightexpress.nexshop.modules.EModule;
import su.nightexpress.nexshop.modules.ShopModule;
import su.nightexpress.nexshop.shop.auction.command.AuctionCommand;
import su.nightexpress.nexshop.shop.auction.command.ExpiredCommand;
import su.nightexpress.nexshop.shop.auction.command.HistoryCommand;
import su.nightexpress.nexshop.shop.auction.command.SellCommand;
import su.nightexpress.nexshop.shop.auction.compatibility.ImportAuctionHouse;
import su.nightexpress.nexshop.shop.auction.menu.*;
import su.nightexpress.nexshop.shop.auction.object.AbstractAuctionItem;
import su.nightexpress.nexshop.shop.auction.object.AuctionHistoryItem;
import su.nightexpress.nexshop.shop.auction.object.AuctionListing;

import java.util.*;
import java.util.function.Predicate;

public class AuctionManager extends ShopModule {

    private List<AuctionListing>     listings;
    private List<AuctionListing>     expired;
    private List<AuctionHistoryItem> history;

    private AuctionMainMenu    auctionMainMenu;
    private AuctionConfirmMenu auctionConfirmMenu;
    private AuctionExpiredMenu auctionExpiredMenu;
    private AuctionHistoryMenu auctionHistoryMenu;

    private VaultHK     vaultHook;

    public AuctionManager(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getId() {
        return EModule.AUCTION;
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.50";
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        this.vaultHook = plugin.getVault();
        if (this.vaultHook == null || !this.vaultHook.hasEconomy()) {
            this.interruptLoad("No Vault Economy found! Auction will be disabled!");
            return;
        }

        if (!this.isSynced()) {
            this.listings = new ArrayList<>(this.getListingsData());
            this.expired = new ArrayList<>(this.getExpiredData());
            this.history = new ArrayList<>(this.getHistoryData());
        }

        AuctionConfig.load(this);

        JYML cfgGui = JYML.loadOrExtract(plugin, this.getPath() + "auction.menu.yml");
        this.auctionMainMenu = new AuctionMainMenu(this, cfgGui);

        JYML cfgGuiConfirm = JYML.loadOrExtract(plugin, this.getPath() + "auction.confirm.menu.yml");
        this.auctionConfirmMenu = new AuctionConfirmMenu(this, cfgGuiConfirm);

        JYML cfgGuiExpired = JYML.loadOrExtract(plugin, this.getPath() + "expired.menu.yml");
        this.auctionExpiredMenu = new AuctionExpiredMenu(this, cfgGuiExpired);

        JYML cfgGuiHistory = JYML.loadOrExtract(plugin, this.getPath() + "history.menu.yml");
        this.auctionHistoryMenu = new AuctionHistoryMenu(this, cfgGuiHistory);

        this.moduleCommand.addDefaultCommand(new AuctionCommand(this));
        this.moduleCommand.addChildren(new SellCommand(this));
        this.moduleCommand.addChildren(new HistoryCommand(this));
        this.moduleCommand.addChildren(new ExpiredCommand(this));

        this.addListener(new Listener(this.plugin));

        this.plugin.getServer().getScheduler().runTaskLater(plugin, this::importData, 5L);
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();

        if (this.auctionConfirmMenu != null) {
            this.auctionConfirmMenu.clear();
            this.auctionConfirmMenu = null;
        }
        if (this.auctionMainMenu != null) {
            this.auctionMainMenu.clear();
            this.auctionMainMenu = null;
        }
        if (this.auctionExpiredMenu != null) {
            this.auctionExpiredMenu.clear();
            this.auctionExpiredMenu = null;
        }
        if (this.auctionHistoryMenu != null) {
            this.auctionHistoryMenu.clear();
            this.auctionHistoryMenu = null;
        }

        if (this.listings != null) {
            this.listings.clear();
            this.listings = null;
        }
        if (this.expired != null) {
            this.expired.clear();
            this.expired = null;
        }
        if (this.history != null) {
            this.history.clear();
            this.history = null;
        }
    }

    public boolean isSynced() {
        return plugin.cfg().dataStorage == StorageType.MYSQL && plugin.cfg().dataSaveInstant;
    }

    private void importData() {
        if (AuctionConfig.STORAGE_IMPORT_PLUGIN.equalsIgnoreCase(Constants.NONE)) return;

        this.info("Preparing to import auction data...");
        String pluginName = AuctionConfig.STORAGE_IMPORT_PLUGIN;
        if (!Hooks.hasPlugin(pluginName)) {
            this.warn("Could not import data from " + pluginName + ": No such plugin installed.");
        }

        if (pluginName.equalsIgnoreCase(EHook.AUCTION_HOUSE)) {
            ImportAuctionHouse.importData(this);
        }
        else {
            this.warn("Could not import data from " + pluginName + ": Not supported.");
        }

        this.info("Import finished.");
        this.cfg.set("settings.storage.import-from", Constants.NONE);
        this.cfg.saveChanges();
    }

    public boolean isAllowedItem(@NotNull ItemStack item) {
        if (AuctionConfig.LISTINGS_DISABLED_MATERIALS.contains(item.getType().name())) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return true;

        String metaName = meta.getDisplayName();
        if (AuctionConfig.LISTINGS_DISABLED_NAMES.stream().anyMatch(metaName::contains)) {
            return false;
        }

        List<String> metaLore = meta.getLore();
        if (metaLore == null) return true;

        if (metaLore.stream().anyMatch(line -> {
            return AuctionConfig.LISTINGS_DISABLED_LORES.stream().anyMatch(line::contains);
        })) {
            return false;
        }

        return true;
    }

    public boolean add(@NotNull Player player, @NotNull ItemStack item, double price) {
        GameMode gameMode = player.getGameMode();
        if (AuctionConfig.DISABLED_GAMEMODES.contains(gameMode.name())) {
            plugin.lang().Auction_Listing_Add_Error_DisabledGamemode.send(player);
            return false;
        }

        if (!this.isAllowedItem(item)) {
            plugin.lang().Auction_Listing_Add_Error_BadItem
                    .replace("%item%", ItemUT.getItemName(item))
                    .send(player);
            return false;
        }

        price = NumberUT.round(price);
        if (price <= 0) {
            plugin.lang().Auction_Listing_Add_Error_Price_Negative.send(player);
            return false;
        }

        Material material = item.getType();
        double mOne = price / (double) item.getAmount();
        double mMin = AuctionConfig.getMaterialMinPrice(material);
        if (mMin >= 0 && mOne < mMin) {
            plugin.lang().Auction_Listing_Add_Error_Price_Material_Min
                    .replace("%material%", plugin.lang().getEnum(material))
                    .replace("%min%", NumberUT.format(mMin))
                    .send(player);
            return false;
        }

        double mMax = AuctionConfig.getMaterialMaxPrice(material);
        if (mMax >= 0 && mOne > mMax) {
            plugin.lang().Auction_Listing_Add_Error_Price_Material_Max
                    .replace("%max%", NumberUT.format(mMax))
                    .replace("%material%", plugin.lang().getEnum(material))
                    .send(player);
            return false;
        }

        if (!player.hasPermission(Perms.AUCTION_BYPASS_LISTING_PRICE)) {
            if (AuctionConfig.LISTINGS_PRICE_MAX > 0 && price > AuctionConfig.LISTINGS_PRICE_MAX) {
                plugin.lang().Auction_Listing_Add_Error_Price_Max.replace("%max%", AuctionConfig.LISTINGS_PRICE_MAX)
                        .send(player);
                return false;
            }
            if (AuctionConfig.LISTINGS_PRICE_MIN > 0 && price < AuctionConfig.LISTINGS_PRICE_MIN) {
                plugin.lang().Auction_Listing_Add_Error_Price_Min.replace("%min%", AuctionConfig.LISTINGS_PRICE_MIN)
                        .send(player);
                return false;
            }
        }

        double tax = player.hasPermission(Perms.AUCTION_BYPASS_LISTING_TAX) ? 0D : AuctionConfig.LISTINGS_PRICE_TAX;
        double taxPay = price * (tax / 100D);

        if (taxPay > 0) {
            double balance = this.vaultHook.getBalance(player);
            if (balance < taxPay) {
                plugin.lang().Auction_Listing_Add_Error_Price_Tax
                        .replace("%tax-percent%", tax)
                        .replace("%tax-amount%", taxPay)
                        .send(player);
                return false;
            }
            this.vaultHook.take(player, taxPay);
        }

        int bidsHas = this.getListings(player).size();
        int bidsCan = this.getListingsMaximum(player);
        if (bidsCan >= 0 && bidsHas >= bidsCan) {
            plugin.lang().Auction_Listing_Add_Error_Limit.replace("%amount%", bidsCan).send(player);
            return false;
        }

        AuctionListing listing = new AuctionListing(player, item, price);
        if (!this.isSynced()) this.getListings().add(0, listing);
        this.plugin.getData().addAuctionListing(listing, true);

        plugin.lang().Auction_Listing_Add_Success_Info.replace(listing.replacePlaceholders()).send(player);

        if (taxPay > 0) {
            plugin.lang().Auction_Listing_Add_Success_Tax
                    .replace("%tax-percent%", tax)
                    .replace("%tax-amount%", taxPay)
                    .send(player);
        }

        if (AuctionConfig.LISTINGS_ANNOUNCE) {
            ClickText clickText = new ClickText(plugin.lang().Auction_Listing_Add_Success_Announce
                    .replace("%player%", player.getDisplayName())
                    .replace(listing.replacePlaceholders())
                    .getMsg());

            clickText.createPlaceholder("%item%", ItemUT.getItemName(item)).showItem(item);
            clickText.send(new HashSet<>(plugin.getServer().getOnlinePlayers()));
        }

        this.getAuctionMainMenu().update();
        return true;
    }

    public boolean buy(@NotNull Player buyer, @NotNull AuctionListing listing) {
        if (!this.getListings().contains(listing)) return false;

        double balance = this.vaultHook.getBalance(buyer);
        double price = listing.getPrice();
        if (balance < price) {
            plugin.lang().Auction_Listing_Buy_Error_NoMoney
                    .replace("%balance%", NumberUT.format(balance))
                    .replace(listing.replacePlaceholders())
                    .send(buyer);
            return false;
        }

        this.vaultHook.take(buyer, price);

        AuctionHistoryItem historyItem = new AuctionHistoryItem(listing, buyer);

        if (!this.isSynced()) {
            this.getListings().remove(listing);
            this.getHistory().add(historyItem);
        }

        ItemStack item = listing.getItemStack();
        ItemUT.addItem(buyer, item);

        OfflinePlayer seller = this.plugin.getServer().getOfflinePlayer(listing.getOwner());
        this.vaultHook.give(seller, historyItem.getPrice());

        Player sellerOnline = seller.getPlayer();
        if (sellerOnline != null && sellerOnline.isOnline()) {
            this.notifyHistory(sellerOnline, historyItem);
        }

        plugin.lang().Auction_Listing_Buy_Success_Info.replace(listing.replacePlaceholders()).send(buyer);
        plugin.getData().addAuctionHistory(historyItem, !this.isSynced());
        plugin.getData().deleteAuctionListing(listing, !this.isSynced());

        this.getAuctionMainMenu().update();
        return true;
    }

    private void notifyHistory(@NotNull Player player, @NotNull AuctionHistoryItem historyItem) {
        historyItem.setNotified(true);
        plugin.lang().Auction_Listing_Sell_Success_Info.replace(historyItem.replacePlaceholders()).send(player);
    }

    public void takeExpired(@NotNull Player player, @NotNull AuctionListing listing) {
        if (!this.getExpired().removeIf(list -> list.getId().equals(listing.getId()))) return;

        ItemUT.addItem(player, listing.getItemStack());
        this.plugin.getData().deleteAuctionListing(listing, !this.isSynced());

        this.getAuctionMainMenu().update();
    }

    public void takeListing(@NotNull Player player, @NotNull AuctionListing listing) {
        if (!this.getListings().removeIf(list -> list.getId().equals(listing.getId()))) return;

        ItemUT.addItem(player, listing.getItemStack());
        this.plugin.getData().deleteAuctionListing(listing, !this.isSynced());

        this.getAuctionMainMenu().update();
    }

    private boolean checkDisableds(@NotNull Player player) {
        if (player.hasPermission(Perms.ADMIN)) return true;

        World world = player.getWorld();
        if (AuctionConfig.DISABLED_WORLDS.contains(world.getName().toLowerCase())) {
            plugin.lang().Auction_Error_DisabledWorld.send(player);
            return false;
        }

        return true;
    }

    public boolean openAuction(@NotNull Player player) {
        return this.openAuctionGUI(player, this.getAuctionMainMenu());
    }

    public boolean openAuctionConfirm(@NotNull Player player, @NotNull AuctionListing listing) {
        if (!this.checkDisableds(player)) return false;

        this.auctionConfirmMenu.open(player, listing);
        return true;
    }

    public boolean openAuctionExpired(@NotNull Player player) {
        return this.openAuctionGUI(player, this.getAuctionExpiredMenu());
    }

    public boolean openAuctionHistory(@NotNull Player player) {
        return this.openAuctionGUI(player, this.getAuctionHistoryMenu());
    }

    private boolean openAuctionGUI(@NotNull Player player, @NotNull AbstractAuctionMenu<?> menu) {
        if (!this.checkDisableds(player)) return false;

        menu.open(player, menu.getPage(player));
        return true;
    }

    @NotNull
    public AuctionMainMenu getAuctionMainMenu() {
        return this.auctionMainMenu;
    }

    @NotNull
    public AuctionExpiredMenu getAuctionExpiredMenu() {
        return this.auctionExpiredMenu;
    }

    @NotNull
    public AuctionHistoryMenu getAuctionHistoryMenu() {
        return this.auctionHistoryMenu;
    }

    @Nullable
    public AuctionListing getListing(@NotNull String uuid) {
        Optional<AuctionListing> opt = this.getListings().stream()
                .filter(listing -> listing.getId().toString().equalsIgnoreCase(uuid)).findFirst();
        return opt.orElse(null);
    }

    @NotNull
    public List<AuctionListing> getListings() {
        List<AuctionListing> list = this.isSynced() ? this.getListingsData() : this.listings;

        list.removeIf(listing -> {
            if (!listing.isValid()) {
                this.plugin.getData().deleteAuctionListing(listing, true);
                return true;
            }
            if (listing.isExpired()) {
                if (!this.isSynced()) this.getExpired().add(listing);
                return true;
            }
            return false;
        });

        return list;
    }

    @NotNull
    private List<AuctionListing> getListingsData() {
        return new ArrayList<>(Lists.reverse(plugin.getData().getAuctionListing().stream().filter(lis -> !lis.isExpired()).toList()));
    }

    @NotNull
    public List<AuctionListing> getListings(@NotNull Player player) {
        return this.getListings(player.getUniqueId());
    }

    @NotNull
    public List<AuctionListing> getListings(@NotNull UUID id) {
        return this.getItems(id, this.getListings());
    }

    public int getListingsMaximum(@NotNull Player player) {
        return AuctionConfig.getPossibleListings(player);
    }

    @NotNull
    public List<AuctionListing> getExpired() {
        List<AuctionListing> list = this.isSynced() ? this.getExpiredData() : this.expired;

        list.removeIf(listing -> {
            if (!listing.isValid()) {
                this.plugin.getData().deleteAuctionListing(listing, true);
                return true;
            }
            return false;
        });

        return list;
    }

    @NotNull
    private List<AuctionListing> getExpiredData() {
        return new ArrayList<>(Lists.reverse(plugin.getData().getAuctionListing().stream().filter(AuctionListing::isExpired).toList()));
    }

    @NotNull
    public List<AuctionListing> getExpired(@NotNull Player player) {
        return this.getExpired(player.getUniqueId());
    }

    @NotNull
    public List<AuctionListing> getExpired(@NotNull UUID id) {
        return this.getItems(id, this.getExpired());
    }

    @NotNull
    public List<AuctionHistoryItem> getHistory() {
        List<AuctionHistoryItem> list = this.isSynced() ? this.getHistoryData() : this.history;

        list.removeIf(listing -> {
            if (!listing.isValid()) {
                this.plugin.getData().deleteAuctionHistory(listing, true);
                return true;
            }
            return false;
        });

        return list;
    }

    @NotNull
    private List<AuctionHistoryItem> getHistoryData() {
        return new ArrayList<>(Lists.reverse(this.plugin.getData().getAuctionHistory()));
    }

    @NotNull
    public List<AuctionHistoryItem> getHistory(@NotNull Player player) {
        return this.getHistory(player.getUniqueId());
    }

    @NotNull
    public List<AuctionHistoryItem> getHistory(@NotNull UUID id) {
        return this.getItems(id, this.getHistory());
    }

    @NotNull
    private <T extends AbstractAuctionItem> List<T> getItems(@NotNull UUID id, @NotNull List<T> from) {
        return from.stream().filter(listing -> listing != null && listing.getOwner().equals(id)).toList();
    }

    class Listener extends AbstractListener<ExcellentShop> {

        public Listener(@NotNull ExcellentShop plugin) {
            super(plugin);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onSellerJoin(PlayerJoinEvent e) {
            Player player = e.getPlayer();
            getHistory(player).stream().filter(Predicate.not(AuctionHistoryItem::isNotified)).forEach(listing -> {
                notifyHistory(player, listing);
                plugin.getData().saveAuctionHistory(listing, true);
            });

            int expired = getExpired(player).size();
            if (expired > 0) {
                plugin.lang().Auction_Listing_Expired_Notify.replace("%amount%", expired).send(player);
            }
        }
    }
}
