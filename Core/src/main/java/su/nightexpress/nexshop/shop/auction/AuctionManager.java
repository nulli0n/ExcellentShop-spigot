package su.nightexpress.nexshop.shop.auction;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.data.StorageType;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.shop.auction.command.*;
import su.nightexpress.nexshop.shop.auction.config.AuctionConfig;
import su.nightexpress.nexshop.shop.auction.config.AuctionCurrencySetting;
import su.nightexpress.nexshop.shop.auction.config.AuctionLang;
import su.nightexpress.nexshop.shop.auction.data.AuctionDataHandler;
import su.nightexpress.nexshop.shop.auction.listener.AuctionListener;
import su.nightexpress.nexshop.shop.auction.listing.AbstractAuctionItem;
import su.nightexpress.nexshop.shop.auction.listing.AuctionCompletedListing;
import su.nightexpress.nexshop.shop.auction.listing.AuctionListing;
import su.nightexpress.nexshop.shop.auction.menu.*;
import su.nightexpress.nexshop.shop.auction.task.AuctionMenuUpdateTask;
import su.nightexpress.nexshop.shop.module.ShopModule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AuctionManager extends ShopModule {

    public static final String ID = "auction";

    private static final Map<UUID, AuctionListing>               LISTINGS_MAP                  = new ConcurrentHashMap<>();
    private static final Map<UUID, AuctionCompletedListing>      COMPLETED_LISTINGS_MAP        = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<AuctionListing>>          PLAYER_LISTINGS_MAP           = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<AuctionCompletedListing>> PLAYER_COMPLETED_LISTINGS_MAP = new ConcurrentHashMap<>();

    public static final Comparator<AbstractAuctionItem> SORT_BY_CREATION = (l1, l2) -> {
        return Long.compare(l2.getDateCreation(), l1.getDateCreation());
    };

    private AuctionDataHandler dataHandler;

    private AuctionMainMenu                 mainMenu;
    private AuctionPurchaseConfirmationMenu purchaseConfirmationMenu;
    private AuctionExpiredMenu              expiredMenu;
    private AuctionHistoryMenu              historyMenu;
    private AuctionUnclaimedMenu            unclaimedMenu;
    private AuctionSellingMenu              sellingMenu;
    private AuctionCategoryFilterMenu       categoryFilterMenu;
    private AuctionCurrencyFilterMenu       currencyFilterMenu;
    private AuctionCurrencySelectorMenu     currencySelectorMenu;

    private AuctionMenuUpdateTask menuUpdateTask;

    public AuctionManager(@NotNull ExcellentShop plugin) {
        super(plugin, ID);
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        AuctionConfig.load(this);
        if (!this.checkCurrency()) {
            this.error("No Default currency found! Auction will be disabled.");
            return;
        }

        this.plugin.getLangManager().loadMissing(AuctionLang.class);
        this.plugin.getLangManager().loadEnum(AuctionMainMenu.AuctionSortType.class);
        this.plugin.getLang().saveChanges();
        //this.plugin.getConfigManager().extractResources("/" + this.getLocalPath() + "/menu/");

        this.dataHandler = AuctionDataHandler.getInstance(this);
        this.dataHandler.setup();
        this.getDataHandler().onSynchronize();

        this.command.addDefaultCommand(new AuctionOpenCommand(this));
        this.command.addChildren(new AuctionSellCommand(this));
        this.command.addChildren(new AuctionHistoryCommand(this));
        this.command.addChildren(new AuctionExpiredCommand(this));
        this.command.addChildren(new AuctionSellingCommand(this));
        this.command.addChildren(new AuctionUnclaimedCommand(this));

        this.addListener(new AuctionListener(this));

        //AuctionUtils.fillDummy(this);
        this.menuUpdateTask = new AuctionMenuUpdateTask(this);
        this.menuUpdateTask.start();
    }

    @Override
    protected void onShutdown() {
        if (this.menuUpdateTask != null) {
            this.menuUpdateTask.stop();
            this.menuUpdateTask = null;
        }

        super.onShutdown();

        if (this.categoryFilterMenu != null) {
            this.categoryFilterMenu.clear();
            this.categoryFilterMenu = null;
        }
        if (this.currencyFilterMenu != null) {
            this.currencyFilterMenu.clear();
            this.currencyFilterMenu = null;
        }
        if (this.currencySelectorMenu != null) {
            this.currencySelectorMenu.clear();
            this.currencySelectorMenu = null;
        }
        if (this.purchaseConfirmationMenu != null) {
            this.purchaseConfirmationMenu.clear();
            this.purchaseConfirmationMenu = null;
        }
        if (this.mainMenu != null) {
            this.mainMenu.clear();
            this.mainMenu = null;
        }
        if (this.expiredMenu != null) {
            this.expiredMenu.clear();
            this.expiredMenu = null;
        }
        if (this.historyMenu != null) {
            this.historyMenu.clear();
            this.historyMenu = null;
        }
        if (this.sellingMenu != null) {
            this.sellingMenu.clear();
            this.sellingMenu = null;
        }
        if (this.unclaimedMenu != null) {
            this.unclaimedMenu.clear();
            this.unclaimedMenu = null;
        }
        if (this.dataHandler != null) {
            this.dataHandler.shutdown();
            this.dataHandler = null;
        }
        this.clearListings();
    }

    @NotNull
    public AuctionDataHandler getDataHandler() {
        return dataHandler;
    }

    private boolean checkCurrency() {
        if (this.getCurrencies().isEmpty()) return false;
        try {
            this.getCurrencyDefault();
            return true;
        }
        catch (NoSuchElementException e) {
            return false;
        }
    }

    @NotNull
    public Currency getCurrencyDefault() {
        return AuctionConfig.CURRENCIES.values().stream().filter(AuctionCurrencySetting::isDefault)
            .map(AuctionCurrencySetting::getCurrency).findFirst().orElseThrow();
    }

    @NotNull
    public Set<Currency> getCurrencies() {
        return AuctionConfig.CURRENCIES.values().stream()
            .map(AuctionCurrencySetting::getCurrency).collect(Collectors.toSet());
    }

    @NotNull
    public Set<Currency> getCurrencies(@NotNull Player player) {
        return AuctionConfig.CURRENCIES.values().stream()
            .filter(setting -> setting.hasPermission(player) || setting.isDefault())
            .map(AuctionCurrencySetting::getCurrency).collect(Collectors.toSet());
    }

    private boolean needEnsureListingExists() {
        return this.getDataHandler().getDataType() != StorageType.SQLITE;
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
        if (metaLore.stream().anyMatch(line -> AuctionConfig.LISTINGS_DISABLED_LORES.stream().anyMatch(line::contains))) {
            return false;
        }

        return true;
    }

    public boolean canAdd(@NotNull Player player, @NotNull ItemStack item, double price) {
        Set<Currency> currencies = this.getCurrencies(player);
        if (currencies.isEmpty()) {
            plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
            return false;
        }

        if (!player.hasPermission(Perms.AUCTION_BYPASS_DISABLED_GAMEMODES)) {
            GameMode gameMode = player.getGameMode();
            if (AuctionConfig.DISABLED_GAMEMODES.contains(gameMode.name())) {
                plugin.getMessage(AuctionLang.LISTING_ADD_ERROR_DISABLED_GAMEMODE).send(player);
                return false;
            }
        }

        if (!this.isAllowedItem(item)) {
            plugin.getMessage(AuctionLang.LISTING_ADD_ERROR_BAD_ITEM)
                .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item))
                .send(player);
            return false;
        }

        price = NumberUtil.round(price);
        if (price <= 0D) {
            plugin.getMessage(AuctionLang.LISTING_ADD_ERROR_PRICE_NEGATIVE).send(player);
            return false;
        }

        if (!player.hasPermission(Perms.AUCTION_BYPASS_LISTING_PRICE)) {
            Material material = item.getType();
            double matPriceUnit = price / (double) item.getAmount();
            double matPriceMin = AuctionConfig.getMaterialPriceMin(material);
            double matPriceMax = AuctionConfig.getMaterialPriceMax(material);

            if (matPriceMin >= 0D && matPriceUnit < matPriceMin) {
                plugin.getMessage(AuctionLang.LISTING_ADD_ERROR_PRICE_MATERIAL_MIN)
                    .replace(Placeholders.GENERIC_ITEM, plugin.getLangManager().getEnum(material))
                    .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(matPriceMin))
                    .send(player);
                return false;
            }
            if (matPriceMax >= 0D && matPriceUnit > matPriceMax) {
                plugin.getMessage(AuctionLang.LISTING_ADD_ERROR_PRICE_MATERIAL_MAX)
                    .replace(Placeholders.GENERIC_ITEM, plugin.getLangManager().getEnum(material))
                    .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(matPriceMax))
                    .send(player);
                return false;
            }
        }

        int listingsHas = this.getListings(player).size();
        int listingsMax = this.getListingsMaximum(player);
        if (listingsMax >= 0 && listingsHas >= listingsMax) {
            plugin.getMessage(AuctionLang.LISTING_ADD_ERROR_LIMIT).replace(Placeholders.GENERIC_AMOUNT, listingsMax).send(player);
            return false;
        }

        return true;
    }

    @Nullable
    public AuctionListing add(@NotNull Player player, @NotNull ItemStack item, @NotNull Currency currency, double price, boolean takeItem) {
        if (AuctionConfig.LISTINGS_PRICE_ROUND_TO_INT.get()) {
            price = NumberUtil.round(price);
        }
        if (takeItem) {
            player.getInventory().setItemInMainHand(null);
        }
        if (!player.isOnline()) return null;

        if (!player.hasPermission(Perms.AUCTION_BYPASS_LISTING_PRICE)) {
            double curPriceMin = AuctionConfig.getCurrencyPriceMin(currency);
            double curPriceMax = AuctionConfig.getCurrencyPriceMax(currency);

            if (curPriceMax > 0 && price > curPriceMax) {
                plugin.getMessage(AuctionLang.LISTING_ADD_ERROR_PRICE_CURRENCY_MAX)
                    .replace(Placeholders.GENERIC_AMOUNT, currency.format(curPriceMax))
                    .replace(currency.replacePlaceholders())
                    .send(player);
                if (takeItem) PlayerUtil.addItem(player, item);
                return null;
            }
            if (curPriceMin > 0 && price < curPriceMin) {
                plugin.getMessage(AuctionLang.LISTING_ADD_ERROR_PRICE_CURRENCY_MIN)
                    .replace(Placeholders.GENERIC_AMOUNT, currency.format(curPriceMin))
                    .replace(currency.replacePlaceholders())
                    .send(player);
                if (takeItem) PlayerUtil.addItem(player, item);
                return null;
            }
        }

        double tax = player.hasPermission(Perms.AUCTION_BYPASS_LISTING_TAX) ? 0D : AuctionConfig.LISTINGS_TAX_ON_LISTING_ADD;
        double taxPay = AuctionUtils.calculateTax(price, tax);
        if (taxPay > 0) {
            double balance = currency.getHandler().getBalance(player);
            if (balance < taxPay) {
                plugin.getMessage(AuctionLang.LISTING_ADD_ERROR_PRICE_TAX)
                    .replace(Placeholders.GENERIC_TAX, tax)
                    .replace(Placeholders.GENERIC_AMOUNT, currency.format(taxPay))
                    .send(player);
                if (takeItem) PlayerUtil.addItem(player, item);
                return null;
            }
            currency.getHandler().take(player, taxPay);
        }

        AuctionListing listing = new AuctionListing(player, item, currency, price);
        this.addListing(listing);
        this.plugin.runTaskAsync(task -> this.getDataHandler().addListing(listing));
        this.plugin.getMessage(AuctionLang.LISTING_ADD_SUCCESS_INFO)
            .replace(Placeholders.GENERIC_TAX, currency.format(taxPay))
            .replace(listing.replacePlaceholders())
            .send(player);

        if (AuctionConfig.LISTINGS_ANNOUNCE) {
            plugin.getMessage(AuctionLang.LISTING_ADD_SUCCESS_ANNOUNCE)
                .replace(Placeholders.forPlayer(player))
                .replace(listing.replacePlaceholders())
                .broadcast();
        }

        this.getMainMenu().update();
        this.getSellingMenu().update();
        return listing;
    }

    public boolean buy(@NotNull Player buyer, @NotNull AuctionListing listing) {
        if (this.needEnsureListingExists() && !this.getDataHandler().isListingExist(listing.getId())) return false;
        if (!this.hasListing(listing.getId())) return false;

        double balance = listing.getCurrency().getHandler().getBalance(buyer);
        double price = listing.getPrice();
        if (balance < price) {
            plugin.getMessage(AuctionLang.LISTING_BUY_ERROR_NOT_ENOUGH_FUNDS)
                .replace(Placeholders.GENERIC_BALANCE, listing.getCurrency().format(balance))
                .replace(listing.replacePlaceholders())
                .send(buyer);
            return false;
        }

        listing.getCurrency().getHandler().take(buyer, price);
        PlayerUtil.addItem(buyer, listing.getItemStack());

        AuctionCompletedListing completedListing = new AuctionCompletedListing(listing, buyer);

        this.removeListing(listing);
        this.addCompletedListing(completedListing);
        this.plugin.runTaskAsync(task -> {
            this.getDataHandler().addCompletedListing(completedListing);
            this.getDataHandler().deleteListing(listing);
        });
        this.plugin.getMessage(AuctionLang.LISTING_BUY_SUCCESS_INFO).replace(listing.replacePlaceholders()).send(buyer);

        // Notify the seller about the purchase.
        Player seller = plugin.getServer().getOfflinePlayer(listing.getOwner()).getPlayer();
        if (seller != null) {
            int unclaimed = this.getUnclaimedListings(seller).size();
            this.plugin.getMessage(AuctionLang.NOTIFY_LISTING_UNCLAIMED)
                .replace(Placeholders.GENERIC_AMOUNT, unclaimed)
                .send(seller);
        }

        this.getMainMenu().update();
        this.getSellingMenu().update();
        this.getUnclaimedMenu().update();
        return true;
    }

    public void takeListing(@NotNull Player player, @NotNull AuctionListing listing) {
        if (this.needEnsureListingExists() && !this.getDataHandler().isListingExist(listing.getId())) return;
        if (!this.hasListing(listing.getId())) return;

        PlayerUtil.addItem(player, listing.getItemStack());
        this.removeListing(listing);
        this.plugin.runTaskAsync(task -> this.getDataHandler().deleteListing(listing));

        this.getMainMenu().update();
    }

    public boolean canBeUsedHere(@NotNull Player player) {
        if (!player.hasPermission(Perms.AUCTION_BYPASS_DISABLED_WORLDS)) {
            if (AuctionConfig.DISABLED_WORLDS.contains(player.getWorld().getName().toLowerCase())) {
                plugin.getMessage(AuctionLang.ERROR_DISABLED_WORLD).send(player);
                return false;
            }
        }
        return true;
    }

    @NotNull
    public AuctionMainMenu getMainMenu() {
        if (this.mainMenu == null) {
            JYML cfg = JYML.loadOrExtract(plugin, this.getLocalPath() + "/menu/", "main.yml");
            this.mainMenu = new AuctionMainMenu(this, cfg);
        }
        return this.mainMenu;
    }

    @NotNull
    public AuctionPurchaseConfirmationMenu getPurchaseConfirmationMenu() {
        if (this.purchaseConfirmationMenu == null) {
            JYML cfg = JYML.loadOrExtract(plugin, this.getLocalPath() + "/menu/", "purchase_confirm.yml");
            this.purchaseConfirmationMenu = new AuctionPurchaseConfirmationMenu(this, cfg);
        }
        return purchaseConfirmationMenu;
    }

    @NotNull
    public AuctionExpiredMenu getExpiredMenu() {
        if (this.expiredMenu == null) {
            JYML cfg = JYML.loadOrExtract(plugin, this.getLocalPath() + "/menu/", "expired.yml");
            this.expiredMenu = new AuctionExpiredMenu(this, cfg);
        }
        return this.expiredMenu;
    }

    @NotNull
    public AuctionHistoryMenu getHistoryMenu() {
        if (this.historyMenu == null) {
            JYML cfg = JYML.loadOrExtract(plugin, this.getLocalPath() + "/menu/", "history.yml");
            this.historyMenu = new AuctionHistoryMenu(this, cfg);
        }
        return this.historyMenu;
    }

    @NotNull
    public AuctionSellingMenu getSellingMenu() {
        if (this.sellingMenu == null) {
            JYML cfg = JYML.loadOrExtract(plugin, this.getLocalPath() + "/menu/", "selling.yml");
            this.sellingMenu = new AuctionSellingMenu(this, cfg);
        }
        return this.sellingMenu;
    }

    @NotNull
    public AuctionUnclaimedMenu getUnclaimedMenu() {
        if (this.unclaimedMenu == null) {
            JYML cfg = JYML.loadOrExtract(plugin, this.getLocalPath() + "/menu/", "unclaimed.yml");
            this.unclaimedMenu = new AuctionUnclaimedMenu(this, cfg);
        }
        return this.unclaimedMenu;
    }

    @NotNull
    public AuctionCategoryFilterMenu getCategoryFilterMenu() {
        if (this.categoryFilterMenu == null) {
            JYML cfg = JYML.loadOrExtract(plugin, this.getLocalPath() + "/menu/", "category_filter.yml");
            this.categoryFilterMenu = new AuctionCategoryFilterMenu(this, cfg);
        }
        return categoryFilterMenu;
    }

    @NotNull
    public AuctionCurrencyFilterMenu getCurrencyFilterMenu() {
        if (this.currencyFilterMenu == null) {
            JYML cfg = JYML.loadOrExtract(plugin, this.getLocalPath() + "/menu/", "currency_filter.yml");
            this.currencyFilterMenu = new AuctionCurrencyFilterMenu(this, cfg);
        }
        return currencyFilterMenu;
    }

    @NotNull
    public AuctionCurrencySelectorMenu getCurrencySelectorMenu() {
        if (this.currencySelectorMenu == null) {
            JYML cfg = JYML.loadOrExtract(plugin, this.getLocalPath() + "/menu/", "currency_selector.yml");
            this.currencySelectorMenu = new AuctionCurrencySelectorMenu(this, cfg);
        }
        return currencySelectorMenu;
    }

    public int getListingsMaximum(@NotNull Player player) {
        return AuctionConfig.getPossibleListings(player);
    }

    public void clearListings() {
        LISTINGS_MAP.clear();
        COMPLETED_LISTINGS_MAP.clear();
        PLAYER_LISTINGS_MAP.clear();
        PLAYER_COMPLETED_LISTINGS_MAP.clear();
    }

    public void addListing(@NotNull AuctionListing listing) {
        LISTINGS_MAP.put(listing.getId(), listing);
        PLAYER_LISTINGS_MAP.computeIfAbsent(listing.getOwner(), k -> new HashSet<>()).add(listing);
    }

    public void removeListing(@NotNull AuctionListing listing) {
        LISTINGS_MAP.remove(listing.getId());
        PLAYER_LISTINGS_MAP.getOrDefault(listing.getOwner(), Collections.emptySet()).remove(listing);
    }

    public void addCompletedListing(@NotNull AuctionCompletedListing listing) {
        COMPLETED_LISTINGS_MAP.put(listing.getId(), listing);
        PLAYER_COMPLETED_LISTINGS_MAP.computeIfAbsent(listing.getOwner(), k -> new HashSet<>()).add(listing);
    }

    public void removeCompletedListing(@NotNull AuctionCompletedListing listing) {
        COMPLETED_LISTINGS_MAP.remove(listing.getId());
        PLAYER_COMPLETED_LISTINGS_MAP.getOrDefault(listing.getOwner(), Collections.emptySet()).remove(listing);
    }

    public boolean hasListing(@NotNull UUID uuid) {
        return this.getListing(uuid) != null;
    }

    @Nullable
    public AuctionListing getListing(@NotNull UUID uuid) {
        return LISTINGS_MAP.getOrDefault(uuid, null);
    }

    @NotNull
    public List<AuctionListing> getListings() {
        return LISTINGS_MAP.values().stream().sorted(SORT_BY_CREATION).toList();
    }

    @NotNull
    public List<AuctionListing> getListings(@NotNull Player player) {
        return this.getListings(player.getUniqueId());
    }

    @NotNull
    public List<AuctionListing> getListings(@NotNull UUID id) {
        return new ArrayList<>(PLAYER_LISTINGS_MAP.getOrDefault(id, Collections.emptySet())).stream().sorted(SORT_BY_CREATION).toList();
    }

    @NotNull
    public List<AuctionCompletedListing> getCompletedListings() {
        return COMPLETED_LISTINGS_MAP.values().stream().sorted(SORT_BY_CREATION).toList();
    }

    @NotNull
    public List<AuctionCompletedListing> getCompletedListings(@NotNull Player player) {
        return this.getCompletedListings(player.getUniqueId());
    }

    @NotNull
    public List<AuctionCompletedListing> getCompletedListings(@NotNull UUID id) {
        return new ArrayList<>(PLAYER_COMPLETED_LISTINGS_MAP.getOrDefault(id, Collections.emptySet())).stream().sorted(SORT_BY_CREATION).toList();
    }

    @NotNull
    public List<AuctionListing> getActiveListings() {
        return LISTINGS_MAP.values().stream().filter(Predicate.not(AuctionListing::isExpired))/*.sorted(SORT_BY_CREATION)*/.toList();
    }

    @NotNull
    public List<AuctionListing> getActiveListings(@NotNull Player owner) {
        return this.getActiveListings(owner.getUniqueId());
    }

    @NotNull
    public List<AuctionListing> getActiveListings(@NotNull UUID owner) {
        return this.getListings(owner).stream().filter(Predicate.not(AuctionListing::isExpired)).toList();
    }

    @NotNull
    public List<AuctionListing> getExpiredListings() {
        return LISTINGS_MAP.values().stream().filter(AuctionListing::isExpired).sorted(SORT_BY_CREATION).toList();
    }

    @NotNull
    public List<AuctionListing> getExpiredListings(@NotNull Player player) {
        return this.getExpiredListings(player.getUniqueId());
    }

    @NotNull
    public List<AuctionListing> getExpiredListings(@NotNull UUID id) {
        return this.getListings(id).stream().filter(AuctionListing::isExpired).toList();
    }

    @NotNull
    public List<AuctionCompletedListing> getHistoryListings() {
        return COMPLETED_LISTINGS_MAP.values().stream().filter(AuctionCompletedListing::isRewarded).sorted(SORT_BY_CREATION).toList();
    }

    @NotNull
    public List<AuctionCompletedListing> getHistoryListings(@NotNull Player player) {
        return this.getHistoryListings(player.getUniqueId());
    }

    @NotNull
    public List<AuctionCompletedListing> getHistoryListings(@NotNull UUID id) {
        return this.getCompletedListings(id).stream().filter(AuctionCompletedListing::isRewarded).toList();
    }

    @NotNull
    public List<AuctionCompletedListing> getUnclaimedListings() {
        return COMPLETED_LISTINGS_MAP.values().stream().filter(Predicate.not(AuctionCompletedListing::isRewarded)).sorted(SORT_BY_CREATION).toList();
    }

    @NotNull
    public List<AuctionCompletedListing> getUnclaimedListings(@NotNull Player player) {
        return this.getUnclaimedListings(player.getUniqueId());
    }

    @NotNull
    public List<AuctionCompletedListing> getUnclaimedListings(@NotNull UUID id) {
        return this.getCompletedListings(id).stream().filter(Predicate.not(AuctionCompletedListing::isRewarded)).toList();
    }
}
