package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
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
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.TransactionLogger;
import su.nightexpress.nexshop.api.shop.TransactionModule;
import su.nightexpress.nexshop.api.shop.event.ChestShopCreateEvent;
import su.nightexpress.nexshop.api.shop.event.ChestShopRemoveEvent;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.module.AbstractShopModule;
import su.nightexpress.nexshop.shop.chest.command.*;
import su.nightexpress.nexshop.shop.chest.compatibility.*;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.config.ChestPerms;
import su.nightexpress.nexshop.shop.chest.display.DisplayHandler;
import su.nightexpress.nexshop.shop.chest.impl.ChestBank;
import su.nightexpress.nexshop.shop.chest.impl.ChestProduct;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.menu.ShopView;
import su.nightexpress.nexshop.shop.chest.listener.RegionMarketListener;
import su.nightexpress.nexshop.shop.chest.listener.ShopListener;
import su.nightexpress.nexshop.shop.chest.menu.*;
import su.nightexpress.nexshop.shop.chest.util.BlockPos;
import su.nightexpress.nexshop.shop.chest.util.ShopMap;
import su.nightexpress.nexshop.shop.chest.util.ShopType;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.integration.VaultHook;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Plugins;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ChestShopModule extends AbstractShopModule implements TransactionModule {

    public static final String ID = "chest_shop";
    public static final String DIR_SHOPS = "/shops/";

    private final Map<UUID, ChestBank> bankMap;
    private final ShopMap              shopMap;
    private final Set<Currency>        allowedCurrencies;

    private ShopSettingsMenu settingsMenu;
    private ShopProductsMenu productsMenu;
    private ShopDisplayMenu displayMenu;
    private ShopShowcaseMenu showcaseMenu;
    private ProductPriceMenu productPriceMenu;

    private BankMenu       bankMenu;
    private ShopBrowseMenu browseMenu;
    private ShopListMenu   listMenu;
    private ShopSearchMenu searchMenu;
    private ShopView       shopView;

    private Set<ClaimHook> claimHooks;
    private DisplayHandler displayHandler;

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
        config.initializeOptions(ChestConfig.class);
        this.plugin.getLangManager().loadEntries(ChestLang.class);
        this.plugin.registerPermissions(ChestPerms.class);
        this.logger = new TransactionLogger(this, config);

        this.loadCurrencies();
        this.loadHooks();

        this.addListener(new ShopListener(this.plugin, this));

        this.settingsMenu = new ShopSettingsMenu(this.plugin, this);
        this.displayMenu = new ShopDisplayMenu(this.plugin, this);
        this.showcaseMenu = new ShopShowcaseMenu(this.plugin, this);
        this.productsMenu = new ShopProductsMenu(this.plugin, this);
        this.productPriceMenu = new ProductPriceMenu(this.plugin, this);

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
    protected void addCommands(@NotNull ChainedNodeBuilder builder) {
        if (!ChestConfig.SHOP_AUTO_BANK.get()) {
            BankCommand.build(this.plugin, this, builder);
        }
        BrowseCommand.build(this, builder);
        CreateCommand.build(this, builder);
        ListCommand.build(this.plugin, this, builder);
        OpenCommand.build(this, builder);
        RemoveCommand.build(this, builder);
    }

    public void loadCurrencies() {
        for (String curId : ChestConfig.ALLOWED_CURRENCIES.get()) {
            Currency currency = this.plugin.getCurrencyManager().getCurrency(curId);
            if (currency == null) {
                this.error("Unknown currency '" + curId + "'. Skipping.");
                continue;
            }
            this.allowedCurrencies.add(currency);
        }

        Currency def = this.getDefaultCurrency();
        if (def == CurrencyManager.DUMMY_CURRENCY) {
            this.error("You have invalid currency set in the 'Default_Currency' setting.");
            this.error("You must fix this issue to make your shops working properly.");
        }
        else {
            this.allowedCurrencies.add(def);
        }
    }

    private void loadHooks() {
        if (Plugins.isLoaded(HookId.PROTOCOL_LIB)) {
            this.displayHandler = new DisplayHandler(this.plugin, this);
            this.displayHandler.setup();

            this.addTask(this.plugin.createAsyncTask(() -> this.displayHandler.update()).setSecondsInterval(ChestConfig.DISPLAY_UPDATE_INTERVAL.get()));
        }

        if (ChestConfig.SHOP_CREATION_CLAIM_ONLY.get()) {
            this.claimHooks = new HashSet<>();
            if (Plugins.isInstalled(HookId.LANDS)) this.claimHooks.add(new LandsHook(this.plugin));
            if (Plugins.isInstalled(HookId.GRIEF_PREVENTION)) this.claimHooks.add(new GriefPreventionHook());
            if (Plugins.isInstalled(HookId.WORLD_GUARD)) this.claimHooks.add(new WorldGuardFlags());
            if (Plugins.isInstalled(HookId.KINGDOMS)) this.claimHooks.add(new KingdomsHook());
        }

        if (Plugins.isInstalled(HookId.ADVANCED_REGION_MARKET)) {
            this.addListener(new RegionMarketListener(this.plugin, this));
        }
    }

    public void loadBanks() {
        this.plugin.getData().getChestDataHandler().getChestBanks().forEach(bank -> {
            // Add missing currencies to display them as 0 in balance placeholder, so they are visible.
            this.getAllowedCurrencies().forEach(currency -> {
                bank.getBalanceMap().computeIfAbsent(currency, k -> 0D);
            });

            this.getBankMap().put(bank.getHolder(), bank);
        });
    }

    public void loadShops() {
        for (FileConfig config : FileConfig.loadAll(this.getAbsolutePath() + DIR_SHOPS, false)) {
            this.loadShop(config);
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
                bank.deposit(currency, config.getDouble("Bank." + currency.getId()));
            }
            this.plugin.getData().getChestDataHandler().saveChestBank(bank);
            config.remove("Bank");
            config.saveChanges();
        }
        // ----- OLD BANK UPDATE - END -----

        shop.updatePosition();

        this.shopMap.put(shop);
        return true;
    }

    public void unloadShop(@NotNull ChestShop shop) {
        if (this.displayHandler != null) {
            this.displayHandler.remove(shop);
        }
        this.shopMap.remove(shop);
        shop.deactivate();
    }

    public void removeShop(@NotNull ChestShop shop) {
        if (!shop.getFile().delete()) return;

        this.unloadShop(shop);
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
            this.plugin.runTaskAsync(task -> this.plugin.getData().getChestDataHandler().createChestBank(bank2));
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
        this.plugin.runTaskAsync(task -> this.plugin.getData().getChestDataHandler().saveChestBank(bank));
    }

    @NotNull
    public Map<UUID, ChestBank> getBankMap() {
        return this.bankMap;
    }

    public boolean isAllowedCurrency(@NotNull Currency currency) {
        return this.getAllowedCurrencies().contains(currency);
    }

    @NotNull
    public Set<Currency> getAllowedCurrencies() {
        return this.allowedCurrencies;
    }

    @NotNull
    public Currency getDefaultCurrency() {
        Currency currency = this.plugin.getCurrencyManager().getCurrency(ChestConfig.DEFAULT_CURRENCY.get());
        return currency == null ? CurrencyManager.DUMMY_CURRENCY : currency;
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
        if (!force) {
            if (!shop.canAccess(player, true)) return false;
        }

        MenuViewer viewer = this.shopView.getViewerOrCreate(player);
        viewer.setPage(Math.abs(page));

        return this.shopView.open(player, shop);
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
            shop.getProducts().forEach(product -> {
                if (!(product.getPacker() instanceof ItemPacker packer)) return;

                ItemStack item = packer.getItem();
                String material = BukkitThing.toString(item.getType()).toLowerCase();
                String localized = LangAssets.get(item.getType()).toLowerCase();
                String displayName = ItemUtil.getItemName(item);
                if (material.contains(searchFor) || localized.contains(searchFor) || displayName.contains(searchFor)) {
                    products.add(product);
                    return;
                }

                if (packer instanceof PluginItemPacker pluginPacker) {
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

    public void renameShop(@NotNull Player player, @NotNull ChestShop shop, @NotNull String name) {
        int maxLength = ChestConfig.SHOP_MAX_NAME_LENGTH.get();
        if (name.length() > maxLength) {
            name = name.substring(0, maxLength);
        }

        shop.setName(name);
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

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (player.isSneaking()) {
            ItemStack item = event.getItem();
            if (item != null && !originalDeny) {
                if (Tag.SIGNS.isTagged(item.getType()) || item.getType() == Material.ITEM_FRAME || item.getType() == Material.GLOW_ITEM_FRAME || item.getType() == Material.HOPPER) {
                    if (!shop.isOwner(player)) {
                        ChestLang.SHOP_ERROR_NOT_OWNER.getMessage().send(player);
                    }
                    else event.setUseInteractedBlock(Event.Result.ALLOW);
                    return;
                }
            }

            if (shop.isOwner(player) || player.hasPermission(ChestPerms.EDIT_OTHERS)) {
                this.openShopSettings(player, shop);
            }
            else {
                ChestLang.SHOP_ERROR_NOT_OWNER.getMessage().send(player);
            }
            return;
        }

        if (shop.isAdminShop() || !shop.isOwner(player)) {
            if (shop.canAccess(player, true)) {
                shop.open(player);
            }
        }
        else if (!originalDeny) {
            event.setUseInteractedBlock(Event.Result.ALLOW);
        }
    }

    public boolean createShop(@NotNull Player player, @NotNull Block block, @NotNull ShopType type) {
        return this.createShop(player, block, type, -1, -1);
    }

    public boolean createShop(@NotNull Player player, @NotNull Block block, @NotNull ShopType type, double buyPrice, double sellPrice) {
        if (!ChestUtils.isValidContainer(block)) {
            ChestLang.SHOP_CREATION_ERROR_NOT_A_CHEST.getMessage().send(player);
            return false;
        }

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

        if (!type.hasPermission(player)) {
            ChestLang.SHOP_CREATION_ERROR_TYPE_PERMISSION.getMessage().send(player);
            return false;
        }

        int shopLimit = ChestUtils.getShopLimit(player);
        int shopAmount = this.getShopsAmount(player);
        if (shopLimit > 0 && shopAmount >= shopLimit) {
            ChestLang.SHOP_CREATION_ERROR_LIMIT_REACHED.getMessage().send(player);
            return false;
        }

        if (!this.payForCreate(player)) {
            ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS.getMessage().send(player);
            return false;
        }

        Container container = (Container) block.getState();
        Inventory inventory = container.getInventory();
        if (Stream.of(inventory.getContents()).anyMatch(inside -> inside != null && !inside.getType().isAir())) {
            ChestLang.SHOP_CREATION_ERROR_NOT_EMPTY.getMessage().send(player);
            return false;
        }

        ItemStack hand = new ItemStack(player.getInventory().getItemInMainHand());

        ChestShopCreateEvent event = new ChestShopCreateEvent(player, block, hand, type);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        String id = ChestUtils.generateShopId(player, container.getLocation());
        File file = new File(this.getAbsolutePath() + DIR_SHOPS, id + ".yml");
        ChestShop shop = new ChestShop(this.plugin, this, file, id);

        //shop.updateLocation(container.getLocation());
        shop.assignLocation(container.getWorld(), container.getLocation());
        shop.setType(type);
        shop.setOwner(player);
        shop.setName(Placeholders.forPlayer(player).apply(ChestConfig.DEFAULT_NAME.get()));
        shop.setHologramEnabled(true);
        shop.setShowcaseEnabled(true);
        shop.setShowcaseType(null);
        Arrays.asList(TradeType.values()).forEach(tradeType -> shop.setTransactionEnabled(tradeType, true));

        ChestProduct product = shop.createProduct(player, hand);
        if (product != null) {
            if (buyPrice > 0) product.setPrice(TradeType.BUY, buyPrice);
            if (sellPrice > 0) product.setPrice(TradeType.SELL, sellPrice);
        }

        shop.save();

        this.shopMap.put(shop);
        if (this.displayHandler != null) {
            this.displayHandler.update(shop);
        }
        ChestLang.SHOP_CREATION_INFO_DONE.getMessage().send(player);
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
        if (!player.hasPermission(ChestPerms.REMOVE)) {
            ChestLang.ERROR_NO_PERMISSION.getMessage().send(player);
            return false;
        }

        if (!shop.isOwner(player) && !player.hasPermission(ChestPerms.REMOVE_OTHERS)) {
            ChestLang.SHOP_ERROR_NOT_OWNER.getMessage().send(player);
            return false;
        }

        if (!this.payForRemoval(player)) {
            ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS.getMessage().send(player);
            return false;
        }

        ChestShopRemoveEvent event = new ChestShopRemoveEvent(player, shop);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        this.removeShop(shop);

        if (this.getShopsAmount(player) <= 0) {
            for (Currency currency : this.getAllowedCurrencies()) {
                this.withdrawFromBank(player, currency, -1);
            }
        }

        ChestLang.SHOP_REMOVAL_INFO_DONE.getMessage().send(player);
        return true;
    }

    public boolean depositToBank(@NotNull Player player, @NotNull Currency currency, double amount) {
        return this.depositToBank(player, player.getUniqueId(), currency, amount);
    }

    public boolean depositToBank(@NotNull Player player, @NotNull UUID target, @NotNull Currency currency, double amount) {
        if (!this.isAllowedCurrency(currency)) {
            ChestLang.BANK_ERROR_INVALID_CURRENCY.getMessage().send(player);
            return false;
        }

        if (amount < 0D) amount = currency.getHandler().getBalance(player);

        if (currency.getHandler().getBalance(player) < amount) {
            ChestLang.BANK_DEPOSIT_ERROR_NOT_ENOUGH.getMessage().send(player);
            return false;
        }

        currency.getHandler().take(player, amount);

        ChestBank bank = this.getPlayerBank(target);
        bank.deposit(currency, amount);
        this.savePlayerBank(bank);

        ChestLang.BANK_DEPOSIT_SUCCESS.getMessage()
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
            .send(player);
        return true;
    }

    public boolean withdrawFromBank(@NotNull Player player, @NotNull Currency currency, double amount) {
        return this.withdrawFromBank(player, player.getUniqueId(), currency, amount);
    }

    public boolean withdrawFromBank(@NotNull Player player, @NotNull UUID target, @NotNull Currency currency, double amount) {
        if (!this.isAllowedCurrency(currency)) {
            ChestLang.BANK_ERROR_INVALID_CURRENCY.getMessage().send(player);
            return false;
        }

        ChestBank bank = this.getPlayerBank(target);
        if (amount < 0D) amount = bank.getBalance(currency);

        if (!bank.hasEnough(currency, amount)) {
            ChestLang.BANK_WITHDRAW_ERROR_NOT_ENOUGH.getMessage().send(player);
            return false;
        }

        currency.getHandler().give(player, amount);
        bank.withdraw(currency, amount);
        this.savePlayerBank(bank);

        ChestLang.BANK_WITHDRAW_SUCCESS.getMessage()
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
