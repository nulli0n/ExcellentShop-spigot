package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
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
import su.nightexpress.nexshop.api.module.ShopModule;
import su.nightexpress.nexshop.api.shop.TransactionLogger;
import su.nightexpress.nexshop.api.shop.event.ChestShopCreateEvent;
import su.nightexpress.nexshop.api.shop.event.ChestShopRemoveEvent;
import su.nightexpress.nexshop.api.shop.stock.StockValues;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.module.AbstractModule;
import su.nightexpress.nexshop.module.ModuleConfig;
import su.nightexpress.nexshop.shop.chest.command.*;
import su.nightexpress.nexshop.shop.chest.compatibility.*;
import su.nightexpress.nexshop.shop.chest.config.*;
import su.nightexpress.nexshop.shop.chest.display.DisplayManager;
import su.nightexpress.nexshop.shop.chest.impl.*;
import su.nightexpress.nexshop.shop.chest.listener.RegionMarketListener;
import su.nightexpress.nexshop.shop.chest.listener.ShopListener;
import su.nightexpress.nexshop.shop.chest.listener.UpgradeHopperListener;
import su.nightexpress.nexshop.shop.chest.lookup.ShopLookup;
import su.nightexpress.nexshop.shop.chest.menu.*;
import su.nightexpress.nexshop.shop.chest.rent.RentSettings;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.UIUtils;
import su.nightexpress.nightcore.ui.menu.confirmation.Confirmation;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.nightcore.util.text.NightMessage;
import su.nightexpress.nightcore.util.time.TimeFormats;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ChestShopModule extends AbstractModule implements ShopModule {

    public static final String DIR_SHOPS   = "/shops/";
    public static final String BLOCKS_FILE = "blocks.yml";

    private final Map<Material, ShopBlock> blockMap;
    private final Map<UUID, ChestBank>     bankMap;
    private final Set<ClaimHook>           claimHooks;
    private final ShopLookup               lookup;

    private SettingsMenu      settingsMenu;
    private ProductsMenu      productsMenu;
    private ShowcaseMenu      showcaseMenu;
    private PriceMenu         priceMenu;
    private RentMenu          rentMenu;
    private BankMenu          bankMenu;
    private PlayerBrowserMenu playerBrowserMenu;
    private ShopBrowserMenu   shopBrowserMenu;
    private ShopView          shopView;

    private DisplayManager displayManager;

    private TransactionLogger logger;

    public ChestShopModule(@NotNull ShopPlugin plugin, @NotNull String id, @NotNull ModuleConfig config) {
        super(plugin, id, config);
        this.blockMap = new HashMap<>();
        this.bankMap = new ConcurrentHashMap<>();
        this.claimHooks = new HashSet<>();
        this.lookup = new ShopLookup();
    }

    @NotNull
    public String getShopsPath() {
        return this.getLocalPath() + DIR_SHOPS;
    }

    @NotNull
    public FileConfig getBlocksConfig() {
        return FileConfig.loadOrExtract(this.plugin, this.getLocalPath(), BLOCKS_FILE);
    }

    @Override
    protected void loadModule(@NotNull FileConfig config) {
        ChestKeys.load(this.plugin);
        this.loadConfig(config);
        this.plugin.getLangManager().loadEntries(ChestLang.class);
        this.plugin.registerPermissions(ChestPerms.class);
        this.logger = new TransactionLogger(this, config);

        this.loadDisplayManager();
        this.loadHooks();
        this.loadUI();
        this.loadShops();

        this.addListener(new ShopListener(this.plugin, this));

        this.addAsyncTask(this::saveShopsIfRequired, ChestConfig.SAVE_INTERVAL.get());

        this.plugin.runTaskAsync(task -> this.loadBanks());
        this.plugin.runTask(task -> this.lookup().getAll().forEach(this::activateShop));
    }

    private void loadConfig(@NotNull FileConfig config) {
        FileConfig blocksConfig = this.getBlocksConfig();
        ConfigMigration.migrateHologramSettings(config);
        ConfigMigration.migrateShopBlocks(config, blocksConfig); // First migrate block settings and erase 'Showcase' section.
        ConfigMigration.migrateShowcaseCatalog(config); // Then migrate showcase catalog, write 'Showcase.Catalog' section.

        config.initializeOptions(ChestConfig.class);
        this.loadBlocks(blocksConfig);
    }

    private void loadBlocks(@NotNull FileConfig blocksConfig) {
        if (blocksConfig.getSection("Blocks").isEmpty()) {
            ChestUtils.getDefaultShopBlockTypes().forEach(material -> {
                NightItem item = ChestUtils.getDefaultShopItem(material);
                Showcase showcase = Showcase.fromMaterial(Material.GLASS);
                ShopBlock shopBlock = new ShopBlock(material, item, showcase);
                blocksConfig.set("Blocks." + BukkitThing.getValue(material), shopBlock);
            });
        }

        blocksConfig.getSection("Blocks").forEach(sId -> {
            ShopBlock shopBlock = ShopBlock.read(blocksConfig, "Blocks." + sId);
            if (shopBlock == null) {
                this.error("Could not load '" + sId + "' shop block. Found in '" + blocksConfig.getFile().getAbsolutePath() + "'.");
                return;
            }

            this.blockMap.put(shopBlock.getMaterial(), shopBlock);
        });
        blocksConfig.saveChanges();
    }

    private void loadUI() {
        String path = this.getMenusPath();

        this.priceMenu = new PriceMenu(this.plugin, this);

        this.bankMenu = this.addMenu(new BankMenu(this.plugin, this), path, "bank.yml");
        this.shopView = new ShopView(this.plugin, this);

        this.settingsMenu = this.addMenu(new SettingsMenu(this.plugin, this), path, "shop_settings.yml");
        this.productsMenu = this.addMenu(new ProductsMenu(this.plugin, this), path, "shop_products_v2.yml");
        this.showcaseMenu = this.addMenu(new ShowcaseMenu(this.plugin, this), path, "shop_showcase_v2.yml");
        this.playerBrowserMenu = this.addMenu(new PlayerBrowserMenu(this.plugin, this), path, "player_browser.yml");
        this.shopBrowserMenu = this.addMenu(new ShopBrowserMenu(this.plugin, this), path, "shop_browser.yml");

        if (ChestConfig.isRentEnabled()) {
            this.rentMenu = this.addMenu(new RentMenu(this.plugin, this), path, "shop_rent.yml");
        }
    }

    @Override
    protected void disableModule() {
        this.saveShopsIfRequired();

        if (this.shopView != null) this.shopView.clear();
        if (this.bankMenu != null) this.bankMenu.clear();
        if (this.priceMenu != null) this.priceMenu.clear();

        this.lookup.getAll().forEach(this::unloadShop);
        this.lookup.clear();
        this.bankMap.clear();
        this.claimHooks.clear();

        if (this.displayManager != null) this.displayManager.shutdown();
    }

    @Override
    protected void loadCommands(@NotNull ChainedNodeBuilder builder) {
        ChestShopCommands.build(this.plugin, this, builder);
    }

    private void loadHooks() {
        if (ChestConfig.SHOP_CREATION_CLAIM_ONLY.get()) {
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

    private void loadDisplayManager() {
        this.displayManager = new DisplayManager(this.plugin, this);
        this.displayManager.setup();
    }

    public void loadBanks() {
        this.plugin.getDataHandler().loadChestBanks().forEach(bank -> {
            // Add missing currencies to display them as 0 in balance placeholder, so they are visible.
            this.getEnabledCurrencies().forEach(currency -> {
                bank.getBalanceMap().computeIfAbsent(currency.getInternalId(), k -> 0D);
            });

            this.getBankMap().put(bank.getHolder(), bank);
        });
    }

    public void loadShops() {
        for (File file : FileUtil.getConfigFiles(this.getAbsolutePath() + DIR_SHOPS)) {
            this.loadShop(file);
        }
        this.info("Loaded " + this.lookup.countShops() + " shops.");
    }

    private void loadShop(@NotNull File file) {
        String id = FileConfig.getName(file);

        ChestShop shop = new ChestShop(this.plugin, this, file, id);
        if (!shop.load()) {
            this.error("Invalid configuration for the '" + id + "' shop. Removing now...");
            file.delete();
            return;
        }

        this.lookup.put(shop);
    }

    public void unloadShop(@NotNull ChestShop shop) {
        this.deactivateShop(shop);
        this.lookup.remove(shop);
    }

    public void removeShop(@NotNull ChestShop shop) {
        this.unloadShop(shop);
        shop.getFile().delete();
    }

    public void activateShop(@NotNull ChestShop shop) {
        if (shop.isActive()) return;

        World world = this.plugin.getServer().getWorld(shop.getWorldName());
        if (world == null) return;

        this.activateShop(shop, world);
    }

    public void activateShop(@NotNull ChestShop shop, @NotNull World world) {
        if (shop.activate(world)) {
            if (shop.isChunkLoaded()) {
                this.onChunkLoad(shop);
            }

            this.displayManager.render(shop);
        }
    }

    public void deactivateShop(@NotNull ChestShop shop) {
        if (!shop.isActive()) return;

        this.displayManager.remove(shop);
        shop.deactivate();
    }

    public void saveShopsIfRequired() {
        this.lookup.getAll().stream().filter(ChestShop::isSaveRequired).peek(ChestShop::save).forEach(shop -> shop.setSaveRequired(false));
    }

    public void onChunkLoad(@NotNull ChestShop shop) {
        //this.plugin.debug("Handle chunk load for " + shop.getId());
        World world = shop.location().getWorld();
        BlockPos blockPos = shop.getBlockPos();
        Block block = blockPos.toBlock(world);
        Material blockType = block.getType();

        if (!this.isShopBlock(blockType) && !ChestUtils.isContainer(blockType)) {
            this.warn("Shop at '" + blockPos + "' in '" + world.getName() + "' is in illegal block.");
            this.unloadShop(shop);
            return;
        }

        this.splitShop(shop);
        shop.updateStockCache();
    }

    public void onChunkUnload(@NotNull ChestShop shop) {
        //this.plugin.debug("Handle chunk unload for " + shop.getId());
        shop.updateStockCache();
    }

    private void splitShop(@NotNull ChestShop shop) {
        if (!shop.isActive()) return;

        Inventory inventory = shop.inventory();
        if (!(inventory instanceof DoubleChestInventory chestInventory)) return;

        Chest left = (Chest) chestInventory.getLeftSide().getHolder();
        Chest right = (Chest) chestInventory.getRightSide().getHolder();
        if (left == null || right == null) return;

        this.splitSide(left);
        this.splitSide(right);
    }

    private void splitSide(@NotNull Chest chest) {
        org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) chest.getBlockData();
        chestData.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
        chest.getWorld().setBlockData(chest.getLocation(), chestData);
    }

    @NotNull
    public ShopLookup lookup() {
        return this.lookup;
    }

    @NotNull
    public DisplayManager getDisplayManager() {
        return this.displayManager;
    }

    @NotNull
    public Map<Material, ShopBlock> getShopBlockMap() {
        return this.blockMap;
    }

    @Nullable
    public ShopBlock getShopBlock(@NotNull String name) {
        Material material = BukkitThing.getMaterial(name);
        return material == null ? null : this.getShopBlock(material);
    }

    @Nullable
    public ShopBlock getShopBlock(@NotNull Material material) {
        return this.blockMap.get(material);
    }

    public boolean isShopBlock(@NotNull Block block) {
        return this.isShopBlock(block.getType());
    }

    public boolean isShopBlock(@NotNull Material material) {
        return this.getShopBlock(material) != null;
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

    @Override
    @NotNull
    public String getDefaultCartUI() {
        return ChestConfig.DEFAULT_CART_UI.get();
    }

    @Override
    @NotNull
    public TransactionLogger getLogger() {
        return this.logger;
    }

    @NotNull
    public ShopView getShopView() {
        return this.shopView;
    }

    public void openShopSettings(@NotNull Player player, @NotNull ChestShop shop) {
        this.settingsMenu.open(player, shop);
    }

    public void openShowcaseMenu(@NotNull Player player, @NotNull ChestShop shop) {
        this.showcaseMenu.open(player, shop);
    }

    public void openProductsMenu(@NotNull Player player, @NotNull ChestShop shop) {
        this.productsMenu.open(player, shop);
    }

    public void openAdvancedPriceMenu(@NotNull Player player, @NotNull ChestProduct product) {
        this.priceMenu.open(player, product);
    }

    public void openBank(@NotNull Player player, @NotNull ChestShop shop) {
        this.bankMenu.open(player, shop);
    }

    public void openBank(@NotNull Player player, @NotNull UUID target) {
        this.bankMenu.open(player, target);
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

    public void browseShopOwners(@NotNull Player player) {
        this.playerBrowserMenu.open(player);
    }

    public void browseAllShops(@NotNull Player player) {
        this.shopBrowserMenu.open(player);
    }

    public void browsePlayerShops(@NotNull Player player, @NotNull String ownerName) {
        this.shopBrowserMenu.openByPlayer(player, ownerName);
    }

    public void browseItemShops(@NotNull Player player, @NotNull String itemSearch) {
        this.shopBrowserMenu.openByItem(player, itemSearch);
    }

    public void browseShopsByShop(@NotNull Player player, @NotNull ChestShop source) {
        this.shopBrowserMenu.openFromShop(player, source);
    }

    public boolean teleportToShop(@NotNull Player player, @NotNull ChestShop shop) {
        if (!shop.isActive()) {
            this.getPrefixed(ChestLang.ERROR_SHOP_INACTIVE).send(player);
            return false;
        }

        Location location = shop.location().getTeleportLocation();

        if (ChestConfig.CHECK_SAFE_LOCATION.get()) {
            if (!shop.isOwner(player) && !ChestUtils.isSafeLocation(location)) {
                this.getPrefixed(ChestLang.SHOP_TELEPORT_ERROR_UNSAFE).send(player);
                return false;
            }
        }

        return player.teleport(location);
    }

    public boolean renameShop(@NotNull Player player, @NotNull ChestShop shop, @NotNull String name) {
        String rawName = NightMessage.stripTags(name);
        int maxLength = ChestConfig.SHOP_MAX_NAME_LENGTH.get();

        if (rawName.length() > maxLength) {
            this.getPrefixed(ChestLang.SHOP_RENAME_ERROR_LONG_NAME).send(player, replacer -> replacer.replace(Placeholders.GENERIC_AMOUNT, maxLength));
            return false;
        }

        shop.setName(name);
        shop.setSaveRequired(true);
        return true;
    }

    public void interactShop(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull Block block) {
        // TODO Also handle interaction with item frames attached to shops
        // TODO Create shops using signs

        ChestShop blockShop = this.getShop(block);
        if (blockShop != null) {
            this.interactShop(event, player, blockShop);
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(block.getBlockData() instanceof WallSign wallSign)) return;

        Block backend = block.getRelative(wallSign.getFacing().getOppositeFace());
        ChestShop signShop = this.getShop(backend);
        if (signShop == null) return;

        // Allow owners to decorate shop signs with glows and colors.
        ItemStack itemStack = event.getItem();
        if (itemStack != null && ChestUtils.isSignDecor(itemStack) && signShop.canDecorate(player)) {
            return;
        }

        event.setUseItemInHand(Event.Result.DENY);
        this.interactShop(event, player, signShop);
    }

    public void interactShop(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ChestShop shop) {
        //if (event.useInteractedBlock() == Event.Result.DENY) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (player.isSneaking()) {
            ItemStack item = event.getItem();
            if (item != null && ChestUtils.isShopDecor(item)) {
                if (!shop.canDecorate(player)) {
                    this.getPrefixed(ChestLang.SHOP_ERROR_NOT_OWNER).send(player);
                    event.setUseInteractedBlock(Event.Result.DENY);
                }
                return;
            }

            if (shop.canManage(player)) {
                this.openShopSettings(player, shop);
            }
            else {
                this.getPrefixed(ChestLang.SHOP_ERROR_NOT_OWNER).send(player);
            }
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        if (shop.isOwnerOrRenter(player) && ChestUtils.isInfiniteStorage()) {
            event.setUseInteractedBlock(Event.Result.DENY);
            this.openShopSettings(player, shop);
            return;
        }

        if (shop.isAdminShop() || !shop.isOwnerOrRenter(player)) {
            event.setUseInteractedBlock(Event.Result.DENY);

            if (shop.isRentable() && !shop.isRented()) {
                UIUtils.openConfirmation(player, Confirmation.builder()
                    .onAccept((viewer, event1) -> this.rentShopOrExtend(player, shop))
                    .onReturn((viewer, event1) -> this.plugin.runTask(task -> player.closeInventory()))
                    .returnOnAccept(true)
                    .build());
                return;
            }

            if (shop.canAccess(player, true)) {
                shop.open(player);
            }
        }
    }

    public boolean checkShopCreation(@NotNull Player player, @NotNull Block block) {
        if (this.isShop(block)) {
            this.getPrefixed(ChestLang.SHOP_CREATION_ERROR_ALREADY_SHOP).send(player);
            return false;
        }

        if (!this.checkCreationLocation(player, block)) {
            this.getPrefixed(ChestLang.SHOP_CREATION_ERROR_BAD_LOCATION).send(player);
            return false;
        }

        if (!this.checkCreationClaim(player, block)) {
            this.getPrefixed(ChestLang.SHOP_CREATION_ERROR_BAD_AREA).send(player);
            return false;
        }

        int shopLimit = ChestUtils.getShopLimit(player);
        int shopAmount = this.countShops(player);
        if (shopLimit >= 0 && shopAmount >= shopLimit) {
            this.getPrefixed(ChestLang.SHOP_CREATION_ERROR_LIMIT_REACHED).send(player);
            return false;
        }

        if (!this.canPayCreation(player)) {
            this.getPrefixed(ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS).send(player);
            return false;
        }

        return true;
    }

    public boolean createShopFromItem(@NotNull Player player, @NotNull Block block, @NotNull ItemStack itemStack) {
        Material material = ChestUtils.getShopItemType(itemStack);
        if (material == null) return false;

        if (!this.isShopBlock(material)) {
            this.getPrefixed(ChestLang.SHOP_CREATION_ERROR_BAD_BLOCK).send(player);
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
        if (!this.isShopBlock(block)) {
            this.getPrefixed(ChestLang.SHOP_CREATION_ERROR_BAD_BLOCK).send(player);
            return false;
        }

        if (!this.checkShopCreation(player, block)) return false;

        Container container = (Container) block.getState();
        Inventory inventory = container.getInventory();
        if (Stream.of(inventory.getContents()).anyMatch(inside -> inside != null && !inside.getType().isAir())) {
            this.getPrefixed(ChestLang.SHOP_CREATION_ERROR_NOT_EMPTY).send(player);
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
        World world = block.getWorld();

        shop.setLocation(world, block.getLocation());
        shop.setAdminShop(player.hasPermission(ChestPerms.ADMIN_SHOP));
        shop.setOwner(player);
        shop.setName(Placeholders.forPlayer(player).apply(ChestConfig.DEFAULT_NAME.get()));
        shop.setHologramEnabled(true);
        shop.setShowcaseEnabled(true);
        shop.setShowcaseId(null);
        shop.setBuyingAllowed(true);
        shop.setSellingAllowed(true);
        if (ChestConfig.isRentEnabled()) {
            shop.setRentSettings(new RentSettings(false, 7, CurrencyId.VAULT, 1000));
        }

        consumer.accept(shop);
        shop.setSaveRequired(true);

        this.lookup.put(shop);
        this.activateShop(shop, world);

        this.getPrefixed(ChestLang.SHOP_CREATION_INFO_DONE).send(player);
        return shop;
    }

    public boolean rentShopOrExtend(@NotNull Player player, @NotNull ChestShop shop) {
        if (!ChestConfig.isRentEnabled()) return false;

        if (!shop.isActive()) {
            this.getPrefixed(ChestLang.ERROR_SHOP_INACTIVE).send(player, replacer -> replacer.replace(shop.replacePlaceholders()));
            return false;
        }

        RentSettings settings = shop.getRentSettings();
        if (!shop.isRentable() || !settings.isValid()) {
            this.getPrefixed(ChestLang.RENT_ERROR_NOT_RENTABLE).send(player, replacer -> replacer.replace(shop.replacePlaceholders()));
            return false;
        }

        boolean isExtend = false;

        if (shop.isRented()) {
            if (!shop.isRenter(player)) {
                this.getPrefixed(ChestLang.RENT_ERROR_ALREADY_RENTED).send(player, replacer -> replacer.replace(shop.replacePlaceholders()));
                return false;
            }
            isExtend = true;
        }

        String currencyId = settings.getCurrencyId();
        double price = settings.getPrice();
        if (!EconomyBridge.hasEnough(player, currencyId, price)) {
            this.getPrefixed(ChestLang.RENT_ERROR_INSUFFICIENT_FUNDS).send(player, replacer -> replacer
                .replace(Placeholders.GENERIC_PRICE, settings.getPriceFormatted())
                .replace(shop.replacePlaceholders())
            );
            return false;
        }

        EconomyBridge.withdraw(player, currencyId, price);
        EconomyBridge.deposit(shop.getOwnerId(), currencyId, price);
        shop.setRentedBy(player);
        shop.extendRent();
        shop.setSaveRequired(true);

        this.getPrefixed(isExtend ? ChestLang.RENT_EXTEND_SUCCESS : ChestLang.RENT_RENT_SUCCESS).send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_TIME, TimeFormats.toLiteral(settings.getDurationMillis()))
            .replace(Placeholders.GENERIC_PRICE, settings.getPriceFormatted())
            .replace(shop.replacePlaceholders())
        );
        return true;
    }

    public boolean cancelRent(@NotNull Player player, @NotNull ChestShop shop) {
        if (!shop.isRented()) return false;

        boolean isRenter = shop.isRenter(player);

        this.getPrefixed((isRenter ? ChestLang.RENT_CANCEL_BY_RENTER : ChestLang.RENT_CANCEL_BY_OWNER)).send(player, replacer -> replacer
            .replace(shop.replacePlaceholders())
        );

        // TODO Notify renter

        shop.cancelRent();
        shop.setSaveRequired(true);
        return true;
    }

    public boolean canBreak(@NotNull Player player, @NotNull ChestShop shop) {
        if (!player.hasPermission(ChestPerms.REMOVE)) {
            this.getPrefixed(ChestLang.ERROR_NO_PERMISSION).send(player);
            return false;
        }

        if (!shop.isOwner(player) && !player.hasPermission(ChestPerms.REMOVE_OTHERS)) {
            this.getPrefixed(ChestLang.SHOP_ERROR_NOT_OWNER).send(player);
            return false;
        }

        return true;
    }

    public boolean deleteShop(@NotNull Player player, @NotNull Block block) {
        ChestShop shop = this.getShop(block);
        if (shop == null) {
            this.getPrefixed(ChestLang.ERROR_BLOCK_IS_NOT_SHOP).send(player);
            return false;
        }

        return this.deleteShop(player, shop);
    }

    public boolean deleteShop(@NotNull Player player, @NotNull ChestShop shop) {
        if (!this.canBreak(player, shop)) return false;

        if (!this.payForRemoval(player)) {
            this.getPrefixed(ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS).send(player);
            return false;
        }

        if (ChestUtils.isInfiniteStorage()) {
            if (shop.getValidProducts().stream().anyMatch(product -> product.countStock(TradeType.BUY, null) > 0)) {
                this.getPrefixed(ChestLang.SHOP_REMOVAL_ERROR_NOT_EMPTY).send(player);
                return false;
            }
        }

        ChestShopRemoveEvent event = new ChestShopRemoveEvent(player, shop);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        if (shop.isItemCreated() && shop.isActive()) {
            Block block = shop.location().getBlock();
            ShopBlock shopBlock = this.getShopBlock(block.getType());

            if (shopBlock != null) {
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), shopBlock.getItemStack());
            }
        }

        this.removeShop(shop);

        if (this.countShops(player) <= 0 && !ChestConfig.isAutoBankEnabled()) {
            for (Currency currency : this.getAvailableCurrencies(player)) {
                this.withdrawFromBank(player, currency, -1);
            }
        }

        this.getPrefixed(ChestLang.SHOP_REMOVAL_INFO_DONE).send(player);
        return true;
    }

    public boolean depositToBank(@NotNull Player player, @NotNull Currency currency, double amount) {
        return this.depositToBank(player, player.getUniqueId(), currency, amount);
    }

    public boolean depositToBank(@NotNull Player player, @NotNull UUID target, @NotNull Currency currency, double value) {
        if (!this.isAvailableCurrency(player, currency)) {
            this.getPrefixed(ChestLang.BANK_ERROR_INVALID_CURRENCY).send(player);
            return false;
        }

        double balance = currency.getBalance(player);
        double amount = value < 0 ? balance : value;

        if (balance < amount) {
            this.getPrefixed(ChestLang.BANK_DEPOSIT_ERROR_NOT_ENOUGH).send(player);
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

        this.getPrefixed(ChestLang.BANK_DEPOSIT_SUCCESS).send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
        );
        return true;
    }

    public boolean withdrawFromBank(@NotNull Player player, @NotNull Currency currency, double amount) {
        return this.withdrawFromBank(player, player.getUniqueId(), currency, amount);
    }

    public boolean withdrawFromBank(@NotNull Player player, @NotNull UUID target, @NotNull Currency currency, double value) {
        ChestBank bank = this.getPlayerBank(target);
        double amount = value < 0D ? bank.getBalance(currency) : value;

        if (!bank.hasEnough(currency, amount)) {
            this.getPrefixed(ChestLang.BANK_WITHDRAW_ERROR_NOT_ENOUGH).send(player);
            return false;
        }

        currency.give(player, amount);
        bank.withdraw(currency, amount);
        this.savePlayerBank(bank);

        this.getPrefixed(ChestLang.BANK_WITHDRAW_SUCCESS).send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
        );
        return true;
    }

    public boolean depositToShop(@NotNull Player player, @NotNull ChestProduct product, int units) {
        if (units == 0) return false;

        ChestShop shop = product.getShop();

        int playerUnits = product.countUnits(player);
        if (playerUnits < units) {
            this.getPrefixed(ChestLang.STORAGE_DEPOSIT_ERROR_NOT_ENOUGH).send(player);
            return false;
        }

        product.storeStock(TradeType.BUY, units, null);
        product.take(player, units);
        shop.setSaveRequired(true);

        this.getPrefixed(ChestLang.STORAGE_DEPOSIT_SUCCESS).send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(units))
            .replace(Placeholders.GENERIC_ITEM, ItemUtil.getNameSerialized(product.getPreview()))
        );
        return true;
    }

    public boolean withdrawFromShop(@NotNull Player player, @NotNull ChestProduct product, int units) {
        if (units == 0) return false;

        ChestShop shop = product.getShop();

        int shopUnits = product.countUnitAmount();
        if (shopUnits < units) {
            this.getPrefixed(ChestLang.STORAGE_WITHDRAW_ERROR_NOT_ENOUGH).send(player);
            return false;
        }

        int spaceUnits = product.countUnits(product.countSpace(player));
        if (spaceUnits <= StockValues.UNLIMITED) return false;

        int maxUnits = Math.min(spaceUnits, units);

        product.delivery(player, maxUnits);
        product.consumeStock(TradeType.BUY, maxUnits, null);
        shop.setSaveRequired(true);

        this.getPrefixed(ChestLang.STORAGE_WITHDRAW_SUCCESS).send(player, replacer -> replacer
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(maxUnits))
            .replace(Placeholders.GENERIC_ITEM, ItemUtil.getNameSerialized(product.getPreview()))
        );
        return true;
    }

    public int countShops(@NotNull Player player) {
        return this.lookup.getOwnedBy(player.getUniqueId()).size();
    }

    @Nullable
    public ChestShop getShop(@NotNull Block block) {
        return this.lookup.getAt(block);
    }

    @Nullable
    public ChestShop getShop(@NotNull Location location) {
        return this.lookup.getAt(location);
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
