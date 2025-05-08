package su.nightexpress.nexshop.shop.virtual;

import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.ShopModule;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.TransactionLogger;
import su.nightexpress.nexshop.api.shop.product.Product;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.impl.AbstractModule;
import su.nightexpress.nexshop.shop.virtual.command.impl.VirtualCommands;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.editor.DiscountListEditor;
import su.nightexpress.nexshop.shop.virtual.editor.DiscountMainEditor;
import su.nightexpress.nexshop.shop.virtual.editor.product.*;
import su.nightexpress.nexshop.shop.virtual.editor.rotation.*;
import su.nightexpress.nexshop.shop.virtual.editor.shop.*;
import su.nightexpress.nexshop.shop.virtual.impl.*;
import su.nightexpress.nexshop.shop.virtual.menu.CentralMenu;
import su.nightexpress.nexshop.shop.virtual.menu.SellMenu;
import su.nightexpress.nexshop.shop.virtual.menu.ShopLayout;
import su.nightexpress.nexshop.shop.virtual.type.RotationType;
import su.nightexpress.nexshop.util.ShopUtils;
import su.nightexpress.nexshop.util.UnitUtils;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.Replacer;
import su.nightexpress.nightcore.util.text.tag.Tags;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VirtualShopModule extends AbstractModule implements ShopModule {

    public static final String ID                 = "virtual_shop";
    public static final String DIR_SHOPS          = "/shops/";
    public static final String DIR_LAYOUTS        = "/layouts/";

    private final Map<String, ShopLayout>  layoutByIdMap;
    private final Map<String, VirtualShop> shopByIdMap;

    private CentralMenu centralMenu;
    private SellMenu    sellMenu;

    private DiscountListEditor     discountListEditor;
    private DiscountMainEditor     discountEditor;
    private NormalProductsMenu     normalProductsMenu;
    private RotatingProductsMenu   rotatingProductsMenu;
    private ProductCreationMenu    productCreationMenu;
    private ProductOptionsMenu productOptionsMenu;
    private PriceMenu          productPriceMenu;
    private ProductStocksMenu  productStocksMenu;
    private ShopListMenu           shopListMenu;
    private ShopOptionsMenu        shopOptionsMenu;
    private ShopLayoutsMenu        shopLayoutsMenu;
    private RotationListMenu       rotationListMenu;
    private RotationOptionsMenu    rotationOptionsMenu;
    private RotationSlotsMenu      rotationSlotsMenu;
    private RotationItemsListMenu  rotationItemsListMenu;
    private RotationItemSelectMenu rotationItemSelectMenu;
    private RotationTimesMenu      rotationTimesMenu;

    private TransactionLogger logger;

    public VirtualShopModule(@NotNull ShopPlugin plugin) {
        super(plugin, ID, Config.getVirtualShopAliases());

        this.layoutByIdMap = new HashMap<>();
        this.shopByIdMap = new HashMap<>();
    }

    @Override
    protected void loadModule(@NotNull FileConfig config) {
        this.updateConfiguration(config);
        config.initializeOptions(VirtualConfig.class);

        this.plugin.getLangManager().loadEntries(VirtualLang.class);
        this.plugin.getLangManager().loadEntries(VirtualLocales.class);
        this.plugin.registerPermissions(VirtualPerms.class);
        this.logger = new TransactionLogger(this, config);

        // Create default shops & layouts.
        new ShopCreator(this.plugin, this).createDefaults();

        this.loadEditors();
        this.loadLayouts();
        this.loadShops();

        if (VirtualConfig.isCentralMenuEnabled()) {
            this.loadMainMenu();
        }
        if (VirtualConfig.SELL_MENU_ENABLED.get()) {
            this.sellMenu = new SellMenu(this.plugin, this);
        }
    }

    @Override
    protected void disableModule() {
        if (this.discountListEditor != null) this.discountListEditor.clear();
        if (this.discountEditor != null) this.discountEditor.clear();

        if (this.centralMenu != null) {
            this.centralMenu.clear();
            this.centralMenu = null; // Main menu is toggleable.
        }
        if (this.sellMenu != null) {
            this.sellMenu.clear();
            this.sellMenu = null; // Sell menu is toggleable.
        }

        this.getLayouts().forEach(ShopLayout::clear);
        this.layoutByIdMap.clear();
        this.shopByIdMap.clear();

        VirtualCommands.unload(this.plugin, this);
    }

    private void updateConfiguration(@NotNull FileConfig config) {
        if (config.getInt("_dataver") >= 414) return;

        this.replaceListLines(config, VirtualConfig.PRODUCT_FORMAT_LORE_GENERAL.getPath());
        this.replaceListLines(config, VirtualConfig.PRODUCT_FORMAT_LORE_BUY.getPath());
        this.replaceListLines(config, VirtualConfig.PRODUCT_FORMAT_LORE_SELL.getPath());
        this.replaceListLines(config, VirtualConfig.PRODUCT_FORMAT_LORE_STOCK_BUY.getPath());
        this.replaceListLines(config, VirtualConfig.PRODUCT_FORMAT_LORE_STOCK_SELL.getPath());
        this.replaceListLines(config, VirtualConfig.PRODUCT_FORMAT_LORE_LIMIT_BUY.getPath());
        this.replaceListLines(config, VirtualConfig.PRODUCT_FORMAT_LORE_LIMIT_SELL.getPath());

        config.set("_dataver", 414);
    }

    private void replaceListLines(@NotNull FileConfig config, @NotNull String path) {
        if (!config.contains(path)) return;

        List<String> list = config.getStringList(path);
        list.replaceAll(line -> {
            if (line.isBlank()) return Placeholders.EMPTY_IF_ABOVE;

            for (TradeType tradeType : TradeType.values()) {
                line = line
                    .replace(Placeholders.PRODUCT_STOCK_RESTOCK_DATE.apply(tradeType), Placeholders.PRODUCT_STOCKS_RESET_IN)
                    .replace(Placeholders.PRODUCT_LIMIT_RESTOCK_DATE.apply(tradeType), Placeholders.PRODUCT_LIMITS_RESET_IN);
            }

            return line;
        });
        config.set(path, list);
    }

    @Override
    protected void loadCommands(@NotNull ChainedNodeBuilder builder) {
        VirtualCommands.load(this.plugin, this, builder);
    }

    private void loadEditors() {
        this.discountListEditor = new DiscountListEditor(this.plugin, this);
        this.discountEditor = new DiscountMainEditor(this.plugin, this);
        this.normalProductsMenu = this.addMenu(new NormalProductsMenu(this.plugin, this));
        this.rotatingProductsMenu = this.addMenu(new RotatingProductsMenu(this.plugin, this));
        this.productCreationMenu = this.addMenu(new ProductCreationMenu(this.plugin, this));
        this.productOptionsMenu = this.addMenu(new ProductOptionsMenu(this.plugin, this));
        this.productPriceMenu = this.addMenu(new PriceMenu(this.plugin, this));
        this.productStocksMenu = this.addMenu(new ProductStocksMenu(this.plugin, this));

        this.shopListMenu = this.addMenu(new ShopListMenu(this.plugin, this));
        this.shopOptionsMenu = this.addMenu(new ShopOptionsMenu(this.plugin, this));
        this.shopLayoutsMenu = this.addMenu(new ShopLayoutsMenu(this.plugin, this));

        this.rotationListMenu = this.addMenu(new RotationListMenu(this.plugin, this));
        this.rotationOptionsMenu = this.addMenu(new RotationOptionsMenu(this.plugin, this));
        this.rotationTimesMenu = this.addMenu(new RotationTimesMenu(this.plugin, this));
        this.rotationSlotsMenu = this.addMenu(new RotationSlotsMenu(this.plugin, this));
        this.rotationItemsListMenu = this.addMenu(new RotationItemsListMenu(this.plugin, this));
        this.rotationItemSelectMenu = this.addMenu(new RotationItemSelectMenu(this.plugin, this));
    }

    private void updateRotatingShops() {
        for (File folder : FileUtil.getFolders(this.getAbsolutePath() + "/rotating_shops/")) {
            String id = folder.getName();
            File file = new File(folder.getAbsolutePath(), VirtualShop.FILE_NAME);
            if (!file.exists()) continue;

            File dir = new File(this.getAbsolutePath() + DIR_SHOPS + id);
            if (dir.exists()) {
                this.error("Could not migrate rotating shop '" + id + "': Shop with such name already exists.");
                continue;
            }

            dir.mkdirs();

            VirtualShop shop = new VirtualShop(this.plugin, this, file, id);
            if (!shop.load()) return;

            FileConfig config = shop.getConfig();
            FileConfig itemsConfig = shop.getConfigProducts();
            Set<Integer> slots = IntStream.of(config.getIntArray("Rotation.Products.Slots")).boxed().collect(Collectors.toSet());

            Rotation rotation = new Rotation(Placeholders.DEFAULT, shop);
            rotation.setRotationType(config.getEnum("Rotation.Type", RotationType.class, RotationType.INTERVAL));
            rotation.setRotationInterval(config.getInt("Rotation.Interval", 86400));
            rotation.setSlots(slots, 1);

            for (String sDay : config.getSection("Rotation.Fixed")) {
                DayOfWeek day = StringUtil.getEnum(sDay, DayOfWeek.class).orElse(null);
                if (day == null) continue;

                TreeSet<LocalTime> times = new TreeSet<>(ShopUtils.parseTimes(config.getStringList("Rotation.Fixed." + sDay)));
                rotation.getRotationTimes().put(day, times);
            }

            shop.getProducts().forEach(product -> {
                double weight = itemsConfig.getDouble("List." + product.getId() + ".Rotation.Chance");

                product.setRotating(true);
                rotation.addItem(new RotationItem(product.getId(), weight));
            });

            shop.addRotation(rotation);
            shop.save();

            config.getFile().renameTo(new File(dir.getAbsolutePath(), VirtualShop.FILE_NAME));
            itemsConfig.getFile().renameTo(new File(dir.getAbsolutePath(), VirtualShop.FILE_PRODUCTS));

            FileUtil.deleteRecursive(config.getFile().getParentFile());
        }
    }

    private void loadShops() {
        this.updateRotatingShops();

        for (File folder : FileUtil.getFolders(this.getAbsolutePath() + DIR_SHOPS)) {
            String id = folder.getName();
            File file = new File(folder.getAbsolutePath(), VirtualShop.FILE_NAME);

            VirtualShop shop = new VirtualShop(this.plugin, this, file, id);
            this.loadShop(shop);
        }
        this.info("Loaded " + this.shopByIdMap.size() + " shops.");

        this.printShops();
    }

    private void loadShop(@NotNull VirtualShop shop) {
        if (!shop.load()) {
            this.error("Shop not loaded: '" + shop.getFile().getPath() + "'");
            return;
        }

        this.shopByIdMap.put(shop.getId(), shop);
    }

    private void loadLayouts() {
        for (FileConfig config : FileConfig.loadAll(this.getAbsolutePath() + DIR_LAYOUTS)) {
            String id = FileConfig.getName(config.getFile());
            ShopLayout layout = new ShopLayout(this.plugin, this, config);
            this.layoutByIdMap.put(id, layout);
        }
        this.info("Loaded " + this.layoutByIdMap.size() + " shop layouts.");
    }

    private void loadMainMenu() {
        this.centralMenu = new CentralMenu(this.plugin, this);
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

    @Override
    @NotNull
    public Currency getDefaultCurrency() {
        return EconomyBridge.getCurrencyOrDummy(VirtualConfig.DEFAULT_CURRENCY.get());
    }

    @Override
    @NotNull
    public String getDefaultCartUI() {
        return VirtualConfig.DEFAULT_CART_UI.get();
    }

    @Override
    @NotNull
    public String getDefaultCartUI(@NotNull TradeType type) {
        if (!VirtualConfig.SPLIT_BUY_SELL_CART_UI.get()) return this.getDefaultCartUI();

        return type == TradeType.BUY ? VirtualConfig.DEFAULT_BUY_CART_UI.get() : VirtualConfig.DEFAULT_SELL_CART_UI.get();
    }

    @Override
    @NotNull
    public TransactionLogger getLogger() {
        return this.logger;
    }

    @NotNull
    public SellMenu getSellMenu() {
        return sellMenu;
    }

    @Nullable
    public CentralMenu getMainMenu() {
        return centralMenu;
    }

    @NotNull
    public Map<String, ShopLayout> getLayoutByIdMap() {
        return this.layoutByIdMap;
    }

    @NotNull
    public Set<ShopLayout> getLayouts() {
        return new HashSet<>(this.layoutByIdMap.values());
    }

    @Nullable
    public ShopLayout getLayout(@NotNull VirtualShop shop, int page) {
        return this.getLayout(shop.getLayout(page));
    }

    @Nullable
    public ShopLayout getLayout(@NotNull String id) {
        return this.layoutByIdMap.get(id.toLowerCase());
    }

    @NotNull
    public List<String> getLayoutNames() {
        return new ArrayList<>(this.layoutByIdMap.keySet());
    }



    @NotNull
    public Set<VirtualShop> getShops() {
        return new HashSet<>(this.shopByIdMap.values());
    }

    @NotNull
    public List<VirtualShop> getShops(@NotNull Player player) {
        return this.getShops(player, this.getShops());
    }

    @NotNull
    public Map<String, VirtualShop> getShopByIdMap() {
        return this.shopByIdMap;
    }

    @Nullable
    public VirtualShop getShopById(@NotNull String id) {
        return this.shopByIdMap.get(id.toLowerCase());
    }

    @NotNull
    private List<VirtualShop> getShops(@NotNull Player player, @NotNull Collection<VirtualShop> shops) {
        return shops.stream().filter(shop -> this.isAvailable(player, false) && shop.canAccess(player, false)).toList();
    }

    @Nullable
    public VirtualProduct getBestProductFor(@NotNull ItemStack itemStack, @NotNull TradeType tradeType) {
        return this.getBestProductFor(itemStack, tradeType, null, null);
    }

    @Nullable
    public VirtualProduct getBestProductFor(@NotNull ItemStack itemStack, @NotNull TradeType type, @Nullable VirtualShop shop) {
        return this.getBestProductFor(itemStack, type, shop, null);
    }

    @Nullable
    public VirtualProduct getBestProductFor(@NotNull ItemStack itemStack, @NotNull TradeType tradeType, @Nullable Player player) {
        return this.getBestProductFor(itemStack, tradeType, null, player);
    }

    @Nullable
    public VirtualProduct getBestProductFor(@NotNull ItemStack itemStack, @NotNull TradeType tradeType, @Nullable VirtualShop shop, @Nullable Player player) {
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

//        Comparator<VirtualProduct> comparator = Comparator.comparingDouble(product -> product.getPrice(tradeType, player) * UnitUtils.amountToUnits(product, stackSize));
//        return (tradeType == TradeType.BUY ? candidates.stream().min(comparator) : candidates.stream().max(comparator)).orElse(null);
    }

    public boolean createShop(@NotNull Player player, @NotNull String name) {
        String id = StringUtil.transformForID(name);
        if (id.isBlank()) {
            VirtualLang.SHOP_CREATE_ERROR_BAD_NAME.getMessage().send(player);
            return false;
        }

        if (this.getShopById(id) != null) {
            VirtualLang.SHOP_CREATE_ERROR_EXIST.getMessage().send(player);
            return false;
        }

        File file = new File(this.getAbsolutePath() + DIR_SHOPS + id, VirtualShop.FILE_NAME);
        VirtualShop shop = new VirtualShop(plugin, this, file, id);

        shop.setName(Tags.LIGHT_YELLOW.wrap(Tags.BOLD.wrap(StringUtil.capitalizeUnderscored(id))));
        shop.setDescription(new ArrayList<>());
        shop.setIcon(NightItem.asCustomHead("34ccb52750e97e830aebfa8a21d5da0d364d0fdad9fb0cc220fe2ca8411842c3"));
        shop.setDefaultLayout(Placeholders.DEFAULT);
        shop.setBuyingAllowed(true);
        shop.setSellingAllowed(true);
        shop.save();
        this.loadShop(shop);

        return true;
    }

    public boolean delete(@NotNull VirtualShop shop) {
        if (!FileUtil.deleteRecursive(this.getAbsolutePath() + DIR_SHOPS + shop.getId())) return false;

        this.plugin.getDataManager().deleteAllData(shop);
        this.shopByIdMap.remove(shop.getId());
        return true;
    }

    public boolean isAvailable(@NotNull Player player, boolean notify) {
        if (!player.hasPermission(VirtualPerms.BYPASS_WORLDS)) {
            if (VirtualConfig.DISABLED_WORLDS.get().contains(player.getWorld().getName())) {
                if (notify) VirtualLang.SHOP_ERROR_BAD_WORLD.getMessage().send(player);
                return false;
            }
        }

        if (!player.hasPermission(VirtualPerms.BYPASS_GAMEMODE)) {
            if (VirtualConfig.DISABLED_GAMEMODES.get().contains(player.getGameMode())) {
                if (notify) VirtualLang.SHOP_ERROR_BAD_GAMEMODE.getMessage().send(player);
                return false;
            }
        }

        return true;
    }

    public static double getSellMultiplier(@NotNull Player player) {
        return VirtualConfig.SELL_RANK_MULTIPLIERS.get().getGreatest(player);
    }

    public void openDiscountsEditor(@NotNull Player player, @NotNull VirtualShop shop) {
        this.discountListEditor.open(player, shop);
    }

    public void openDiscountEditor(@NotNull Player player, @NotNull VirtualDiscount discount) {
        this.discountEditor.open(player, discount);
    }

    public void openNormalProducts(@NotNull Player player, @NotNull VirtualShop shop) {
        this.openNormalProducts(player, shop, 1);
    }

    public void openNormalProducts(@NotNull Player player, @NotNull VirtualShop shop, int page) {
        this.normalProductsMenu.open(player, shop, page);
    }

    public void openRotatingsProducts(@NotNull Player player, @NotNull VirtualShop shop) {
        this.rotatingProductsMenu.open(player, shop);
    }

    public void openProductCreation(@NotNull Player player, @NotNull VirtualShop shop, boolean rotating, int page, int slot) {
        this.productCreationMenu.open(player, shop, rotating, page, slot);
    }

    public void openProductOptions(@NotNull Player player, @NotNull VirtualProduct product) {
        this.productOptionsMenu.open(player, product);
    }

    public void openPriceOptions(@NotNull Player player, @NotNull VirtualProduct product) {
        this.productPriceMenu.open(player, product);
    }

    public void openStockOptions(@NotNull Player player, @NotNull VirtualProduct product) {
        this.productStocksMenu.open(player, product);
    }

    public void openRotationsList(@NotNull Player player, @NotNull VirtualShop shop) {
        this.rotationListMenu.open(player, shop);
    }

    public void openRotationOptions(@NotNull Player player, @NotNull Rotation rotation) {
        this.rotationOptionsMenu.open(player, rotation);
    }

    public void openRotationSlots(@NotNull Player player, @NotNull Rotation rotation) {
        this.rotationSlotsMenu.open(player, rotation);
    }

    public void openRotationItemsList(@NotNull Player player, @NotNull Rotation rotation) {
        this.rotationItemsListMenu.open(player, rotation);
    }

    public void openRotationItemSelection(@NotNull Player player, @NotNull Rotation rotation) {
        this.rotationItemSelectMenu.open(player, rotation);
    }

    public void openRotationTimes(@NotNull Player player, @NotNull Rotation rotation) {
        this.rotationTimesMenu.open(player, rotation);
    }

    public void openShopsEditor(@NotNull Player player) {
        this.shopListMenu.open(player);
    }

    public void openShopOptions(@NotNull Player player, @NotNull VirtualShop shop) {
        this.shopOptionsMenu.open(player, shop);
    }

    public void openShopLayouts(@NotNull Player player, @NotNull VirtualShop shop) {
        this.shopLayoutsMenu.open(player, shop);
    }

    public boolean openShop(@NotNull Player player, @NotNull VirtualShop shop) {
        return this.openShop(player, shop, false);
    }

    public boolean openShop(@NotNull Player player, @NotNull VirtualShop shop, boolean force) {
        return this.openShop(player, shop, 1, force);
    }

    public boolean openShop(@NotNull Player player, @NotNull VirtualShop shop, int page) {
        return this.openShop(player, shop, page, false);
    }

    public boolean openShop(@NotNull Player player, @NotNull VirtualShop shop, int page, boolean force) {
        if (!this.plugin.getDataManager().isLoaded()) return false;
        if (!force) {
            if (!this.isAvailable(player, true)) return false;
            if (!shop.canAccess(player, true)) return false;
        }

        int normalPage = Math.max(0, page);

        ShopLayout layout = this.getLayout(shop, normalPage);
        if (layout == null) layout = this.getLayout(VirtualConfig.DEFAULT_LAYOUT.get());
        if (layout == null) {
            VirtualLang.SHOP_ERROR_INVALID_LAYOUT.getMessage().send(player, replacer -> replacer.replace(shop.replacePlaceholders()));
            return false;
        }

        return layout.open(player, shop, viewer -> {
            viewer.setPages(shop.getPages());
            viewer.setPage(Math.min(normalPage, shop.getPages()));
        });
    }

    public boolean openMainMenu(@NotNull Player player) {
        return this.openMainMenu(player, false);
    }

    public boolean openMainMenu(@NotNull Player player, boolean force) {
        if (this.centralMenu == null) return false;

        if (!force) {
            if (!this.isAvailable(player, true)) return false;
        }

        return this.centralMenu.open(player);
    }

    public boolean openSellMenu(@NotNull Player player, boolean force) {
        if (this.sellMenu == null) return false;

        if (!force) {
            if (!this.isAvailable(player, true)) return false;
        }

        this.sellMenu.open(player, new ArrayList<>());
        return true;
    }

    // TODO Check isAvailable for sell methods.

    public void sellWithReturn(@NotNull Player player, @NotNull Inventory inventory) {
        this.sellAll(player, inventory);

        for (ItemStack left : inventory.getContents()) {
            if (left == null || left.getType().isAir() || left.getAmount() < 1) continue;

            Players.addItem(player, left);
        }
    }

    public void sellSlots(@NotNull Player player, int... slots) {
        Inventory inventory = plugin.getServer().createInventory(null, 54);
        PlayerInventory playerInventory = player.getInventory();

        for (int index : slots) {
            ItemStack item = playerInventory.getItem(index);
            if (item == null || item.getType().isAir()) continue;

            inventory.addItem(new ItemStack(item));
            item.setAmount(0);
        }

        this.sellWithReturn(player, inventory);
    }

    public void sellAll(@NotNull Player player) {
        this.sellAll(player, false);
    }

    public void sellAll(@NotNull Player player, boolean silent) {
        this.sellAll(player, player.getInventory(), silent);
    }

    public void sellAll(@NotNull Player player, @NotNull Inventory inventory) {
        this.sellAll(player, inventory, false);
    }

    public void sellAll(@NotNull Player player, @NotNull Inventory inventory, boolean silent) {
        this.sellAll(player, inventory, null, silent);
    }

    public void sellAll(@NotNull Player player, @NotNull Inventory inventory, @Nullable VirtualShop shop) {
        this.sellAll(player, inventory, shop, false);
    }

    public void sellAll(@NotNull Player player, @NotNull Inventory inventory, @Nullable VirtualShop shop, boolean silent) {
        SellResult sellResult = this.bulkSell(player, inventory, shop);

        if (!silent) {
            if (sellResult.isEmpty()) {
                VirtualLang.SELL_MENU_NOTHING_RESULT.getMessage().send(player);
                VirtualLang.SELL_MENU_NOTHING_DETAILS.getMessage().send(player);
                return;
            }

            String total = sellResult.getTotalIncome();

            VirtualLang.SELL_MENU_SALE_RESULT.getMessage().send(player, replacer -> replacer.replace(Placeholders.GENERIC_TOTAL, total));

            VirtualLang.SELL_MENU_SALE_DETAILS.getMessage().send(player, replacer -> replacer
                .replace(Placeholders.GENERIC_TOTAL, total)
                .replace(Placeholders.GENERIC_ENTRY, list -> {
                    sellResult.getTransactions().forEach(transaction -> {
                        Product product = transaction.getProduct();

                        list.add(Replacer.create()
                            .replace(Placeholders.GENERIC_ITEM, () -> ItemUtil.getSerializedName(product.getPreview()))
                            .replace(Placeholders.GENERIC_AMOUNT, () -> NumberUtil.format(transaction.getAmount()))
                            .replace(Placeholders.GENERIC_PRICE, () -> transaction.getCurrency().format(transaction.getPrice()))
                            .replace(Placeholders.SHOP_NAME, () -> product.getShop().getName())
                            .apply(VirtualLang.SELL_MENU_SALE_ENTRY.getString())
                        );
                    });
                })
            );
        }
    }

    @NotNull
    public SellResult bulkSell(@NotNull Player player, @NotNull Inventory inventory, @Nullable VirtualShop shop) {
        SellResult sellResult = new SellResult();

        Map<VirtualProduct, Integer> products = new HashMap<>();
        Map<ItemStack, Integer> distinctItems = new HashMap<>(); // Distinct itemStack map to find the best products more effectively.

        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType().isAir()) continue;

            if (VirtualConfig.SELL_CONTAINERS_CONTENT_INCLUDED.get()) {
                if (itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof BlockStateMeta stateMeta) {
                    if (stateMeta.getBlockState() instanceof Container container) {
                        // Do not handle stacked containers.
                        // Because of complex selling process (stocks, limits, dynamic prices, etc.) it's a mandatory to handle each item stack individually.
                        // Which means that each container's inventory must be handled individually as well.
                        // Which means that the ItemStack of said containers must be split to handle each container with it's inventory invididually.
                        // Which may result in theory in a up to 64 different ItemStacks split from said container, that should be given back to a player.
                        // So it seems more safer to disable this behavior.
                        if (itemStack.getAmount() == 1) {
                            sellResult.inherit(this.bulkSell(player, container.getInventory(), shop));
                            stateMeta.setBlockState(container);
                            itemStack.setItemMeta(stateMeta);
                        }
                        continue;
                    }
                }
            }

            ItemStack copy = new ItemStack(itemStack);
            copy.setAmount(1);

            int has = distinctItems.getOrDefault(copy, 0);
            distinctItems.put(copy, has + itemStack.getAmount());
        }

        distinctItems.forEach((itemStack, amount) -> {
            ItemStack copy = new ItemStack(itemStack);
            copy.setAmount(amount);

            VirtualProduct product = this.getBestProductFor(copy, TradeType.SELL, shop, player);
            if (product == null) return;

            int has = products.computeIfAbsent(product, k -> 0);

            products.put(product, has + amount);
        });

        sellResult.inherit(this.bulkSell(player, inventory, products));

        return sellResult;
    }

    @NotNull
    private SellResult bulkSell(@NotNull Player player, @NotNull Inventory inventory, @NotNull Map<VirtualProduct, Integer> products) {
        SellResult result = new SellResult();

        products.forEach((product, amount) -> {
            int units = UnitUtils.amountToUnits(product, amount);

            VirtualPreparedProduct preparedProduct = product.getPrepared(player, TradeType.SELL, false);
            preparedProduct.setUnits(units);
            preparedProduct.setInventory(inventory);
            preparedProduct.setSilent(true);

            Transaction transaction = preparedProduct.trade();
            result.addTransaction(product, amount, transaction);
        });

        return result;
    }
}
