package su.nightexpress.nexshop.shop.virtual;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.external.citizens.CitizensHK;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.utils.FileUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.IShop;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.modules.EModule;
import su.nightexpress.nexshop.modules.ShopModule;
import su.nightexpress.nexshop.shop.virtual.command.EditorCommand;
import su.nightexpress.nexshop.shop.virtual.command.OpenCommand;
import su.nightexpress.nexshop.shop.virtual.compatibility.citizens.NpcShopListener;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorHandler;
import su.nightexpress.nexshop.shop.virtual.editor.menu.EditorShopList;
import su.nightexpress.nexshop.shop.virtual.object.ShopVirtual;
import su.nightexpress.nexshop.shop.virtual.object.ShopVirtualMain;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualShop extends ShopModule {

    private Map<String, IShopVirtual> shops;
    private ShopVirtualMain           mainMenu;

    private NpcShopListener npcShopListener;
    private ProductTask productTask;

    private VirtualEditorHandler editorHandler;
    private EditorShopList editor;

    public static final String DIR_SHOPS = "/shops/";

    public VirtualShop(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getId() {
        return EModule.VIRTUAL_SHOP;
    }

    @Override
    @NotNull
    public String getVersion() {
        return "2.00";
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.shops = new HashMap<>();

        this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "shops/blocks");
        this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "shops/brewing");
        this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "shops/food");
        this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "shops/loot");
        this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "shops/tools");
        this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "shops/weapons");
        this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "shops/wool");

        this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "editor");

        VirtualShopConfig.load(this, this.cfg);

        this.editorHandler = new VirtualEditorHandler(this);
        this.editorHandler.setup();

        this.moduleCommand.addDefaultCommand(new OpenCommand(this));
        this.moduleCommand.addChildren(new EditorCommand(this));

        this.loadShops();
        this.loadMainMenu();
        this.loadCitizens();
        this.addListener(new VirtualShopListener(this));

        this.productTask = new ProductTask(this.plugin);
        this.productTask.start();
    }

    private void loadShops() {
        for (File folder : FileUT.getFolders(this.getFullPath() + "shops")) {
            JYML cfg = new JYML(folder.getAbsolutePath(), folder.getName() + ".yml");
            try {
                IShopVirtual shop = new ShopVirtual(this, cfg);
                this.shops.put(shop.getId(), shop);
            } catch (Exception ex) {
                this.error("Could not load shop: " + cfg.getFile().getName());
                ex.printStackTrace();
            }
        }
        this.info("Shops Loaded: " + this.shops.size());
    }

    private void loadMainMenu() {
        if (this.mainMenu != null) {
            this.mainMenu.clear();
            this.mainMenu = null;
        }

        JYML menuConfig = JYML.loadOrExtract(this.plugin, this.getPath() + "main.menu.yml");

        if (!menuConfig.getBoolean("Enabled")) return;
        this.mainMenu = new ShopVirtualMain(this, menuConfig, "");
    }

    private void loadCitizens() {
        CitizensHK citizens = this.plugin.getCitizens();
        if (citizens == null) return;

        this.info("Detected " + citizens.getPlugin() + "! Enabling hooks...");
        this.npcShopListener = new NpcShopListener(this, citizens);
        this.npcShopListener.setup();
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();

        if (this.productTask != null) {
            this.productTask.stop();
            this.productTask = null;
        }
        if (this.npcShopListener != null) {
            this.npcShopListener.shutdown();
            this.npcShopListener = null;
        }
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
        if (this.mainMenu != null) {
            this.mainMenu.clear();
            this.mainMenu = null;
        }

        // Shutdown Shop Views and editors
        this.shops.values().forEach(IShopVirtual::clear);
        this.shops.clear();
    }

    @NotNull
    public EditorShopList getEditor() {
        if (this.editor == null) {
            this.editor = new EditorShopList(this);
        }
        return editor;
    }

    public boolean hasMainMenu() {
        return this.mainMenu != null;
    }

    public void openMainMenu(@NotNull Player player) {
        if (!this.hasMainMenu()) {
            plugin.lang().Virtual_Shop_MainMenu_Error_Disabled.send(player);
            return;
        }

        if (!player.hasPermission(Perms.VIRTUAL_MAINMENU)) {
            plugin.lang().Error_NoPerm.send(player);
            return;
        }

        if (!this.isShopAllowed(player)) {
            return;
        }

        this.mainMenu.open(player, 1);
    }

    public boolean delete(@NotNull IShopVirtual shop) {
        if (FileUT.deleteRecursive(this.getFullPath() + DIR_SHOPS + shop.getId())) {
            shop.clear();
            this.shops.remove(shop.getId());
            this.loadMainMenu();
            return true;
        }
        return false;
    }

    public boolean isShopAllowed(@NotNull Player player) {
        if (player.hasPermission(Perms.ADMIN)) return true;

        String world = player.getWorld().getName();
        if (VirtualShopConfig.GEN_DISABLED_WORLDS.contains(world)) {
            plugin.lang().Virtual_Shop_Open_Error_BadWorld.send(player);
            return false;
        }

        String mode = player.getGameMode().name();
        if (VirtualShopConfig.GEN_DISABLED_GAMEMODES.contains(mode)) {
            plugin.lang().Virtual_Shop_Open_Error_BadGamemode
                    .replace("%mode%", plugin.lang().getEnum(player.getGameMode()))
                    .send(player);
            return false;
        }

        return true;
    }

    @NotNull
    public VirtualEditorHandler getEditorHandler() {
        return this.editorHandler;
    }

    @NotNull
    public Map<String, IShopVirtual> getShopsMap() {
        return this.shops;
    }

    @NotNull
    public Collection<IShopVirtual> getShops() {
        return this.getShopsMap().values();
    }

    @NotNull
    public List<String> getShops(@NotNull Player player) {
        return this.getShops().stream().filter(shop -> shop.hasPermission(player)).map(IShop::getId).toList();
    }

    @Nullable
    public IShopVirtual getShopById(@NotNull String id) {
        return this.getShopsMap().get(id.toLowerCase());
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
