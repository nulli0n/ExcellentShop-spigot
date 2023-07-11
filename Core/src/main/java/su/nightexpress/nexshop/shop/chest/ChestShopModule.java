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
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.integration.VaultHook;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.Pair;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.data.price.ProductPriceStorage;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.nms.v1_20_R1.V1_20_R1;
import su.nightexpress.nexshop.shop.chest.command.*;
import su.nightexpress.nexshop.shop.chest.compatibility.ClaimHook;
import su.nightexpress.nexshop.shop.chest.compatibility.GriefPreventionHook;
import su.nightexpress.nexshop.shop.chest.compatibility.LandsHook;
import su.nightexpress.nexshop.shop.chest.compatibility.WorldGuardFlags;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.listener.ChestShopListener;
import su.nightexpress.nexshop.shop.chest.menu.ShopsListMenu;
import su.nightexpress.nexshop.shop.chest.menu.ShopsSearchMenu;
import su.nightexpress.nexshop.shop.chest.nms.ChestNMS;
import su.nightexpress.nexshop.shop.chest.nms.V1_17_R1;
import su.nightexpress.nexshop.shop.chest.nms.V1_18_R2;
import su.nightexpress.nexshop.shop.chest.nms.V1_19_R3;
import su.nightexpress.nexshop.shop.chest.type.ChestShopType;
import su.nightexpress.nexshop.shop.module.ShopModule;
import su.nightexpress.nexshop.shop.util.TransactionLogger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChestShopModule extends ShopModule {

    public static final String ID = "chest_shop";
    public static final String DIR_SHOPS = "/shops/";

    public static Currency       DEFAULT_CURRENCY;
    public static Set<Currency> ALLOWED_CURRENCIES;

    private final Map<Location, ChestShop> shops;

    private Set<ClaimHook> claimHooks;
    private ShopsListMenu   listMenu;
    private ShopsSearchMenu searchMenu;
    private ChestDisplayHandler displayHandler;
    private ChestNMS  chestNMS;
    private TransactionLogger logger;

    public ChestShopModule(@NotNull ExcellentShop plugin) {
        super(plugin, ID);
        this.shops = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        this.getConfig().initializeOptions(ChestConfig.class);

        DEFAULT_CURRENCY = plugin.getCurrencyManager().getCurrency(ChestConfig.DEFAULT_CURRENCY.get());
        ALLOWED_CURRENCIES = ChestConfig.ALLOWED_CURRENCIES.get().stream().map(String::toLowerCase)
            .map(currencyId -> plugin.getCurrencyManager().getCurrency(currencyId))
            .filter(Objects::nonNull).collect(Collectors.toSet());

        if (DEFAULT_CURRENCY == null || ALLOWED_CURRENCIES.isEmpty()) {
            this.error("No default/allowed currencies!");
            return;
        }

        this.plugin.getLangManager().loadMissing(ChestLang.class);
        this.plugin.getLangManager().setupEnum(ChestShopType.class);
        this.plugin.getLang().saveChanges();
        this.plugin.registerPermissions(ChestPerms.class);

        this.chestNMS = switch (Version.getCurrent()) {
            case V1_19_R1, V1_19_R2, UNKNOWN -> null;

            case V1_17_R1 -> new V1_17_R1();
            case V1_18_R2 -> new V1_18_R2();
            case V1_19_R3 -> new V1_19_R3();
            case V1_20_R1 -> new V1_20_R1();
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
        }

        this.addListener(new ChestShopListener(this));

        this.listMenu = new ShopsListMenu(this);
        this.searchMenu = new ShopsSearchMenu(this);

        this.command.addChildren(new CreateCommand(this));
        this.command.addChildren(new RemoveCommand(this));
        this.command.addChildren(new ListCommand(this));
        this.command.addChildren(new SearchCommand(this));
        this.command.addChildren(new OpenCommand(this));

        this.plugin.runTask(task -> this.loadShops());
    }

    public void loadShops() {
        for (JYML shopConfig : JYML.loadAll(this.getAbsolutePath() + DIR_SHOPS, false)) {
            this.loadShop(shopConfig);
        }
        this.info("Shops Loaded: " + this.getShops().size());
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
        this.addShop(shop);
        ProductPriceStorage.loadData(shop).thenRun(() -> shop.getProducts().forEach(product -> product.getPricer().update()));
        return true;
    }

    public boolean unloadShop(@NotNull ChestShop shop) {
        shop.clear();
        this.getShopsMap().remove(shop.getLocation());
        return true;
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();

        if (this.listMenu != null) {
            this.listMenu.clear();
            this.listMenu = null;
        }
        if (this.searchMenu != null) {
            this.searchMenu.clear();
            this.searchMenu = null;
        }

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

        this.shops.clear();
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
    public ShopsListMenu getListMenu() {
        return this.listMenu;
    }

    @NotNull
    public ShopsSearchMenu getSearchMenu() {
        return this.searchMenu;
    }

    public boolean createShop(@NotNull Player player, @NotNull Block block, @NotNull ChestShopType type) {
        if (!isValidContainer(block)) {
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

        Container bChest = (Container) block.getState();
        Inventory cInv = bChest.getInventory();
        if (Stream.of(cInv.getContents()).anyMatch(inside -> inside != null && !inside.getType().isAir())) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_NOT_EMPTY).send(player);
            return false;
        }

        int allowed = this.getShopsAllowed(player);
        int has = this.getShopsAmount(player);
        if (allowed > 0 && has >= allowed) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_LIMIT_REACHED).send(player);
            return false;
        }

        if (!this.payForCreate(player)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS).send(player);
            return false;
        }

        ItemStack hand = new ItemStack(player.getInventory().getItemInMainHand());
        ChestShop shop = new ChestShop(this, player, bChest, type);
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
        
        for (Currency currency : ALLOWED_CURRENCIES) {
            this.withdrawFromShop(player, shop, currency, -1);
        }

        this.removeShop(shop);
        plugin.getMessage(ChestLang.SHOP_REMOVAL_INFO_DONE).send(player);
        return true;
    }

    private void addShop(@NotNull ChestShop shop) {
        Pair<Container, Container> sides = shop.getSides();
        this.getShopsMap().put(sides.getFirst().getLocation(), shop);
        this.getShopsMap().put(sides.getSecond().getLocation(), shop);
        shop.updateDisplay();
    }

    void removeShop(@NotNull ChestShop shop) {
        if (!shop.getFile().delete()) return;
        shop.clear();

        Pair<Container, Container> sides = shop.getSides();
        this.getShopsMap().remove(sides.getFirst().getLocation());
        this.getShopsMap().remove(sides.getSecond().getLocation());
        //shop.getChestSides().stream().map(BlockState::getLocation).forEach(this.shops::remove);
    }

    public boolean depositToShop(@NotNull Player player, @NotNull ChestShop shop, @NotNull Currency currency, double amount) {
        if (!isAllowedCurrency(currency)) {
            plugin.getMessage(ChestLang.SHOP_BANK_ERROR_INVALID_CURRENCY).send(player);
            return false;
        }

        if (amount < 0D) amount = currency.getHandler().getBalance(player);

        if (currency.getHandler().getBalance(player) < amount) {
            plugin.getMessage(ChestLang.SHOP_BANK_DEPOSIT_ERROR_NOT_ENOUGH).send(player);
            return false;
        }

        currency.getHandler().take(player, amount);
        shop.getBank().deposit(currency, amount);
        shop.save();

        plugin.getMessage(ChestLang.SHOP_BANK_DEPOSIT_SUCCESS)
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
            .send(player);
        return true;
    }

    public boolean withdrawFromShop(@NotNull Player player, @NotNull ChestShop shop, @NotNull Currency currency, double amount) {
        if (!isAllowedCurrency(currency)) {
            plugin.getMessage(ChestLang.SHOP_BANK_ERROR_INVALID_CURRENCY).send(player);
            return false;
        }

        if (amount < 0D) amount = shop.getBank().getBalance(currency);

        if (!shop.getBank().hasEnough(currency, amount)) {
            plugin.getMessage(ChestLang.SHOP_BANK_WITHDRAW_ERROR_NOT_ENOUGH).send(player);
            return false;
        }

        currency.getHandler().give(player, amount);
        shop.getBank().withdraw(currency, amount);
        shop.save();

        plugin.getMessage(ChestLang.SHOP_BANK_WITHDRAW_SUCCESS)
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
            .send(player);
        return true;
    }

    @Nullable
    public ChestDisplayHandler getDisplayHandler() {
        return this.displayHandler;
    }

    @NotNull
    public Map<Location, ChestShop> getShopsMap() {
        return this.shops;
    }

    @NotNull
    public Collection<ChestShop> getShops() {
        return this.getShopsMap().values();
    }

    @NotNull
    public List<ChestShop> getShops(@NotNull Player player) {
        return this.getShops().stream().filter(shop -> shop.getOwnerId().equals(player.getUniqueId())).toList();
    }

    public int getShopsAllowed(@NotNull Player player) {
        return getShopLimit(player);
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
        return this.getShopsMap().get(location);
    }

    @Nullable
    public ChestShop getShopSideChest(@NotNull Block block) {
        BlockData data = block.getBlockData();
        if (!(data instanceof Directional directional)) return null;
        if (!(block.getState() instanceof Chest chest)) return null;

        BlockFace face = directional.getFacing();
        BlockFace[] faces;
        if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
            faces = new BlockFace[]{BlockFace.EAST, BlockFace.WEST};
        }
        else {
            faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH};
        }

        return Stream.of(faces).map(block::getRelative).filter(near -> {
            return near.getBlockData() instanceof Directional nearDir && nearDir.getFacing() == face;
        })
            .map(this::getShop).filter(shop -> shop != null && !shop.isDoubleChest()).findFirst().orElse(null);
    }

    public boolean isShop(@NotNull Block block) {
        return this.isShop(block.getLocation());
    }

    public boolean isShop(@NotNull Location location) {
        return this.getShop(location) != null;
    }

    public static int getShopLimit(@NotNull Player player) {
        return ChestConfig.SHOP_CREATION_MAX_PER_RANK.get().getBestValue(player, 0);
    }

    public static int getProductLimit(@NotNull Player player) {
        return ChestConfig.SHOP_PRODUCTS_MAX_PER_RANK.get().getBestValue(player, 0);
    }

    @NotNull
    public static List<String> getHologramLines(@NotNull ChestShopType chestType) {
        return ChestConfig.DISPLAY_TEXT.get().getOrDefault(chestType, Collections.emptyList());
    }

    public static boolean isAllowedCurrency(@NotNull Currency currency) {
        return ALLOWED_CURRENCIES.contains(currency);
    }

    public static boolean isAllowedItem(@NotNull ItemStack item) {
        if (ChestConfig.SHOP_PRODUCT_DENIED_MATERIALS.get().contains(item.getType())) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                String name = meta.getDisplayName();
                if (ChestConfig.SHOP_PRODUCT_DENIED_NAMES.get().stream().anyMatch(name::contains)) {
                    return false;
                }
            }
            List<String> lore = meta.getLore();
            if (lore != null) {
                return lore.stream().noneMatch(line -> ChestConfig.SHOP_PRODUCT_DENIED_LORES.get().stream().anyMatch(line::contains));
            }
        }
        return true;
    }

    public static boolean isValidContainer(@NotNull Block block) {
        if (!(block.getState() instanceof Container container)) return false;
        if (block.getType() == Material.ENDER_CHEST) return false;
        return ChestConfig.ALLOWED_CONTAINERS.get().contains(block.getType());
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
