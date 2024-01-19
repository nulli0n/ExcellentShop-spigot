package su.nightexpress.nexshop.shop.virtual;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.*;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.ShopModule;
import su.nightexpress.nexshop.api.shop.Transaction;
import su.nightexpress.nexshop.api.shop.TransactionLogger;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.packer.PluginItemPacker;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.module.AbstractShopModule;
import su.nightexpress.nexshop.shop.impl.AbstractVirtualShop;
import su.nightexpress.nexshop.shop.virtual.command.SellAllCommand;
import su.nightexpress.nexshop.shop.virtual.command.SellHandCommand;
import su.nightexpress.nexshop.shop.virtual.command.SellMenuCommand;
import su.nightexpress.nexshop.shop.virtual.command.ShopCommand;
import su.nightexpress.nexshop.shop.virtual.command.child.EditorCommand;
import su.nightexpress.nexshop.shop.virtual.command.child.MenuCommand;
import su.nightexpress.nexshop.shop.virtual.command.child.OpenCommand;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.config.VirtualPerms;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualLocales;
import su.nightexpress.nexshop.shop.virtual.editor.menu.ShopListEditor;
import su.nightexpress.nexshop.shop.virtual.impl.RotatingShop;
import su.nightexpress.nexshop.shop.virtual.impl.StaticShop;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualPreparedProduct;
import su.nightexpress.nexshop.shop.virtual.listener.VirtualShopNPCListener;
import su.nightexpress.nexshop.shop.virtual.menu.MainMenu;
import su.nightexpress.nexshop.shop.virtual.menu.SellMenu;
import su.nightexpress.nexshop.shop.virtual.type.RotationType;
import su.nightexpress.nexshop.shop.virtual.type.ShopType;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class VirtualShopModule extends AbstractShopModule implements ShopModule {

    public static final String ID                 = "virtual_shop";
    public static final String DIR_SHOPS          = "/shops/";
    public static final String DIR_ROTATING_SHOPS = "/rotating_shops/";

    private final Map<String, StaticShop>   staticShopMap;
    private final Map<String, RotatingShop> rotatingShopMap;

    private MainMenu       mainMenu;
    private SellMenu       sellMenu;
    private ShopListEditor editor;
    private TransactionLogger logger;

    public VirtualShopModule(@NotNull ExcellentShop plugin) {
        super(plugin, ID);
        this.staticShopMap = new HashMap<>();
        this.rotatingShopMap = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        this.plugin.getLangManager().loadMissing(VirtualLang.class);
        this.plugin.getLangManager().loadEnum(ShopType.class);
        this.plugin.getLangManager().loadEditor(VirtualLocales.class);
        this.plugin.getLang().saveChanges();
        this.cfg.initializeOptions(VirtualConfig.class);
        this.plugin.registerPermissions(VirtualPerms.class);
        this.logger = new TransactionLogger(this);

        File dir = new File(this.getAbsolutePath() + DIR_SHOPS);
        if (!dir.exists()) {
            for (String id : new String[]{"blocks", "brewing", "farmers_market", "fish_market", "food", "hostile_loot", "peaceful_loot", "tools", "weapons", "wool"}) {
                this.plugin.getConfigManager().extractResources("/" + this.getLocalPath() + DIR_SHOPS + id);
            }
        }

        File dir2 = new File(this.getAbsolutePath() + DIR_ROTATING_SHOPS);
        if (!dir2.exists()) {
            for (String id : new String[]{"traveller"}) {
                this.plugin.getConfigManager().extractResources("/" + this.getLocalPath() + DIR_ROTATING_SHOPS + id);
            }
        }

        this.loadStaticShops();
        this.loadRotatingShops();
        this.loadMainMenu();
        this.plugin.runTaskAsync(task -> this.loadShopData());
        this.plugin.runTaskLater(task -> this.validateShopProducts(), 100L); // because IA loads too late

        if (EngineUtils.hasPlugin(HookId.CITIZENS)) {
            this.addListener(new VirtualShopNPCListener(this));
        }

        this.command.addChildren(new OpenCommand(this));
        this.command.addChildren(new EditorCommand(this));

        if (VirtualConfig.MAIN_MENU_ENABLED.get()) {
            this.command.addChildren(new MenuCommand(this));
        }
        if (VirtualConfig.SELL_MENU_ENABLED.get()) {
            this.sellMenu = new SellMenu(this, JYML.loadOrExtract(plugin, this.getLocalPath(), "sell.menu.yml"));
            this.plugin.getCommandManager().registerCommand(new SellMenuCommand(this, VirtualConfig.SELL_MENU_COMMANDS.get().split(",")));
        }
        if (!VirtualConfig.SHOP_SHORTCUTS.get().isEmpty()) {
            this.plugin.getCommandManager().registerCommand(new ShopCommand(this));
        }
        if (!VirtualConfig.SELL_ALL_COMMANDS.get().isEmpty()) {
            this.plugin.getCommandManager().registerCommand(new SellAllCommand(this, VirtualConfig.SELL_ALL_COMMANDS.get().split(",")));
        }
        if (!VirtualConfig.SELL_HAND_COMMANDS.get().isEmpty()) {
            this.plugin.getCommandManager().registerCommand(new SellHandCommand(this, VirtualConfig.SELL_HAND_COMMANDS.get().split(",")));
        }
    }

    @Override
    protected void onShutdown() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
        if (this.mainMenu != null) {
            this.mainMenu.clear();
            this.mainMenu = null;
        }
        if (this.sellMenu != null) {
            this.sellMenu.clear();
            this.sellMenu = null;
        }
        this.getStaticShops().forEach(StaticShop::clear);
        this.getStaticShopMap().clear();
        this.getRotatingShops().forEach(RotatingShop::clear);
        this.getRotatingShopMap().clear();
        super.onShutdown();
    }

    private void loadStaticShops() {
        for (File folder : FileUtil.getFolders(this.getAbsolutePath() + DIR_SHOPS)) {
            String id = folder.getName();

            // ---------- OLD DATA START ----------
            File fileOld = new File(folder.getAbsolutePath(), folder.getName() + ".yml");
            File fileNew = new File(folder.getAbsolutePath(), "config.yml");
            if (fileOld.exists() && !fileNew.exists()) {
                if (!fileOld.renameTo(fileNew)) {
                    this.error("Unable to rename shop config file: " + fileOld.getName());
                    continue;
                }
            }
            // ---------- OLD DATA END ----------

            StaticShop shop = new StaticShop(this, new JYML(fileNew), id);
            if (shop.load()) {
                this.getStaticShopMap().put(shop.getId(), shop);
            }
            else this.error("Shop not loaded: " + shop.getFile().getName());
        }
        this.info("Static Shops Loaded: " + this.getStaticShopMap().size());
    }

    private void loadRotatingShops() {
        for (File folder : FileUtil.getFolders(this.getAbsolutePath() + DIR_ROTATING_SHOPS)) {
            String id = folder.getName();
            File fileNew = new File(folder.getAbsolutePath(), "config.yml");

            RotatingShop shop = new RotatingShop(this, new JYML(fileNew), id);
            if (shop.load()) {
                this.getRotatingShopMap().put(shop.getId(), shop);
            }
            else this.warn("Shop not loaded: " + shop.getFile().getName());
        }
        this.info("Rotating Shops Loaded: " + this.getRotatingShopMap().size());
    }

    private void loadMainMenu() {
        if (this.mainMenu != null) {
            this.mainMenu.clear();
            this.mainMenu = null;
        }

        if (!VirtualConfig.MAIN_MENU_ENABLED.get()) return;
        this.mainMenu = new MainMenu(this);
    }

    public void loadShopData() {
        this.getShops().forEach(shop -> {
            shop.getPricer().load();
            shop.getStock().load();
            if (shop instanceof RotatingShop rotatingShop) {
                rotatingShop.loadData();
            }
        });
    }

    public void validateShopProducts() {
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

    public boolean createShop(@NotNull String id, @NotNull ShopType type) {
        id = StringUtil.lowerCaseUnderscore(id);
        if (this.getShopById(id) != null) return false;

        JYML cfg = new JYML(this.getAbsolutePath() + getDirectory(type) + id, "config.yml");

        AbstractVirtualShop<?> shop;
        if (type == ShopType.STATIC) {
            shop = new StaticShop(this, cfg, id);
            this.getStaticShopMap().put(shop.getId(), (StaticShop) shop);
        }
        else {
            RotatingShop rotatingShop = new RotatingShop(this, cfg, id);
            rotatingShop.setRotationType(RotationType.INTERVAL);
            rotatingShop.setRotationInterval(86400);
            rotatingShop.setProductMinAmount(6);
            rotatingShop.setProductMaxAmount(12);
            rotatingShop.setProductSlots(new int[] {10,11,12,13,14,15,16});
            shop = rotatingShop;
            this.getRotatingShopMap().put(shop.getId(), (RotatingShop) shop);
        }
        shop.setName(Colors.YELLOW + Colors.BOLD + StringUtil.capitalizeUnderscored(id));
        shop.setDescription(Arrays.asList(Colors.GRAY + "Configure in " + Colors.GREEN + "/vshop editor", ""));
        shop.setIcon(new ItemStack(Material.CHEST_MINECART));
        shop.save();
        shop.load();
        return true;
    }

    public boolean isAvailable(@NotNull Player player, boolean notify) {
        if (!player.hasPermission(VirtualPerms.BYPASS_WORLDS)) {
            if (VirtualConfig.DISABLED_WORLDS.get().contains(player.getWorld().getName())) {
                if (notify) plugin.getMessage(VirtualLang.SHOP_ERROR_BAD_WORLD).send(player);
                return false;
            }
        }

        if (!player.hasPermission(VirtualPerms.BYPASS_GAMEMODE)) {
            if (VirtualConfig.DISABLED_GAMEMODES.get().contains(player.getGameMode().name())) {
                if (notify) plugin.getMessage(VirtualLang.SHOP_ERROR_BAD_GAMEMODE)
                    .replace(Placeholders.GENERIC_TYPE, plugin.getLangManager().getEnum(player.getGameMode()))
                    .send(player);
                return false;
            }
        }

        return true;
    }

    @NotNull
    public ShopListEditor getEditor() {
        if (this.editor == null) {
            this.editor = new ShopListEditor(this);
        }
        return editor;
    }

    @Override
    @NotNull
    public Currency getDefaultCurrency() {
        Currency currency = this.plugin.getCurrencyManager().getCurrency(VirtualConfig.DEFAULT_CURRENCY.get());
        return currency == null ? CurrencyManager.DUMMY : currency;
    }

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
    public static String getDirectory(@NotNull ShopType type) {
        return type == ShopType.STATIC ? DIR_SHOPS : DIR_ROTATING_SHOPS;
    }

    public boolean delete(@NotNull VirtualShop shop) {
        ShopType type = shop.getType();
        Map<String, ? extends VirtualShop> map = type == ShopType.STATIC ? this.getStaticShopMap() : this.getRotatingShopMap();

        if (FileUtil.deleteRecursive(this.getAbsolutePath() + getDirectory(type) + shop.getId())) {
            shop.clear();
            map.remove(shop.getId());
            this.loadMainMenu();
            return true;
        }
        return false;
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
        return shops.stream().filter(shop -> shop.canAccess(player, false)).toList();
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

    public static double getSellMultiplier(@NotNull Player player) {
        return VirtualConfig.SELL_RANK_MULTIPLIERS.get().getBestValue(player, 1D);
    }

    public void sellWithReturn(@NotNull Player player, @NotNull Inventory inventory) {
        this.sellAll(player, inventory);

        for (ItemStack left : inventory.getContents()) {
            if (left == null || left.getType().isAir() || left.getAmount() < 1) continue;

            PlayerUtil.addItem(player, left);
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

        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType().isAir()) continue;

            VirtualProduct product;
            if (shop == null) product = this.getBestProductFor(player, item, TradeType.SELL);
            else product = shop.getBestProduct(player, item, TradeType.SELL);
            if (product == null) continue;

            VirtualPreparedProduct preparedProduct = product.getPrepared(player, TradeType.SELL, true);
            preparedProduct.setInventory(inventory);

            ItemStack copy = new ItemStack(item);

            Transaction result = preparedProduct.trade();
            if (result.getResult() == Transaction.Result.SUCCESS) {
                Currency currency = result.getProduct().getCurrency();
                double has = profitMap.getOrDefault(currency, 0D) + result.getPrice();
                profitMap.put(currency, has);
                resultMap.put(copy, result);
            }
        }
        if (profitMap.isEmpty()) return;

        String total = profitMap.entrySet().stream()
            .map(entry -> entry.getKey().format(entry.getValue()))
            .collect(Collectors.joining(", "));

        this.plugin.getMessage(VirtualLang.SELL_MENU_SALE_RESULT)
            .replace(Placeholders.GENERIC_TOTAL, total)
            .replace(str -> str.contains(Placeholders.GENERIC_ITEM), (str, list) -> {
                resultMap.forEach((item, result) -> {
                    list.add(str
                        .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item))
                        .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(item.getAmount()))
                        .replace(Placeholders.GENERIC_PRICE, result.getProduct().getCurrency().format(result.getPrice()))
                    );
                });
            })
            .send(player);
    }
}
