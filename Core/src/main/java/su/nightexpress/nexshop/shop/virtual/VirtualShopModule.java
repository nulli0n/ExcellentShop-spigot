package su.nightexpress.nexshop.shop.virtual;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.citizens.CitizensHook;
import su.nexmedia.engine.utils.FileUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.Placeholders;
import su.nightexpress.nexshop.api.currency.ICurrency;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.data.price.ProductPriceManager;
import su.nightexpress.nexshop.data.stock.ProductStockManager;
import su.nightexpress.nexshop.module.ModuleId;
import su.nightexpress.nexshop.module.ShopModule;
import su.nightexpress.nexshop.shop.virtual.command.EditorCommand;
import su.nightexpress.nexshop.shop.virtual.command.OpenCommand;
import su.nightexpress.nexshop.shop.virtual.compat.citizens.VirtualShopNPCListener;
import su.nightexpress.nexshop.shop.virtual.compat.citizens.VirtualShopTrait;
import su.nightexpress.nexshop.shop.virtual.config.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.config.VirtualLang;
import su.nightexpress.nexshop.shop.virtual.editor.menu.EditorShopList;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShopBank;
import su.nightexpress.nexshop.shop.virtual.listener.VirtualShopListener;
import su.nightexpress.nexshop.shop.virtual.menu.VirtualMenuMain;

import java.io.File;
import java.util.*;

public class VirtualShopModule extends ShopModule {

    public static final String DIR_SHOPS = "/shops/";

    public static ICurrency defaultCurrency;

    private Map<String, VirtualShop> shops;
    private VirtualMenuMain          mainMenu;
    private EditorShopList           editor;

    public VirtualShopModule(@NotNull ExcellentShop plugin) {
        super(plugin, ModuleId.VIRTUAL_SHOP);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        this.shops = new HashMap<>();

        File dir = new File(this.getFullPath() + "shops");
        if (!dir.exists()) {
            for (String id : new String[]{"blocks", "brewing", "food", "loot", "tools", "weapons", "wool"}) {
                this.plugin.getConfigManager().extractFullPath(this.getFullPath() + "shops/" + id);
            }
        }

        this.cfg.initializeOptions(VirtualConfig.class);
        if ((defaultCurrency = plugin.getCurrencyManager().getCurrency(VirtualConfig.DEFAULT_CURRENCY.get())) == null) {
            this.interruptLoad("Invalid default currency!");
            return;
        }

        this.moduleCommand.addDefaultCommand(new OpenCommand(this));
        this.moduleCommand.addChildren(new EditorCommand(this));

        this.loadShops();
        this.loadMainMenu();
        this.loadCitizens();
        this.addListener(new VirtualShopListener(this));
    }

    @Override
    protected void onShutdown() {
        if (Hooks.hasCitizens()) {
            CitizensHook.unregisterTraits(this.plugin);
            CitizensHook.unregisterListeners(this.plugin);
        }
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
        if (this.mainMenu != null) {
            this.mainMenu.clear();
            this.mainMenu = null;
        }
        this.shops.values().forEach(VirtualShop::clear);
        this.shops.clear();
        super.onShutdown();
    }

    private void loadShops() {
        for (File folder : FileUtil.getFolders(this.getFullPath() + "shops")) {
            String id = folder.getName();
            File fileOld = new File(folder.getAbsolutePath(), folder.getName() + ".yml");
            File fileNew = new File(folder.getAbsolutePath(), "config.yml");
            if (fileOld.exists() && !fileNew.exists()) {
                if (!fileOld.renameTo(fileNew)) {
                    this.error("Unable to rename shop config file: " + fileOld.getName());
                    continue;
                }
            }

            VirtualShop shop = new VirtualShop(this, new JYML(fileNew), id);
            if (shop.load()) {
                this.getShopsMap().put(shop.getId(), shop);
                ProductStockManager.loadData(shop).thenRun(() -> shop.getProducts().forEach(product -> product.getStock().unlock()));
                ProductPriceManager.loadData(shop).thenRun(() -> shop.getProducts().forEach(product -> product.getPricer().update()));
            }
            else {
                this.error("Shop not loaded: " + shop.getFile().getName());
            }
        }
        this.info("Shops Loaded: " + this.getShopsMap().size());
    }

    private void loadMainMenu() {
        if (this.mainMenu != null) {
            this.mainMenu.clear();
            this.mainMenu = null;
        }

        if (!VirtualConfig.MAIN_MENU_ENABLED.get()) return;
        this.mainMenu = new VirtualMenuMain(this);
    }

    private void loadCitizens() {
        if (!Hooks.hasCitizens()) return;
        this.info("Detected " + Hooks.CITIZENS + "! Enabling hooks...");
        CitizensHook.addListener(this.plugin, new VirtualShopNPCListener(this));
        CitizensHook.registerTrait(this.plugin, VirtualShopTrait.class);
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

    public boolean createShop(@NotNull String id) {
        if (this.getShopById(id) != null) {
            return false;
        }
        JYML cfg = new JYML(this.getFullPath() + DIR_SHOPS + id, "config.yml");
        VirtualShop shop = new VirtualShop(this, cfg, id);
        shop.setName("&e&l" + StringUtil.capitalizeFully(id.replace("_", " ")));
        shop.setDescription(Arrays.asList("&7Freshly created shop.", "&7Edit me in &a/shop editor"));
        shop.setIcon(new ItemStack(Material.REDSTONE));
        shop.setBank(new VirtualShopBank(shop));
        shop.setupView();
        shop.save();
        this.getShopsMap().put(shop.getId(), shop);
        return true;
    }

    public boolean isAvailable(@NotNull Player player, boolean notify) {
        if (player.hasPermission(Perms.ADMIN)) return true;

        if (VirtualConfig.GEN_DISABLED_WORLDS.get().contains(player.getWorld().getName())) {
            if (notify) plugin.getMessage(VirtualLang.OPEN_ERROR_BAD_WORLD).send(player);
            return false;
        }

        if (VirtualConfig.GEN_DISABLED_GAMEMODES.get().contains(player.getGameMode().name())) {
            if (notify) plugin.getMessage(VirtualLang.OPEN_ERROR_BAD_GAMEMODE)
                .replace(Placeholders.GENERIC_TYPE, plugin.getLangManager().getEnum(player.getGameMode()))
                .send(player);
            return false;
        }

        return true;
    }

    public void openMainMenu(@NotNull Player player) {
        if (!this.hasMainMenu()) {
            plugin.getMessage(VirtualLang.MAIN_MENU_ERROR_DISABLED).send(player);
            return;
        }

        if (!player.hasPermission(Perms.VIRTUAL_MAIN_MENU)) {
            plugin.getMessage(Lang.ERROR_PERMISSION_DENY).send(player);
            return;
        }

        if (!this.isAvailable(player, true)) {
            return;
        }

        this.mainMenu.open(player, 1);
    }

    public boolean delete(@NotNull VirtualShop shop) {
        if (FileUtil.deleteRecursive(this.getFullPath() + DIR_SHOPS + shop.getId())) {
            shop.clear();
            this.getShopsMap().remove(shop.getId());
            this.loadMainMenu();
            return true;
        }
        return false;
    }

    @NotNull
    public Map<String, VirtualShop> getShopsMap() {
        return this.shops;
    }

    @NotNull
    public Collection<VirtualShop> getShops() {
        return this.getShopsMap().values();
    }

    @NotNull
    public List<VirtualShop> getShops(@NotNull Player player) {
        return this.getShops().stream().filter(shop -> shop.canAccess(player, false)).toList();
    }

    @Nullable
    public VirtualShop getShopById(@NotNull String id) {
        return this.getShopsMap().get(id.toLowerCase());
    }
}
