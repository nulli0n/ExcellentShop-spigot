package su.nightexpress.excellentshop.feature.playershop;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.claim.ClaimHook;
import su.nightexpress.excellentshop.api.packet.PacketLibrary;
import su.nightexpress.excellentshop.api.packet.display.DisplayAdapter;
import su.nightexpress.excellentshop.api.playershop.PlayerShop;
import su.nightexpress.excellentshop.api.playershop.PlayerShopManager;
import su.nightexpress.excellentshop.api.transaction.ETransactionItem;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.feature.playershop.bank.Bank;
import su.nightexpress.excellentshop.feature.playershop.bank.BankManager;
import su.nightexpress.excellentshop.feature.playershop.core.*;
import su.nightexpress.excellentshop.feature.playershop.dialog.impl.AddTrustedPlayerDialog;
import su.nightexpress.excellentshop.feature.playershop.dialog.impl.BankManagementDialog;
import su.nightexpress.excellentshop.feature.playershop.display.PlayerShopDisplaySettings;
import su.nightexpress.excellentshop.feature.playershop.exception.PlayerShopLoadException;
import su.nightexpress.excellentshop.feature.playershop.menu.*;
import su.nightexpress.excellentshop.integration.claim.*;
import su.nightexpress.excellentshop.integration.shop.UpgradeHopperListener;
import su.nightexpress.excellentshop.shop.dialog.impl.ProductPurchaseOptionsDialog;
import su.nightexpress.excellentshop.shop.formatter.ProductFormatter;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.api.shop.Shop;
import su.nightexpress.excellentshop.shop.TransactionLogger;
import su.nightexpress.excellentshop.feature.playershop.event.ChestShopCreateEvent;
import su.nightexpress.excellentshop.feature.playershop.event.ChestShopRemoveEvent;
import su.nightexpress.excellentshop.api.product.Product;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.nexshop.hook.HookPlugin;
import su.nightexpress.nexshop.module.ModuleContext;
import su.nightexpress.nexshop.user.ShopUser;
import su.nightexpress.excellentshop.product.price.FlatPricing;
import su.nightexpress.excellentshop.shop.ShopManager;
import su.nightexpress.excellentshop.shop.TransactionProcessor;
import su.nightexpress.excellentshop.feature.playershop.command.ChestShopCommands;
import su.nightexpress.excellentshop.feature.playershop.dialog.PSDialogKeys;
import su.nightexpress.excellentshop.feature.playershop.display.DisplayManager;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestProduct;
import su.nightexpress.excellentshop.feature.playershop.impl.ChestShop;
import su.nightexpress.excellentshop.feature.playershop.impl.ShopBlock;
import su.nightexpress.excellentshop.feature.playershop.impl.Showcase;
import su.nightexpress.excellentshop.feature.playershop.listener.ShopListener;
import su.nightexpress.excellentshop.feature.playershop.repository.ShopLookup;
import su.nightexpress.excellentshop.feature.playershop.rent.RentSettings;
import su.nightexpress.excellentshop.product.click.ProductClickContext;
import su.nightexpress.excellentshop.shop.AbstractShop;
import su.nightexpress.nexshop.module.AbstractShopModule;
import su.nightexpress.nexshop.util.BalanceHolder;
import su.nightexpress.nexshop.util.PacketUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.ui.UIUtils;
import su.nightexpress.nightcore.ui.menu.confirmation.Confirmation;
import su.nightexpress.nightcore.user.UserInfo;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.geodata.Cuboid;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.text.NightMessage;
import su.nightexpress.nightcore.util.time.TimeFormats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChestShopModule extends AbstractShopModule implements PlayerShopManager {

    private final TransactionProcessor           transactionProcessor;
    private final PlayerShopSettings             settings;
    private final ProductFormatter<ChestProduct> productFormatter;

    private final Map<Material, ShopBlock> blockMap;
    private final Set<ClaimHook>           claimHooks;
    private final ShopLookup               lookup;

    private SettingsMenu       settingsMenu;
    private ProductsMenu       productsMenu;
    private ShowcaseMenu       showcaseMenu;
    private TrustedPlayersMenu trustedPlayersMenu;
    private RentMenu           rentMenu;
    private PlayerBrowserMenu  playerBrowserMenu;
    private ShopBrowserMenu    shopBrowserMenu;
    private ShopView           shopView;

    private BankManager    bankManager;
    private DisplayManager displayManager;

    private TransactionLogger logger;

    public ChestShopModule(@NonNull ModuleContext context, @NonNull ShopManager shopManager,
                           @NonNull TransactionProcessor transactionProcessor) {
        super(context, shopManager);
        this.transactionProcessor = transactionProcessor;
        this.settings = new PlayerShopSettings();
        this.productFormatter = new ProductFormatter<>();

        this.blockMap = new EnumMap<>(Material.class);
        this.claimHooks = new HashSet<>();
        this.lookup = new ShopLookup();
    }

    @Override
    protected void loadModule(@NonNull FileConfig config) {
        ChestKeys.load(this.plugin);
        this.loadConfig(config);
        this.settings.load(config);
        this.plugin.registerPermissions(ChestPerms.class);
        this.logger = new TransactionLogger(this, config);

        if (this.settings.isBankEnabled()) {
            this.loadBank();
        }

        this.loadFormatter();
        this.loadDisplayManager(config);
        this.loadHooks();
        this.loadUI();
        this.loadDialogs();
        this.loadShops();

        this.addListener(new ShopListener(this.plugin, this));

        this.addAsyncTask(this::saveDirtyShops, ChestConfig.SAVE_INTERVAL.get());
        this.addTask(this::tickShops, 1);

        this.plugin.runTask(() -> {
            this.plugin.getServer().getWorlds().forEach(world -> {
                for (Chunk chunk : world.getLoadedChunks()) {
                    this.handleChunkLoad(chunk);
                }
            });
        });
    }

    private void loadConfig(@NonNull FileConfig config) {
        FileConfig blocksConfig = FileConfig.load(this.getPath().resolve(CSFiles.FILE_BLOCKS));
        ConfigMigration.migrateHologramSettings(config);
        ConfigMigration.migrateShopBlocks(config, blocksConfig); // First migrate block settings and erase 'Showcase' section.
        ConfigMigration.migrateShowcaseCatalog(config); // Then migrate showcase catalog, write 'Showcase.Catalog' section.

        config.initializeOptions(ChestConfig.class);
        this.loadBlocks(blocksConfig);
    }

    private void loadBank() {
        this.bankManager = new BankManager(this.plugin, this, this.dataHandler);
        this.bankManager.setup();

        this.dialogRegistry.register(PSDialogKeys.BANK_MANAGEMENT, new BankManagementDialog(this, this.bankManager));
    }

    private void loadFormatter() {
        this.productFormatter.registerCondition("buyable", (product, player) -> product.isBuyable());
        this.productFormatter.registerCondition("sellable", (product, player) -> product.isSellable());
        this.productFormatter.registerCondition("admin_shop", (product, player) -> product.getShop().isAdminShop());
        this.productFormatter.registerCondition("has_stock", (product, player) -> !product.getShop().isAdminShop());

        this.productFormatter.registerVariable("sell_all_price", (product, player) -> {
            return product.getCurrency().format(product.getFinalSellAllPrice(player));
        });

        this.productFormatter.registerVariable("max_units_to_buy", (product, player) -> {
            int amount = product.getMaxBuyableUnitAmount(player, player.getInventory());
            return amount < 0 ? CoreLang.OTHER_INFINITY.text() : NumberUtil.format(amount);
        });

        this.productFormatter.registerVariable("max_units_to_sell", (product, player) -> {
            int amount = product.getMaxSellableUnitAmount(player, player.getInventory());
            return amount < 0 ? CoreLang.OTHER_INFINITY.text() : NumberUtil.format(amount);
        });

        for (TradeType tradeType : TradeType.values()) {
            String name = LowerCase.INTERNAL.apply(tradeType.name());

            this.productFormatter.registerVariable(name + "_price", (product, player) -> {
                if (!product.isTradeable(tradeType)) return Lang.OTHER_N_A.text();

                return product.getCurrency().format(product.getFinalPrice(tradeType, player));
            });
        }
    }

    private void loadBlocks(@NonNull FileConfig blocksConfig) {
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
                this.error("Could not load '" + sId + "' shop block. Found in '" + blocksConfig.getPath() + "'.");
                return;
            }

            this.blockMap.put(shopBlock.getMaterial(), shopBlock);
        });
        blocksConfig.saveChanges();
    }

    private void loadUI() {
        this.shopView = this.initMenu(new ShopView(this.plugin, this), this.getPath().resolve(CSFiles.FILE_SHOP_VIEW));

        this.trustedPlayersMenu = this.initMenu(
            new TrustedPlayersMenu(this.plugin, this),
            this.getUIPath().resolve(CSFiles.UI_TRUSTED_PLAYERS)
        );

        String path = this.getMenusPath();

        this.settingsMenu = this.addMenu(new SettingsMenu(this.plugin, this), path, "shop_settings.yml");
        this.productsMenu = this.addMenu(new ProductsMenu(this.plugin, this), path, "shop_products_v2.yml");
        this.showcaseMenu = this.addMenu(new ShowcaseMenu(this.plugin, this), path, "shop_showcase_v2.yml");
        this.playerBrowserMenu = this.addMenu(new PlayerBrowserMenu(this.plugin, this), path, "player_browser.yml");
        this.shopBrowserMenu = this.addMenu(new ShopBrowserMenu(this.plugin, this), path, "shop_browser.yml");

        if (ChestConfig.isRentEnabled()) {
            this.rentMenu = this.addMenu(new RentMenu(this.plugin, this), path, "shop_rent.yml");
        }
    }

    private void loadDialogs() {
        this.dialogRegistry.register(PSDialogKeys.PRODUCT_PURCHASE_OPTIONS, new ProductPurchaseOptionsDialog(this));

        this.dialogRegistry.register(PSDialogKeys.SHOP_ADD_TRUSTED_PLAYER, new AddTrustedPlayerDialog(this));
    }

    @Override
    protected void disableModule() {
        this.saveDirtyShops();

        this.lookup.getAll().forEach(this::unloadShop);
        this.lookup.clear();
        this.claimHooks.clear();

        if (this.displayManager != null) {
            this.displayManager.clear();
        }

        if (this.bankManager != null) {
            this.bankManager.shutdown();
            this.bankManager = null;
        }
    }

    @Override
    protected void loadCommands(@NonNull HubNodeBuilder builder) {
        ChestShopCommands.build(this.plugin, this, builder);
    }

    private void loadHooks() {
        if (ChestConfig.SHOP_CREATION_CLAIM_ONLY.get()) {
            this.loadClaimHook(HookPlugin.LANDS, () -> new LandsHook(this.plugin));
            this.loadClaimHook(HookPlugin.GRIEF_PREVENTION, GriefPreventionHook::new);
            this.loadClaimHook(HookPlugin.GRIEF_DEFENDER, GriefDefenderHook::new);
            this.loadClaimHook(HookPlugin.WORLD_GUARD, WorldGuardHook::new);
            this.loadClaimHook(HookPlugin.KINGDOMS, KingdomsHook::new);
            this.loadClaimHook(HookPlugin.HUSK_CLAIMS, HuskClaimsHook::new);
            this.loadClaimHook(HookPlugin.SIMPLE_CLAIM_SYSTEM, SimpleClaimHook::new);
            this.loadClaimHook(HookPlugin.EXCELLENT_CLAIMS, ExcellentClaimsHook::new);
            this.loadClaimHook(HookPlugin.PLOT_SQUARED, () -> new PlotSquaredClaimHook(this));
        }

        if (Plugins.isInstalled(HookPlugin.ADVANCED_REGION_MARKET)) {
            this.addListener(new RegionMarketListener(this.plugin, this));
        }

        if (ChestUtils.isInfiniteStorage()) {
            if (Plugins.isInstalled(HookPlugin.UPGRADEABLE_HOPPERS)) {
                this.addListener(new UpgradeHopperListener(this.plugin, this));
            }
        }
    }

    private boolean loadClaimHook(@NonNull String plugin, @NonNull Supplier<ClaimHook> supplier) {
        if (!Plugins.isInstalled(plugin)) return false;

        this.claimHooks.add(supplier.get());
        this.info("Hooked into claim plugin: " + plugin);
        return true;
    }

    private void loadDisplayManager(@NonNull FileConfig config) {
        PacketLibrary library = PacketUtils.getLibrary();
        if (library == null) {
            this.warn("No packet library plugins found. Shop displays will be disabled.");
            return;
        }

        PlayerShopDisplaySettings displaySettings = new PlayerShopDisplaySettings();
        displaySettings.load(config);

        DisplayAdapter adapter = library.createDisplayAdapter(displaySettings);

        this.displayManager = new DisplayManager(this, adapter);
        this.addTask(this::updateShopDisplays, displaySettings.getUpdateInterval());
    }

    public void loadShops() {
        FileUtil.findYamlFiles(this.getPath().resolve(CSFiles.DIR_SHOPS)).forEach(this::loadShop);
        this.info("Loaded " + this.lookup.countShops() + " shops.");
    }

    private void loadShop(@NonNull Path file) {
        String id = FileUtil.getNameWithoutExtension(file);
        ChestShop shop = new ChestShop(this.plugin, this, file, id);

        try {
            shop.load();
        }
        catch (PlayerShopLoadException exception) {
            this.error("Corrupted shop data '%s': %s".formatted(file, exception.getMessage()));
            try {
                Files.delete(file);
            }
            catch (IOException exception2) {
                exception2.printStackTrace();
            }
            return;
        }

        this.lookup.put(shop);
    }

    @Override
    public void onDataLoadFinished() {

    }

    public void saveDirtyShops() {
        this.lookup().getAll().forEach(AbstractShop::saveIfDirty);
    }

    private void updateShopDisplays() {
        this.lookup().getAll().forEach(shop -> this.displayManager.render(shop));
    }

    public void tickShops() {
        this.lookup.getAll().forEach(this::tickShop);
    }

    public void tickShop(@NonNull ChestShop shop) {
        if (ChestConfig.isRentEnabled() && shop.isRented() && shop.isRentExpired()) {
            shop.cancelRent();
        }
    }

    public void unloadShop(@NonNull ChestShop shop) {
        shop.deactivate2();
        this.lookup.remove(shop);
    }

    @Override
    public void removeShop(@NonNull PlayerShop playerShop) {
        ChestShop shop = (ChestShop) playerShop;
        this.unloadShop(shop);

        try {
            Files.delete(shop.getPath());
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void onShopActivation(@NonNull ChestShop shop) {
        this.displayManager.render(shop);
    }

    public void onShopDeactivation(@NonNull ChestShop shop) {
        this.displayManager.remove(shop);
    }

    private void splitShop(@NonNull ChestShop shop) {
        Inventory inventory = shop.getInventory().orElse(null);
        if (!(inventory instanceof DoubleChestInventory chestInventory)) return;

        Chest left = (Chest) chestInventory.getLeftSide().getHolder();
        Chest right = (Chest) chestInventory.getRightSide().getHolder();
        if (left == null || right == null) return;

        this.splitSide(left);
        this.splitSide(right);
    }

    private void splitSide(@NonNull Chest chest) {
        org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) chest.getBlockData();
        chestData.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
        chest.getWorld().setBlockData(chest.getLocation(), chestData);
    }

    @Override
    @NonNull
    public PlayerShopSettings getSettings() {
        return this.settings;
    }

    @NonNull
    public ShopLookup lookup() {
        return this.lookup;
    }

    @NonNull
    public DisplayManager getDisplayManager() {
        return this.displayManager;
    }

    @NonNull
    public Map<Material, ShopBlock> getShopBlockMap() {
        return this.blockMap;
    }

    @Nullable
    public ShopBlock getShopBlock(@NonNull String name) {
        Material material = BukkitThing.getMaterial(name);
        return material == null ? null : this.getShopBlock(material);
    }

    @Nullable
    public ShopBlock getShopBlock(@NonNull Material material) {
        return this.blockMap.get(material);
    }

    public boolean isShopBlock(@NonNull Block block) {
        return this.isShopBlock(block.getType());
    }

    public boolean isShopBlock(@NonNull Material material) {
        return this.getShopBlock(material) != null;
    }

    @Override
    @NonNull
    public TransactionLogger getLogger() {
        return this.logger;
    }

    @NonNull
    public ShopView getShopView() {
        return this.shopView;
    }

    @Override
    public void openPurchaseOptionsDialog(@NonNull ProductClickContext context) {
        this.dialogRegistry.show(context.player(), PSDialogKeys.PRODUCT_PURCHASE_OPTIONS, context, null);
    }

    public void openShopSettings(@NonNull Player player, @NonNull ChestShop shop) {
        this.settingsMenu.open(player, shop);
    }

    public void openShowcaseMenu(@NonNull Player player, @NonNull ChestShop shop) {
        this.showcaseMenu.open(player, shop);
    }

    public void openProductsMenu(@NonNull Player player, @NonNull ChestShop shop) {
        this.productsMenu.open(player, shop);
    }

    public void openBankManagementDialog(@NonNull Player player, @NonNull Bank bank, @Nullable Runnable callback) {
        this.dialogRegistry.show(player, PSDialogKeys.BANK_MANAGEMENT, bank, callback);
    }

    public void openTrustedPlayers(@NonNull Player player, @NonNull ChestShop shop) {
        this.trustedPlayersMenu.show(player, shop);
    }

    public void openRentSettings(@NonNull Player player, @NonNull ChestShop shop) {
        if (this.rentMenu == null) return;

        this.rentMenu.open(player, shop);
    }

    public boolean openShop(@NonNull Player player, @NonNull ChestShop shop) {
        return this.openShop(player, shop, false);
    }

    public boolean openShop(@NonNull Player player, @NonNull ChestShop shop, int page) {
        return this.openShop(player, shop, page, false);
    }

    public boolean openShop(@NonNull Player player, @NonNull ChestShop shop, boolean force) {
        return this.openShop(player, shop, 1, force);
    }

    public boolean openShop(@NonNull Player player, @NonNull ChestShop shop, int page, boolean force) {
        if (!this.plugin.getDataManager().isLoaded()) return false;

        if (!force) {
            if (!shop.canAccess(player, true)) return false;
        }

        return this.shopView.show(player, shop); // No pages for chest shops
    }

    public void browseShopOwners(@NonNull Player player) {
        this.playerBrowserMenu.open(player);
    }

    public void browseAllShops(@NonNull Player player) {
        this.shopBrowserMenu.open(player);
    }

    public void browsePlayerShops(@NonNull Player player, @NonNull String ownerName) {
        this.shopBrowserMenu.openByPlayer(player, ownerName);
    }

    public void browseItemShops(@NonNull Player player, @NonNull String itemSearch) {
        this.shopBrowserMenu.openByItem(player, itemSearch);
    }

    public void browseShopsByShop(@NonNull Player player, @NonNull ChestShop source) {
        this.shopBrowserMenu.openFromShop(player, source);
    }

    @NonNull
    public List<String> formatProductLore(@NonNull ChestProduct product, @NonNull Player player) {
        return this.formatProductInfo(product, this.productFormatter, player);
    }

    @NonNull
    public List<String> formatProductLore(@NonNull ChestProduct product, @NonNull List<String> masterLore,
                                          @NonNull Player player) {
        return this.formatProductInfo(product, this.productFormatter, masterLore, player);
    }

    public boolean teleportToShop(@NonNull Player player, @NonNull ChestShop shop) {
        if (!shop.isAccessible()) {
            this.sendPrefixed(ChestLang.ERROR_SHOP_INACTIVE, player);
            return false;
        }

        Location location = shop.getTeleportLocation();

        if (ChestConfig.CHECK_SAFE_LOCATION.get()) {
            if (!shop.isOwner(player) && !ChestUtils.isSafeLocation(location)) {
                this.sendPrefixed(ChestLang.SHOP_TELEPORT_ERROR_UNSAFE, player);
                return false;
            }
        }

        return player.teleport(location);
    }

    @Deprecated
    public boolean renameShop(@NonNull Player player, @NonNull ChestShop shop, @NonNull String name) {
        if (!shop.isAccessible()) return false;

        String rawName = NightMessage.stripTags(name);
        int maxLength = ChestConfig.SHOP_MAX_NAME_LENGTH.get();

        if (rawName.length() > maxLength) {
            this.sendPrefixed(ChestLang.SHOP_RENAME_ERROR_LONG_NAME, player, builder -> builder
                .with(ShopPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(maxLength))
            );
            return false;
        }

        shop.setName(name);
        shop.markDirty();
        return true;
    }

    public void addTrustedPlayer(@NonNull Player player, @NonNull ChestShop shop, @NonNull String name,
                                 @Nullable Runnable callback) {
        this.userManager.loadByNameAsync(name).thenAcceptAsync(opt -> {
            if (opt.isEmpty()) {
                this.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, player);
                return;
            }

            ShopUser user = opt.get();

            if (shop.isOwner(user.getId())) {
                this.sendPrefixed(ChestLang.SHOP_ADD_TRUSTED_OWNER, player, builder -> builder
                    .with(CommonPlaceholders.PLAYER_NAME, user::getName)
                    .with(shop.placeholders())
                );
                return;
            }

            if (shop.isTrusted(user.getId())) {
                this.sendPrefixed(ChestLang.SHOP_ADD_TRUSTED_ALREADY_ADDED, player, builder -> builder
                    .with(CommonPlaceholders.PLAYER_NAME, user::getName)
                    .with(shop.placeholders())
                );
                return;
            }

            shop.addTrustedPlayer(UserInfo.of(user));
            shop.markDirty();
            if (callback != null) callback.run();

            this.sendPrefixed(ChestLang.SHOP_ADD_TRUSTED_DONE, player, builder -> builder
                .with(CommonPlaceholders.PLAYER_NAME, user::getName)
                .with(shop.placeholders())
            );
        }, this.plugin::runTask);
    }

    public void removeTrustedPlayer(@NonNull Player player, @NonNull ChestShop shop, @NonNull UUID trustedId,
                                    @Nullable Runnable callback) {
        this.userManager.loadByIdAsync(trustedId).thenAcceptAsync(opt -> {
            if (opt.isEmpty()) {
                this.sendPrefixed(CoreLang.ERROR_INVALID_PLAYER, player);
                return;
            }

            ShopUser user = opt.get();

            if (!shop.isTrusted(user.getId())) {
                this.sendPrefixed(ChestLang.SHOP_REMOVE_TRUSTED_NOT_ADDED, player, builder -> builder
                    .with(CommonPlaceholders.PLAYER_NAME, user::getName)
                    .with(shop.placeholders())
                );
                return;
            }

            shop.removeTrustedPlayer(user.getId());
            shop.markDirty();
            if (callback != null) callback.run();

            this.sendPrefixed(ChestLang.SHOP_REMOVE_TRUSTED_DONE, player, builder -> builder
                .with(CommonPlaceholders.PLAYER_NAME, user::getName)
                .with(shop.placeholders())
            );
        }, this.plugin::runTask);
    }

    @Override
    @NonNull
    protected CompletableFuture<Boolean> handleSuccessfulTransaction(@NonNull ECompletedTransaction transaction) {
        return this.transactionProcessor.queueTransaction(() -> {
            Player player = transaction.player();
            TradeType type = transaction.type();

            List<ETransactionItem> products = transaction.items();
            List<CompletableFuture<Boolean>> shopFutures = new ArrayList<>();
            List<Runnable> playerBalanceUpdates = new ArrayList<>();

            products.forEach(quantified -> {
                Product product = quantified.product();
                Shop shop = product.getShop();
                BalanceHolder price = quantified.price();

                price.getBalanceMap().forEach((currencyId, amount) -> {
                    Currency currency = EconomyBridge.api().getCurrency(currencyId);
                    if (currency == null) return;

                    if (!shop.isAdminShop()) {
                        CompletableFuture<Boolean> shopFuture = switch (type) {
                            case BUY -> shop.depositBalance(currency, amount);
                            case SELL -> shop.withdrawBalance(currency, amount);
                        };

                        shopFutures.add(shopFuture.exceptionally(throwable -> {
                            throwable.printStackTrace();
                            return false;
                        }));
                    }

                    Runnable runnable = switch (type) {
                        case BUY -> () -> currency.withdraw(player, amount);
                        case SELL -> () -> currency.deposit(player, amount);
                    };
                    playerBalanceUpdates.add(runnable);
                });
            });

            // Wait for all database tasks to finish
            CompletableFuture.allOf(shopFutures.toArray(new CompletableFuture[0])).join();

            // All data comitted successfully
            boolean allGood = shopFutures.stream().map(CompletableFuture::join).allMatch(Boolean.TRUE::equals);

            if (allGood) {
                playerBalanceUpdates.forEach(Runnable::run);
                return true;
            }

            return false;
        });
    }

    @Override
    protected void finishSuccessfulTransaction(@NonNull ECompletedTransaction transaction) {
        Player player = transaction.player();
        TradeType type = transaction.type();
        List<ETransactionItem> products = transaction.items();

        products.forEach(transactionItem -> {
            transactionItem.product().onSuccessfulTransaction(transaction, transactionItem.units());
        });

        if (!transaction.silent()) {
            MessageLocale feedbackLocale;
            MessageLocale notifyLocale;
            if (type == TradeType.BUY) {
                feedbackLocale = transaction
                    .isSingleItem() ? ChestLang.PRODUCT_PURCHASE_BUY_SINGLE : ChestLang.PRODUCT_PURCHASE_BUY_MULTIPLE;
                notifyLocale = ChestLang.PURCHASE_NOTIFY_BUY_SINGLE;
            }
            else {
                feedbackLocale = transaction
                    .isSingleItem() ? ChestLang.PRODUCT_PURCHASE_SELL_SINGLE : ChestLang.PRODUCT_PURCHASE_SELL_MULTIPLE;
                notifyLocale = ChestLang.PURCHASE_NOTIFY_SELL_SINGLE;
            }

            this.sendPrefixed(feedbackLocale, player, builder -> this.addTransactionPlaceholderContext(builder,
                transaction));

            products.forEach(transactionItem -> {
                Product product = transactionItem.product();
                Shop shop = product.getShop();
                if (!(shop instanceof ChestShop chestShop)) return;
                if (shop.isAdminShop()) return; // No notifications for admin shops.

                chestShop.getEffectiveMerchant().ifPresent(merchant -> {
                    this.sendPrefixed(notifyLocale, merchant, builder -> this.addTransactionPlaceholderContext(builder,
                        transaction)
                        .with(CommonPlaceholders.PLAYER.resolver(player))
                        .with(shop.placeholders())
                    );
                });
            });
        }

        this.logger.logTransaction(transaction);
    }

    public void handleWorldLoad(@NonNull World world) {
        for (Chunk chunk : world.getLoadedChunks()) {
            this.handleChunkLoad(chunk);
        }
    }

    public void handleWorldUnload(@NonNull World world) {
        for (Chunk chunk : world.getLoadedChunks()) {
            this.handleChunkUnload(chunk);
        }
    }

    public void handleChunkLoad(@NonNull Chunk chunk) {
        this.lookup.getAll(chunk).forEach(shop -> {
            try {
                shop.activate(chunk);

                World world = chunk.getWorld();
                BlockPos blockPos = shop.getBlockPos();
                Block block = shop.getBlock();
                Material blockType = block.getType();

                if (!this.isShopBlock(blockType) && !ChestUtils.isContainer(blockType)) {
                    this.warn("Shop at '" + blockPos + "' in '" + world.getName() + "' is in illegal block.");
                    this.removeShop(shop);
                    return;
                }

                this.splitShop(shop);
                shop.updateStockCache();
            }
            catch (IllegalStateException exception) {
                this.removeShop(shop);
                this.plugin.warn("Shop not loaded: " + exception.getMessage());
            }
        });
    }

    public void handleChunkUnload(@NonNull Chunk chunk) {
        this.lookup.getAll(chunk).forEach(ChestShop::deactivate2);
    }

    public void handleInteractEvent(@NonNull PlayerInteractEvent event) {
        // TODO handle interaction with item frames attached to shops
        // TODO Create shops using signs
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        ChestShop blockShop = this.getShop(block);
        if (blockShop != null) {
            this.interactShop(event, player, blockShop);
            return;
        }

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

    private void interactShop(@NonNull PlayerInteractEvent event, @NonNull Player player, @NonNull ChestShop shop) {
        // Added to avoid "interaction blocked" messages from protection plugins.
        event.setCancelled(true);

        if (player.isSneaking()) {
            ItemStack item = event.getItem();
            if (item != null && ChestUtils.isShopDecor(item)) {
                if (!shop.canDecorate(player)) {
                    this.sendPrefixed(ChestLang.SHOP_ERROR_NOT_OWNER, player);
                    event.setUseInteractedBlock(Event.Result.DENY);
                }
                return;
            }

            if (shop.canManage(player)) {
                this.openShopSettings(player, shop);
            }
            else {
                this.sendPrefixed(ChestLang.SHOP_ERROR_NOT_OWNER, player);
            }
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        if (shop.canManage(player)) {
            event.setUseInteractedBlock(Event.Result.DENY);
            if (shop.isAdminShop() || ChestUtils.isInfiniteStorage()) {
                this.openShopSettings(player, shop);
            }
            else {
                shop.getInventory().ifPresent(player::openInventory);
            }
            return;
        }

        if (shop.isAdminShop() || !shop.canManage(player)) {
            event.setUseInteractedBlock(Event.Result.DENY);

            if (shop.isRentable() && !shop.isRented()) {
                // TODO Dialog
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

    public boolean checkShopCreation(@NonNull Player player, @NonNull Block block) {
        if (this.isShop(block)) {
            this.sendPrefixed(ChestLang.SHOP_CREATION_ERROR_ALREADY_SHOP, player);
            return false;
        }

        if (!this.checkCreationLocation(player, block)) {
            this.sendPrefixed(ChestLang.SHOP_CREATION_ERROR_BAD_LOCATION, player);
            return false;
        }

        if (!this.checkCreationClaim(player, block)) {
            this.sendPrefixed(ChestLang.SHOP_CREATION_ERROR_BAD_AREA, player);
            return false;
        }

        int shopLimit = ChestUtils.getShopLimit(player);
        int shopAmount = this.countShops(player);
        if (shopLimit >= 0 && shopAmount >= shopLimit) {
            this.sendPrefixed(ChestLang.SHOP_CREATION_ERROR_LIMIT_REACHED, player);
            return false;
        }

        if (!this.canPayCreation(player)) {
            this.sendPrefixed(ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS, player);
            return false;
        }

        return true;
    }

    public boolean createShopFromItem(@NonNull Player player, @NonNull Block block, @NonNull ItemStack itemStack) {
        Material material = ChestUtils.getShopItemType(itemStack);
        if (material == null) return false;

        if (!this.isShopBlock(material)) {
            this.sendPrefixed(ChestLang.SHOP_CREATION_ERROR_BAD_BLOCK, player);
            return false;
        }

        if (!this.checkShopCreation(player, block)) return false;

        ChestShopCreateEvent event = new ChestShopCreateEvent(player, block, itemStack);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        BlockData blockData = material.createBlockData();
        if (blockData instanceof Directional directional) {
            if (blockData instanceof org.bukkit.block.data.type.Chest) {
                BlockFace face = EntityUtil.getDirection(player);
                if (face != null) {
                    directional.setFacing(face.getOppositeFace());
                }
            }
            else {
                directional.setFacing(BlockFace.UP);
            }
        }

        block.getWorld().setBlockData(block.getLocation(), blockData);

        this.createShop(player, block, shop -> {
            shop.setItemCreated(true);
        });

        itemStack.setAmount(itemStack.getAmount() - 1);

        return true;
    }

    public boolean createShopNaturally(@NonNull Player player, @NonNull Block block) {
        return this.createShopNaturally(player, block, -1, -1);
    }

    public boolean createShopNaturally(@NonNull Player player, @NonNull Block block, double buyPrice,
                                       double sellPrice) {
        if (!this.isShopBlock(block)) {
            this.sendPrefixed(ChestLang.SHOP_CREATION_ERROR_BAD_BLOCK, player);
            return false;
        }

        if (!this.checkShopCreation(player, block)) return false;

        BlockInventoryHolder container = (BlockInventoryHolder) block.getState();
        Inventory inventory = container.getInventory();
        if (Stream.of(inventory.getContents()).anyMatch(inside -> inside != null && !inside.getType().isAir())) {
            this.sendPrefixed(ChestLang.SHOP_CREATION_ERROR_NOT_EMPTY, player);
            return false;
        }

        ChestShopCreateEvent event = new ChestShopCreateEvent(player, block);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        this.payForCreate(player);

        this.createShop(player, block, shop -> {
            ItemStack hand = new ItemStack(player.getInventory().getItemInMainHand());
            if (!ChestUtils.isShopItem(hand) && !hand.getType().isAir() && !shop.isProduct(hand) && ChestUtils
                .isAllowedItem(hand)) {
                ChestProduct product = shop.createProduct(player, hand, false);
                product.setPricing(FlatPricing.of(ChestUtils.clampPrice(buyPrice), ChestUtils.clampPrice(sellPrice)));
                product.updatePrice(false);
            }
        });

        return true;
    }

    @NonNull
    public ChestShop createShop(@NonNull Player player, @NonNull Block block, @NonNull Consumer<ChestShop> consumer) {
        String id = ChestUtils.generateShopId(player, block.getLocation());
        Path file = this.getPath().resolve(CSFiles.DIR_SHOPS).resolve(id + ".yml");
        FileUtil.createFileIfNotExists(file);
        ChestShop shop = new ChestShop(this.plugin, this, file, id);
        World world = block.getWorld();

        shop.setLocation(world, block.getLocation());
        shop.setAdminShop(player.hasPermission(ChestPerms.ADMIN_SHOP));
        shop.setOwner(player);
        shop.setName(ShopPlaceholders.forPlayer(player).apply(ChestConfig.DEFAULT_NAME.get()));
        shop.setHologramEnabled(true);
        shop.setShowcaseEnabled(true);
        shop.setShowcaseId(null);
        shop.setBuyingAllowed(true);
        shop.setSellingAllowed(true);
        if (ChestConfig.isRentEnabled()) {
            shop.setRentSettings(new RentSettings(false, 7, this.getDefaultCurrency().getInternalId(), 1000));
        }

        consumer.accept(shop);
        shop.markDirty();
        shop.activate(block.getChunk());

        this.lookup.put(shop);

        this.sendPrefixed(ChestLang.SHOP_CREATION_INFO_DONE, player);
        return shop;
    }

    public boolean rentShopOrExtend(@NonNull Player player, @NonNull ChestShop shop) {
        if (!ChestConfig.isRentEnabled()) return false;

        if (!shop.isAccessible()) {
            this.sendPrefixed(ChestLang.ERROR_SHOP_INACTIVE, player, builder -> builder.with(shop.placeholders()));
            return false;
        }

        RentSettings settings = shop.getRentSettings();
        if (!shop.isRentable() || !settings.isValid()) {
            this.sendPrefixed(ChestLang.RENT_ERROR_NOT_RENTABLE, player, builder -> builder.with(shop.placeholders()));
            return false;
        }

        boolean isExtend = false;

        if (shop.isRented()) {
            if (!shop.isRenter(player)) {
                this.sendPrefixed(ChestLang.RENT_ERROR_ALREADY_RENTED, player, builder -> builder.with(shop
                    .placeholders()));
                return false;
            }
            isExtend = true;
        }

        String currencyId = settings.getCurrencyId();
        double price = settings.getPrice();
        if (EconomyBridge.api().queryBalance(player, currencyId) < price) {
            this.sendPrefixed(ChestLang.RENT_ERROR_INSUFFICIENT_FUNDS, player, builder -> builder
                .with(ShopPlaceholders.GENERIC_PRICE, settings::getPriceFormatted)
                .with(shop.placeholders())
            );
            return false;
        }

        EconomyBridge.api().withdraw(player, currencyId, price);
        EconomyBridge.api().deposit(shop.getOwnerId(), currencyId, price);
        shop.setRentedBy(player);
        shop.extendRent();
        shop.markDirty();

        this.sendPrefixed(isExtend ? ChestLang.RENT_EXTEND_SUCCESS : ChestLang.RENT_RENT_SUCCESS, player,
            builder -> builder
                .with(ShopPlaceholders.GENERIC_TIME, () -> TimeFormats.toLiteral(settings.getDurationMillis()))
                .with(ShopPlaceholders.GENERIC_PRICE, settings::getPriceFormatted)
                .with(shop.placeholders())
        );
        return true;
    }

    public boolean cancelRent(@NonNull Player player, @NonNull ChestShop shop) {
        if (!shop.isRented()) return false;

        boolean isRenter = shop.isRenter(player);

        this.sendPrefixed((isRenter ? ChestLang.RENT_CANCEL_BY_RENTER : ChestLang.RENT_CANCEL_BY_OWNER), player,
            builder -> builder
                .with(shop.placeholders())
        );

        // TODO Notify renter

        shop.cancelRent();
        shop.markDirty();
        return true;
    }

    public boolean canBreak(@NonNull Player player, @NonNull ChestShop shop) {
        if (!player.hasPermission(ChestPerms.REMOVE)) {
            this.sendPrefixed(CoreLang.ERROR_NO_PERMISSION, player);
            return false;
        }

        if (!shop.isOwner(player) && !player.hasPermission(ChestPerms.REMOVE_OTHERS)) {
            this.sendPrefixed(ChestLang.SHOP_ERROR_NOT_OWNER, player);
            return false;
        }

        return true;
    }

    public boolean deleteShop(@NonNull Player player, @NonNull Block block) {
        ChestShop shop = this.getShop(block);
        if (shop == null) {
            this.sendPrefixed(ChestLang.ERROR_BLOCK_IS_NOT_SHOP, player);
            return false;
        }

        return this.deleteShop(player, shop);
    }

    public boolean deleteShop(@NonNull Player player, @NonNull ChestShop shop) {
        if (!this.canBreak(player, shop)) return false;

        if (!this.payForRemoval(player)) {
            this.sendPrefixed(ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS, player);
            return false;
        }

        if (ChestUtils.isInfiniteStorage()) {
            if (shop.getValidProducts().stream().anyMatch(product -> product.getStock() > 0)) {
                this.sendPrefixed(ChestLang.SHOP_REMOVAL_ERROR_NOT_EMPTY, player);
                return false;
            }
        }

        ChestShopRemoveEvent event = new ChestShopRemoveEvent(player, shop);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        if (shop.isItemCreated() && shop.isAccessible()) {
            Block block = shop.getBlock();
            ShopBlock shopBlock = this.getShopBlock(block.getType());

            if (shopBlock != null) {
                Location dropLocation = LocationUtil.setCenter3D(block.getLocation().clone());
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(dropLocation, shopBlock.getItemStack());
            }
        }

        this.removeShop(shop);

        this.sendPrefixed(ChestLang.SHOP_REMOVAL_INFO_DONE, player);
        return true;
    }

    @NonNull
    public CompletableFuture<Double> queryShopBalance(@NonNull ChestShop shop, @NonNull Currency currency) {
        UserInfo profile = shop.getEffectiveMerchantProfile();
        Player player = Players.getPlayer(profile.id());

        Bank bank = this.getEffectiveBank(shop);
        if (bank == null && this.settings.isBankMandatory()) return CompletableFuture.completedFuture(-1D);
        if (bank != null) {
            return CompletableFuture.completedFuture(bank.getAccount().query(currency));
        }

        if (player != null) {
            return CompletableFuture.completedFuture(currency.queryBalance(player));
        }

        if (!currency.canHandleOffline()) {
            return CompletableFuture.completedFuture(-1D);
        }

        return currency.queryBalanceAsync(profile.id());
    }

    @NonNull
    public CompletableFuture<Boolean> depositShopBalance(@NonNull ChestShop shop, @NonNull Currency currency,
                                                         double amount) {
        UserInfo profile = shop.getEffectiveMerchantProfile();
        Player player = Players.getPlayer(profile.id());

        Bank bank = this.getEffectiveBank(shop);
        if (bank == null && this.settings.isBankMandatory()) return CompletableFuture.completedFuture(false);
        if (bank != null) {
            bank.getAccount().store(currency, amount);
            bank.markDirty();
            return CompletableFuture.completedFuture(true);
        }

        if (player != null) {
            return currency.depositAsync(player, amount);
        }

        return currency.depositAsync(profile.id(), amount);
    }

    @NonNull
    public CompletableFuture<Boolean> withdrawShopBalance(@NonNull ChestShop shop, @NonNull Currency currency,
                                                          double amount) {
        UserInfo profile = shop.getEffectiveMerchantProfile();
        Player player = Players.getPlayer(profile.id());

        Bank bank = this.getEffectiveBank(shop);
        if (bank == null && this.settings.isBankMandatory()) return CompletableFuture.completedFuture(false);
        if (bank != null) {
            bank.getAccount().remove(currency, amount);
            bank.markDirty();
            return CompletableFuture.completedFuture(true);
        }

        if (player != null) {
            return currency.withdrawAsync(player, amount);
        }

        return currency.withdrawAsync(profile.id(), amount);
    }

    @Nullable
    public Bank getEffectiveBank(@NonNull ChestShop shop) {
        if (this.bankManager == null) return null;

        UserInfo profile = shop.getEffectiveMerchantProfile();

        return this.bankManager.getBankById(profile.id());
    }

    public boolean depositToShop(@NonNull Player player, @NonNull ChestProduct product, int units) {
        if (units == 0) return false;

        ChestShop shop = product.getShop();
        if (!shop.isAccessible()) return false;
        if (shop.isAdminShop()) return false;

        int playerUnits = product.countUnits(player);
        if (playerUnits < units) {
            this.sendPrefixed(ChestLang.STORAGE_DEPOSIT_ERROR_NOT_ENOUGH, player);
            return false;
        }

        int maxUnits = product.getSpace();
        int finalUnits = maxUnits < 0 ? playerUnits : Math.min(maxUnits, playerUnits);

        product.getStockData().store(finalUnits);

        if (finalUnits > 0) {
            product.take(player, finalUnits);
        }

        shop.markDirty();

        this.sendPrefixed(ChestLang.STORAGE_DEPOSIT_SUCCESS, player, builder -> builder
            .with(ShopPlaceholders.GENERIC_AMOUNT, () -> NumberUtil.format(finalUnits))
            .with(ShopPlaceholders.GENERIC_ITEM, () -> ItemUtil.getNameSerialized(product.getEffectivePreview()))
        );
        return true;
    }

    public boolean withdrawFromShop(@NonNull Player player, @NonNull ChestProduct product, int units) {
        if (units == 0) return false;

        ChestShop shop = product.getShop();
        if (!shop.isAccessible()) return false;
        if (shop.isAdminShop()) return false;

        int productStock = product.getStock();
        if (productStock < units) {
            this.sendPrefixed(ChestLang.STORAGE_WITHDRAW_ERROR_NOT_ENOUGH, player);
            return false;
        }

        int inventorySpace = product.countUnits(product.countSpace(player));
        int maxUnits = Math.min(inventorySpace, units);
        if (maxUnits < 0) return false;

        product.delivery(player, maxUnits);
        product.getStockData().consume(maxUnits);
        shop.markDirty();

        this.sendPrefixed(ChestLang.STORAGE_WITHDRAW_SUCCESS, player, builder -> builder
            .with(ShopPlaceholders.GENERIC_AMOUNT, () -> NumberUtil.format(maxUnits))
            .with(ShopPlaceholders.GENERIC_ITEM, () -> ItemUtil.getNameSerialized(product.getEffectivePreview()))
        );
        return true;
    }

    public int countShops(@NonNull Player player) {
        return this.lookup.getOwnedBy(player.getUniqueId()).size();
    }

    @Override
    @NonNull
    public Set<? extends PlayerShop> getShopsInArea(@NonNull World world, @NonNull Cuboid cuboid) {
        return this.lookup.worldLookup(world).map(worldLookup -> worldLookup.getAllIn(cuboid)).orElse(Collections
            .emptySet());
    }

    @Override
    public Set<ChestShop> getShops(@NonNull Player player) {
        return this.lookup.getAll().stream().filter(shop -> shop.isAccessible() && shop.canAccess(player, false))
            .collect(Collectors.toSet());
    }

    @Nullable
    public ChestShop getShop(@NonNull Block block) {
        return this.lookup.getAt(block);
    }

    @Nullable
    public ChestShop getShop(@NonNull Location location) {
        return this.lookup.getAt(location);
    }

    @Override
    public boolean isShop(@NonNull Block block) {
        return this.isShop(block.getLocation());
    }

    public boolean isShop(@NonNull Location location) {
        return this.getShop(location) != null;
    }

    public boolean checkCreationLocation(@NonNull Player player, @NonNull Block block) {
        if (ChestConfig.SHOP_CREATION_WORLD_BLACKLIST.get().contains(block.getWorld().getName())) {
            return false;
        }

        if (ChestConfig.SHOP_CREATION_CHECK_BUILD.get()) {
            Block placed = block.getRelative(BlockFace.UP);
            ItemStack item = new ItemStack(Material.CHEST);
            BlockPlaceEvent event = new BlockPlaceEvent(placed, placed
                .getState(), block, item, player, true, EquipmentSlot.HAND);
            plugin.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
        }

        if (Plugins.isInstalled(HookPlugin.WORLD_GUARD)) {
            return WorldGuardFlags.checkFlag(player, block.getLocation());
        }
        return true;
    }

    public boolean checkCreationClaim(@NonNull Player player, @NonNull Block block) {
        if (!ChestConfig.SHOP_CREATION_CLAIM_ONLY.get()) return true;
        if (player.hasPermission(ChestPerms.BYPASS_CREATION_CLAIMS)) return true;

        return this.claimHooks.isEmpty() || this.claimHooks.stream().anyMatch(claim -> claim.isInOwnClaim(player,
            block));
    }

    public boolean canPayCreation(@NonNull Player player) {
        return this.canPayForShop(player, ChestConfig.SHOP_CREATION_COST_CREATE.get());
    }

    private boolean payForCreate(@NonNull Player player) {
        return this.payForShop(player, ChestConfig.SHOP_CREATION_COST_CREATE.get());
    }

    private boolean payForRemoval(@NonNull Player player) {
        return this.payForShop(player, ChestConfig.SHOP_CREATION_COST_REMOVE.get());
    }

    private boolean canPayForShop(@NonNull Player player, double price) {
        if (price <= 0) return true;

        return this.getDefaultCurrency().queryBalance(player) >= price;
    }

    private boolean payForShop(@NonNull Player player, double price) {
        if (price <= 0) return true;

        Currency currency = this.getDefaultCurrency();

        double balance = currency.queryBalance(player);
        if (balance < price) return false;

        currency.withdraw(player, price);
        return true;
    }

    @Nullable
    public BankManager getBankManager() {
        return this.bankManager;
    }
}
