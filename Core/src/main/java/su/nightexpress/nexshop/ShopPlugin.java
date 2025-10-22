package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.module.Module;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.config.AuctionLang;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Keys;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.config.Perms;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.data.DataManager;
import su.nightexpress.nexshop.exception.ModuleLoadException;
import su.nightexpress.nexshop.hook.HookPlugin;
import su.nightexpress.nexshop.hook.PlaceholderHook;
import su.nightexpress.nexshop.module.ModuleId;
import su.nightexpress.nexshop.module.ModuleSettings;
import su.nightexpress.nexshop.module.ModuleSupplier;
import su.nightexpress.nexshop.shop.ShopManager;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.compatibility.WorldGuardFlags;
import su.nightexpress.nexshop.shop.chest.config.ChestLang;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualIconsLang;
import su.nightexpress.nexshop.shop.virtual.lang.VirtualLang;
import su.nightexpress.nexshop.user.UserManager;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.Plugins;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class ShopPlugin extends NightPlugin {

    private DataHandler dataHandler;
    private DataManager dataManager;
    private UserManager userManager;
    private ShopManager shopManager;

    private Map<Class<? extends Module>, Module> modules;

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("Shop", new String[]{"eshop", "excellentshop"})
            .setConfigClass(Config.class)
            //.setLangClass(Lang.class)
            .setPermissionsClass(Perms.class);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.getServer().getPluginManager().getPlugin(HookPlugin.WORLD_GUARD) != null) {
            WorldGuardFlags.setupFlag();
        }
    }

    @Override
    protected void addRegistries() {
        // After the config
        this.registerLang(Lang.class);
        this.registerLang(AuctionLang.class);
        this.registerLang(ChestLang.class);
        this.registerLang(VirtualLang.class);
        this.registerLang(VirtualIconsLang.class);
    }

    @Override
    protected boolean disableCommandManager() {
        return true;
    }

    @Override
    public void enable() {
        this.loadAPI();
        this.loadCommands();

        this.dataHandler = new DataHandler(this);
        this.dataHandler.setup();
        this.dataHandler.updateStockDatas();

        this.dataManager = new DataManager(this);
        this.dataManager.setup();

        this.userManager = new UserManager(this, this.dataHandler);
        this.userManager.setup();

        this.shopManager = new ShopManager(this);
        this.shopManager.setup();

        this.loadModules();

        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.setup(this);
        }

        // Sync all price & stock datas for all products after everything is loaded.
        this.runTaskAsync(task -> this.dataManager.loadAllData());
    }

    @Override
    public void disable() {
        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }

        if (this.shopManager != null) {
            this.shopManager.shutdown();
        }

        this.modules.values().forEach(Module::shutdown);
        this.modules.clear();

        if (this.userManager != null) this.userManager.shutdown();
        if (this.dataManager != null) this.dataManager.shutdown();
        if (this.dataHandler != null) this.dataHandler.shutdown();

        Keys.clear();
        ShopAPI.clear();
    }

    private void loadAPI() {
        ShopAPI.load(this);
        Keys.load(this);
    }

    private void loadModules() {
        this.modules = new HashMap<>();
        this.migrateModuleSettings(this.config);

        ModuleSupplier.create().forEach(supplier -> {
            String id = supplier.getId();
            ModuleSettings settings = ConfigValue.create("Module." + id, (cfg, path) -> ModuleSettings.read(cfg, path, id), supplier.getSettings()).read(this.config);
            if (!settings.isEnabled()) return;

            Module module = supplier.init(this, settings);

            try {
                module.setup();
                this.modules.put(module.getClass(), module);
            }
            catch (ModuleLoadException exception) {
                this.error("Could not load module '" + id + "': " + exception.getMessage());
            }
        });
    }

    private void loadCommands() {
        this.rootCommand = NightCommand.forPlugin(this, builder -> {
            builder.branch(Commands.literal("reload")
                .description(CoreLang.COMMAND_RELOAD_DESC)
                .permission(Perms.COMMAND_RELOAD)
                .executes((context, arguments) -> {
                    this.doReload(context.getSender());
                    return true;
                })
            );
        });
    }

    @NotNull
    public DataHandler getDataHandler() {
        return this.dataHandler;
    }

    @NotNull
    public DataManager getDataManager() {
        return this.dataManager;
    }

    public void dataAccess(@NotNull Consumer<DataManager> consumer) {
        if (this.dataManager.isLoaded()) {
            consumer.accept(this.dataManager);
        }
    }

    @NotNull
    public UserManager getUserManager() {
        return userManager;
    }

    @NotNull
    public ShopManager getShopManager() {
        return shopManager;
    }

    @Nullable
    public VirtualShopModule getVirtualShop() {
        return this.getModule(VirtualShopModule.class).orElse(null);
    }

    @Nullable
    public ChestShopModule getChestShop() {
        return this.getModule(ChestShopModule.class).orElse(null);
    }

    @Nullable
    public AuctionManager getAuction() {
        return this.getModule(AuctionManager.class).orElse(null);
    }

    @NotNull
    public <T extends Module> Optional<T> getModule(@NotNull Class<T> clazz) {
        return Optional.ofNullable(this.modules.get(clazz)).map(clazz::cast);
    }

    private void migrateModuleSettings(@NotNull FileConfig config) {
        if (!config.contains("Modules")) return;

        boolean vshopEnabled = config.getBoolean("Modules.VirtualShop.Enabled");
        boolean cshopEnabled = config.getBoolean("Modules.ChestShop.Enabled");
        boolean aucEnabled = config.getBoolean("Modules.Auction.Enabled");

        String[] vshopCommands = config.getStringArray("Modules.VirtualShop.Command_Aliases");
        String[] cshopCommands = config.getStringArray("Modules.ChestShop.Command_Aliases");
        String[] aucCommands = config.getStringArray("Modules.Auction.Command_Aliases");

        config.set("Module." + ModuleId.AUCTION + ".Enabled", aucEnabled);
        config.setStringArray("Module." + ModuleId.AUCTION + ".Command_Aliases", aucCommands);

        config.set("Module." + ModuleId.CHEST_SHOP + ".Enabled", cshopEnabled);
        config.setStringArray("Module." + ModuleId.CHEST_SHOP + ".Command_Aliases", cshopCommands);

        config.set("Module." + ModuleId.VIRTUAL_SHOP + ".Enabled", vshopEnabled);
        config.setStringArray("Module." + ModuleId.VIRTUAL_SHOP + ".Command_Aliases", vshopCommands);

        for (String modId : ModuleId.values()) {
            File settingsFile = new File(this.getDataFolder() + "/" + modId + "/settings.yml");
            if (!settingsFile.exists()) continue;

            String readPath;
            String writePath = "Module." + modId;
            if (modId.equalsIgnoreCase(ModuleId.AUCTION)) readPath = "Settings";
            else if (modId.equalsIgnoreCase(ModuleId.CHEST_SHOP)) readPath = "Shops";
            else if (modId.equalsIgnoreCase(ModuleId.VIRTUAL_SHOP)) readPath = "General";
            else continue;

            FileConfig settings = new FileConfig(settingsFile);
            String defCur = settings.getString(readPath + ".Default_Currency");
            Set<String> enabledCur = settings.getStringSet(readPath + ".Allowed_Currencies");
            if (enabledCur.isEmpty()) enabledCur.add(Placeholders.WILDCARD);

            config.set(writePath + ".Currency.Default", defCur);
            config.set(writePath + ".Currency.Enabled", enabledCur);
        }

        config.remove("Modules");
    }
}
