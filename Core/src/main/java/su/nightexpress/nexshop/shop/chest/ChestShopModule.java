package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.integration.VaultHook;
import su.nexmedia.engine.utils.EngineUtils;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.ShopModule;
import su.nightexpress.nexshop.api.shop.TransactionLogger;
import su.nightexpress.nexshop.api.shop.event.ChestShopCreateEvent;
import su.nightexpress.nexshop.api.shop.event.ChestShopRemoveEvent;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.module.AbstractShopModule;
import su.nightexpress.nexshop.shop.chest.command.*;
import su.nightexpress.nexshop.shop.chest.compatibility.*;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.display.DisplayHandler;
import su.nightexpress.nexshop.shop.chest.impl.ChestPlayerBank;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.listener.RegionMarketListener;
import su.nightexpress.nexshop.shop.chest.listener.ShopListener;
import su.nightexpress.nexshop.shop.chest.menu.*;
import su.nightexpress.nexshop.shop.chest.util.ShopMap;
import su.nightexpress.nexshop.shop.chest.util.ShopType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ChestShopModule extends AbstractShopModule implements ShopModule {

    public static final String ID = "chest_shop";
    public static final String DIR_SHOPS = "/shops/";
    public static final String DIR_MENUS = "/menu/";

    private final Map<UUID, ChestPlayerBank> bankMap;
    private final ShopMap shopMap;

    private ShopSettingsMenu settingsMenu;
    private ShopProductsMenu productsMenu;
    private ShopBrowseMenu browseMenu;
    private ShopListMenu   listMenu;
    private ShopSearchMenu searchMenu;
    private BankMenu       bankMenu;

    private Set<ClaimHook> claimHooks;
    private DisplayHandler displayHandler;

    private TransactionLogger logger;

    public ChestShopModule(@NotNull ExcellentShop plugin) {
        super(plugin, ID);
        this.shopMap = new ShopMap();
        this.bankMap = new ConcurrentHashMap<>();
    }

    @NotNull
    public String getShopsPath() {
        return this.getLocalPath() + DIR_SHOPS;
    }

    @NotNull
    public String getMenusPath() {
        return this.getLocalPath() + DIR_MENUS;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        this.getConfig().initializeOptions(ChestConfig.class);

        if (this.getDefaultCurrency() == CurrencyManager.DUMMY) {
            this.error("You have invalid currency set in 'Default_Currency' setting AND/OR have no valid currency in 'Allowed_Currencies' setting.");
            this.error("You must fix this issue to make your shops working properly.");
        }

        this.plugin.getLangManager().loadMissing(ChestLang.class);
        this.plugin.getLangManager().loadEnum(ShopType.class);
        this.plugin.getLang().saveChanges();
        this.plugin.registerPermissions(ChestPerms.class);

        if (EngineUtils.hasPlugin(HookId.PROTOCOL_LIB)) {
            this.displayHandler = new DisplayHandler(this.plugin, this);
            this.displayHandler.setup();
        }

        this.logger = new TransactionLogger(this);

        // Setup Claim Hooks
        if (ChestConfig.SHOP_CREATION_CLAIM_ONLY.get()) {
            this.claimHooks = new HashSet<>();
            if (EngineUtils.hasPlugin(HookId.LANDS)) this.claimHooks.add(new LandsHook(this.plugin));
            if (EngineUtils.hasPlugin(HookId.GRIEF_PREVENTION)) this.claimHooks.add(new GriefPreventionHook());
            if (EngineUtils.hasPlugin(HookId.WORLD_GUARD)) this.claimHooks.add(new WorldGuardFlags());
            if (EngineUtils.hasPlugin(HookId.KINGDOMS)) this.claimHooks.add(new KingdomsHook());
        }

        this.addListener(new ShopListener(this));
        if (EngineUtils.hasPlugin(HookId.ADVANCED_REGION_MARKET)) {
            this.addListener(new RegionMarketListener(this.plugin, this));
        }

        this.settingsMenu = new ShopSettingsMenu(this.plugin, this);
        this.productsMenu = new ShopProductsMenu(this.plugin, this);
        this.browseMenu = new ShopBrowseMenu(this.plugin, this);
        this.listMenu = new ShopListMenu(this.plugin, this);
        this.searchMenu = new ShopSearchMenu(this);
        this.bankMenu = new BankMenu(this);

        this.command.addChildren(new CreateCommand(this));
        this.command.addChildren(new RemoveCommand(this));
        this.command.addChildren(new ListCommand(this));
        this.command.addChildren(new BrowseCommand(this));
        this.command.addChildren(new OpenCommand(this));
        if (!ChestConfig.SHOP_AUTO_BANK.get()) {
            this.command.addChildren(new BankCommand(this));
        }

        this.plugin.runTaskAsync(task -> {
            this.loadBanks();
            this.plugin.runTask(task2 -> this.loadShops());
        });
    }

    public void loadBanks() {
        this.plugin.getData().getChestDataHandler().getChestBanks().forEach(bank -> {
            this.getBankMap().put(bank.getHolder(), bank);
        });
    }

    public void loadShops() {
        for (JYML shopConfig : JYML.loadAll(this.getAbsolutePath() + DIR_SHOPS, false)) {
            this.loadShop(shopConfig);
        }
        this.info("Shops Loaded: " + this.getShops().size());

        this.plugin.runTaskAsync(task -> this.loadShopData());
    }

    public void loadShopData() {
        this.getShops().forEach(shop -> {
            shop.getPricer().load();
            shop.getStock().load();
        });
    }

    public boolean loadShop(@NotNull JYML cfg) {
        ChestShop shop = new ChestShop(this, cfg);
        if (!shop.load()) {
            this.error("Shop not loaded '" + cfg.getFile().getName());
            if (ChestConfig.DELETE_INVALID_SHOP_CONFIGS.get() && cfg.getFile().delete()) {
                this.info("Deleted invalid shop config.");
            }
            return false;
        }

        // ----- OLD BANK UPDATE - START -----
        if (cfg.contains("Bank")) {
            ChestPlayerBank bank = this.getPlayerBank(shop.getOwnerId());
            for (Currency currency : ChestUtils.getAllowedCurrencies()) {
                bank.deposit(currency, cfg.getDouble("Bank." + currency.getId()));
            }
            this.plugin.getData().getChestDataHandler().saveChestBank(bank);
            cfg.remove("Bank");
            cfg.saveChanges();
        }
        // ----- OLD BANK UPDATE - END -----

        this.addShop(shop);
        return true;
    }

    public boolean unloadShop(@NotNull ChestShop shop) {
        shop.clear();
        this.getShopMap().remove(shop);
        return true;
    }

    public void addShop(@NotNull ChestShop shop) {
        //shop.updateContainerInfo();
        this.getShopMap().put(shop);
    }

    public void removeShop(@NotNull ChestShop shop) {
        if (!shop.getFile().delete()) return;

        this.unloadShop(shop);
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();

        if (this.displayHandler != null) {
            this.displayHandler.shutdown();
            this.displayHandler = null;
        }

        if (this.listMenu != null) this.listMenu.clear();
        if (this.browseMenu != null) this.browseMenu.clear();
        if (this.searchMenu != null) this.searchMenu.clear();
        if (this.bankMenu != null) this.bankMenu.clear();
        if (this.productsMenu != null) this.productsMenu.clear();
        if (this.settingsMenu != null) this.settingsMenu.clear();

        // Destroy shop editors and displays.
        this.getShops().forEach(ChestShop::clear);

        if (this.claimHooks != null) {
            this.claimHooks.clear();
            this.claimHooks = null;
        }

        this.getShopMap().clear();
        this.getBankMap().clear();
    }

    @NotNull
    public ChestPlayerBank getPlayerBank(@NotNull ChestShop shop) {
        return this.getPlayerBank(shop.getOwnerId());
    }

    @NotNull
    public ChestPlayerBank getPlayerBank(@NotNull Player player) {
        return this.getPlayerBank(player.getUniqueId());
    }

    @NotNull
    public ChestPlayerBank getPlayerBank(@NotNull UUID uuid) {
        ChestPlayerBank bank = this.getBankMap().get(uuid);
        if (bank == null) {
            ChestPlayerBank bank2 = new ChestPlayerBank(uuid, new HashMap<>());
            this.plugin.runTaskAsync(task -> this.plugin.getData().getChestDataHandler().createChestBank(bank2));
            this.getBankMap().put(uuid, bank2);
            return bank2;
        }
        return bank;
    }

    public void savePlayerBank(@NotNull UUID uuid) {
        ChestPlayerBank bank = this.getBankMap().get(uuid);
        if (bank == null) return;

        this.savePlayerBank(bank);
    }

    public void savePlayerBank(@NotNull ChestPlayerBank bank) {
        this.plugin.runTaskAsync(task -> this.plugin.getData().getChestDataHandler().saveChestBank(bank));
    }

    @NotNull
    public Map<UUID, ChestPlayerBank> getBankMap() {
        return this.bankMap;
    }

    @NotNull
    public Currency getDefaultCurrency() {
        Currency currency = this.plugin.getCurrencyManager().getCurrency(ChestConfig.DEFAULT_CURRENCY.get());
        if (currency != null) return currency;

        return this.plugin.getCurrencyManager().getCurrencies()
            .stream().filter(cur -> ChestConfig.ALLOWED_CURRENCIES.get().contains(cur.getId())).findFirst().orElse(CurrencyManager.DUMMY);
    }

    @Override
    @NotNull
    public TransactionLogger getLogger() {
        return logger;
    }

    @NotNull
    public ShopSettingsMenu getSettingsMenu() {
        return settingsMenu;
    }

    @NotNull
    public ShopProductsMenu getProductsMenu() {
        return productsMenu;
    }

    @NotNull
    public ShopBrowseMenu getBrowseMenu() {
        return browseMenu;
    }

    @NotNull
    public ShopListMenu getListMenu() {
        return this.listMenu;
    }

    @NotNull
    public ShopSearchMenu getSearchMenu() {
        return this.searchMenu;
    }

    @NotNull
    public BankMenu getBankMenu() {
        return bankMenu;
    }

    public void listShops(@NotNull Player player) {
        this.listShops(player, player.getUniqueId());
    }

    public void listShops(@NotNull Player player, @NotNull UUID target) {
        this.getListMenu().open(player, target, 1);
    }

    public boolean createShop(@NotNull Player player, @NotNull Block block, @NotNull ShopType type) {
        if (!ChestUtils.isValidContainer(block)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_NOT_A_CHEST).send(player);
            return false;
        }

        if (this.isShop(block)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_ALREADY_SHOP).send(player);
            return false;
        }

        if (!this.checkCreationLocation(player, block)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_BAD_LOCATION).send(player);
            return false;
        }

        if (!this.checkCreationClaim(player, block)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_BAD_AREA).send(player);
            return false;
        }

        if (!type.hasPermission(player)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_TYPE_PERMISSION).send(player);
            return false;
        }

        int shopLimit = ChestUtils.getShopLimit(player);
        int shopAmount = this.getShopsAmount(player);
        if (shopLimit > 0 && shopAmount >= shopLimit) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_LIMIT_REACHED).send(player);
            return false;
        }

        if (!this.payForCreate(player)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS).send(player);
            return false;
        }

        Container container = (Container) block.getState();
        Inventory inventory = container.getInventory();
        if (Stream.of(inventory.getContents()).anyMatch(inside -> inside != null && !inside.getType().isAir())) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_NOT_EMPTY).send(player);
            return false;
        }

        ItemStack hand = new ItemStack(player.getInventory().getItemInMainHand());

        ChestShopCreateEvent event = new ChestShopCreateEvent(player, block, hand, type);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        JYML cfg = new JYML(this.getAbsolutePath() + DIR_SHOPS, UUID.randomUUID() + ".yml");
        ChestShop shop = new ChestShop(this, cfg);
        shop.updateLocation(container.getLocation());
        shop.setType(type);
        shop.setOwner(player);
        shop.setName(Placeholders.forPlayer(player).apply(ChestConfig.DEFAULT_NAME.get()));
        Arrays.asList(TradeType.values()).forEach(tradeType -> shop.setTransactionEnabled(tradeType, true));
        shop.createProduct(player, hand);
        shop.save();

        this.addShop(shop);
        plugin.getMessage(ChestLang.SHOP_CREATION_INFO_DONE).send(player);
        return true;
    }

    public boolean deleteShop(@NotNull Player player, @NotNull Block block) {
        ChestShop shop = this.getShop(block);
        if (shop == null) {
            plugin.getMessage(ChestLang.SHOP_REMOVAL_ERROR_NOT_A_SHOP).send(player);
            return false;
        }

        return this.deleteShop(player, shop);
    }

    public boolean deleteShop(@NotNull Player player, @NotNull ChestShop shop) {
        if (!player.hasPermission(ChestPerms.REMOVE)) {
            plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
            return false;
        }

        if (!shop.isOwner(player) && !player.hasPermission(ChestPerms.REMOVE_OTHERS)) {
            plugin.getMessage(ChestLang.SHOP_ERROR_NOT_OWNER).send(player);
            return false;
        }

        if (!this.payForRemoval(player)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS).send(player);
            return false;
        }

        ChestShopRemoveEvent event = new ChestShopRemoveEvent(player, shop);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        this.removeShop(shop);

        // TODO Option to withdraw bank for 0 active shops
        if (this.getShopsAmount(player) <= 0) {
            for (Currency currency : ChestUtils.getAllowedCurrencies()) {
                this.withdrawFromBank(player, currency, -1);
            }
        }

        plugin.getMessage(ChestLang.SHOP_REMOVAL_INFO_DONE).send(player);
        return true;
    }

    public boolean depositToBank(@NotNull Player player, @NotNull Currency currency, double amount) {
        return this.depositToBank(player, player.getUniqueId(), currency, amount);
    }

    public boolean depositToBank(@NotNull Player player, @NotNull UUID target, @NotNull Currency currency, double amount) {
        if (!ChestUtils.isAllowedCurrency(currency)) {
            plugin.getMessage(ChestLang.BANK_ERROR_INVALID_CURRENCY).send(player);
            return false;
        }

        if (amount < 0D) amount = currency.getHandler().getBalance(player);

        if (currency.getHandler().getBalance(player) < amount) {
            plugin.getMessage(ChestLang.BANK_DEPOSIT_ERROR_NOT_ENOUGH).send(player);
            return false;
        }

        currency.getHandler().take(player, amount);

        ChestPlayerBank bank = this.getPlayerBank(target);
        bank.deposit(currency, amount);
        this.savePlayerBank(bank);

        plugin.getMessage(ChestLang.BANK_DEPOSIT_SUCCESS)
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
            .send(player);
        return true;
    }

    public boolean withdrawFromBank(@NotNull Player player, @NotNull Currency currency, double amount) {
        return this.withdrawFromBank(player, player.getUniqueId(), currency, amount);
    }

    public boolean withdrawFromBank(@NotNull Player player, @NotNull UUID target, @NotNull Currency currency, double amount) {
        if (!ChestUtils.isAllowedCurrency(currency)) {
            plugin.getMessage(ChestLang.BANK_ERROR_INVALID_CURRENCY).send(player);
            return false;
        }

        ChestPlayerBank bank = this.getPlayerBank(target);
        if (amount < 0D) amount = bank.getBalance(currency);

        if (!bank.hasEnough(currency, amount)) {
            plugin.getMessage(ChestLang.BANK_WITHDRAW_ERROR_NOT_ENOUGH).send(player);
            return false;
        }

        currency.getHandler().give(player, amount);
        bank.withdraw(currency, amount);
        this.savePlayerBank(bank);

        plugin.getMessage(ChestLang.BANK_WITHDRAW_SUCCESS)
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
            .send(player);
        return true;
    }

    @Nullable
    public DisplayHandler getDisplayHandler() {
        return this.displayHandler;
    }

    @NotNull
    public ShopMap getShopMap() {
        return this.shopMap;
    }

    @NotNull
    public Collection<ChestShop> getShops() {
        return this.getShopMap().getAll();
    }

    @NotNull
    public Set<ChestShop> getShops(@NotNull Player player) {
        return this.getShops(player.getUniqueId());
    }

    @NotNull
    public Set<ChestShop> getShops(@NotNull UUID ownerId) {
        return this.getShopMap().getByOwner(ownerId);
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
        return this.getShop(block.getLocation());
    }

    @Nullable
    public ChestShop getShop(@NotNull Location location) {
        return this.getShopMap().getByLocation(location);
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

        if (EngineUtils.hasPlugin(HookId.WORLD_GUARD)) {
            return WorldGuardFlags.checkFlag(player, block.getLocation());
        }
        return true;
    }

    public boolean checkCreationClaim(@NotNull Player player, @NotNull Block block) {
        if (!ChestConfig.SHOP_CREATION_CLAIM_ONLY.get()) return true;
        if (player.hasPermission(ChestPerms.BYPASS_CREATION_CLAIMS)) return true;

        return this.claimHooks.isEmpty() || this.claimHooks.stream().anyMatch(claim -> claim.isInOwnClaim(player, block));
    }

    private boolean payForCreate(@NotNull Player player) {
        return this.payForShop(player, ChestConfig.SHOP_CREATION_COST_CREATE.get());
    }

    private boolean payForRemoval(@NotNull Player player) {
        return this.payForShop(player, ChestConfig.SHOP_CREATION_COST_REMOVE.get());
    }

    private boolean payForShop(@NotNull Player player, double price) {
        if (price <= 0 || !VaultHook.hasEconomy()) return true;

        double balance = VaultHook.getBalance(player);
        if (balance < price) return false;

        VaultHook.takeMoney(player, price);
        return true;
    }
}
