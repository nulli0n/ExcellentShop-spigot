package su.nightexpress.nexshop.shop.chest;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
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
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.core.Version;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.VaultHK;
import su.nexmedia.engine.hooks.external.WorldGuardHK;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.MsgUT;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.hooks.EHook;
import su.nightexpress.nexshop.modules.EModule;
import su.nightexpress.nexshop.modules.ShopModule;
import su.nightexpress.nexshop.shop.chest.command.CreateCmd;
import su.nightexpress.nexshop.shop.chest.command.ListCmd;
import su.nightexpress.nexshop.shop.chest.command.RemoveCmd;
import su.nightexpress.nexshop.shop.chest.command.SearchCmd;
import su.nightexpress.nexshop.shop.chest.compatibility.ClaimHook;
import su.nightexpress.nexshop.shop.chest.compatibility.GriefPreventionHK;
import su.nightexpress.nexshop.shop.chest.compatibility.LandsHK;
import su.nightexpress.nexshop.shop.chest.compatibility.PlotSquaredHK;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorHandler;
import su.nightexpress.nexshop.shop.chest.menu.ShopListGlobalMenu;
import su.nightexpress.nexshop.shop.chest.menu.ShopListOwnMenu;
import su.nightexpress.nexshop.shop.chest.menu.ShopListSearchMenu;
import su.nightexpress.nexshop.shop.chest.nms.ChestNMS;
import su.nightexpress.nexshop.shop.chest.object.ShopChest;

import java.util.*;

public class ChestShop extends ShopModule {

    private Map<Location, IShopChest> chests;
    private Set<ClaimHook>            claimHooks;

    private ShopListOwnMenu    listOwnGUI;
    private ShopListGlobalMenu listGlobalGUI;
    private ShopListSearchMenu listSearchGUI;

    private ChestDisplayHandler           displayHandler;
    private Map<String, List<IShopChest>> searchCache;

    ChestNMS    chestNMS;
    Set<String> loadFails;
    private ChestEditorHandler editorHandler;
    private ProductTask productTask;

    public static final String DIR_SHOPS = "/shops/";

    public ChestShop(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getId() {
        return EModule.CHEST_SHOP;
    }

    @Override
    @NotNull
    public String getVersion() {
        return "2.00";
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        this.searchCache = new HashMap<>();
        this.loadFails = new HashSet<>();
        this.chests = new HashMap<>();

        ChestShopConfig.load(this);
        if (ChestShopConfig.DEFAULT_CURRENCY == null) {
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
            } catch (ReflectiveOperationException ex) {
                this.error("Could not setup internal NMS handler! Shop display will be disabled.");
                ex.printStackTrace();
            }
        }

        // Setup Claim Hooks
        if (ChestShopConfig.SHOP_CREATION_CLAIM_ONLY) {
            this.claimHooks = new HashSet<>();

            this.plugin.registerHook(EHook.PLOT_SQUARED, PlotSquaredHK.class);
            this.plugin.registerHook(EHook.LANDS, LandsHK.class);
            this.plugin.registerHook(EHook.GRIEF_PREVENTION, GriefPreventionHK.class);

            this.plugin.getHooks().getHooks(this.plugin).forEach(eHook -> {
                if (eHook instanceof ClaimHook && ChestShopConfig.isClaimPlugin(eHook.getPlugin())) {
                    this.claimHooks.add((ClaimHook) eHook);
                    this.info("Registered claim plugin for shop creation: " + eHook.getPlugin());
                }
            });
        }

        this.addListener(new ChestShopListener(this));

        this.listOwnGUI = new ShopListOwnMenu(this);
        this.listGlobalGUI = new ShopListGlobalMenu(this);
        this.listSearchGUI = new ShopListSearchMenu(this);

        this.editorHandler = new ChestEditorHandler(this);
        this.editorHandler.setup();

        this.moduleCommand.addChildren(new CreateCmd(this));
        this.moduleCommand.addChildren(new RemoveCmd(this));
        this.moduleCommand.addChildren(new ListCmd(this));
        this.moduleCommand.addChildren(new SearchCmd(this));

        for (JYML shopConfig : JYML.loadAll(this.getFullPath() + DIR_SHOPS, true)) {
            try {
                IShopChest shopChest = new ShopChest(this, shopConfig);
                this.addShop(shopChest);
            } catch (Exception e) {
                this.error("Could not load shop '" + shopConfig.getFile().getName() + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
        this.info("Shops Loaded: " + this.getShops().size());

        this.productTask = new ProductTask(this.plugin);
        this.productTask.start();
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();

        if (this.productTask != null) {
            this.productTask.stop();
            this.productTask = null;
        }
        if (this.editorHandler != null) {
            this.editorHandler.shutdown();
            this.editorHandler = null;
        }
        if (this.listOwnGUI != null) {
            this.listOwnGUI.clear();
            this.listOwnGUI = null;
        }
        if (this.listGlobalGUI != null) {
            this.listGlobalGUI.clear();
            this.listGlobalGUI = null;
        }
        if (this.listSearchGUI != null) {
            this.listSearchGUI.clear();
            this.listSearchGUI = null;
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

        if (this.loadFails != null) {
            this.loadFails.clear();
            this.loadFails = null;
        }
        if (this.searchCache != null) {
            this.searchCache.clear();
            this.searchCache = null;
        }
        if (this.claimHooks != null) {
            this.claimHooks.clear();
            this.claimHooks = null;
        }
    }

    @NotNull
    public ChestEditorHandler getEditorHandler() {
        return editorHandler;
    }

    public boolean createShop(@NotNull Player player, @NotNull Block block, boolean isAdmin) {
        if (!this.isValidChest(block)) {
            plugin.lang().Chest_Shop_Creation_Error_NotAChest.send(player);
            return false;
        }

        if (this.isShop(block)) {
            plugin.lang().Chest_Shop_Creation_Error_AlreadyShop.send(player);
            return false;
        }

        if (!this.isAllowedHere(block.getLocation())) {
            plugin.lang().Chest_Shop_Creation_Error_BadLocation.send(player);
            return false;
        }

        if (!this.isAllowedHereCompat(player, block)) {
            plugin.lang().Chest_Shop_Creation_Error_BadArea.send(player);
            return false;
        }

        if (isAdmin && !player.hasPermission(Perms.CHEST_EDITOR_ADMINSHOP)) {
            plugin.lang().Error_NoPerm.send(player);
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
            plugin.lang().Chest_Shop_Creation_Error_BadArea.send(player);
            return false;
        }

        Chest bChest = (Chest) block.getState();
        Inventory cInv = bChest.getInventory();
        for (ItemStack inside : cInv.getContents()) {
            if (!ItemUT.isAir(inside)) {
                plugin.lang().Chest_Shop_Creation_Error_NotEmpty.send(player);
                return false;
            }
        }

        int allowed = this.getShopsAllowed(player);
        int has = this.getShopsAmount(player);
        if (allowed > 0 && has >= allowed) {
            plugin.lang().Chest_Shop_Creation_Error_LimitReached.send(player);
            return false;
        }

        if (!this.payForCreate(player)) {
            plugin.lang().Chest_Shop_Creation_Error_NotEnoughFunds.send(player);
            return false;
        }

        IShopChest shop = new ShopChest(this, player, bChest, UUID.randomUUID(), isAdmin);
        shop.createProduct(player, hand);
        shop.save();

        this.addShop(shop);
        this.plugin.lang().Chest_Shop_Creation_Info_Done.send(player);

        MsgUT.sound(player, ChestShopConfig.SOUND_CREATION);
        return true;
    }

    public boolean deleteShop(@NotNull Player player, @NotNull Block block) {
        IShopChest shop = this.getShop(block);
        if (shop == null) {
            plugin.lang().Chest_Shop_Removal_Error_NotAShop.send(player);
            return false;
        }

        if (!shop.isOwner(player) && !player.hasPermission(Perms.CHEST_REMOVE_OTHERS)) {
            plugin.lang().Chest_Shop_Error_NotOwner.send(player);
            return false;
        }

        if (!this.payForRemoval(player)) {
            plugin.lang().Chest_Shop_Creation_Error_NotEnoughFunds.send(player);
            return false;
        }

        // Delete shop editor, display and remove it from the database.
        this.removeShop(shop, true);
        this.plugin.lang().Chest_Shop_Removal_Info_Done.send(player);

        MsgUT.sound(player, ChestShopConfig.SOUND_REMOVAL);
        return true;
    }

    void addShop(@NotNull IShopChest shop) {
        this.chests.put(shop.getLocation(), shop);

        if (shop.isChestDouble()) {
            DoubleChest doubleChest = (DoubleChest) shop.getChestInventory().getHolder();
            if (doubleChest == null) return;

            Chest left = (Chest) doubleChest.getLeftSide();
            Chest right = (Chest) doubleChest.getRightSide();
            if (left != null) this.chests.put(left.getLocation(), shop);
            if (right != null) this.chests.put(right.getLocation(), shop);
        }
    }

    void removeShop(@NotNull IShopChest shop, boolean withData) {
        if (withData && shop instanceof AbstractLoadableItem<?> item) {
            if (!item.getFile().delete()) return;
        }

        // Destroy shop editor and display.
        shop.clear();
        this.chests.remove(shop.getLocation());

        if (shop.isChestDouble()) {
            DoubleChest doubleChest = (DoubleChest) shop.getChestInventory().getHolder();
            if (doubleChest == null) return;

            Chest left = (Chest) doubleChest.getLeftSide();
            Chest right = (Chest) doubleChest.getRightSide();
            if (left != null) this.chests.remove(left.getLocation());
            if (right != null) this.chests.remove(right.getLocation());
        }
    }

    @NotNull
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
        return this.getShops().stream()
                .filter(shop -> shop.getOwnerId().equals(player.getUniqueId())).toList();
    }

    @NotNull
    public List<IShopChest> getShopsSearched(@NotNull Player player) {
        List<IShopChest> search = this.searchCache.get(player.getName());
        return search != null ? search : Collections.emptyList();
    }

    public int getShopsAllowed(@NotNull Player player) {
        if (player.hasPermission(Perms.ADMIN)) return -1;
        return ChestShopConfig.getMaxShops(player);
    }

    public int getShopsAmount(@NotNull Player player) {
        return this.getShops(player).size();
    }

    @Nullable
    public IShopChest getShop(@NotNull Inventory inv) {
        Location location = inv.getLocation();
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
    public IShopChest getShopNearBlock(@NotNull Block block) {
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

        for (BlockFace blockFace : faces) {
            Block relative = block.getRelative(blockFace);
            IShopChest shop = this.getShop(relative);
            if (shop != null && !shop.isChestDouble()) {
                return shop;
            }
        }
        return null;
    }

    public boolean isShop(@NotNull Block block) {
        return this.isShop(block.getLocation());
    }

    public boolean isShop(@NotNull Location location) {
        return this.getShop(location) != null;
    }

    public boolean isValidChest(@NotNull Block block) {
        if (block.getType() == Material.ENDER_CHEST) return false;
        return block.getState() instanceof Chest;
    }

    public boolean isAllowedHere(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) return true;

        String name = world.getName();
        if (ChestShopConfig.SHOP_CREATION_WORLD_BLACKLIST.contains(name)) {
            return false;
        }
        return true;
    }

    public boolean isAllowedHereCompat(@NotNull Player player, @NotNull Block block) {
        if (player.hasPermission(Perms.ADMIN) || !ChestShopConfig.SHOP_CREATION_CLAIM_ONLY) return true;

        WorldGuardHK wgHook = plugin.getWorldGuard();
        if (wgHook != null && ChestShopConfig.isClaimPlugin(Hooks.WORLD_GUARD)) {
            ProtectedRegion region = wgHook.getProtectedRegion(player);
            if (region == null || !region.getOwners().contains(player.getUniqueId())) {
                return false;
            }
        }

        return this.claimHooks.stream().anyMatch(claim -> claim.isInOwnClaim(player, block));
    }

    private boolean payForCreate(@NotNull Player player) {
        return this.payForShop(player, ChestShopConfig.SHOP_CREATION_COST_CREATE);
    }

    private boolean payForRemoval(@NotNull Player player) {
        return this.payForShop(player, ChestShopConfig.SHOP_CREATION_COST_REMOVE);
    }

    private boolean payForShop(@NotNull Player player, double price) {
        if (price <= 0) return true;

        VaultHK vault = plugin.getVault();
        if (vault == null) return true;

        double balance = vault.getBalance(player);
        if (balance < price) return false;

        vault.take(player, price);
        return true;
    }

    @NotNull
    public ShopListOwnMenu getListOwnGUI() {
        return this.listOwnGUI;
    }

    @NotNull
    public ShopListGlobalMenu getListGlobalGUI() {
        return this.listGlobalGUI;
    }

    @NotNull
    public ShopListSearchMenu getListSearchGUI() {
        return this.listSearchGUI;
    }

    public void searchForItem(@NotNull Player player, @NotNull Material mat) {
        List<IShopChest> find = this.getShops().stream()
                .filter(shop -> shop.getProducts().stream().anyMatch(product -> product.getItem().getType() == mat))
                .toList();

        this.searchCache.put(player.getName(), find);
        // Fix async inventory open.
        this.plugin.runTask((c) -> getListSearchGUI().open(player, 1), false);
    }

    public void cacheFailedLoad(@NotNull String uuid) {
        this.loadFails.add(uuid);
    }

    class ProductTask extends ITask<ExcellentShop> {

        public ProductTask(@NotNull ExcellentShop plugin) {
            super(plugin, 60, true);
        }

        @Override
        public void action() {
            getShops().forEach(shop -> {
                shop.getProducts().forEach(product -> product.getPricer().randomizePrices());
            });
        }
    }
}
