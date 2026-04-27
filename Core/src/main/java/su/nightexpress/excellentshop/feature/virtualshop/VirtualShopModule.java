package su.nightexpress.excellentshop.feature.virtualshop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.product.TradeType;
import su.nightexpress.excellentshop.api.product.limit.LimitData;
import su.nightexpress.excellentshop.api.product.stock.StockData;
import su.nightexpress.excellentshop.api.transaction.ECompletedTransaction;
import su.nightexpress.excellentshop.api.transaction.ERawTransaction;
import su.nightexpress.excellentshop.feature.virtualshop.command.VirtualCommands;
import su.nightexpress.excellentshop.feature.virtualshop.core.*;
import su.nightexpress.excellentshop.feature.virtualshop.dialog.VSDialogKeys;
import su.nightexpress.excellentshop.feature.virtualshop.dialog.product.*;
import su.nightexpress.excellentshop.feature.virtualshop.dialog.shop.*;
import su.nightexpress.excellentshop.feature.virtualshop.listener.VirtualShopListener;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.product.editor.ProductOptionsMenu;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.Rotation;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.RotationItem;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.RotationType;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.data.RotationData;
import su.nightexpress.excellentshop.feature.virtualshop.rotation.editor.*;
import su.nightexpress.excellentshop.feature.virtualshop.shop.VirtualShop;
import su.nightexpress.excellentshop.feature.virtualshop.shop.editor.ShopListMenu;
import su.nightexpress.excellentshop.feature.virtualshop.shop.editor.ShopOptionsMenu;
import su.nightexpress.excellentshop.feature.virtualshop.shop.menu.CentralMenu;
import su.nightexpress.excellentshop.feature.virtualshop.shop.menu.ShopMenu;
import su.nightexpress.excellentshop.feature.virtualshop.shop.menu.ViewMode;
import su.nightexpress.excellentshop.product.PriceType;
import su.nightexpress.excellentshop.product.click.ProductClickContext;
import su.nightexpress.excellentshop.shop.AbstractShop;
import su.nightexpress.excellentshop.shop.TransactionLogger;
import su.nightexpress.excellentshop.shop.dialog.impl.ProductPurchaseOptionsDialog;
import su.nightexpress.excellentshop.shop.formatter.ProductFormatter;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.core.Keys;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.nexshop.exception.ShopLoadException;
import su.nightexpress.nexshop.module.AbstractShopModule;
import su.nightexpress.nexshop.module.ModuleContext;
import su.nightexpress.excellentshop.shop.ShopManager;
import su.nightexpress.excellentshop.shop.TransactionProcessor;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.integration.currency.EconomyBridge;
import su.nightexpress.nightcore.locale.entry.MessageLocale;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;
import su.nightexpress.nightcore.util.time.TimeFormatType;
import su.nightexpress.nightcore.util.time.TimeFormats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VirtualShopModule extends AbstractShopModule {

    private final VirtualSettings settings;
    private final VirtualCommands commands;

    private final TransactionProcessor             transactionProcessor;
    private final ProductFormatter<VirtualProduct> productFormatter;

    private final Map<String, ShopMenu>     layoutByIdMap;
    private final Map<String, VirtualShop>  shopByIdMap;
    private final Map<UUID, VirtualProduct> productCache; // Product cache for product editors.

    private CentralMenu centralMenu;

    private RotatingProductsMenu   rotatingProductsMenu;
    private ProductOptionsMenu     productOptionsMenu;
    private ShopListMenu           shopListMenu;
    private ShopOptionsMenu        shopOptionsMenu;
    private RotationListMenu       rotationListMenu;
    private RotationOptionsMenu    rotationOptionsMenu;
    private RotationItemsListMenu  rotationItemsListMenu;
    private RotationItemSelectMenu rotationItemSelectMenu;
    private RotationTimesMenu      rotationTimesMenu;

    private TransactionLogger logger;

    public VirtualShopModule(@NonNull ModuleContext context, @NonNull ShopManager shopManager,
                             @NonNull TransactionProcessor transactionProcessor) {
        super(context, shopManager);
        this.transactionProcessor = transactionProcessor;
        this.productFormatter = new ProductFormatter<>();

        this.settings = new VirtualSettings();
        this.commands = new VirtualCommands(this.plugin, this);

        this.layoutByIdMap = new HashMap<>();
        this.shopByIdMap = new HashMap<>();
        this.productCache = new HashMap<>();
    }

    @Override
    protected void loadModule(@NonNull FileConfig config) {
        config.initializeOptions(VirtualConfig.class);

        this.plugin.injectLang(VirtualLang.class);
        this.plugin.injectLang(VirtualIconsLang.class);

        this.plugin.registerPermissions(VirtualPerms.class);
        this.logger = new TransactionLogger(this, config);

        this.loadFormatter();
        this.settings.load(config);
        this.loadEditors();
        this.loadLayouts();
        this.loadUI();
        this.loadShops();

        if (VirtualConfig.isCentralMenuEnabled()) {
            this.loadMainMenu();
        }

        this.loadDialogs();

        this.addListener(new VirtualShopListener(this.plugin, this));

        this.addAsyncTask(this::saveDirtyShops, VirtualConfig.SAVE_INTERVAL.get());
    }

    @Override
    protected void disableModule() {
        this.saveDirtyShops();

        this.getShops().forEach(this::unloadShopAliases);

        if (this.centralMenu != null) {
            this.centralMenu.clear();
            this.centralMenu = null; // Main menu is toggleable.
        }

        this.layoutByIdMap.clear();
        this.shopByIdMap.clear();
        this.productCache.clear();
        this.commands.unload();
    }

    @Override
    protected void loadCommands(@NonNull HubNodeBuilder builder) {
        this.commands.load(builder);
    }

    private void loadFormatter() {
        this.productFormatter.registerCondition("buyable", (product, player) -> product.isBuyable());
        this.productFormatter.registerCondition("sellable", (product, player) -> product.isSellable());
        this.productFormatter.registerCondition("dynamic_price", (product, player) -> product
            .getPricingType() != PriceType.FLAT);
        this.productFormatter.registerCondition("has_stock", (product, player) -> product.getStockOptions()
            .isEnabled());
        this.productFormatter.registerCondition("stock_resettable", (product, player) -> product.getStockOptions()
            .isEnabled() && product.getStockOptions().hasRestockTime());
        this.productFormatter.registerCondition("has_limits", (product, player) -> product.getLimitOptions()
            .isEnabled() && product.getLimitOptions().hasLimits());
        this.productFormatter.registerCondition("has_buy_limit", (product, player) -> product.getLimitOptions()
            .isEnabled() && product.getLimitOptions().hasBuyLimit());
        this.productFormatter.registerCondition("has_sell_limit", (product, player) -> product.getLimitOptions()
            .isEnabled() && product.getLimitOptions().hasSellLimit());
        this.productFormatter.registerCondition("limit_resettable", (product, player) -> product.getLimitOptions()
            .isEnabled() && product.getLimitOptions().hasResetTime());

        this.productFormatter.registerVariable("stock_reset_time", (product, player) -> {
            StockData data = product.getStockData();
            long resetDate = data.getRestockDate();

            return resetDate < 0L ? CoreLang.OTHER_NEVER.text() : TimeFormats.formatDuration(resetDate,
                TimeFormatType.LITERAL);
        });

        this.productFormatter.registerVariable("limit_reset_time", (product, player) -> {
            LimitData data = product.getLimitData(player);
            if (!data.isActive()) {
                long resetDate = TimeUnit.SECONDS.toMillis(product.getLimitOptions().getRestockTime());
                return resetDate < 0L ? CoreLang.OTHER_NEVER.text() : TimeFormats.formatAmount(resetDate,
                    TimeFormatType.LITERAL);
            }

            long resetDate = data.getRestockDate();
            return resetDate < 0L ? CoreLang.OTHER_NEVER.text() : TimeFormats.formatDuration(resetDate,
                TimeFormatType.LITERAL);
        });

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

            this.productFormatter.registerVariable(name + "_price_trend", (product, player) -> {
                if (product.getPricingType() == PriceType.FLAT) return Lang.OTHER_N_A.text();

                String format;
                double trending = product.getPriceTrending(tradeType);

                if (trending >= 0) {
                    format = this.settings.getProductDisplayComponentPriceTrendUp();
                }
                else {
                    format = this.settings.getProductDisplayComponentPriceTrendDown();
                }

                return PlaceholderContext.builder()
                    .with(ShopPlaceholders.GENERIC_TREND, () -> NumberUtil.format(Math.abs(trending * 100D)))
                    .build().apply(format);
            });

            this.productFormatter.registerVariable(name + "_limit_current", (product, player) -> {
                LimitData data = product.getLimitData(player);
                return NumberUtil.format(data.getTrades(tradeType));
            });
        }
    }

    private void loadEditors() {
        this.rotatingProductsMenu = this.initMenu(new RotatingProductsMenu(this.plugin, this));
        this.productOptionsMenu = this.initMenu(new ProductOptionsMenu(this.plugin, this));

        this.shopListMenu = this.addMenu(new ShopListMenu(this.plugin, this));
        this.shopOptionsMenu = this.addMenu(new ShopOptionsMenu(this.plugin, this));

        this.rotationListMenu = this.addMenu(new RotationListMenu(this.plugin, this));
        this.rotationOptionsMenu = this.addMenu(new RotationOptionsMenu(this.plugin, this));
        this.rotationTimesMenu = this.addMenu(new RotationTimesMenu(this.plugin, this));
        this.rotationItemsListMenu = this.addMenu(new RotationItemsListMenu(this.plugin, this));
        this.rotationItemSelectMenu = this.addMenu(new RotationItemSelectMenu(this.plugin, this));
    }

    private void updateRotatingShops() {
        for (File folder : FileUtil.getFolders(this.getPath() + "/rotating_shops/")) {
            String id = folder.getName();
            File file = new File(folder.getAbsolutePath(), "config.yml");
            if (!file.exists()) continue;

            File dir = new File(this.getPath().resolve(VSFiles.DIR_SHOPS).toString(), id);
            if (dir.exists()) {
                this.error("Could not migrate rotating shop '" + id + "': Shop with such name already exists.");
                continue;
            }

            dir.mkdirs();

            VirtualShop shop = new VirtualShop(this.plugin, this, this.dataManager, file.toPath(), id);
            try {
                shop.load();
            }
            catch (ShopLoadException e) {
                e.printStackTrace();
                return;
            }

            FileConfig config = shop.loadConfig();
            FileConfig itemsConfig = new FileConfig(config.getFile().getParentFile().getAbsolutePath(), "products.yml");
            Set<Integer> slots = IntStream.of(config.getIntArray("Rotation.Products.Slots")).boxed().collect(Collectors
                .toSet());

            Rotation rotation = new Rotation(UUID.randomUUID(), ShopPlaceholders.DEFAULT, shop);
            rotation.setRotationType(config.getEnum("Rotation.Type", RotationType.class, RotationType.INTERVAL));
            rotation.setRotationInterval(config.getInt("Rotation.Interval", 86400));
            rotation.setSlots(slots, 1);

            for (String sDay : config.getSection("Rotation.Fixed")) {
                DayOfWeek day = Enums.get(sDay, DayOfWeek.class);
                if (day == null) continue;

                TreeSet<LocalTime> times = new TreeSet<>(ShopUtils.parseTimes(config.getStringList("Rotation.Fixed." +
                    sDay)));
                rotation.getRotationTimes().put(day, times);
            }

            shop.getProducts().forEach(product -> {
                double weight = itemsConfig.getDouble("List." + product.getId() + ".Rotation.Chance");

                product.setRotating(true);
                rotation.addItem(new RotationItem(product.getId(), weight));
            });

            shop.addRotation(rotation);
            shop.saveForce();

            config.getFile().renameTo(new File(dir.getAbsolutePath(), "config.yml"));
            itemsConfig.getFile().renameTo(new File(dir.getAbsolutePath(), "products.yml"));

            FileUtil.deleteRecursive(config.getFile().getParentFile());
        }
    }

    private void updateShopsFolder() {
        String shopsPath = this.getPath().resolve(VSFiles.DIR_SHOPS).toString();

        for (File folder : FileUtil.getFolders(shopsPath)) {
            String id = folder.getName();

            File newFile = new File(shopsPath, FileConfig.withExtension(id));
            if (newFile.exists()) continue;

            FileConfig newConfig = new FileConfig(newFile);
            File configFile = new File(folder.getAbsolutePath(), "config.yml");
            File productsFile = new File(folder.getAbsolutePath(), "products.yml");

            Path configBackupPath = Path.of(shopsPath, id + "_config.backup");
            Path productsBackupPath = Path.of(shopsPath, id + "_products.backup");

            FileConfig config = new FileConfig(configFile);
            FileConfig productsConfig = new FileConfig(productsFile);

            VirtualShop shop = new VirtualShop(this.plugin, this, this.dataManager, configFile.toPath(), id);
            shop.loadSettings(config, "");
            shop.loadProducts(productsConfig, "List");
            shop.loadRotations(config, "Rotations");
            shop.write(newConfig);

            try {
                if (!Files.exists(configBackupPath)) {
                    Files.move(configFile.toPath(), configBackupPath);
                }
                if (!Files.exists(productsBackupPath)) {
                    Files.move(productsFile.toPath(), productsBackupPath);
                }
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private void loadShops() {
        Path dir = this.getPath().resolve(VSFiles.DIR_SHOPS);
        if (!Files.exists(dir)) {
            VSDefaults.createDefaultShops(this);
        }

        this.updateRotatingShops();
        this.updateShopsFolder();

        FileUtil.findYamlFiles(dir).forEach(file -> {
            String id = FileUtil.getNameWithoutExtension(file);
            VirtualShop shop = new VirtualShop(this.plugin, this, this.dataManager, file, id);
            this.loadShop(shop);
        });

        this.info("Loaded %s shops.".formatted(this.shopByIdMap.size()));

        this.printShops();
    }

    private void loadShop(@NonNull VirtualShop shop) {
        try {
            shop.load();
            this.loadShopAliases(shop);
            this.shopByIdMap.put(shop.getId(), shop);
        }
        catch (ShopLoadException exception) {
            if (exception.isFatal()) exception.printStackTrace();
            this.error("Shop not loaded: '" + shop.getPath() + "'.");
        }
    }

    private void loadUI() {

    }

    private void loadDialogs() {
        this.dialogRegistry.register(VSDialogKeys.PURCHASE_OPTIONS, new ProductPurchaseOptionsDialog(this));

        this.dialogRegistry.register(VSDialogKeys.SHOP_CREATION, ShopCreationDialog::new);
        this.dialogRegistry.register(VSDialogKeys.SHOP_ALIASES, () -> new ShopAliasesDialog(this));
        this.dialogRegistry.register(VSDialogKeys.SHOP_DESCRIPTION, ShopDescriptionDialog::new);
        this.dialogRegistry.register(VSDialogKeys.SHOP_PAGE_LAYOUTS, () -> new ShopLayoutsDialog(this));
        if (this.centralMenu != null) {
            this.dialogRegistry.register(VSDialogKeys.SHOP_MENU_SLOTS,
                () -> new ShopMenuSlotsDialog(this, this.centralMenu));
        }
        this.dialogRegistry.register(VSDialogKeys.SHOP_NAME, ShopNameDialog::new);
        this.dialogRegistry.register(VSDialogKeys.SHOP_PAGES, ShopPagesDialog::new);

        this.dialogRegistry.register(VSDialogKeys.PRODUCT_CONTENT_TYPE, () -> new ProductTypeDialog(this));
        this.dialogRegistry.register(VSDialogKeys.PRODUCT_COMMANDS, ProductCommandsDialog::new);
        this.dialogRegistry.register(VSDialogKeys.PRODUCT_PRICE, () -> new ProductPriceDialog(this));
        this.dialogRegistry.register(VSDialogKeys.PRODUCT_CURRENCY, () -> new ProductCurrencyDialog(this));
        this.dialogRegistry.register(VSDialogKeys.PRODUCT_FLOAT_PRICE_TIMINGS,
            () -> new ProductFloatPriceTimesDialog(this));
        this.dialogRegistry.register(VSDialogKeys.PRODUCT_STOCKS, ProductStocksDialog::new);
        this.dialogRegistry.register(VSDialogKeys.PRODUCT_LIMITS, ProductLimitsDialog::new);
        this.dialogRegistry.register(VSDialogKeys.PRODUCT_RANK_REQUIREMENTS, ProductRanksDialog::new);
        this.dialogRegistry.register(VSDialogKeys.PRODUCT_PERMISSION_REQUIREMENTS, ProductPermissionsDialog::new);
    }

    private void loadLayouts() {
        Path dir = this.getPath().resolve(VSFiles.DIR_LAYOUTS);
        if (!Files.exists(dir)) {
            Path file = dir.resolve(FileConfig.withExtension(ShopPlaceholders.DEFAULT));
            FileConfig config = FileConfig.load(file);

            new ShopMenu(this.plugin, this).load(config); // Write defaults
        }

        FileUtil.findYamlFiles(dir).forEach(file -> {
            String id = FileUtil.getNameWithoutExtension(file);
            ShopMenu layout = this.initMenu(new ShopMenu(this.plugin, this), file);
            this.layoutByIdMap.put(id, layout);
        });

        this.info("Loaded %s shop layouts.".formatted(this.layoutByIdMap.size()));
    }

    private void loadMainMenu() {
        this.centralMenu = new CentralMenu(this.plugin, this);
    }

    public void reloadShopAliases(@NonNull VirtualShop shop) {
        this.unloadShopAliases(shop);
        this.loadShopAliases(shop);
    }

    public void loadShopAliases(@NonNull VirtualShop shop) {
        this.commands.registerAliases(shop);
    }

    public void unloadShopAliases(@NonNull VirtualShop shop) {
        this.commands.unregisterAliases(shop);
    }

    @Override
    public void onDataLoadFinished() {
        this.addTask(this::tickShops, 30);
        this.dataHandler.addSyncAction(this::tickShops);
    }

    @Override
    @NonNull
    public VirtualSettings getSettings() {
        return this.settings;
    }

    private void printShops() {
        //        this.getShops().forEach(shop -> {
        //            this.info("=".repeat(30));
        //            this.info("Shop Id: " + shop.getId());
        //
        //            List<VirtualProduct> products = shop.getProducts().stream()
        //                .sorted(Comparator.comparing(VirtualProduct::getPage).thenComparing(VirtualProduct::getSlot))
        //                .toList();
        //
        //            products.forEach(product -> {
        //                if (product.getType() instanceof PhysicalTyping typing) {
        //                    AbstractProductPricer pricer = product.getPricer();
        //
        //                    String buy = NumberUtil.format(pricer.getBuyPrice()).replace(",", "_");
        //                    String sell = NumberUtil.format(pricer.getSellPrice()).replace(",", "_");
        //                    int page = product.getPage();
        //                    int slot = product.getSlot();
        //
        //                    ItemStack itemStack = typing.getItem();
        //                    String type = itemStack.getType().name();
        //                    String extra = "";
        //
        //                    if (itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
        //                        PotionType baseType = potionMeta.getBasePotionType();
        //                        if (baseType != null) {
        //                            extra = "PotionType." + baseType.name() + ", ";
        //                        }
        //                    }
        //
        //                    String text = "this.addShopProduct(shop, Material." + type + ", " + extra + buy + ", " + sell + ", " + page + ", " + slot + ");";
        //
        //                    info(text);
        //
        //                }
        //            });
        //            this.info("=".repeat(30));
        //        });
    }

    @NonNull
    public List<String> formatProductLore(@NonNull VirtualProduct product, @NonNull Player player) {
        return this.formatProductInfo(product, this.productFormatter, player);
    }

    public boolean createShop(@NonNull Player player, @NonNull String name) {
        String id = Strings.varStyle(name).orElse(null);
        if (id == null) {
            VirtualLang.SHOP_CREATE_ERROR_BAD_NAME.message().send(player);
            return false;
        }

        if (this.getShopById(id) != null) {
            VirtualLang.SHOP_CREATE_ERROR_EXIST.message().send(player);
            return false;
        }

        VirtualShop virtualShop = this.createShop(id, shop -> {
            shop.setName(TagWrappers.SOFT_YELLOW.and(TagWrappers.BOLD).wrap(StringUtil.capitalizeUnderscored(id)));
            shop.setDescription(new ArrayList<>());
            shop.setIcon(NightItem.fromType(Material.CHEST));
            shop.setBuyingAllowed(true);
            shop.setSellingAllowed(true);
            shop.setPages(1);
            shop.setAliases(new HashSet<>());
            shop.setPaginatedLayouts(false);
        });

        this.loadShop(virtualShop);
        return true;
    }

    @NonNull
    public VirtualShop createShop(@NonNull String id, @NonNull Consumer<VirtualShop> consumer) {
        Path path = this.getPath().resolve(VSFiles.DIR_SHOPS).resolve(FileConfig.withExtension(id));
        FileUtil.createFileIfNotExists(path);
        VirtualShop shop = new VirtualShop(this.plugin, this, this.dataManager, path, id);

        consumer.accept(shop);
        shop.saveForce();
        return shop;
    }

    public boolean delete(@NonNull VirtualShop shop) {
        try {
            if (!Files.deleteIfExists(shop.getPath())) return false;
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }

        shop.invalidateData();

        this.shopByIdMap.remove(shop.getId());
        return true;
    }

    public void saveDirtyShops() {
        this.getShops().forEach(AbstractShop::saveIfDirty);
    }

    public void tickShops() {
        this.getShops().forEach(this::tickShop);
    }

    public void tickShop(@NonNull VirtualShop shop) {
        int itemsAffected = 0;
        for (Rotation rotation : shop.getRotations()) {
            if (rotation.countItems() == 0) continue;
            if (rotation.countAllSlots() == 0) continue;

            RotationData data = shop.getRotationData(rotation);
            if (!data.isRotationTime()) continue;

            shop.performRotation(rotation);

            if (rotation.isNotifyOnRotation()) {
                itemsAffected += rotation.countAllSlots();
            }
        }

        if (itemsAffected > 0) {
            int totalItems = itemsAffected;

            Players.getOnline().forEach(player -> {
                if (!shop.canAccess(player, false)) return;

                this.sendPrefixed(VirtualLang.SHOP_ROTATION_NOTIFY, player, builder -> builder
                    .with(ShopPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(totalItems))
                    .with(shop.placeholders())
                );
            });
        }

        shop.updatePrices(false);
    }

    @Override
    @NonNull
    public TransactionLogger getLogger() {
        return this.logger;
    }

    @NonNull
    public ProductFormatter<VirtualProduct> getProductFormatter() {
        return this.productFormatter;
    }

    @Nullable
    public CentralMenu getMainMenu() {
        return this.centralMenu;
    }

    public boolean hasCentralMenu() {
        return this.centralMenu != null;
    }

    @NonNull
    public Map<String, ShopMenu> getLayoutByIdMap() {
        return this.layoutByIdMap;
    }

    @NonNull
    public Set<ShopMenu> getLayouts() {
        return new HashSet<>(this.layoutByIdMap.values());
    }

    @Nullable
    public ShopMenu getLayout(@NonNull VirtualShop shop, int page) {
        return this.getLayout(shop.getLayout(page));
    }

    @Nullable
    public ShopMenu getLayout(@NonNull String id) {
        return this.layoutByIdMap.get(id.toLowerCase());
    }

    @NonNull
    public List<String> getLayoutNames() {
        return new ArrayList<>(this.layoutByIdMap.keySet());
    }


    @NonNull
    public Set<VirtualShop> getShops() {
        return new HashSet<>(this.shopByIdMap.values());
    }

    @NonNull
    public Set<VirtualShop> getShops(@NonNull Player player) {
        return this.getShops(player, this.getShops());
    }

    @NonNull
    public Map<String, VirtualShop> getShopByIdMap() {
        return this.shopByIdMap;
    }

    @Nullable
    public VirtualShop getShopById(@NonNull String id) {
        return this.shopByIdMap.get(id.toLowerCase());
    }

    @NonNull
    private Set<VirtualShop> getShops(@NonNull Player player, @NonNull Collection<VirtualShop> shops) {
        return shops.stream().filter(shop -> this.isAvailable(player, false) && shop.canAccess(player, false)).collect(
            Collectors.toSet());
    }

    @Nullable
    public VirtualProduct getBestProductFor(@NonNull ItemStack itemStack, @NonNull TradeType tradeType) {
        return this.getBestProductFor(itemStack, tradeType, null, null);
    }

    @Nullable
    public VirtualProduct getBestProductFor(@NonNull ItemStack itemStack, @NonNull TradeType type,
                                            @Nullable VirtualShop shop) {
        return this.getBestProductFor(itemStack, type, shop, null);
    }

    @Nullable
    public VirtualProduct getBestProductFor(@NonNull ItemStack itemStack, @NonNull TradeType tradeType,
                                            @Nullable Player player) {
        return this.getBestProductFor(itemStack, tradeType, null, player);
    }

    @Nullable
    public VirtualProduct getBestProductFor(@NonNull ItemStack itemStack, @NonNull TradeType tradeType,
                                            @Nullable VirtualShop shop, @Nullable Player player) {
        // No product if player is in bad world/gamemode.
        if (player != null && !this.isAvailable(player, false)) return null;

        Set<VirtualShop> shopsLookup = shop == null ? this.getShops() : Lists.newSet(shop);
        Set<VirtualProduct> candidates = new HashSet<>();
        int stackSize = itemStack.getAmount();

        shopsLookup.forEach(shopLookup -> {
            // No product if player can't access a shop.
            if (player != null && !shopLookup.canAccess(player, false)) return;

            VirtualProduct best = shopLookup.getBestProduct(itemStack, tradeType, player);
            if (best != null) {
                candidates.add(best);
            }
        });

        return ShopUtils.getBestProduct(candidates, tradeType, stackSize, player);
    }

    @NonNull
    public ItemStack cacheProduct(@NonNull VirtualProduct product) {
        UUID pId = UUID.randomUUID();
        this.productCache.put(pId, product);

        ItemStack stack = product.getEffectivePreview();
        PDCUtil.set(stack, Keys.keyProductCache, pId);
        return stack;
    }

    @Nullable
    public VirtualProduct uncacheProduct(@NonNull ItemStack stack) {
        UUID pId = PDCUtil.getUUID(stack, Keys.keyProductCache).orElse(null);
        if (pId == null) return null;

        PDCUtil.remove(stack, Keys.keyProductCache);
        return this.productCache.remove(pId);
    }

    public boolean isAvailable(@NonNull Player player, boolean notify) {
        if (!player.hasPermission(VirtualPerms.BYPASS_WORLDS)) {
            if (VirtualConfig.DISABLED_WORLDS.get().contains(player.getWorld().getName())) {
                if (notify) VirtualLang.SHOP_ERROR_BAD_WORLD.message().send(player);
                return false;
            }
        }

        if (!player.hasPermission(VirtualPerms.BYPASS_GAMEMODE)) {
            if (VirtualConfig.DISABLED_GAMEMODES.get().contains(player.getGameMode())) {
                if (notify) VirtualLang.SHOP_ERROR_BAD_GAMEMODE.message().send(player);
                return false;
            }
        }

        return true;
    }

    public static double getSellMultiplier(@NonNull Player player) {
        return VirtualConfig.SELL_RANK_MULTIPLIERS.get().getGreatest(player);
    }

    @Override
    public void openPurchaseOptionsDialog(@NonNull ProductClickContext context) {
        this.dialogRegistry.show(context.player(), VSDialogKeys.PURCHASE_OPTIONS, context, null);
    }

    public void openNormalProducts(@NonNull Player player, @NonNull VirtualShop shop) {
        this.openNormalProducts(player, shop, 1);
    }

    public void openNormalProducts(@NonNull Player player, @NonNull VirtualShop shop, int page) {
        this.openShop(player, shop, page, false, ViewMode.EDIT_PRODUCTS, null);
    }

    public void openShopCreationDialog(@NonNull Player player, @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.SHOP_CREATION, this, callback);
    }

    public void openShopAliasesDialog(@NonNull Player player, @NonNull VirtualShop shop, @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.SHOP_ALIASES, shop, callback);
    }

    public void openShopDescription(@NonNull Player player, @NonNull VirtualShop shop, @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.SHOP_DESCRIPTION, shop, callback);
    }

    public void openShopPageLayoutsDialog(@NonNull Player player, @NonNull VirtualShop shop,
                                          @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.SHOP_PAGE_LAYOUTS, shop, callback);
    }

    public void openShopMenuSlotsDialog(@NonNull Player player, @NonNull VirtualShop shop,
                                        @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.SHOP_MENU_SLOTS, shop, callback);
    }

    public void openShopNameDialog(@NonNull Player player, @NonNull VirtualShop shop, @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.SHOP_NAME, shop, callback);
    }

    public void openShopPagesDialog(@NonNull Player player, @NonNull VirtualShop shop, @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.SHOP_PAGES, shop, callback);
    }

    public void openRotatingsProducts(@NonNull Player player, @NonNull VirtualShop shop) {
        this.rotatingProductsMenu.show(player, shop);
    }


    public void openProductTypeDialog(@NonNull Player player, @NonNull VirtualProduct product,
                                      @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.PRODUCT_CONTENT_TYPE, product, callback);
    }

    public void openProductCommandsDialog(@NonNull Player player, @NonNull VirtualProduct product,
                                          @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.PRODUCT_COMMANDS, product, callback);
    }

    public void openProductPriceDialog(@NonNull Player player, @NonNull VirtualProduct product,
                                       @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.PRODUCT_PRICE, product, callback);
    }

    public void openProductCurrencyDialog(@NonNull Player player, @NonNull VirtualProduct product,
                                          @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.PRODUCT_CURRENCY, product, callback);
    }

    public void openProductFloatPriceTimingsDialog(@NonNull Player player, @NonNull VirtualProduct product,
                                                   @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.PRODUCT_FLOAT_PRICE_TIMINGS, product, callback);
    }

    public void openProductStocksDialog(@NonNull Player player, @NonNull VirtualProduct product,
                                        @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.PRODUCT_STOCKS, product, callback);
    }

    public void openProductLimitsDialog(@NonNull Player player, @NonNull VirtualProduct product,
                                        @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.PRODUCT_LIMITS, product, callback);
    }

    public void openProductRanksDialog(@NonNull Player player, @NonNull VirtualProduct product,
                                       @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.PRODUCT_RANK_REQUIREMENTS, product, callback);
    }

    public void openProductPermsDialog(@NonNull Player player, @NonNull VirtualProduct product,
                                       @Nullable Runnable callback) {
        this.dialogRegistry.show(player, VSDialogKeys.PRODUCT_PERMISSION_REQUIREMENTS, product, callback);
    }

    public boolean openProductOptions(@NonNull Player player, @NonNull VirtualProduct product) {
        return this.productOptionsMenu.show(player, product);
    }

    public void openRotationsList(@NonNull Player player, @NonNull VirtualShop shop) {
        this.rotationListMenu.open(player, shop);
    }

    public void openRotationOptions(@NonNull Player player, @NonNull Rotation rotation) {
        this.rotationOptionsMenu.open(player, rotation);
    }

    public void openRotationSlots(@NonNull Player player, @NonNull Rotation rotation) {
        this.openShop(player, rotation.getShop(), 1, false, ViewMode.EDIT_ROTATION_SLOTS, rotation);
    }

    public void openRotationItemsList(@NonNull Player player, @NonNull Rotation rotation) {
        this.rotationItemsListMenu.open(player, rotation);
    }

    public void openRotationItemSelection(@NonNull Player player, @NonNull Rotation rotation) {
        this.rotationItemSelectMenu.open(player, rotation);
    }

    public void openRotationTimes(@NonNull Player player, @NonNull Rotation rotation) {
        this.rotationTimesMenu.open(player, rotation);
    }

    public void openShopsEditor(@NonNull Player player) {
        this.shopListMenu.open(player);
    }

    public void openShopOptions(@NonNull Player player, @NonNull VirtualShop shop) {
        this.shopOptionsMenu.open(player, shop);
    }

    public boolean openShop(@NonNull Player player, @NonNull VirtualShop shop) {
        return this.openShop(player, shop, 1);
    }

    public boolean openShop(@NonNull Player player, @NonNull VirtualShop shop, int page) {
        return this.openShop(player, shop, page, false);
    }

    public boolean openShop(@NonNull Player player, @NonNull VirtualShop shop, int page, boolean force) {
        return this.openShop(player, shop, page, force, ViewMode.NORMAL, null);
    }

    public boolean openShop(@NonNull Player player, @NonNull VirtualShop shop, int page, boolean force,
                            @NonNull ViewMode viewMode, @Nullable Rotation rotation) {
        if (!this.plugin.getDataManager().isLoaded()) return false;
        if (!force) {
            if (!this.isAvailable(player, true)) return false;
            if (!shop.canAccess(player, true)) return false;
        }

        int normalPage = Math.max(0, page);

        ShopMenu layout = this.getLayout(shop, normalPage);
        if (layout == null) layout = this.getLayout(VirtualConfig.DEFAULT_LAYOUT.get());
        if (layout == null) {
            this.sendPrefixed(VirtualLang.SHOP_ERROR_INVALID_LAYOUT, player, builder -> builder.with(shop
                .placeholders()));
            return false;
        }

        return layout.show(player, shop, normalPage, viewMode, rotation);
    }

    public boolean openMainMenu(@NonNull Player player) {
        return this.openMainMenu(player, false);
    }

    public boolean openMainMenu(@NonNull Player player, boolean force) {
        if (this.centralMenu == null) return false;

        if (!force) {
            if (!this.isAvailable(player, true)) return false;
        }

        return this.centralMenu.open(player);
    }

    public boolean openSellMenu(@NonNull Player player, boolean force) {
        if (!force) {
            if (!this.isAvailable(player, true)) return false;
        }

        return this.openSellingMenu(player);
    }

    @Override
    @NonNull
    protected CompletableFuture<Boolean> handleSuccessfulTransaction(@NonNull ECompletedTransaction transaction) {
        return this.transactionProcessor.queueTransaction(() -> {
            return true;
        });
    }

    @Override
    protected void finishSuccessfulTransaction(@NonNull ECompletedTransaction transaction) {
        Player player = transaction.player();
        TradeType type = transaction.type();

        transaction.worth().getBalanceMap().forEach((currencyId, amount) -> {
            Currency currency = EconomyBridge.api().getCurrency(currencyId);
            if (currency == null) return;

            if (type == TradeType.BUY) {
                currency.withdraw(player, amount);
            }
            else {
                currency.deposit(player, amount);
            }
        });

        transaction.items().forEach(quantified -> {
            quantified.product().onSuccessfulTransaction(transaction, quantified.units());
        });

        if (!transaction.silent()) {
            MessageLocale locale;

            if (type == TradeType.BUY) {
                locale = transaction
                    .isSingleItem() ? VirtualLang.PRODUCT_PURCHASE_BUY_SINGLE : VirtualLang.PRODUCT_PURCHASE_BUY_MULTIPLE;
            }
            else {
                locale = transaction
                    .isSingleItem() ? VirtualLang.PRODUCT_PURCHASE_SELL_SINGLE : VirtualLang.PRODUCT_PURCHASE_SELL_MULTIPLE;
            }

            this.sendPrefixed(locale, player, builder -> this.addTransactionPlaceholderContext(builder, transaction));
        }

        this.logger.logTransaction(transaction);
    }

    public void sellSlots(@NonNull Player player, int... slots) {
        PlayerInventory inventory = player.getInventory();

        ERawTransaction.Builder builder = ERawTransaction.builder(player, TradeType.SELL)
            .setPreview(false)
            .setStrict(false);

        for (int index : slots) {
            ItemStack itemStack = inventory.getItem(index);
            if (itemStack == null || itemStack.getType().isAir()) continue;

            builder.addItem(itemStack);
        }

        this.proceedTransaction(builder.build(), completed -> {
        });
    }

    public void sellAll(@NonNull Player player, @NonNull Consumer<ECompletedTransaction> callback) {
        this.sellAll(player, player.getInventory(), callback);
    }

    public void sellAll(@NonNull Player player, @NonNull Inventory inventory,
                        @NonNull Consumer<ECompletedTransaction> callback) {
        this.sellAll(player, inventory, false, callback);
    }

    public void sellAll(@NonNull Player player, @NonNull Inventory inventory, boolean silent,
                        @NonNull Consumer<ECompletedTransaction> callback) {
        ERawTransaction raw = ERawTransaction.builder(player, TradeType.SELL)
            .addItems(inventory)
            .setUserInventory(inventory)
            .setPreview(false)
            .setStrict(false)
            .setSilent(silent)
            .build();

        this.proceedTransaction(raw, callback);
    }
}
