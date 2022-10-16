package su.nightexpress.nexshop.shop.chest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.api.shop.chest.IShopChest;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.hooks.HookId;
import su.nightexpress.nexshop.module.ModuleId;
import su.nightexpress.nexshop.module.ShopModule;
import su.nightexpress.nexshop.shop.chest.command.CreateCmd;
import su.nightexpress.nexshop.shop.chest.command.ListCmd;
import su.nightexpress.nexshop.shop.chest.command.RemoveCmd;
import su.nightexpress.nexshop.shop.chest.command.SearchCmd;
import su.nightexpress.nexshop.shop.chest.compatibility.ClaimHook;
import su.nightexpress.nexshop.shop.chest.compatibility.GriefPreventionHook;
import su.nightexpress.nexshop.shop.chest.compatibility.LandsHook;
import su.nightexpress.nexshop.shop.chest.compatibility.WorldGuardFlags;
import su.nightexpress.nexshop.shop.chest.config.ChestConfig;
import su.nightexpress.nexshop.shop.chest.listener.ChestShopListener;
import su.nightexpress.nexshop.shop.chest.menu.ChestListGlobalMenu;
import su.nightexpress.nexshop.shop.chest.menu.ChestListOwnMenu;
import su.nightexpress.nexshop.shop.chest.menu.ChestListSearchMenu;
import su.nightexpress.nexshop.shop.chest.nms.ChestNMS;
import su.nightexpress.nexshop.shop.chest.object.ShopChest;
import su.nightexpress.nexshop.shop.chest.task.ChestProductTask;
import su.nightexpress.nexshop.shop.chest.type.ChestType;

import java.util.*;
import java.util.stream.Stream;

public class ChestShop extends ShopModule {

    private ChestNMS  chestNMS;

    private Map<Location, IShopChest> chests;
    private Set<ClaimHook>            claimHooks;

    private ChestListOwnMenu    listOwnMenu;
    private ChestListGlobalMenu listGlobalMenu;
    private ChestListSearchMenu listSearchMenu;

    private ChestDisplayHandler displayHandler;

    private ChestProductTask productTask;

    public static final String DIR_SHOPS = "/shops/";

    public ChestShop(@NotNull ExcellentShop plugin) {
        super(plugin, ModuleId.CHEST_SHOP);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "editor");
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
            this.claimHooks.add(this.plugin.registerHook(HookId.LANDS, LandsHook.class));
            this.claimHooks.add(this.plugin.registerHook(HookId.GRIEF_PREVENTION, GriefPreventionHook.class));
            this.claimHooks.add(this.plugin.registerHook(Hooks.WORLD_GUARD, WorldGuardFlags.class));
            this.claimHooks.removeIf(Objects::isNull);
        }

        this.addListener(new ChestShopListener(this));

        this.listOwnMenu = new ChestListOwnMenu(this);
        this.listGlobalMenu = new ChestListGlobalMenu(this);
        this.listSearchMenu = new ChestListSearchMenu(this);

        this.moduleCommand.addChildren(new CreateCmd(this));
        this.moduleCommand.addChildren(new RemoveCmd(this));
        this.moduleCommand.addChildren(new ListCmd(this));
        this.moduleCommand.addChildren(new SearchCmd(this));

        this.plugin.runTask(c -> this.loadShops(), false);

        this.productTask = new ChestProductTask(this);
        this.productTask.start();
    }

    public void loadShops() {
        for (JYML shopConfig : JYML.loadAll(this.getFullPath() + DIR_SHOPS, true)) {
            try {
                IShopChest shopChest = new ShopChest(this, shopConfig);
                if (this.isShop(shopChest.getLocation())) continue;

                this.addShop(shopChest);
            }
            catch (Exception e) {
                this.error("Could not load shop '" + shopConfig.getFile().getName() + "': " + e.getMessage());
                e.printStackTrace();

                if (ChestConfig.DELETE_INVALID_SHOP_CONFIGS && shopConfig.getFile().delete()) {
                    this.info("Deleted invalid shop config.");
                }
            }
        }
        this.info("Shops Loaded: " + this.getShops().size());
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();

        if (this.productTask != null) {
            this.productTask.stop();
            this.productTask = null;
        }
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
        this.getShops().forEach(IShopChest::clear);

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

    public boolean createShop(@NotNull Player player, @NotNull Block block, @NotNull ChestType type) {
        if (!isValidChest(block)) {
            plugin.getMessage(Lang.Shop_Creation_Error_NotAChest).send(player);
            return false;
        }

        if (this.isShop(block)) {
            plugin.getMessage(Lang.Shop_Creation_Error_AlreadyShop).send(player);
            return false;
        }

        if (!this.isAllowedHere(player, block.getLocation())) {
            plugin.getMessage(Lang.Shop_Creation_Error_BadLocation).send(player);
            return false;
        }

        if (!this.isAllowedHereClaim(player, block)) {
            plugin.getMessage(Lang.Shop_Creation_Error_BadArea).send(player);
            return false;
        }

        if (!type.hasPermission(player)) {
            plugin.getMessage(Lang.Shop_Creation_Error_TypePermission).send(player);
            return false;
        }

        ItemStack hand = new ItemStack(player.getInventory().getItemInMainHand());

        BlockFace face = block.getFace(player.getLocation().getBlock());
        if (face == null) face = BlockFace.NORTH;

        // Set fake item to the hand to bypass custom item event cancellation.
        player.getInventory().setItemInMainHand(new ItemStack(Material.STONE));

        // Check for protection plugins interaction.
        PlayerInteractEvent e2 = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.STONE), block, face);
        plugin.getPluginManager().callEvent(e2);

        // Set original item back.
        player.getInventory().setItemInMainHand(hand);

        if (e2.useInteractedBlock() == Result.DENY) {
            plugin.getMessage(Lang.Shop_Creation_Error_BadArea).send(player);
            return false;
        }

        Chest bChest = (Chest) block.getState();
        Inventory cInv = bChest.getInventory();
        if (Stream.of(cInv.getContents()).anyMatch(inside -> inside != null && !inside.getType().isAir())) {
            plugin.getMessage(Lang.Shop_Creation_Error_NotEmpty).send(player);
            return false;
        }

        int allowed = this.getShopsAllowed(player);
        int has = this.getShopsAmount(player);
        if (allowed > 0 && has >= allowed) {
            plugin.getMessage(Lang.Shop_Creation_Error_LimitReached).send(player);
            return false;
        }

        if (!this.payForCreate(player)) {
            plugin.getMessage(Lang.Shop_Creation_Error_NotEnoughFunds).send(player);
            return false;
        }

        IShopChest shop = new ShopChest(this, player, bChest, UUID.randomUUID(), type);
        shop.createProduct(player, hand);
        shop.save();

        this.addShop(shop);
        plugin.getMessage(Lang.Shop_Creation_Info_Done).send(player);
        return true;
    }

    public boolean deleteShop(@NotNull Player player, @NotNull Block block) {
        IShopChest shop = this.getShop(block);
        if (shop == null) {
            plugin.getMessage(Lang.Shop_Removal_Error_NotAShop).send(player);
            return false;
        }

        if (!shop.isOwner(player) && !player.hasPermission(Perms.CHEST_REMOVE_OTHERS)) {
            plugin.getMessage(Lang.Shop_Error_NotOwner).send(player);
            return false;
        }

        if (!this.payForRemoval(player)) {
            plugin.getMessage(Lang.Shop_Creation_Error_NotEnoughFunds).send(player);
            return false;
        }
        
        for (ICurrency currency : ChestConfig.ALLOWED_CURRENCIES) {
            this.withdrawFromShop(player, shop, currency, -1);
        }

        this.removeShop(shop);
        plugin.getMessage(Lang.Shop_Removal_Info_Done).send(player);
        return true;
    }

    private void addShop(@NotNull IShopChest shop) {
        shop.getSides().forEach(chest -> this.chests.put(chest.getLocation(), shop));
        shop.setupView();
        shop.updateDisplay();
    }

    void removeShop(@NotNull IShopChest shop) {
        if (shop instanceof AbstractLoadableItem<?> item) {
            if (!item.getFile().delete()) return;
        }
        shop.clear();
        shop.getSides().stream().map(BlockState::getLocation).forEach(this.chests::remove);
    }

    public boolean depositToShop(@NotNull Player player, @NotNull IShopChest shop, @NotNull ICurrency currency, double amount) {
        if (!ChestConfig.ALLOWED_CURRENCIES.contains(currency)) {
            plugin.getMessage(Lang.Shop_Bank_Error_InvalidCurrency).send(player);
            return false;
        }

        if (amount < 0D) amount = currency.getBalance(player);

        if (currency.getBalance(player) < amount) {
            plugin.getMessage(Lang.Shop_Bank_Deposit_Error_NotEnough).send(player);
            return false;
        }

        currency.take(player, amount);
        shop.getBank().deposit(currency, amount);
        shop.save();

        plugin.getMessage(Lang.Shop_Bank_Deposit_Success)
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
            .send(player);
        return true;
    }

    public boolean withdrawFromShop(@NotNull Player player, @NotNull IShopChest shop, @NotNull ICurrency currency, double amount) {
        if (!ChestConfig.ALLOWED_CURRENCIES.contains(currency)) {
            plugin.getMessage(Lang.Shop_Bank_Error_InvalidCurrency).send(player);
            return false;
        }

        if (amount < 0D) amount = shop.getBank().getBalance(currency);

        if (!shop.getBank().hasEnough(currency, amount)) {
            plugin.getMessage(Lang.Shop_Bank_Withdraw_Error_NotEnough).send(player);
            return false;
        }

        currency.give(player, amount);
        shop.getBank().withdraw(currency, amount);
        shop.save();

        plugin.getMessage(Lang.Shop_Bank_Withdraw_Success)
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
            .send(player);
        return true;
    }

    @Nullable
    public ChestDisplayHandler getDisplayHandler() {
        return this.displayHandler;
    }

    @NotNull
    public Map<Location, IShopChest> getShopsMap() {
        return this.chests;
    }

    @NotNull
    public Collection<IShopChest> getShops() {
        return this.getShopsMap().values();
    }

    @NotNull
    public List<IShopChest> getShops(@NotNull Player player) {
        return this.getShops().stream().filter(shop -> shop.getOwnerId().equals(player.getUniqueId())).toList();
    }

    public int getShopsAllowed(@NotNull Player player) {
        return ChestConfig.getMaxShops(player);
    }

    public int getShopsAmount(@NotNull Player player) {
        return this.getShops(player).size();
    }

    @Nullable
    public IShopChest getShop(@NotNull Inventory inventory) {
        Location location = inventory.getLocation();
        if (location == null) return null;

        return this.getShop(location.getBlock());
    }

    @Nullable
    public IShopChest getShop(@NotNull Block block) {
        return this.getShop(block.getLocation());
    }

    @Nullable
    public IShopChest getShop(@NotNull Location location) {
        return this.getShopsMap().get(location);
    }

    @Nullable
    public IShopChest getShopSideChest(@NotNull Block block) {
        BlockData data = block.getBlockData();
        if (!(data instanceof Directional directional)) return null;

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
            .filter(shop -> shop != null && !shop.isChestDouble()).findFirst().orElse(null);
    }

    public boolean isShop(@NotNull Block block) {
        return this.isShop(block.getLocation());
    }

    public boolean isShop(@NotNull Location location) {
        return this.getShop(location) != null;
    }

    public static boolean isValidChest(@NotNull Block block) {
        if (block.getType() == Material.ENDER_CHEST) return false;
        return block.getState() instanceof Chest; // TODO any block
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
        if (player.hasPermission(Perms.ADMIN) || !ChestConfig.SHOP_CREATION_CLAIM_ONLY) return true;

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
