package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.data.price.ProductPriceManager;
import su.nightexpress.nexshop.hooks.HookId;
import su.nightexpress.nexshop.module.ModuleId;
import su.nightexpress.nexshop.module.ShopModule;
import su.nightexpress.nexshop.shop.chest.command.*;
import su.nightexpress.nexshop.shop.chest.compatibility.ClaimHook;
import su.nightexpress.nexshop.shop.chest.compatibility.GriefPreventionHook;
import su.nightexpress.nexshop.shop.chest.compatibility.LandsHook;
import su.nightexpress.nexshop.shop.chest.compatibility.WorldGuardFlags;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.chest.impl.ChestShop;
import su.nightexpress.nexshop.shop.chest.listener.ChestShopListener;
import su.nightexpress.nexshop.shop.chest.menu.ChestListGlobalMenu;
import su.nightexpress.nexshop.shop.chest.menu.ChestListOwnMenu;
import su.nightexpress.nexshop.shop.chest.menu.ChestListSearchMenu;
import su.nightexpress.nexshop.shop.chest.nms.ChestNMS;
import su.nightexpress.nexshop.shop.chest.type.ChestShopType;

import java.util.*;
import java.util.stream.Stream;

public class ChestShopModule extends ShopModule {

    private ChestNMS  chestNMS;

    private Map<Location, ChestShop> chests;
    private Set<ClaimHook>           claimHooks;

    private ChestListOwnMenu    listOwnMenu;
    private ChestListGlobalMenu listGlobalMenu;
    private ChestListSearchMenu listSearchMenu;
    private ChestDisplayHandler displayHandler;

    public static final String DIR_SHOPS = "/shops/";

    public ChestShopModule(@NotNull ExcellentShop plugin) {
        super(plugin, ModuleId.CHEST_SHOP);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        this.chests = new HashMap<>();

        ChestConfig.load(this);
        if (ChestConfig.DEFAULT_CURRENCY == null) {
            this.interruptLoad("Invalid default currency!");
            return;
        }

        String pack = ChestNMS.class.getPackage().getName();
        Class<?> nmsClazz = Reflex.getClass(pack, Version.CURRENT.name());
        if (nmsClazz != null) {
            try {
                this.chestNMS = (ChestNMS) nmsClazz.getConstructor().newInstance();
                this.displayHandler = new ChestDisplayHandler(this);
                this.displayHandler.setup();
            }
            catch (ReflectiveOperationException ex) {
                this.error("Could not setup internal NMS handler! Shop display will be disabled.");
                ex.printStackTrace();
            }
        }

        // Setup Claim Hooks
        if (ChestConfig.SHOP_CREATION_CLAIM_ONLY) {
            this.claimHooks = new HashSet<>();
            if (Hooks.hasPlugin(HookId.LANDS)) this.claimHooks.add(new LandsHook(this.plugin));
            if (Hooks.hasPlugin(HookId.GRIEF_PREVENTION)) this.claimHooks.add(new GriefPreventionHook());
            if (Hooks.hasWorldGuard()) this.claimHooks.add(new WorldGuardFlags());
        }

        this.addListener(new ChestShopListener(this));

        this.listOwnMenu = new ChestListOwnMenu(this);
        this.listGlobalMenu = new ChestListGlobalMenu(this);
        this.listSearchMenu = new ChestListSearchMenu(this);

        this.moduleCommand.addChildren(new CreateCmd(this));
        this.moduleCommand.addChildren(new RemoveCmd(this));
        this.moduleCommand.addChildren(new ListCmd(this));
        this.moduleCommand.addChildren(new SearchCmd(this));
        this.moduleCommand.addChildren(new OpenCommand(this));

        this.plugin.runTask(c -> this.loadShops(), false);
    }

    public void loadShops() {
        for (JYML shopConfig : JYML.loadAll(this.getFullPath() + DIR_SHOPS, false)) {
            this.loadShop(shopConfig);
        }
        this.info("Shops Loaded: " + this.getShops().size());
    }

    public boolean loadShop(@NotNull JYML cfg) {
        ChestShop shop = new ChestShop(this, cfg);
        if (!shop.load()) {
            this.error("Shop not loaded '" + cfg.getFile().getName());
            if (ChestConfig.DELETE_INVALID_SHOP_CONFIGS && cfg.getFile().delete()) {
                this.info("Deleted invalid shop config.");
            }
            return false;
        }
        this.addShop(shop);
        ProductPriceManager.loadData(shop).thenRun(() -> shop.getProducts().forEach(product -> product.getPricer().update()));
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

        if (this.listOwnMenu != null) {
            this.listOwnMenu.clear();
            this.listOwnMenu = null;
        }
        if (this.listGlobalMenu != null) {
            this.listGlobalMenu.clear();
            this.listGlobalMenu = null;
        }
        if (this.listSearchMenu != null) {
            this.listSearchMenu.clear();
            this.listSearchMenu = null;
        }

        // Destroy shop editors and displays.
        this.getShops().forEach(ChestShop::clear);

        if (this.displayHandler != null) {
            this.displayHandler.shutdown();
            this.displayHandler = null;
        }
        if (this.chests != null) {
            this.chests.clear();
            this.chests = null;
        }
        if (this.claimHooks != null) {
            this.claimHooks.clear();
            this.claimHooks = null;
        }
    }

    @NotNull
    public ChestNMS getNMS() {
        return chestNMS;
    }

    @NotNull
    public ChestListOwnMenu getListOwnMenu() {
        return this.listOwnMenu;
    }

    @NotNull
    public ChestListGlobalMenu getListGlobalMenu() {
        return this.listGlobalMenu;
    }

    @NotNull
    public ChestListSearchMenu getListSearchMenu() {
        return this.listSearchMenu;
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

        if (!shop.isOwner(player) && !player.hasPermission(Perms.CHEST_SHOP_REMOVE_OTHERS)) {
            plugin.getMessage(ChestLang.SHOP_ERROR_NOT_OWNER).send(player);
            return false;
        }

        if (!this.payForRemoval(player)) {
            plugin.getMessage(ChestLang.SHOP_CREATION_ERROR_NOT_ENOUGH_FUNDS).send(player);
            return false;
        }
        
        for (ICurrency currency : ChestConfig.ALLOWED_CURRENCIES) {
            this.withdrawFromShop(player, shop, currency, -1);
        }

        this.removeShop(shop);
        plugin.getMessage(ChestLang.SHOP_REMOVAL_INFO_DONE).send(player);
        return true;
    }

    private void addShop(@NotNull ChestShop shop) {
        if (shop.getChestSides().isEmpty()) {
            this.getShopsMap().put(shop.getLocation(), shop);
        }
        else {
            shop.getChestSides().forEach(chest -> this.chests.put(chest.getLocation(), shop));
        }
        shop.setupView();
        shop.updateDisplay();
    }

    void removeShop(@NotNull ChestShop shop) {
        if (!shop.getFile().delete()) return;
        shop.clear();
        shop.getChestSides().stream().map(BlockState::getLocation).forEach(this.chests::remove);
    }

    public boolean depositToShop(@NotNull Player player, @NotNull ChestShop shop, @NotNull ICurrency currency, double amount) {
        if (!ChestConfig.ALLOWED_CURRENCIES.contains(currency)) {
            plugin.getMessage(ChestLang.SHOP_BANK_ERROR_INVALID_CURRENCY).send(player);
            return false;
        }

        if (amount < 0D) amount = currency.getBalance(player);

        if (currency.getBalance(player) < amount) {
            plugin.getMessage(ChestLang.SHOP_BANK_DEPOSIT_ERROR_NOT_ENOUGH).send(player);
            return false;
        }

        currency.take(player, amount);
        shop.getBank().deposit(currency, amount);
        shop.save();

        plugin.getMessage(ChestLang.SHOP_BANK_DEPOSIT_SUCCESS)
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
            .send(player);
        return true;
    }

    public boolean withdrawFromShop(@NotNull Player player, @NotNull ChestShop shop, @NotNull ICurrency currency, double amount) {
        if (!ChestConfig.ALLOWED_CURRENCIES.contains(currency)) {
            plugin.getMessage(ChestLang.SHOP_BANK_ERROR_INVALID_CURRENCY).send(player);
            return false;
        }

        if (amount < 0D) amount = shop.getBank().getBalance(currency);

        if (!shop.getBank().hasEnough(currency, amount)) {
            plugin.getMessage(ChestLang.SHOP_BANK_WITHDRAW_ERROR_NOT_ENOUGH).send(player);
            return false;
        }

        currency.give(player, amount);
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
        return this.chests;
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
        return ChestConfig.getMaxShops(player);
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
            .map(this::getShop)
            .filter(shop -> shop != null && !shop.isDoubleChest()).findFirst().orElse(null);
    }

    public boolean isShop(@NotNull Block block) {
        return this.isShop(block.getLocation());
    }

    public boolean isShop(@NotNull Location location) {
        return this.getShop(location) != null;
    }

    public static boolean isValidContainer(@NotNull Block block) {
        if (!(block.getState() instanceof Container container)) return false;
        if (block.getType() == Material.ENDER_CHEST) return false;
        return ChestConfig.ALLOWED_CONTAINERS.get().contains(block.getType().name());
    }

    public boolean isAllowedHere(@NotNull Player player, @NotNull Location location) {
        World world = location.getWorld();
        if (world == null) return true;

        String name = world.getName();
        if (ChestConfig.SHOP_CREATION_WORLD_BLACKLIST.contains(name)) {
            return false;
        }
        if (Hooks.hasPlugin(Hooks.WORLD_GUARD) && !WorldGuardFlags.checkFlag(player, location)) {
            return false;
        }
        return true;
    }

    public boolean isAllowedHereClaim(@NotNull Player player, @NotNull Block block) {
        // TODO Permission
        if (player.hasPermission(Perms.ADMIN) || !ChestConfig.SHOP_CREATION_CLAIM_ONLY || this.claimHooks.isEmpty()) return true;

        return this.claimHooks.stream().anyMatch(claim -> claim.isInOwnClaim(player, block));
    }

    private boolean payForCreate(@NotNull Player player) {
        return this.payForShop(player, ChestConfig.SHOP_CREATION_COST_CREATE);
    }

    private boolean payForRemoval(@NotNull Player player) {
        return this.payForShop(player, ChestConfig.SHOP_CREATION_COST_REMOVE);
    }

    private boolean payForShop(@NotNull Player player, double price) {
        if (price <= 0 || !VaultHook.hasEconomy()) return true;

        double balance = VaultHook.getBalance(player);
        if (balance < price) return false;

        VaultHook.takeMoney(player, price);
        return true;
    }
}
