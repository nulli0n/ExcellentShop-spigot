package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.integration.VaultHook;
import su.nexmedia.engine.utils.EngineUtils;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.price.ProductPriceStorage;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.nms.v1_20_R1.V1_20_R1;
import su.nightexpress.nexshop.nms.v1_20_R2.V1_20_R2;
import su.nightexpress.nexshop.shop.chest.command.*;
import su.nightexpress.nexshop.shop.chest.compatibility.*;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.impl.ChestPlayerBank;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.listener.RegionMarketListener;
import su.nightexpress.nexshop.shop.chest.listener.ShopListener;
import su.nightexpress.nexshop.shop.chest.menu.BankMenu;
import su.nightexpress.nexshop.shop.chest.menu.ShopBrowseMenu;
import su.nightexpress.nexshop.shop.chest.menu.ShopListMenu;
import su.nightexpress.nexshop.shop.chest.menu.ShopSearchMenu;
import su.nightexpress.nexshop.shop.chest.nms.ChestNMS;
import su.nightexpress.nexshop.shop.chest.nms.V1_18_R2;
import su.nightexpress.nexshop.shop.chest.nms.V1_19_R3;
import su.nightexpress.nexshop.shop.chest.util.ShopMap;
import su.nightexpress.nexshop.shop.chest.util.ShopType;
import su.nightexpress.nexshop.shop.chest.util.ShopUtils;
import su.nightexpress.nexshop.shop.module.ShopModule;
import su.nightexpress.nexshop.shop.util.TransactionLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChestShopModule extends ShopModule {

    public static final String ID = "chest_shop";
    public static final String DIR_SHOPS = "/shops/";
    public static final String DIR_MENUS = "/menu/";

    private final Map<UUID, ChestPlayerBank> bankMap;
    private final ShopMap shopMap;

    private ShopBrowseMenu browseMenu;
    private ShopListMenu   listMenu;
    private ShopSearchMenu searchMenu;
    private BankMenu       bankMenu;

    private Set<ClaimHook> claimHooks;
    private ChestDisplayHandler displayHandler;
    private ChestNMS  chestNMS;
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

        try {
            if (ShopUtils.getAllowedCurrencies().isEmpty()) throw new NoSuchElementException();
            ShopUtils.getDefaultCurrency();
        }
        catch (NoSuchElementException e) {
            this.error("Invalid 'Default_Currency' or 'Allowed_Currencies' has no valid currency.");
            return;
        }

        this.plugin.getLangManager().loadMissing(ChestLang.class);
        this.plugin.getLangManager().loadEnum(ShopType.class);
        this.plugin.getLang().saveChanges();
        this.plugin.registerPermissions(ChestPerms.class);

        this.chestNMS = switch (Version.getCurrent()) {
            case V1_18_R2 -> new V1_18_R2();
            case V1_19_R3 -> new V1_19_R3();
            case V1_20_R1 -> new V1_20_R1();
            case V1_20_R2 -> new V1_20_R2();
            default -> null;
        };
        if (this.chestNMS != null) {
            this.displayHandler = new ChestDisplayHandler(this);
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

        this.browseMenu = new ShopBrowseMenu(this);
        this.listMenu = new ShopListMenu(this);
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
    }

    public void removeInvalidShops() {
        Set<ChestShop> invalid = this.getShopMap().getAll().stream().filter(Predicate.not(ChestShop::isValid))
            .collect(Collectors.toCollection(HashSet::new));

        invalid.forEach(this::removeShop);
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
            for (Currency currency : ShopUtils.getAllowedCurrencies()) {
                bank.deposit(currency, cfg.getDouble("Bank." + currency.getId()));
            }
            this.plugin.getData().getChestDataHandler().saveChestBank(bank);
            cfg.remove("Bank");
            cfg.saveChanges();
        }
        // ----- OLD BANK UPDATE - END -----

        this.addShop(shop);
        ProductPriceStorage.loadData(shop).thenRun(() -> shop.getProducts().forEach(product -> product.getPricer().update()));
        return true;
    }

    public boolean unloadShop(@NotNull ChestShop shop) {
        shop.clear();
        this.getShopMap().remove(shop.getLocation());
        return true;
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();

        if (this.listMenu != null) this.listMenu.clear();
        if (this.browseMenu != null) this.browseMenu.clear();
        if (this.searchMenu != null) this.searchMenu.clear();
        if (this.bankMenu != null) this.bankMenu.clear();

        // Destroy shop editors and displays.
        this.getShops().forEach(ChestShop::clear);

        if (this.displayHandler != null) {
            this.displayHandler.shutdown();
            this.displayHandler = null;
        }
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
    public TransactionLogger getLogger() {
        return logger;
    }

    @NotNull
    public ChestNMS getNMS() {
        return chestNMS;
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

    public boolean createShop(@NotNull Player player, @NotNull Block block, @NotNull ShopType type) {
        if (!ShopUtils.isValidContainer(block)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_NOT_A_CHEST).send(player);
            return false;
        }

        if (this.isShop(block)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_ALREADY_SHOP).send(player);
            return false;
        }

        if (!this.isAllowedHere(player, block.getLocation())) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_BAD_LOCATION).send(player);
            return false;
        }

        if (!this.isAllowedHereClaim(player, block)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_BAD_AREA).send(player);
            return false;
        }

        if (!type.hasPermission(player)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_TYPE_PERMISSION).send(player);
            return false;
        }

        int shopLimit = ShopUtils.getShopLimit(player);
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

        JYML cfg = new JYML(this.getAbsolutePath() + DIR_SHOPS, UUID.randomUUID() + ".yml");
        ChestShop shop = new ChestShop(this, cfg);
        shop.setLocation(container.getLocation());
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

        if (!shop.isOwner(player) && !player.hasPermission(ChestPerms.REMOVE_OTHERS)) {
            plugin.getMessage(ChestLang.SHOP_ERROR_NOT_OWNER).send(player);
            return false;
        }

        if (!this.payForRemoval(player)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS).send(player);
            return false;
        }

        this.removeShop(shop);

        // TODO Option to withdraw bank for 0 active shops
        if (this.getShopsAmount(player) <= 0) {
            for (Currency currency : ShopUtils.getAllowedCurrencies()) {
                this.withdrawFromBank(player, currency, -1);
            }
        }

        plugin.getMessage(ChestLang.SHOP_REMOVAL_INFO_DONE).send(player);
        return true;
    }

    public void addShop(@NotNull ChestShop shop) {
        shop.updateContainerInfo();
        shop.updateDisplay();
        this.getShopMap().put(shop);
    }

    private void removeShop(@NotNull ChestShop shop) {
        if (!shop.getFile().delete()) return;
        shop.clear();
        this.getShopMap().remove(shop);
    }

    public boolean depositToBank(@NotNull Player player, @NotNull Currency currency, double amount) {
        return this.depositToBank(player, player.getUniqueId(), currency, amount);
    }

    public boolean depositToBank(@NotNull Player player, @NotNull UUID target, @NotNull Currency currency, double amount) {
        if (!ShopUtils.isAllowedCurrency(currency)) {
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
        if (!ShopUtils.isAllowedCurrency(currency)) {
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
    public ChestDisplayHandler getDisplayHandler() {
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
        return this.getShopMap().get(ownerId);
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
        return this.getShopMap().get(location);
    }

    public boolean isShop(@NotNull Block block) {
        return this.isShop(block.getLocation());
    }

    public boolean isShop(@NotNull Location location) {
        return this.getShop(location) != null;
    }

    public boolean isAllowedHere(@NotNull Player player, @NotNull Location location) {
        World world = location.getWorld();
        if (world == null) return true;

        String name = world.getName();
        if (ChestConfig.SHOP_CREATION_WORLD_BLACKLIST.get().contains(name)) {
            return false;
        }
        if (EngineUtils.hasPlugin(HookId.WORLD_GUARD) && !WorldGuardFlags.checkFlag(player, location)) {
            return false;
        }
        return true;
    }

    public boolean isAllowedHereClaim(@NotNull Player player, @NotNull Block block) {
        // TODO Permission
        if (player.hasPermission(ChestPerms.MODULE) || !ChestConfig.SHOP_CREATION_CLAIM_ONLY.get() || this.claimHooks.isEmpty()) return true;

        return this.claimHooks.stream().anyMatch(claim -> claim.isInOwnClaim(player, block));
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
