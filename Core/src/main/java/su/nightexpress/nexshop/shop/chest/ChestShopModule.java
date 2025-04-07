package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.ShopModule;
import su.nightexpress.nexshop.api.shop.TransactionLogger;
import su.nightexpress.nexshop.api.shop.event.ChestShopCreateEvent;
import su.nightexpress.nexshop.api.shop.event.ChestShopRemoveEvent;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.PluginTyping;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.shop.chest.command.*;
import su.nightexpress.nexshop.shop.chest.compatibility.*;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestKeys;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.display.DisplayHandler;
import su.nightexpress.nexshop.shop.chest.display.PacketEventsHandler;
import su.nightexpress.nexshop.shop.chest.display.ProtocolLibHandler;
import su.nightexpress.nexshop.shop.chest.impl.ChestBank;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.listener.RegionMarketListener;
import su.nightexpress.nexshop.shop.chest.listener.ShopListener;
import su.nightexpress.nexshop.shop.chest.listener.UpgradeHopperListener;
import su.nightexpress.nexshop.shop.chest.menu.*;
import su.nightexpress.nexshop.shop.chest.rent.RentSettings;
import su.nightexpress.nexshop.shop.chest.util.BlockPos;
import su.nightexpress.nexshop.shop.chest.util.ShopMap;
import su.nightexpress.nexshop.shop.impl.AbstractModule;
import su.nightexpress.nexshop.shop.menu.Confirmation;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.text.NightMessage;
import su.nightexpress.nightcore.util.text.tag.TagPool;
import su.nightexpress.nightcore.util.time.TimeFormats;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ChestShopModule extends AbstractModule implements ShopModule {

    public static final String ID = "chest_shop";
    public static final String DIR_SHOPS = "/shops/";

    private final Map<UUID, ChestBank> bankMap;
    private final ShopMap              shopMap;
    private final Set<Currency>        allowedCurrencies;

    private ShopSettingsMenu settingsMenu;
    private ShopProductsMenu productsMenu;
    private ShopDisplayMenu  displayMenu;
    private ShopShowcaseMenu showcaseMenu;
    private PriceMenu productPriceMenu;

    private RentMenu       rentMenu;
    private BankMenu       bankMenu;
    private StorageMenu    storageMenu;
    private ShopBrowseMenu browseMenu;
    private ShopListMenu   listMenu;
    private ShopSearchMenu searchMenu;
    private ShopView       shopView;

    private Set<ClaimHook> claimHooks;
    private DisplayHandler<?> displayHandler;

    private TransactionLogger logger;

    public ChestShopModule(@NotNull ShopPlugin plugin) {
        super(plugin, ID, Config.getChestShopAliases());
        this.shopMap = new ShopMap();
        this.bankMap = new ConcurrentHashMap<>();
        this.allowedCurrencies = new HashSet<>();
    }

    @NotNull
    public String getShopsPath() {
        return this.getLocalPath() + DIR_SHOPS;
    }

    @Override
    protected void loadModule(@NotNull FileConfig config) {
        ChestKeys.load(this.plugin);
        config.initializeOptions(ChestConfig.class);
        this.plugin.getLangManager().loadEntries(ChestLang.class);
        this.plugin.registerPermissions(ChestPerms.class);
        this.logger = new TransactionLogger(this, config);

        this.loadCurrencies();
        this.loadDisplayHandler();
        this.loadHooks();

        this.addListener(new ShopListener(this.plugin, this));

        this.settingsMenu = new ShopSettingsMenu(this.plugin, this);
        this.displayMenu = new ShopDisplayMenu(this.plugin, this);
        this.showcaseMenu = new ShopShowcaseMenu(this.plugin, this);
        this.productsMenu = new ShopProductsMenu(this.plugin, this);
        this.productPriceMenu = new PriceMenu(this.plugin, this);
        if (ChestUtils.isInfiniteStorage()) {
            this.storageMenu = new StorageMenu(this.plugin, this);
        }
        if (ChestConfig.isRentEnabled()) {
            this.rentMenu = new RentMenu(this.plugin, this);
        }

        this.bankMenu = new BankMenu(this.plugin, this);
        this.browseMenu = new ShopBrowseMenu(this.plugin, this);
        this.listMenu = new ShopListMenu(this.plugin, this);
        this.searchMenu = new ShopSearchMenu(this.plugin, this);
        this.shopView = new ShopView(this.plugin, this);

        this.plugin.runTaskAsync(task -> {
            this.loadBanks();
            this.plugin.runTask(task2 -> this.loadShops());
        });
    }

    @Override
    protected void disableModule() {
        if (this.displayHandler != null) {
            this.displayHandler.shutdown();
            this.displayHandler = null;
        }

        if (this.shopView != null) this.shopView.clear();
        if (this.listMenu != null) this.listMenu.clear();
        if (this.browseMenu != null) this.browseMenu.clear();
        if (this.searchMenu != null) this.searchMenu.clear();
        if (this.bankMenu != null) this.bankMenu.clear();
        if (this.storageMenu != null) {
            this.storageMenu.clear();
            this.storageMenu = null;
        }
        if (this.rentMenu != null) {
            this.rentMenu.clear();
            this.rentMenu = null;
        }

        if (this.productsMenu != null) this.productsMenu.clear();
        if (this.productPriceMenu != null) this.productPriceMenu.clear();
        if (this.displayMenu != null) this.displayMenu.clear();
        if (this.showcaseMenu != null) this.showcaseMenu.clear();
        if (this.settingsMenu != null) this.settingsMenu.clear();

        this.getShops().forEach(this::unloadShop);

        if (this.claimHooks != null) {
            this.claimHooks.clear();
            this.claimHooks = null;
        }

        this.shopMap.clear();
        this.bankMap.clear();
        this.allowedCurrencies.clear();
    }

    @Override
    protected void loadCommands(@NotNull ChainedNodeBuilder builder) {
        if (!ChestConfig.isAutoBankEnabled()) {
            BankCommand.build(this.plugin, this, builder);
        }
        if (ChestConfig.SHOP_ITEM_CREATION_ENABLED.get()) {
            GiveItemCommand.build(this, builder);
        }
        BrowseCommand.build(this, builder);
        CreateCommand.build(this, builder);
        ListCommand.build(this.plugin, this, builder);
        OpenCommand.build(this, builder);
        RemoveCommand.build(this, builder);
    }

    public void loadCurrencies() {
        for (String curId : ChestConfig.ALLOWED_CURRENCIES.get()) {
            Currency currency = EconomyBridge.getCurrency(curId);
            if (currency == null) {
                this.error("Unknown currency '" + curId + "'. Skipping.");
                continue;
            }
            this.allowedCurrencies.add(currency);
        }

        Currency def = this.getDefaultCurrency();
        if (def.isDummy()) {
            this.error("You have invalid currency set in the 'Default_Currency' setting.");
            this.error("You must fix this issue to make your shops working properly.");
        }
        else {
            this.allowedCurrencies.add(def);
        }
    }

    private void loadHooks() {
        if (ChestConfig.SHOP_CREATION_CLAIM_ONLY.get()) {
            this.claimHooks = new HashSet<>();
            this.loadClaimHook(HookId.LANDS, () -> new LandsHook(this.plugin));
            this.loadClaimHook(HookId.GRIEF_PREVENTION, GriefPreventionHook::new);
            this.loadClaimHook(HookId.GRIEF_DEFENDER, GriefDefenderHook::new);
            this.loadClaimHook(HookId.WORLD_GUARD, WorldGuardHook::new);
            this.loadClaimHook(HookId.KINGDOMS, KingdomsHook::new);
            this.loadClaimHook(HookId.HUSK_CLAIMS, HuskClaimsHook::new);
        }

        if (Plugins.isInstalled(HookId.ADVANCED_REGION_MARKET)) {
            this.addListener(new RegionMarketListener(this.plugin, this));
        }

        if (ChestUtils.isInfiniteStorage()) {
            if (Plugins.isInstalled(HookId.UPGRADEABLE_HOPPERS)) {
                this.addListener(new UpgradeHopperListener(this.plugin, this));
            }
        }
    }

    private boolean loadClaimHook(@NotNull String plugin, @NotNull Supplier<ClaimHook> supplier) {
        if (!Plugins.isInstalled(plugin)) return false;

        this.claimHooks.add(supplier.get());
        this.info("Hooked into claim plugin: " + plugin);
        return true;
    }

    private void loadDisplayHandler() {
        if (Plugins.isInstalled(HookId.PACKET_EVENTS)) {
            this.displayHandler = new PacketEventsHandler(this.plugin, this);
        }
        else if (Plugins.isLoaded(HookId.PROTOCOL_LIB)) {
            this.displayHandler = new ProtocolLibHandler(this.plugin, this);
        }

        if (this.displayHandler != null) {
            this.displayHandler.setup();

            this.addAsyncTask(() -> this.displayHandler.update(), ChestConfig.DISPLAY_UPDATE_INTERVAL.get());
        }
    }

    public void loadBanks() {
        this.plugin.getDataHandler().loadChestBanks().forEach(bank -> {
            // Add missing currencies to display them as 0 in balance placeholder, so they are visible.
            this.getAllowedCurrencies().forEach(currency -> {
                bank.getBalanceMap().computeIfAbsent(currency.getInternalId(), k -> 0D);
            });

            this.getBankMap().put(bank.getHolder(), bank);
        });
    }

    public void loadShops() {
        for (FileConfig config : FileConfig.loadAll(this.getAbsolutePath() + DIR_SHOPS, false)) {
            this.loadShop(config);
        }
        this.info("Shops Loaded: " + this.getShops().size());
    }

    public boolean loadShop(@NotNull FileConfig config) {
        File file = config.getFile();
        String id = FileConfig.getName(file);

        ChestShop shop = new ChestShop(this.plugin, this, file, id);
        if (!shop.load()) {
            this.error("Shop not loaded '" + id + "'");
            if (ChestConfig.DELETE_INVALID_SHOP_CONFIGS.get() && file.delete()) {
                this.info("Deleted invalid shop config.");
            }
            return false;
        }

        // ----- OLD BANK UPDATE - START -----
        if (config.contains("Bank")) {
            ChestBank bank = this.getPlayerBank(shop.getOwnerId());
            for (Currency currency : this.getAllowedCurrencies()) {
                bank.deposit(currency, config.getDouble("Bank." + currency.getInternalId()));
            }
            this.plugin.getDataHandler().saveChestBank(bank);
            config.remove("Bank");
            config.saveChanges();
        }
        // ----- OLD BANK UPDATE - END -----

        shop.updatePosition();

        this.shopMap.put(shop);
        return true;
    }

    public void unloadShop(@NotNull ChestShop shop) {
        if (ChestUtils.isInfiniteStorage()) {
            shop.saveProductQuantity();
        }
        if (this.displayHandler != null) {
            this.displayHandler.remove(shop);
        }
        this.shopMap.remove(shop);
        shop.deactivate();
    }

    public void removeShop(@NotNull ChestShop shop) {
        this.unloadShop(shop);

        shop.getFile().delete();
    }

    @NotNull
    public ChestBank getPlayerBank(@NotNull ChestShop shop) {
        return this.getPlayerBank(shop.getOwnerId());
    }

    @NotNull
    public ChestBank getPlayerBank(@NotNull Player player) {
        return this.getPlayerBank(player.getUniqueId());
    }

    @NotNull
    public ChestBank getPlayerBank(@NotNull UUID uuid) {
        ChestBank bank = this.getBankMap().get(uuid);
        if (bank == null) {
            ChestBank bank2 = new ChestBank(uuid, new HashMap<>());
            this.plugin.runTaskAsync(task -> this.plugin.getDataHandler().createChestBank(bank2));
            this.getBankMap().put(uuid, bank2);
            return bank2;
        }
        return bank;
    }

    public void savePlayerBank(@NotNull UUID uuid) {
        ChestBank bank = this.getBankMap().get(uuid);
        if (bank == null) return;

        this.savePlayerBank(bank);
    }

    public void savePlayerBank(@NotNull ChestBank bank) {
        this.plugin.runTaskAsync(task -> this.plugin.getDataHandler().saveChestBank(bank));
    }

    @NotNull
    public Map<UUID, ChestBank> getBankMap() {
        return this.bankMap;
    }

    public boolean isAllowedCurrency(@NotNull Currency currency) {
        return this.allowedCurrencies.contains(currency);
    }

    public boolean isAllowedCurrency(@NotNull Currency currency, @NotNull Player player) {
        return this.isAllowedCurrency(currency) && (!ChestConfig.CHECK_CURRENCY_PERMISSIONS.get() || ChestUtils.hasCurrencyPermission(player, currency));
    }

    @NotNull
    public Set<Currency> getAllowedCurrencies() {
        return new HashSet<>(this.allowedCurrencies);
    }

    @NotNull
    public Set<Currency> getAllowedCurrencies(@NotNull Player player) {
        Set<Currency> currencies = this.getAllowedCurrencies();

        // Remove currency for which player dont have permissions.
        if (ChestConfig.CHECK_CURRENCY_PERMISSIONS.get()) {
            currencies.removeIf(currency -> !ChestUtils.hasCurrencyPermission(player, currency));
        }

        return currencies;
    }

    @NotNull
    public Currency getDefaultCurrency() {
        return EconomyBridge.getCurrencyOrDummy(ChestConfig.DEFAULT_CURRENCY.get());
    }

    @Override
    @NotNull
    public String getDefaultCartUI() {
        return ChestConfig.DEFAULT_CART_UI.get();
    }

    @Override
    @NotNull
    public TransactionLogger getLogger() {
        return logger;
    }

    @NotNull
    public ShopView getShopView() {
        return shopView;
    }

    public void openShopSettings(@NotNull Player player, @NotNull ChestShop shop) {
        this.settingsMenu.open(player, shop);
    }

    public void openDisplayMenu(@NotNull Player player, @NotNull ChestShop shop) {
        this.displayMenu.open(player, shop);
    }

    public void openShowcaseMenu(@NotNull Player player, @NotNull ChestShop shop) {
        this.showcaseMenu.open(player, shop);
    }

    public void openProductsMenu(@NotNull Player player, @NotNull ChestShop shop) {
        this.productsMenu.open(player, shop);
    }

    public void openPriceMenu(@NotNull Player player, @NotNull ChestProduct product) {
        this.productPriceMenu.open(player, product);
    }

    public void openBank(@NotNull Player player, @NotNull ChestShop shop) {
        this.bankMenu.open(player, shop);
    }

    public void openBank(@NotNull Player player, @NotNull UUID target) {
        this.bankMenu.open(player, target);
    }

    public void openStorage(@NotNull Player player, @NotNull ChestShop shop) {
        if (this.storageMenu == null) return;

        this.storageMenu.open(player, shop);
    }

    public void openRentSettings(@NotNull Player player, @NotNull ChestShop shop) {
        if (this.rentMenu == null) return;

        this.rentMenu.open(player, shop);
    }

    public boolean openShop(@NotNull Player player, @NotNull ChestShop shop) {
        return this.openShop(player, shop, false);
    }

    public boolean openShop(@NotNull Player player, @NotNull ChestShop shop, int page) {
        return this.openShop(player, shop, page, false);
    }

    public boolean openShop(@NotNull Player player, @NotNull ChestShop shop, boolean force) {
        return this.openShop(player, shop, 1, force);
    }

    public boolean openShop(@NotNull Player player, @NotNull ChestShop shop, int page, boolean force) {
        if (!this.plugin.getDataManager().isLoaded()) return false;

        if (!force) {
            if (!shop.canAccess(player, true)) return false;
        }

        return this.shopView.open(player, shop, viewer -> viewer.setPage(Math.abs(page)));
    }

    public void remakeDisplay(@NotNull ChestShop shop) {
        if (this.displayHandler == null) return;

        this.displayHandler.remove(shop);
        this.displayHandler.refresh(shop);
    }

    public void browseShops(@NotNull Player player) {
        this.browseMenu.open(player);
    }

    public void listShops(@NotNull Player player) {
        this.listShops(player, player.getName());
    }

    public void listShops(@NotNull Player player, @NotNull String playerName) {
        this.listMenu.open(player, playerName);
    }

    public void searchShops(@NotNull Player player, @NotNull String input) {
        String searchFor = input.toLowerCase();

        List<ChestProduct> products = new ArrayList<>();
        this.getActiveShops().forEach(shop -> {
            shop.getValidProducts().forEach(product -> {
                if (!(product.getType() instanceof PhysicalTyping typing)) return;

                ItemStack item = typing.getItem();
                String material = BukkitThing.toString(item.getType()).toLowerCase();
                String localized = LangAssets.get(item.getType()).toLowerCase();
                String displayName = NightMessage.stripTags(ItemUtil.getSerializedName(item)).toLowerCase();
                if (material.contains(searchFor) || localized.contains(searchFor) || displayName.contains(searchFor)) {
                    products.add(product);
                    return;
                }

                if (typing instanceof PluginTyping pluginPacker && pluginPacker.isValid()) {
                    String itemId = pluginPacker.getItemId();
                    if (itemId.contains(searchFor)) {
                        products.add(product);
                    }
                }
            });
        });
        products.sort(Comparator.comparing(product -> product.getPricer().getBuyPrice()));

        this.searchMenu.open(player, products);
    }

    public boolean renameShop(@NotNull Player player, @NotNull ChestShop shop, @NotNull String name) {
        String rawName = NightMessage.stripTags(name, TagPool.ALL_COLORS_AND_STYLES);
        int maxLength = ChestConfig.SHOP_MAX_NAME_LENGTH.get();

        if (rawName.length() > maxLength) {
            ChestLang.SHOP_RENAME_ERROR_LONG_NAME.getMessage().send(player, replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, maxLength));
            return false;
        }

        shop.setName(name);
        return true;
    }

    public void interactShop(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull Block block) {
        ChestShop shop = this.getShop(block);
        if (shop != null) {
            this.interactShop(event, player, shop);
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (block.getBlockData() instanceof WallSign directional) {
                Block backend = block.getRelative(directional.getFacing().getOppositeFace());
                shop = this.getShop(backend);

                if (shop != null) {
                    ItemStack item = event.getItem();
                    if (item != null && (item.getType() == Material.GLOW_INK_SAC || ChestUtils.isDye(item.getType()))) {
                        if (shop.isOwner(player) || player.hasPermission(ChestPerms.EDIT_OTHERS)) return;
                    }

                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                    this.interactShop(event, player, shop);
                }
            }
        }
    }

    public void interactShop(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ChestShop shop) {
        boolean originalDeny = event.useInteractedBlock() == Event.Result.DENY;
        event.setUseInteractedBlock(Event.Result.DENY);

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            if (!originalDeny) {
                event.setUseInteractedBlock(Event.Result.ALLOW);
            }
            return;
        }

        if (player.isSneaking()) {
            ItemStack item = event.getItem();
            if (item != null && !originalDeny) {
                if (Tag.SIGNS.isTagged(item.getType()) || item.getType() == Material.ITEM_FRAME || item.getType() == Material.GLOW_ITEM_FRAME || item.getType() == Material.HOPPER) {
                    if (!shop.isOwnerOrRenter(player) && !player.hasPermission(ChestPerms.EDIT_OTHERS)) {
                        ChestLang.SHOP_ERROR_NOT_OWNER.getMessage().send(player);
                    }
                    else event.setUseInteractedBlock(Event.Result.ALLOW);
                    return;
                }
            }

            if (shop.isOwnerOrRenter(player) || player.hasPermission(ChestPerms.EDIT_OTHERS)) {
                this.openShopSettings(player, shop);
            }
            else {
                ChestLang.SHOP_ERROR_NOT_OWNER.getMessage().send(player);
            }
            return;
        }

        if (shop.isOwnerOrRenter(player) && ChestUtils.isInfiniteStorage()) {
            this.openShopSettings(player, shop);
        }
        else if (shop.isAdminShop() || !shop.isOwnerOrRenter(player)) {
            if (shop.isRentable() && !shop.isRented()) {
                this.plugin.getShopManager().openConfirmation(player, Confirmation.create(
                    (viewer, event1) -> {
                        this.rentShop(player, shop);
                        player.closeInventory();
                    },
                    (viewer, event1) -> player.closeInventory()
                ));
                return;
            }

            if (shop.canAccess(player, true)) {
                shop.open(player);
            }
        }
        else if (!originalDeny) {
            event.setUseInteractedBlock(Event.Result.ALLOW);
        }
    }

    public boolean checkShopCreation(@NotNull Player player, @NotNull Block block) {
        if (this.isShop(block)) {
            ChestLang.SHOP_CREATION_ERROR_ALREADY_SHOP.getMessage().send(player);
            return false;
        }

        if (!this.checkCreationLocation(player, block)) {
            ChestLang.SHOP_CREATION_ERROR_BAD_LOCATION.getMessage().send(player);
            return false;
        }

        if (!this.checkCreationClaim(player, block)) {
            ChestLang.SHOP_CREATION_ERROR_BAD_AREA.getMessage().send(player);
            return false;
        }

        int shopLimit = ChestUtils.getShopLimit(player);
        int shopAmount = this.getShopsAmount(player);
        if (shopLimit > 0 && shopAmount >= shopLimit) {
            ChestLang.SHOP_CREATION_ERROR_LIMIT_REACHED.getMessage().send(player);
            return false;
        }

        if (!this.canPayCreation(player)) {
            ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS.getMessage().send(player);
            return false;
        }

        return true;
    }

    public boolean createShopFromItem(@NotNull Player player, @NotNull Block block, @NotNull ItemStack itemStack) {
        Material material = ChestUtils.getShopItemType(itemStack);
        if (material == null) return false;

        if (!ChestUtils.isValidContainer(material)) {
            ChestLang.SHOP_CREATION_ERROR_NOT_A_CHEST.getMessage().send(player);
            return false;
        }

        if (!this.checkShopCreation(player, block)) return false;

        ChestShopCreateEvent event = new ChestShopCreateEvent(player, block, itemStack);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        BlockData blockData = material.createBlockData();
        if (blockData instanceof Directional directional) {
            BlockFace face = EntityUtil.getDirection(player);
            if (face != null) {
                directional.setFacing(face.getOppositeFace());
            }
        }

        block.getWorld().setBlockData(block.getLocation(), blockData);

        this.createShop(player, block, shop -> {
            shop.setItemCreated(true);
        });

        itemStack.setAmount(itemStack.getAmount() - 1);

        return true;
    }

    public boolean createShopNaturally(@NotNull Player player, @NotNull Block block) {
        return this.createShopNaturally(player, block, -1, -1);
    }

    public boolean createShopNaturally(@NotNull Player player, @NotNull Block block, double buyPrice, double sellPrice) {
        if (!ChestUtils.isValidContainer(block)) {
            ChestLang.SHOP_CREATION_ERROR_NOT_A_CHEST.getMessage().send(player);
            return false;
        }

        if (!this.checkShopCreation(player, block)) return false;

        Container container = (Container) block.getState();
        Inventory inventory = container.getInventory();
        if (Stream.of(inventory.getContents()).anyMatch(inside -> inside != null && !inside.getType().isAir())) {
            ChestLang.SHOP_CREATION_ERROR_NOT_EMPTY.getMessage().send(player);
            return false;
        }

        ChestShopCreateEvent event = new ChestShopCreateEvent(player, block);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        this.payForCreate(player);

        this.createShop(player, block, shop -> {
            ItemStack hand = new ItemStack(player.getInventory().getItemInMainHand());
            if (!ChestUtils.isShopItem(hand)) {
                ChestProduct product = shop.createProduct(player, hand, false);
                if (product != null) {
                    if (buyPrice > 0) product.setPrice(TradeType.BUY, buyPrice);
                    if (sellPrice > 0) product.setPrice(TradeType.SELL, sellPrice);
                }
            }
        });

        return true;
    }

    @NotNull
    public ChestShop createShop(@NotNull Player player, @NotNull Block block, @NotNull Consumer<ChestShop> consumer) {
        String id = ChestUtils.generateShopId(player, block.getLocation());
        File file = new File(this.getAbsolutePath() + DIR_SHOPS, id + ".yml");
        ChestShop shop = new ChestShop(this.plugin, this, file, id);

        shop.assignLocation(block.getWorld(), block.getLocation());
        shop.setAdminShop(player.hasPermission(ChestPerms.ADMIN_SHOP));
        shop.setOwner(player);
        shop.setName(Placeholders.forPlayer(player).apply(ChestConfig.DEFAULT_NAME.get()));
        shop.setHologramEnabled(true);
        shop.setShowcaseEnabled(true);
        shop.setShowcaseType(null);
        shop.setBuyingAllowed(true);
        shop.setSellingAllowed(true);
        if (ChestConfig.isRentEnabled()) {
            shop.setRentSettings(new RentSettings(false, 7, CurrencyId.VAULT, 1000));
        }

        consumer.accept(shop);
        shop.save();

        this.shopMap.put(shop);
        if (this.displayHandler != null) {
            this.displayHandler.refresh(shop);
        }

        ChestLang.SHOP_CREATION_INFO_DONE.getMessage().send(player);
        return shop;
    }

    public boolean rentShop(@NotNull Player player, @NotNull ChestShop shop) {
        if (!ChestConfig.isRentEnabled()) return false;

        if (!shop.isActive()) {
            ChestLang.ERROR_SHOP_INACTIVE.getMessage().send(player, replacer -> replacer.replace(shop.replacePlaceholders()));
            return false;
        }

        if (shop.isRented()) {
            ChestLang.RENT_ERROR_ALREADY_RENTED.getMessage().send(player, replacer -> replacer.replace(shop.replacePlaceholders()));
            return false;
        }

        RentSettings settings = shop.getRentSettings();

        if (!shop.isRentable() || !settings.isValid()) {
            ChestLang.RENT_ERROR_NOT_RENTABLE.getMessage().send(player, replacer -> replacer.replace(shop.replacePlaceholders()));
            return false;
        }

        String currencyId = settings.getCurrencyId();
        double price = settings.getPrice();
        if (!EconomyBridge.hasEnough(player, currencyId, price)) {
            ChestLang.RENT_ERROR_INSUFFICIENT_FUNDS.getMessage().send(player, replacer -> replacer
                .replace(Placeholders.GENERIC_PRICE, settings.getPriceFormatted())
                .replace(shop.replacePlaceholders())
            );
            return false;
        }

        EconomyBridge.withdraw(player, currencyId, price);
        EconomyBridge.deposit(shop.getOwnerId(), currencyId, price); // TODO Notify?
        shop.setRentedBy(player);
        shop.setRentedUntil(TimeUtil.createFutureTimestamp(TimeUnit.DAYS.toSeconds(shop.getRentSettings().getDuration())));
        shop.saveSettings();

        ChestLang.RENT_RENT_SUCCESS.getMessage().send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_TIME, TimeFormats.toLiteral(settings.getDurationMillis()))
            .replace(Placeholders.GENERIC_PRICE, settings.getPriceFormatted())
            .replace(shop.replacePlaceholders())
        );
        return true;
    }

    public boolean extendRentShop(@NotNull Player player, @NotNull ChestShop shop) {
        if (!ChestConfig.isRentEnabled()) return false;

        if (!shop.isActive()) {
            ChestLang.ERROR_SHOP_INACTIVE.getMessage().send(player, replacer -> replacer.replace(shop.replacePlaceholders()));
            return false;
        }

        if (!shop.isRenter(player)) {
            ChestLang.RENT_ERROR_NOT_RENTED.getMessage().send(player, replacer -> replacer.replace(shop.replacePlaceholders()));
            return false;
        }

        RentSettings settings = shop.getRentSettings();
        if (!settings.isValid()) {
            ChestLang.RENT_ERROR_NOT_RENTABLE.getMessage().send(player, replacer -> replacer.replace(shop.replacePlaceholders()));
            return false;
        }

        String currencyId = settings.getCurrencyId();
        double price = settings.getPrice();
        if (!EconomyBridge.hasEnough(player, currencyId, price)) {
            ChestLang.RENT_ERROR_INSUFFICIENT_FUNDS.getMessage().send(player, replacer -> replacer
                .replace(Placeholders.GENERIC_PRICE, settings.getPriceFormatted())
                .replace(shop.replacePlaceholders())
            );
            return false;
        }

        EconomyBridge.withdraw(player, currencyId, price);
        EconomyBridge.deposit(shop.getOwnerId(), currencyId, price); // TODO Notify?
        shop.extendRent();
        shop.saveSettings();

        ChestLang.RENT_EXTEND_SUCCESS.getMessage().send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_TIME, TimeFormats.toLiteral(settings.getDurationMillis()))
            .replace(Placeholders.GENERIC_PRICE, settings.getPriceFormatted())
            .replace(shop.replacePlaceholders())
        );
        return true;
    }

    public boolean canBreak(@NotNull Player player, @NotNull ChestShop shop) {
        if (!player.hasPermission(ChestPerms.REMOVE)) {
            ChestLang.ERROR_NO_PERMISSION.getMessage(this.plugin).send(player);
            return false;
        }

        if (!shop.isOwner(player) && !player.hasPermission(ChestPerms.REMOVE_OTHERS)) {
            ChestLang.SHOP_ERROR_NOT_OWNER.getMessage().send(player);
            return false;
        }

        return true;
    }

    public boolean deleteShop(@NotNull Player player, @NotNull Block block) {
        ChestShop shop = this.getShop(block);
        if (shop == null) {
            ChestLang.ERROR_BLOCK_IS_NOT_SHOP.getMessage().send(player);
            return false;
        }

        return this.deleteShop(player, shop);
    }

    public boolean deleteShop(@NotNull Player player, @NotNull ChestShop shop) {
        if (!this.canBreak(player, shop)) return false;
//        if (!player.hasPermission(ChestPerms.REMOVE)) {
//            ChestLang.ERROR_NO_PERMISSION.getMessage(this.plugin).send(player);
//            return false;
//        }
//
//        if (!shop.isOwner(player) && !player.hasPermission(ChestPerms.REMOVE_OTHERS)) {
//            ChestLang.SHOP_ERROR_NOT_OWNER.getMessage().send(player);
//            return false;
//        }
//
        if (!this.payForRemoval(player)) {
            ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS.getMessage().send(player);
            return false;
        }

        if (ChestUtils.isInfiniteStorage()) {
            if (shop.getValidProducts().stream().anyMatch(product -> product.countStock(TradeType.BUY, null) > 0)) {
                ChestLang.SHOP_REMOVAL_ERROR_NOT_EMPTY.getMessage().send(player);
                return false;
            }
        }

        ChestShopRemoveEvent event = new ChestShopRemoveEvent(player, shop);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        if (shop.isItemCreated() && shop.isActive()) {
            Block block = shop.getBlock();
            ItemStack itemStack = ChestUtils.createShopItem(block.getType());

            if (itemStack != null) {
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), itemStack);
            }
        }

        this.removeShop(shop);

        if (this.getShopsAmount(player) <= 0 && !ChestConfig.isAutoBankEnabled()) {
            for (Currency currency : this.getAllowedCurrencies(player)) {
                this.withdrawFromBank(player, currency, -1);
            }
        }

        ChestLang.SHOP_REMOVAL_INFO_DONE.getMessage().send(player);
        return true;
    }

    public boolean depositToBank(@NotNull Player player, @NotNull Currency currency, double amount) {
        return this.depositToBank(player, player.getUniqueId(), currency, amount);
    }

    public boolean depositToBank(@NotNull Player player, @NotNull UUID target, @NotNull Currency currency, double value) {
        if (!this.isAllowedCurrency(currency, player)) {
            ChestLang.BANK_ERROR_INVALID_CURRENCY.getMessage().send(player);
            return false;
        }

        double balance = currency.getBalance(player);
        double amount = value < 0 ? balance : value;

        if (balance < amount) {
            ChestLang.BANK_DEPOSIT_ERROR_NOT_ENOUGH.getMessage().send(player);
            return false;
        }

        currency.take(player, amount);

        // If funds not transfered
        if (currency.getBalance(player) == balance) {
            return false;
        }

        ChestBank bank = this.getPlayerBank(target);
        bank.deposit(currency, amount);
        this.savePlayerBank(bank);

        ChestLang.BANK_DEPOSIT_SUCCESS.getMessage().send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
        );
        return true;
    }

    public boolean withdrawFromBank(@NotNull Player player, @NotNull Currency currency, double amount) {
        return this.withdrawFromBank(player, player.getUniqueId(), currency, amount);
    }

    public boolean withdrawFromBank(@NotNull Player player, @NotNull UUID target, @NotNull Currency currency, double value) {
        if (!this.isAllowedCurrency(currency, player)) {
            ChestLang.BANK_ERROR_INVALID_CURRENCY.getMessage().send(player);
            return false;
        }

        ChestBank bank = this.getPlayerBank(target);
        double amount = value < 0D ? bank.getBalance(currency) : value;

        if (!bank.hasEnough(currency, amount)) {
            ChestLang.BANK_WITHDRAW_ERROR_NOT_ENOUGH.getMessage().send(player);
            return false;
        }

        currency.give(player, amount);
        bank.withdraw(currency, amount);
        this.savePlayerBank(bank);

        ChestLang.BANK_WITHDRAW_SUCCESS.getMessage().send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
        );
        return true;
    }

    public boolean depositToStorage(@NotNull Player player, @NotNull ChestProduct product, int units) {
        ChestShop shop = product.getShop();

        int playerUnits = product.countUnits(player);
        if (playerUnits < units) {
            ChestLang.STORAGE_DEPOSIT_ERROR_NOT_ENOUGH.getMessage().send(player);
            return false;
        }

        product.storeStock(TradeType.BUY, units, null);
        product.take(player, units);
        shop.save();

        ChestLang.STORAGE_DEPOSIT_SUCCESS.getMessage().send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(units))
            .replace(Placeholders.GENERIC_ITEM, ItemUtil.getSerializedName(product.getPreview()))
        );
        return true;
    }

    public boolean withdrawFromStorage(@NotNull Player player, @NotNull ChestProduct product, int units) {
        ChestShop shop = product.getShop();

        int shopUnits = product.countStock(TradeType.BUY, null);
        if (shopUnits < units) {
            ChestLang.STORAGE_WITHDRAW_ERROR_NOT_ENOUGH.getMessage().send(player);
            return false;
        }

        int spaceUnits = product.countSpace(player) / product.getUnitAmount();
        int maxUnits = Math.min(spaceUnits, units);

        product.delivery(player, maxUnits);
        product.consumeStock(TradeType.BUY, maxUnits, null);
        shop.save();

        ChestLang.STORAGE_WITHDRAW_SUCCESS.getMessage().send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(maxUnits))
            .replace(Placeholders.GENERIC_ITEM, ItemUtil.getSerializedName(product.getPreview()))
        );
        return true;
    }

    @Nullable
    public DisplayHandler<?> getDisplayHandler() {
        return this.displayHandler;
    }

    public void manageDisplay(@NotNull Consumer<DisplayHandler<?>> consumer) {
        if (this.displayHandler != null) {
            consumer.accept(this.displayHandler);
        }
    }

    @NotNull
    public ShopMap getShopMap() {
        return this.shopMap;
    }

    @NotNull
    public Collection<ChestShop> getShops() {
        return new HashSet<>(this.shopMap.getAll());
    }

    @NotNull
    public Collection<ChestShop> getActiveShops() {
        return this.shopMap.getActive();
    }

    @NotNull
    public Set<ChestShop> getShops(@NotNull World world) {
        return new HashSet<>(this.shopMap.ofWorld(world).values());
    }

    @NotNull
    public Set<ChestShop> getShops(@NotNull Player player) {
        return this.getShops(player.getUniqueId());
    }

    @NotNull
    public Set<ChestShop> getShops(@NotNull UUID ownerId) {
        return new HashSet<>(this.shopMap.getByOwner(ownerId));
    }

    @NotNull
    public Set<ChestShop> getShops(@NotNull String playerName) {
        return new HashSet<>(this.shopMap.getByOwner(playerName));
    }

    public int getShopsAmount(@NotNull Player player) {
        return this.getShops(player).size();
    }

    @Nullable
    public ChestShop getShop(@NotNull Inventory inventory) {
        Location location = inventory.getLocation();
        if (location == null) return null;

        return this.getShop(location.getBlock());
    }

    @Nullable
    public ChestShop getShop(@NotNull Block block) {
        return this.shopMap.getByWorldPos(block.getWorld(), BlockPos.from(block));
    }

    @Nullable
    public ChestShop getShop(@NotNull Location location) {
        return this.shopMap.getByLocation(location);
    }

    public boolean isShop(@NotNull Block block) {
        return this.isShop(block.getLocation());
    }

    public boolean isShop(@NotNull Location location) {
        return this.getShop(location) != null;
    }

    public boolean checkCreationLocation(@NotNull Player player, @NotNull Block block) {
        if (ChestConfig.SHOP_CREATION_WORLD_BLACKLIST.get().contains(block.getWorld().getName())) {
            return false;
        }

        if (ChestConfig.SHOP_CREATION_CHECK_BUILD.get()) {
            Block placed = block.getRelative(BlockFace.UP);
            ItemStack item = new ItemStack(Material.CHEST);
            BlockPlaceEvent event = new BlockPlaceEvent(placed, placed.getState(), block, item, player, true, EquipmentSlot.HAND);
            plugin.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
        }

        if (Plugins.isInstalled(HookId.WORLD_GUARD)) {
            return WorldGuardFlags.checkFlag(player, block.getLocation());
        }
        return true;
    }

    public boolean checkCreationClaim(@NotNull Player player, @NotNull Block block) {
        if (!ChestConfig.SHOP_CREATION_CLAIM_ONLY.get()) return true;
        if (player.hasPermission(ChestPerms.BYPASS_CREATION_CLAIMS)) return true;

        return this.claimHooks.isEmpty() || this.claimHooks.stream().anyMatch(claim -> claim.isInOwnClaim(player, block));
    }

    public boolean canPayCreation(@NotNull Player player) {
        return this.canPayForShop(player, ChestConfig.SHOP_CREATION_COST_CREATE.get());
    }

    private boolean payForCreate(@NotNull Player player) {
        return this.payForShop(player, ChestConfig.SHOP_CREATION_COST_CREATE.get());
    }

    private boolean payForRemoval(@NotNull Player player) {
        return this.payForShop(player, ChestConfig.SHOP_CREATION_COST_REMOVE.get());
    }

    private boolean canPayForShop(@NotNull Player player, double price) {
        if (price <= 0) return true;

        return this.getDefaultCurrency().getBalance(player) >= price;
    }

    private boolean payForShop(@NotNull Player player, double price) {
        if (price <= 0) return true;

        Currency currency = this.getDefaultCurrency();

        double balance = currency.getBalance(player);
        if (balance < price) return false;

        currency.take(player, price);
        return true;
    }
}
