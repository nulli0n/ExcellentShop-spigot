package su.nightexpress.nexshop.shop.virtual;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.TransactionLogger;
import su.nightexpress.nexshop.api.shop.TransactionModule;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.module.AbstractShopModule;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualShop;
import su.nightexpress.nexshop.shop.virtual.command.child.EditorCommand;
import su.nightexpress.nexshop.shop.virtual.command.child.MenuCommand;
import su.nightexpress.nexshop.shop.virtual.command.child.OpenCommand;
import su.nightexpress.nexshop.shop.virtual.command.standalone.SellAllCommand;
import su.nightexpress.nexshop.shop.virtual.command.standalone.SellHandCommand;
import su.nightexpress.nexshop.shop.virtual.command.standalone.SellMenuCommand;
import su.nightexpress.nexshop.shop.virtual.command.standalone.ShopCommand;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.editor.*;
import su.nightexpress.nexshop.shop.virtual.impl.*;
import su.nightexpress.nexshop.shop.virtual.listener.VirtualShopNPCListener;
import su.nightexpress.nexshop.shop.virtual.menu.MainMenu;
import su.nightexpress.nexshop.shop.virtual.menu.SellMenu;
import su.nightexpress.nexshop.shop.virtual.menu.ShopLayout;
import su.nightexpress.nexshop.shop.virtual.type.RotationType;
import su.nightexpress.nexshop.shop.virtual.type.ShopType;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.Menu;
import su.nightexpress.nightcore.menu.impl.AbstractMenu;
import su.nightexpress.nightcore.util.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class VirtualShopModule extends AbstractShopModule implements TransactionModule {

    public static final String ID                 = "virtual_shop";
    public static final String DIR_SHOPS          = "/shops/";
    public static final String DIR_ROTATING_SHOPS = "/rotating_shops/";
    public static final String DIR_LAYOUTS        = "/layouts/";

    private final Map<String, ShopLayout>   layoutMap;
    private final Map<String, StaticShop>   staticShopMap;
    private final Map<String, RotatingShop> rotatingShopMap;

    private MainMenu mainMenu;
    private SellMenu sellMenu;

    private DiscountListEditor  discountListEditor;
    private DiscountMainEditor  discountEditor;
    private ProductListEditor   productListEditor;
    private ProductMainEditor   productEditor;
    private ProductPriceEditor  productPriceEditor;
    private ProductStockEditor  productStockEditor;
    private RotationTimesEditor rotationTimesEditor;
    private ShopListEditor      shopListEditor;
    private ShopMainEditor      shopEditor;

    private TransactionLogger logger;

    public VirtualShopModule(@NotNull ShopPlugin plugin) {
        super(plugin, ID, Config.getVirtualShopAliases());

        this.layoutMap = new HashMap<>();
        this.staticShopMap = new HashMap<>();
        this.rotatingShopMap = new HashMap<>();
    }

    @Override
    protected void loadModule(@NotNull FileConfig config) {
        config.initializeOptions(VirtualConfig.class);

        this.plugin.getLangManager().loadEntries(VirtualLang.class);
        this.plugin.getLangManager().loadEntries(VirtualLocales.class);
        this.plugin.registerPermissions(VirtualPerms.class);
        this.logger = new TransactionLogger(this, config);

        // Create default shops & layouts.
        new ShopCreator(this.plugin, this).createDefaults();

        this.loadEditors();
        this.loadShops();
        this.loadLayouts();

        if (Plugins.isLoaded(HookId.CITIZENS)) {
            this.addListener(new VirtualShopNPCListener(this));
        }

        if (VirtualConfig.MAIN_MENU_ENABLED.get()) {
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
        if (this.productListEditor != null) this.productListEditor.clear();
        if (this.productEditor != null) this.productEditor.clear();
        if (this.productPriceEditor != null) this.productPriceEditor.clear();
        if (this.productStockEditor != null) this.productStockEditor.clear();
        if (this.rotationTimesEditor != null) this.rotationTimesEditor.clear();
        if (this.shopListEditor != null) this.shopListEditor.clear();
        if (this.shopEditor != null) this.shopEditor.clear();

        if (this.mainMenu != null) {
            this.mainMenu.clear();
            this.mainMenu = null; // Main menu is toggleable.
        }
        if (this.sellMenu != null) {
            this.sellMenu.clear();
            this.sellMenu = null; // Sell menu is toggleable.
        }

        this.getLayouts().forEach(ShopLayout::clear);
        this.layoutMap.clear();

        for (ShopType shopType : ShopType.values()) {
            this.getShopMap(shopType).clear();
        }
    }

    @Override
    protected void addCommands(@NotNull ChainedNodeBuilder builder) {
        OpenCommand.build(this, builder);
        EditorCommand.build(this, builder);

        if (VirtualConfig.MAIN_MENU_ENABLED.get()) {
            MenuCommand.build(this, builder);
        }
        if (VirtualConfig.SHOP_SHORTCUTS_ENABLED.get()) {
            this.plugin.getCommandManager().registerCommand(ShopCommand.create(this.plugin, this, VirtualConfig.SHOP_SHORTCUTS_COMMANDS.get()));
        }
        if (VirtualConfig.SELL_MENU_ENABLED.get()) {
            this.plugin.getCommandManager().registerCommand(SellMenuCommand.create(this.plugin, this, VirtualConfig.SELL_MENU_COMMANDS.get()));
        }
        if (VirtualConfig.SELL_ALL_ENABLED.get()) {
            this.plugin.getCommandManager().registerCommand(SellAllCommand.create(this.plugin, this, VirtualConfig.SELL_ALL_COMMANDS.get()));
        }
        if (VirtualConfig.SELL_HAND_ENABLED.get()) {
            this.plugin.getCommandManager().registerCommand(SellHandCommand.create(this.plugin, this, VirtualConfig.SELL_HAND_COMMANDS.get()));
        }
    }

    private void loadEditors() {
        this.discountListEditor = new DiscountListEditor(this.plugin, this);
        this.discountEditor = new DiscountMainEditor(this.plugin, this);
        this.productListEditor = new ProductListEditor(this.plugin, this);
        this.productEditor = new ProductMainEditor(this.plugin, this);
        this.productPriceEditor = new ProductPriceEditor(this.plugin, this);
        this.productStockEditor = new ProductStockEditor(this.plugin, this);
        this.rotationTimesEditor = new RotationTimesEditor(this.plugin, this);
        this.shopListEditor = new ShopListEditor(this.plugin, this);
        this.shopEditor = new ShopMainEditor(this.plugin, this);
    }

    private void loadShops() {
        for (ShopType shopType : ShopType.values()) {
            this.loadShops(shopType);
        }

        long delay = Plugins.isInstalled(HookId.ITEMS_ADDER) ? 100L : 10L; // because ItemsAdder loads too late
        this.plugin.runTaskAsync(task -> this.loadShopData());
        this.plugin.runTaskLater(task -> this.validateShopProducts(), delay);
    }

    private void loadShops(@NotNull ShopType shopType) {
        for (File folder : FileUtil.getFolders(this.getAbsolutePath() + getDirectory(shopType))) {
            String id = folder.getName();
            File file = new File(folder.getAbsolutePath(), AbstractVirtualShop.FILE_NAME);

            // ------------ LAYOUT MOVE START ------------
            File viewFile = new File(folder.getAbsolutePath(), "view.yml");
            if (viewFile.exists()) {
                File destination = new File(this.getAbsolutePath() + DIR_LAYOUTS, id + ".yml");
                viewFile.renameTo(destination);
            }
            // ------------ LAYOUT MOVE END ------------


            VirtualShop shop = this.initShop(shopType, file, id);
            this.loadShop(shop);
        }
        this.info(StringUtil.capitalizeFully(shopType.name()) + " Shops Loaded: " + this.getShopMap(shopType).size());
    }

    private void loadShop(@NotNull VirtualShop shop) {
        if (!shop.load()) {
            this.error("Shop not loaded: " + shop.getFile().getName());
            return;
        }

        if (shop instanceof StaticShop staticShop) {
            this.getStaticShopMap().put(shop.getId(), staticShop);
        }
        else if (shop instanceof RotatingShop rotatingShop) {
            this.getRotatingShopMap().put(shop.getId(), rotatingShop);
        }
    }

    private void loadLayouts() {
        for (FileConfig config : FileConfig.loadAll(this.getAbsolutePath() + DIR_LAYOUTS)) {
            String id = FileConfig.getName(config.getFile());
            ShopLayout layout = new ShopLayout(this.plugin, this, config);
            this.layoutMap.put(id, layout);
        }
        this.info("Loaded " + this.layoutMap.size() + " shop layouts!");
    }

    private void loadMainMenu() {
        this.mainMenu = new MainMenu(this.plugin, this);
    }

    public void loadShopData() {
        Collection<? extends Player> players = this.plugin.getServer().getOnlinePlayers();

        this.getShops().forEach(shop -> {
            shop.getPricer().load();
            shop.getStock().load();
            if (shop.getStock() instanceof VirtualStock virtualStock) {
                players.forEach(player -> virtualStock.load(player.getUniqueId()));
            }
            if (shop instanceof RotatingShop rotatingShop) {
                rotatingShop.loadData();
            }
        });
    }

    public void validateShopProducts() {
        //this.printShops();
        this.getShops().forEach(shop -> {
            shop.getProductMap().values().removeIf(product -> {
                if (product.getPacker() instanceof PluginItemPacker packer && !packer.isValidId(packer.getItemId())) {
                    this.error("Invalid item id for '" + product.getId() + "' product in '" + shop.getId() + "' shop!");
                    return true;
                }
                return false;
            });
            shop.setLoaded(true);
        });
    }

    @NotNull
    public static String getDirectory(@NotNull ShopType type) {
        return type == ShopType.STATIC ? DIR_SHOPS : DIR_ROTATING_SHOPS;
    }

    /*private void printShops() {
        this.getStaticShops().forEach(shop -> {
            this.info("=".repeat(30));
            this.info("Shop Id: " + shop.getId());

            List<StaticProduct> products = shop.getProducts().stream()
                .sorted(Comparator.comparing(StaticProduct::getPage).thenComparing(StaticProduct::getSlot))
                .toList();

            products.forEach(product -> {
                if (product.getPacker() instanceof VanillaItemPacker packer) {
                    AbstractProductPricer pricer = product.getPricer();

                    String buy = NumberUtil.format(pricer.getBuyPrice()).replace(",", "_");
                    String sell = NumberUtil.format(pricer.getSellPrice()).replace(",", "_");
                    int page = product.getPage();
                    int slot = product.getSlot();
                    String type = packer.getItem().getType().name();

                    String text = "this.addShopProduct(shop, Material." + type + ", " + buy + ", " + sell + ", " + page + ", " + slot + ");";

                    info(text);

                }
            });
            this.info("=".repeat(30));
        });
    }*/

    @NotNull
    private VirtualShop initShop(@NotNull ShopType shopType, @NotNull File file, @NotNull String id) {
        return shopType == ShopType.STATIC ? new StaticShop(this.plugin, this, file, id) : new RotatingShop(this.plugin, this, file, id);
    }

    @Override
    @NotNull
    public Currency getDefaultCurrency() {
        Currency currency = this.plugin.getCurrencyManager().getCurrency(VirtualConfig.DEFAULT_CURRENCY.get());
        return currency == null ? CurrencyManager.DUMMY_CURRENCY : currency;
    }

    @Override
    @NotNull
    public TransactionLogger getLogger() {
        return logger;
    }

    @NotNull
    public SellMenu getSellMenu() {
        return sellMenu;
    }

    @Nullable
    public MainMenu getMainMenu() {
        return mainMenu;
    }

    @NotNull
    public Map<String, ShopLayout> getLayoutMap() {
        return layoutMap;
    }

    @NotNull
    public Collection<ShopLayout> getLayouts() {
        return this.layoutMap.values();
    }

    @Nullable
    public ShopLayout getLayout(@NotNull VirtualShop shop) {
        return this.getLayout(shop.getLayoutName());
    }

    @Nullable
    public ShopLayout getLayout(@NotNull String id) {
        return this.layoutMap.get(id.toLowerCase());
    }

    @NotNull
    public List<String> getLayoutNames() {
        return new ArrayList<>(this.layoutMap.keySet());
    }

    @NotNull
    public Map<String, ? extends VirtualShop> getShopMap(@NotNull ShopType shopType) {
        return shopType == ShopType.STATIC ? this.getStaticShopMap() : this.getRotatingShopMap();
    }

    @NotNull
    public Set<VirtualShop> getShops() {
        Set<VirtualShop> shops = new HashSet<>();
        shops.addAll(this.getStaticShops());
        shops.addAll(this.getRotatingShops());
        return shops;
    }

    @NotNull
    public Collection<VirtualShop> getShops(@NotNull Player player) {
        return this.getShops(player, this.getShops());
    }

    @NotNull
    public Map<String, StaticShop> getStaticShopMap() {
        return this.staticShopMap;
    }

    @NotNull
    public Collection<StaticShop> getStaticShops() {
        return this.getStaticShopMap().values();
    }

    @NotNull
    public List<StaticShop> getStaticShops(@NotNull Player player) {
        return this.getShops(player, this.getStaticShops());
    }

    @Nullable
    public StaticShop getStaticShopById(@NotNull String id) {
        return this.getStaticShopMap().get(id.toLowerCase());
    }

    @NotNull
    public Map<String, RotatingShop> getRotatingShopMap() {
        return rotatingShopMap;
    }

    @NotNull
    public Collection<RotatingShop> getRotatingShops() {
        return this.getRotatingShopMap().values();
    }

    @NotNull
    public List<RotatingShop> getRotatingShops(@NotNull Player player) {
        return this.getShops(player, this.getRotatingShops());
    }

    @Nullable
    public RotatingShop getRotatingShopById(@NotNull String shopId) {
        return this.getRotatingShopMap().get(shopId.toLowerCase());
    }

    @Nullable
    public VirtualShop getShopById(@NotNull String shopId) {
        VirtualShop shop = this.getStaticShopById(shopId);
        if (shop == null) shop = this.getRotatingShopById(shopId);
        return shop;
    }

    @NotNull
    private <T extends VirtualShop> List<T> getShops(@NotNull Player player, @NotNull Collection<T> shops) {
        return shops.stream().filter(shop -> this.isAvailable(player, false) && shop.canAccess(player, false)).toList();
    }

    @Nullable
    public VirtualProduct getBestProduct(@NotNull Player player, @NotNull ItemStack item, @NotNull TradeType type, @Nullable VirtualShop shop) {
        return shop == null ? this.getBestProductFor(player, item, type) : shop.getBestProduct(player, item, type);
    }

    @Nullable
    public VirtualProduct getBestProductFor(@NotNull Player player, @NotNull ItemStack item, @NotNull TradeType tradeType) {
        Set<VirtualProduct> products = new HashSet<>();
        this.getShops().forEach(shop -> {
            VirtualProduct best = shop.getBestProduct(player, item, tradeType);
            if (best != null) {
                products.add(best);
            }
        });

        Comparator<VirtualProduct> comparator = Comparator.comparingDouble(product -> product.getPrice(player, tradeType));
        return (tradeType == TradeType.BUY ? products.stream().min(comparator) : products.stream().max(comparator)).orElse(null);
    }

    public boolean createShop(@NotNull String id, @NotNull ShopType type) {
        id = StringUtil.lowerCaseUnderscore(id);
        if (this.getShopById(id) != null) return false;

        File file = new File(this.getAbsolutePath() + getDirectory(type) + id, AbstractVirtualShop.FILE_NAME);
        VirtualShop shop = this.initShop(type, file, id);

        if (shop instanceof RotatingShop rotatingShop) {
            rotatingShop.setRotationType(RotationType.INTERVAL);
            rotatingShop.setRotationInterval(86400);
            rotatingShop.setProductMinAmount(6);
            rotatingShop.setProductMaxAmount(12);
            rotatingShop.setProductSlots(new int[] {10,11,12,13,14,15,16});
        }

        shop.setName(LIGHT_YELLOW.enclose(BOLD.enclose(StringUtil.capitalizeUnderscored(id))));
        shop.setDescription(Lists.newList(LIGHT_GRAY.enclose("Configure in " + LIGHT_GREEN.enclose("/vshop editor")), ""));
        shop.setIcon(new ItemStack(Material.CHEST_MINECART));
        shop.setLayoutName(Placeholders.DEFAULT);
        shop.save();
        this.loadShop(shop);
        shop.setLoaded(true);

        return true;
    }

    public boolean delete(@NotNull VirtualShop shop) {
        ShopType type = shop.getType();

        if (FileUtil.deleteRecursive(this.getAbsolutePath() + getDirectory(type) + shop.getId())) {
            this.getShopMap(type).remove(shop.getId());
            return true;
        }
        return false;
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

    public void updateShopMenu(@NotNull Player player, @NotNull VirtualShop shop) {
        Menu menu = AbstractMenu.getMenu(player);
        if (menu instanceof ShopLayout layout && layout.getLink(player) == shop) {
            menu.flush(player);
        }
    }

    public void openDiscountsEditor(@NotNull Player player, @NotNull StaticShop shop) {
        this.discountListEditor.open(player, shop);
    }

    public void openDiscountEditor(@NotNull Player player, @NotNull VirtualDiscount discount) {
        this.discountEditor.open(player, discount);
    }

    public void openProductsEditor(@NotNull Player player, @NotNull VirtualShop shop) {
        this.openProductsEditor(player, shop, 1);
    }

    public void openProductsEditor(@NotNull Player player, @NotNull VirtualShop shop, int page) {
        MenuViewer viewer = this.productListEditor.getViewerOrCreate(player);
        viewer.setPage(page);

        this.productListEditor.open(player, shop);
    }

    public void openProductEditor(@NotNull Player player, @NotNull VirtualProduct product) {
        this.productEditor.open(player, product);
    }

    public void openPriceEditor(@NotNull Player player, @NotNull VirtualProduct product) {
        this.productPriceEditor.open(player, product);
    }

    public void openStockEditor(@NotNull Player player, @NotNull VirtualProduct product) {
        this.productStockEditor.open(player, product);
    }

    public void openRotationTimesEditor(@NotNull Player player, @NotNull RotatingShop shop) {
        this.rotationTimesEditor.open(player, shop);
    }

    public void openShopsEditor(@NotNull Player player) {
        this.shopListEditor.open(player, this);
    }

    public void openShopEditor(@NotNull Player player, @NotNull VirtualShop shop) {
        this.shopEditor.open(player, shop);
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
        if (!shop.isLoaded()) return false;
        if (!force) {
            if (!this.isAvailable(player, true)) return false;
            if (!shop.canAccess(player, true)) return false;
        }

        ShopLayout layout = this.getLayout(shop);
        if (layout == null) layout = this.getLayout(VirtualConfig.DEFAULT_LAYOUT.get());
        if (layout == null) {
            VirtualLang.SHOP_ERROR_INVALID_LAYOUT.getMessage().replace(shop.replacePlaceholders()).send(player);
            return false;
        }

        MenuViewer viewer = layout.getViewerOrCreate(player);
        viewer.setPage(Math.abs(page));

        return layout.open(player, shop);
    }

    public boolean openMainMenu(@NotNull Player player) {
        return this.openMainMenu(player, false);
    }

    public boolean openMainMenu(@NotNull Player player, boolean force) {
        if (this.mainMenu == null) return false;

        if (!force) {
            if (!this.isAvailable(player, true)) return false;
        }

        return this.mainMenu.open(player);
    }

    public void openSellMenu(@NotNull Player player) {
        if (this.sellMenu == null) return;

        this.sellMenu.open(player, new ArrayList<>());
    }

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
        this.sellAll(player, player.getInventory());
    }

    public void sellAll(@NotNull Player player, @NotNull Inventory inventory) {
        this.sellAll(player, inventory, null);
    }

    public void sellAll(@NotNull Player player, @NotNull Inventory inventory, @Nullable VirtualShop shop) {
        Map<Currency, Double> profitMap = new HashMap<>();
        Map<ItemStack, Transaction> resultMap = new HashMap<>();
        Map<VirtualProduct, Integer> productAmountMap = new HashMap<>();

        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType().isAir()) continue;

            VirtualProduct product = this.getBestProduct(player, item, TradeType.SELL, shop);
            if (product == null) continue;

            int amount = item.getAmount();
            int has = productAmountMap.computeIfAbsent(product, k -> 0);

            productAmountMap.put(product, has + amount);
        }

        productAmountMap.forEach((product, amount) -> {
            int left = amount % product.getUnitAmount();
            int units = (amount - left) / product.getUnitAmount();

            VirtualPreparedProduct preparedProduct = product.getPrepared(player, TradeType.SELL, false);
            preparedProduct.setUnits(units);
            preparedProduct.setInventory(inventory);

            Transaction result = preparedProduct.trade();
            if (result.getResult() == Transaction.Result.SUCCESS) {
                ItemStack copy = new ItemStack(product.getPreview());
                copy.setAmount(amount);

                Currency currency = result.getProduct().getCurrency();
                double has = profitMap.getOrDefault(currency, 0D) + result.getPrice();
                profitMap.put(currency, has);
                resultMap.put(copy, result);
            }
        });
        if (profitMap.isEmpty()) return;

        String total = profitMap.entrySet().stream()
            .map(entry -> entry.getKey().format(entry.getValue()))
            .collect(Collectors.joining(", "));

        VirtualLang.SELL_MENU_SALE_RESULT.getMessage()
            .replace(Placeholders.GENERIC_TOTAL, total)
            .send(player);

        VirtualLang.SELL_MENU_SALE_DETAILS.getMessage()
            .replace(Placeholders.GENERIC_TOTAL, total)
            .replace(Placeholders.GENERIC_ENTRY, list -> {
                resultMap.forEach((item, result) -> {
                    list.add(VirtualLang.SELL_MENU_SALE_ENTRY.getString()
                        .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item))
                        .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(item.getAmount()))
                        .replace(Placeholders.GENERIC_PRICE, result.getProduct().getCurrency().format(result.getPrice()))
                    );
                });
            })
            .send(player);
    }
}
