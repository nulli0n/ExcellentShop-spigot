package su.nightexpress.nexshop.auction;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.event.AuctionListingCreateEvent;
import su.nightexpress.nexshop.auction.command.AuctionCommands;
import su.nightexpress.nexshop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.auction.config.AuctionLang;
import su.nightexpress.nexshop.auction.config.AuctionPerms;
import su.nightexpress.nexshop.auction.data.AuctionDatabase;
import su.nightexpress.nexshop.auction.listener.AuctionListener;
import su.nightexpress.nexshop.auction.listing.ActiveListing;
import su.nightexpress.nexshop.auction.listing.CompletedListing;
import su.nightexpress.nexshop.auction.menu.*;
import su.nightexpress.nexshop.module.AbstractModule;
import su.nightexpress.nexshop.module.ModuleSettings;
import su.nightexpress.nexshop.product.content.ContentTypes;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.db.config.DatabaseType;
import su.nightexpress.nightcore.integration.item.ItemBridge;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.ui.UIUtils;
import su.nightexpress.nightcore.ui.menu.confirmation.Confirmation;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.*;
import java.util.stream.Collectors;

public class AuctionManager extends AbstractModule {

    private final Listings                     listings;
    private final Map<String, ListingCategory> categoryMap;

    private AuctionDatabase database;

    private AuctionMenu           mainMenu;
    private ExpiredListingsMenu   expiredMenu;
    private SalesHistoryMenu      historyMenu;
    private UnclaimedListingsMenu unclaimedMenu;
    private PlayerListingsMenu    sellingMenu;
    private CurrencySelectMenu    currencySelectMenu;

    public AuctionManager(@NotNull ShopPlugin plugin, @NotNull String id, @NotNull ModuleSettings config) {
        super(plugin, id, config);
        this.listings = new Listings();
        this.categoryMap = new LinkedHashMap<>();
    }

    @Override
    protected void loadModule(@NotNull FileConfig config) {
        config.initializeOptions(AuctionConfig.class);

        this.loadCategories();

        this.plugin.registerPermissions(AuctionPerms.class);

        this.database = new AuctionDatabase(this.plugin, this, config);
        this.database.setup();
        this.database.onSynchronize();

        this.mainMenu = new AuctionMenu(this.plugin, this);
        this.expiredMenu = new ExpiredListingsMenu(this.plugin, this);
        this.historyMenu = new SalesHistoryMenu(this.plugin, this);
        this.unclaimedMenu = new UnclaimedListingsMenu(this.plugin, this);
        this.sellingMenu = new PlayerListingsMenu(this.plugin, this);
        this.currencySelectMenu = new CurrencySelectMenu(this.plugin, this);

        this.addListener(new AuctionListener(this.plugin, this));
    }

    @Override
    protected void disableModule() {
        if (this.currencySelectMenu != null) this.currencySelectMenu.clear();
        if (this.mainMenu != null) this.mainMenu.clear();
        if (this.expiredMenu != null) this.expiredMenu.clear();
        if (this.historyMenu != null) this.historyMenu.clear();
        if (this.sellingMenu != null) this.sellingMenu.clear();
        if (this.unclaimedMenu != null) this.unclaimedMenu.clear();

        if (this.database != null) {
            this.database.shutdown();
            this.database = null;
        }

        this.listings.clear();
        this.categoryMap.clear();
    }

    @Override
    protected void loadCommands(@NotNull HubNodeBuilder builder) {
        AuctionCommands.build(this.plugin, this, builder);
    }

    private void loadCategories() {
        FileConfig cfg = FileConfig.loadOrExtract(this.plugin, this.getLocalPath(), "categories.yml");
        for (String sId : cfg.getSection("")) {
            ListingCategory category = ListingCategory.read(cfg, sId, sId);
            this.categoryMap.put(category.getId(), category);
        }
    }

    @NotNull
    public AuctionDatabase getDatabase() {
        return this.database;
    }

    @NotNull
    public Listings getListings() {
        return listings;
    }

    @NotNull
    public Collection<ListingCategory> getCategories() {
        return this.categoryMap.values();
    }

    @NotNull
    public ListingCategory getDefaultCategory() {
        ListingCategory category = this.getCategories().stream().filter(ListingCategory::isDefault).findFirst().orElse(null);
        if (category == null) category = this.getCategories().stream().findFirst().orElseThrow();

        return category;
    }

    private boolean needEnsureListingExists() {
        return this.database.getStorageType() != DatabaseType.SQLITE;
    }

    public boolean openAuction(@NotNull Player player, int page) {
        MenuViewer viewer = this.mainMenu.getViewerOrCreate(player);
        viewer.setPage(page);

        return this.openAuction(player);
    }

    public boolean openAuction(@NotNull Player player) {
        return this.openAuction(player, false);
    }

    public boolean openAuction(@NotNull Player player, boolean force) {
        return this.openAuctionMenu(player, player.getUniqueId(), this.mainMenu, force);
    }

    public boolean openExpiedListings(@NotNull Player player) {
        return this.openExpiedListings(player, player.getUniqueId(), false);
    }

    public boolean openExpiedListings(@NotNull Player player, @NotNull UUID target, boolean force) {
        return this.openAuctionMenu(player, target, this.expiredMenu, force);
    }

    public boolean openPlayerListings(@NotNull Player player) {
        return this.openPlayerListings(player, player.getUniqueId(), false);
    }

    public boolean openPlayerListings(@NotNull Player player, @NotNull UUID target, boolean force) {
        return this.openAuctionMenu(player, target, this.sellingMenu, force);
    }

    public boolean openSalesHistory(@NotNull Player player) {
        return this.openSalesHistory(player, player.getUniqueId(), false);
    }

    public boolean openSalesHistory(@NotNull Player player, @NotNull UUID target, boolean force) {
        return this.openAuctionMenu(player, target, this.historyMenu, force);
    }

    public boolean openUnclaimedListings(@NotNull Player player) {
        return this.openUnclaimedListings(player, player.getUniqueId(), false);
    }

    public boolean openUnclaimedListings(@NotNull Player player, @NotNull UUID target, boolean force) {
        return this.openAuctionMenu(player, target, this.unclaimedMenu, force);
    }

    public void openCurrencySelection(@NotNull Player player, @NotNull ItemStack itemStack, double price) {
        this.currencySelectMenu.open(player, Pair.of(itemStack, price));
    }

    public void openPurchaseConfirmation(@NotNull Player player, @NotNull ActiveListing listing) {
        UIUtils.openConfirmation(player, Confirmation.builder()
            .onAccept((viewer, event) -> {
                this.buy(player, listing);
                if (!AuctionConfig.MENU_REOPEN_ON_PURCHASE.get()) {
                    this.plugin.runTask(player, player::closeInventory);
                }
            })
            .onReturn((viewer, event) -> this.plugin.runTask(player, () -> this.openAuction(viewer.getPlayer())))
            .setIcon(NightItem.fromItemStack(listing.getItemStack()).localized(AuctionLang.UI_BUY_CONFIRM).replacement(replacer -> replacer.replace(listing.replacePlaceholders())))
            .returnOnAccept(AuctionConfig.MENU_REOPEN_ON_PURCHASE.get())
            .build());
    }

    private boolean openAuctionMenu(@NotNull Player player, @NotNull UUID target, @NotNull AbstractAuctionMenu<?> menu, boolean force) {
        if (!force) {
            if (!this.canBeUsedHere(player)) return false;
        }

        return menu.open(player, target);
    }

    public boolean isAllowedItem(@NotNull ItemStack item) {
        Set<String> bannedItems = AuctionConfig.LISTINGS_DISABLED_ITEMS.get().stream().map(String::toLowerCase).collect(Collectors.toSet());
        if (bannedItems.contains(BukkitThing.toString(item.getType()))) {
            return false;
        }

        for (ItemAdapter<?> handler : ItemBridge.getAdapters()) {
            if (!(handler instanceof IdentifiableItemAdapter identifiableItemAdapter)) continue;

            String id = identifiableItemAdapter.getItemId(item);
            if (id != null && bannedItems.contains(id.toLowerCase())) {
                return false;
            }
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return true;

        if (meta.hasDisplayName()) {
            String itemName = Colorizer.restrip(meta.getDisplayName().toLowerCase());
            Set<String> badNames = AuctionConfig.LISTINGS_DISABLED_NAMES.get();
            if (badNames.stream().map(String::toLowerCase).anyMatch(itemName::contains)) {
                return false;
            }
        }

        List<String> itemLore = meta.getLore();
        if (itemLore != null) {
            Set<String> badLores = AuctionConfig.LISTINGS_DISABLED_LORES.get().stream().map(String::toLowerCase).collect(Collectors.toSet());
            if (itemLore.stream().anyMatch(loreLine -> badLores.stream().anyMatch(loreLine::contains))) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkItemModel(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) return true;

        int model = meta.getCustomModelData();

        Set<Integer> banned = AuctionConfig.LISTINGS_DISABLED_MODELS.get().getOrDefault(item.getType(), Collections.emptySet());
        return !banned.contains(model);
    }

    public boolean sell(@NotNull Player player, @NotNull ItemStack item, double price) {
        Set<Currency> currencies = this.getAvailableCurrencies(player);
        if (currencies.isEmpty()) {
            CoreLang.ERROR_NO_PERMISSION.withPrefix(this.plugin).send(player);
            return false;
        }

        if (!player.hasPermission(AuctionPerms.BYPASS_DISABLED_GAMEMODES)) {
            if (AuctionUtils.isBadGamemode(player.getGameMode())) {
                AuctionLang.LISTING_ADD_ERROR_DISABLED_GAMEMODE.message().send(player);
                return false;
            }
        }

        if (!this.isAllowedItem(item) || !checkItemModel(item)) {
            AuctionLang.LISTING_ADD_ERROR_BAD_ITEM.message().send(player, replacer -> replacer
                .replace(Placeholders.GENERIC_ITEM, ItemUtil.getSerializedName(item))
            );
            return false;
        }

        price = AuctionUtils.finePrice(price);
        if (price <= 0D) {
            AuctionLang.LISTING_ADD_ERROR_INVALID_PRICE.message().send(player);
            return false;
        }

        if (!player.hasPermission(AuctionPerms.BYPASS_LISTING_PRICE)) {
            Material material = item.getType();
            double matPriceUnit = price / (double) item.getAmount();
            double matPriceMin = AuctionUtils.getMaterialPriceMin(material);
            double matPriceMax = AuctionUtils.getMaterialPriceMax(material);

            if (matPriceMin >= 0D && matPriceUnit < matPriceMin) {
                AuctionLang.LISTING_ADD_ERROR_PRICE_MATERIAL_MIN.message().send(player, replacer -> replacer
                    .replace(Placeholders.GENERIC_ITEM, LangAssets.get(material))
                    .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(matPriceMin))
                );
                return false;
            }
            if (matPriceMax >= 0D && matPriceUnit > matPriceMax) {
                AuctionLang.LISTING_ADD_ERROR_PRICE_MATERIAL_MAX.message().send(player, replacer -> replacer
                    .replace(Placeholders.GENERIC_ITEM, LangAssets.get(material))
                    .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(matPriceMax))
                );
                return false;
            }
        }

        int listingsHas = this.listings.getActive(player).size();
        int listingsMax = this.getListingsMaximum(player);
        if (listingsMax >= 0 && listingsHas >= listingsMax) {
            AuctionLang.LISTING_ADD_ERROR_LIMIT.message().send(player, replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, listingsMax));
            return false;
        }

        ItemStack copyStack = new ItemStack(item);
        player.getInventory().setItemInMainHand(null); // First of all take player's item.

        if (currencies.size() == 1) {
            Currency currency = currencies.stream().findFirst().orElse(null);
            if (this.add(player, copyStack, currency, price) == null) {
                if (player.isOnline()) {
                    Players.addItem(player, copyStack);
                }
                return false;
            }
            return true;
        }

        this.openCurrencySelection(player, item, price);
        return true;
    }

    @Nullable
    public ActiveListing add(@NotNull Player player, @NotNull ItemStack item, @NotNull Currency currency, double price) {
        if (!player.hasPermission(AuctionPerms.BYPASS_LISTING_PRICE)) {
            double curPriceMin = AuctionUtils.getCurrencyPriceMin(currency);
            double curPriceMax = AuctionUtils.getCurrencyPriceMax(currency);

            if (curPriceMax > 0 && price > curPriceMax) {
                AuctionLang.LISTING_ADD_ERROR_PRICE_CURRENCY_MAX.message().send(player, replacer -> replacer
                    .replace(Placeholders.GENERIC_AMOUNT, currency.format(curPriceMax))
                    .replace(currency.replacePlaceholders())
                );
                return null;
            }
            if (curPriceMin > 0 && price < curPriceMin) {
                AuctionLang.LISTING_ADD_ERROR_PRICE_CURRENCY_MIN.message().send(player, replacer -> replacer
                    .replace(Placeholders.GENERIC_AMOUNT, currency.format(curPriceMin))
                    .replace(currency.replacePlaceholders())
                );
                return null;
            }
        }

        double taxAmount = AuctionUtils.getSellTax(player);
        double taxPay = AuctionUtils.getTax(currency, price, taxAmount);
        if (taxPay > 0) {
            double balance = currency.getBalance(player);
            if (balance < taxPay) {
                AuctionLang.LISTING_ADD_ERROR_PRICE_TAX.message().send(player, replacer -> replacer
                    .replace(Placeholders.GENERIC_TAX, taxAmount)
                    .replace(Placeholders.GENERIC_AMOUNT, currency.format(taxPay))
                );
                return null;
            }
            currency.take(player, taxPay);
        }

        ItemContent content = ContentTypes.fromItem(item, this::isItemProviderAllowed);
        ActiveListing listing = ActiveListing.create(player, content, currency, price);

        AuctionListingCreateEvent event = new AuctionListingCreateEvent(player, currency, listing);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return null;

        this.listings.add(listing);
        this.plugin.runTaskAsync(() -> this.database.addListing(listing));

        AuctionLang.LISTING_ADD_SUCCESS_INFO.message().send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_TAX, currency.format(taxPay))
            .replace(listing.replacePlaceholders())
        );

        if (AuctionConfig.LISTINGS_ANNOUNCE.get()) {
            AuctionLang.LISTING_ADD_SUCCESS_ANNOUNCE.message().broadcast(replacer -> replacer
                .replace(Placeholders.forPlayer(player))
                .replace(listing.replacePlaceholders())
            );
        }

        this.mainMenu.flush();
        this.sellingMenu.flush();
        return listing;
    }

    public boolean buy(@NotNull Player buyer, @NotNull ActiveListing listing) {
        if (this.needEnsureListingExists() && !this.database.isListingExist(listing.getId())) return false;
        if (!this.listings.hasListing(listing.getId())) return false;

        double balance = listing.getCurrency().getBalance(buyer);
        double price = listing.getPrice();
        if (balance < price) {
            AuctionLang.LISTING_BUY_ERROR_NOT_ENOUGH_FUNDS.message().send(buyer, replacer -> replacer
                .replace(Placeholders.GENERIC_BALANCE, listing.getCurrency().format(balance))
                .replace(listing.replacePlaceholders())
            );
            return false;
        }

        listing.getCurrency().take(buyer, price);
        Players.addItem(buyer, listing.getItemStack());

        CompletedListing completedListing = CompletedListing.create(listing, buyer);

        this.listings.remove(listing);
        this.listings.addCompleted(completedListing);
        this.plugin.runTaskAsync(() -> {
            this.database.addCompletedListing(completedListing);
            this.database.deleteListing(listing);
        });
        AuctionLang.LISTING_BUY_SUCCESS_INFO.message().send(buyer, replacer -> replacer.replace(listing.replacePlaceholders()));

        // Notify the seller about the purchase.
        Player seller = plugin.getServer().getPlayer(listing.getOwner());
        if (seller != null) {
            if (AuctionConfig.LISINGS_AUTO_CLAIM.get()) {
                // FIXME Poorly works, not saved properly
                this.claimRewards(seller, Lists.newList(completedListing));
            }
            else {
                int unclaimed = this.listings.getUnclaimed(seller).size();
                AuctionLang.NOTIFY_UNCLAIMED_LISTINGS.message().send(seller, replacer -> replacer
                    .replace(Placeholders.GENERIC_AMOUNT, unclaimed)
                );
            }
        }

        this.mainMenu.flush();
        this.sellingMenu.flush();
        this.unclaimedMenu.flush();
        return true;
    }

    public void takeListing(@NotNull Player player, @NotNull ActiveListing listing) {
        if (this.needEnsureListingExists() && !this.database.isListingExist(listing.getId())) return;
        if (!this.listings.hasListing(listing.getId())) return;

        Players.addItem(player, listing.getItemStack());
        this.listings.remove(listing);
        this.plugin.runTaskAsync(() -> this.database.deleteListing(listing));

        this.mainMenu.flush();
    }

    public void claimRewards(@NotNull Player player, @NotNull List<CompletedListing> listings) {
        for (CompletedListing listing : listings) {
            if (this.needEnsureListingExists() && this.database.isCompletedListingClaimed(listing.getId())) {
                listing.setClaimed(true);
            }
            if (listing.isClaimed()) continue;

            listing.getCurrency().give(player, listing.getPrice());
            listing.setClaimed(true);

            AuctionLang.LISTING_CLAIM_SUCCESS.message().send(player, replacer -> replacer.replace(listing.replacePlaceholders()));
        }

        this.plugin.runTaskAsync(() -> this.database.saveCompletedListings(listings));
    }

    public boolean canBeUsedHere(@NotNull Player player) {
        if (!player.hasPermission(AuctionPerms.BYPASS_DISABLED_WORLDS)) {
            if (AuctionUtils.isDisabledWorld(player.getWorld())) {
                AuctionLang.ERROR_DISABLED_WORLD.message().send(player);
                return false;
            }
        }
        return true;
    }

    public int getListingsMaximum(@NotNull Player player) {
        return AuctionUtils.getPossibleListings(player);
    }
}
