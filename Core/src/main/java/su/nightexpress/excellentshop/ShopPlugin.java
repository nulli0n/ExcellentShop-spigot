package su.nightexpress.excellentshop;

import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.api.Module;
import su.nightexpress.excellentshop.api.packet.PacketLibrary;
import su.nightexpress.excellentshop.feature.playershop.ChestShopModule;
import su.nightexpress.excellentshop.feature.playershop.core.ChestLang;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.integration.claim.WorldGuardFlags;
import su.nightexpress.excellentshop.integration.packetevents.PacketEventsHook;
import su.nightexpress.excellentshop.integration.protocollib.ProtocolLibHook;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.auction.config.AuctionLang;
import su.nightexpress.excellentshop.core.Config;
import su.nightexpress.excellentshop.core.Keys;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.core.Perms;
import su.nightexpress.excellentshop.data.DataHandler;
import su.nightexpress.excellentshop.data.DataManager;
import su.nightexpress.nexshop.hook.HookPlugin;
import su.nightexpress.nexshop.hook.PlaceholderHook;
import su.nightexpress.nexshop.module.*;
import su.nightexpress.excellentshop.shop.ShopManager;
import su.nightexpress.excellentshop.shop.TransactionProcessor;
import su.nightexpress.nexshop.user.UserManager;
import su.nightexpress.nexshop.util.PacketUtils;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class ShopPlugin extends NightPlugin implements ModuleContextProvider {

    private final ModuleRegistry moduleRegistry = new ModuleRegistry();

    private final TransactionProcessor transactionProcessor = new TransactionProcessor();

    private DataHandler dataHandler;
    private DataManager dataManager;
    private UserManager userManager;
    private ShopManager shopManager;

    @Override
    @NonNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("Shop", new String[]{"eshop", "excellentshop"})
            .setConfigClass(Config.class)
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
    protected void onStartup() {
        super.onStartup();
        this.loadPacketLibrary();
    }

    @Override
    protected void addRegistries() {
        // After the config
        this.registerLang(Lang.class);
        this.registerLang(AuctionLang.class);
        this.registerLang(ChestLang.class);
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

        this.dataManager = new DataManager(this, this.dataHandler);
        this.dataManager.setup();

        this.userManager = new UserManager(this, this.dataHandler);
        this.userManager.setup();

        this.shopManager = new ShopManager(this);
        this.shopManager.setup();

        this.loadModules();

        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.setup(this);
        }

        this.dataManager.loadAllData().whenCompleteAsync((unused, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }

            this.moduleRegistry.getModules().forEach(Module::onDataLoadFinished);
        }, this::runTask);
    }

    @Override
    public void disable() {
        if (Plugins.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }

        if (this.shopManager != null) {
            this.shopManager.shutdown();
        }

        this.moduleRegistry.clear();

        if (this.userManager != null) this.userManager.shutdown();
        if (this.dataManager != null) this.dataManager.shutdown();
        if (this.dataHandler != null) this.dataHandler.shutdown();

        Keys.clear();
        ShopAPI.clear();
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();

        this.transactionProcessor.shutdown();

        PacketUtils.clear();
    }

    private void loadAPI() {
        ShopAPI.load(this);
        Keys.load(this);
    }

    private void loadPacketLibrary() {
        PacketLibrary library;

        if (Plugins.isInstalled(HookPlugin.PACKET_EVENTS)) {
            library = new PacketEventsHook();
        }
        else if (Plugins.isInstalled(HookPlugin.PROTOCOL_LIB)) {
            library = new ProtocolLibHook();
        }
        else {
            this.warn("No packet library plugins found. Some features will be unavailable.");
            return;
        }

        this.info("Successfully hooked with packet library plugin: %s".formatted(library.getName()));
        PacketUtils.register(library);
    }

    private void loadModules() {
        this.migrateModuleSettings(this.config);

        ModuleLoader loader = new ModuleLoader(this, this.moduleRegistry);

        var gradient = TagWrappers.GRADIENT;

        String aucPrefix = gradient.with("#f77062", "#fe5196").wrap(TagWrappers.BOLD.wrap("AUCTION")) + " " + TagWrappers.DARK_GRAY.wrap("»") + " ";
        String psPrefix = gradient.with("#ff4e50", "#f9d423").wrap(TagWrappers.BOLD.wrap("PLAYER SHOP")) + " " + TagWrappers.DARK_GRAY.wrap("»") + " ";
        String vsPrefix = gradient.with("#0083b0", "#00b4db").wrap(TagWrappers.BOLD.wrap("SERVER SHOP")) + " " + TagWrappers.DARK_GRAY.wrap("»") + " ";

        loader.register(ModuleId.AUCTION,
            ModuleDefinition.createNoItemHandlers(aucPrefix, "auction", "auc", "ah"),
            AuctionManager::new
        );

        loader.register(ModuleId.CHEST_SHOP,
            ModuleDefinition.createNoItemHandlers(psPrefix, "chestshop", "cshop", "playershop", "pshop"),
            context -> new ChestShopModule(context, this.shopManager, this.transactionProcessor)
        );

        loader.register(ModuleId.VIRTUAL_SHOP,
            ModuleDefinition.createDefault(vsPrefix, "virtualshop", "vshop"),
            context -> new VirtualShopModule(context, this.shopManager, this.transactionProcessor)
        );

        loader.loadAll();
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

    @Override
    @NonNull
    public ModuleContext createModuleContext(@NonNull String id, @NonNull Path path, @NonNull ModuleDefinition definition) {
        return new ModuleContext(this, this.dataHandler, this.dataManager, this.userManager, this.dialogRegistry, id, path, definition);
    }

    @NonNull
    public ModuleRegistry getModuleRegistry() {
        return this.moduleRegistry;
    }

    @NonNull
    public DataHandler getDataHandler() {
        return this.dataHandler;
    }

    @NonNull
    public DataManager getDataManager() {
        return this.dataManager;
    }

    @Deprecated
    public void dataAccess(@NonNull Consumer<DataManager> consumer) {
        if (this.dataManager.isLoaded()) {
            consumer.accept(this.dataManager);
        }
    }

    @NonNull
    public UserManager getUserManager() {
        return userManager;
    }

    @NonNull
    public ShopManager getShopManager() {
        return shopManager;
    }

    @Nullable
    @Deprecated
    public VirtualShopModule getVirtualShop() {
        return this.getModule(VirtualShopModule.class).orElse(null);
    }

    @Nullable
    @Deprecated
    public ChestShopModule getChestShop() {
        return this.getModule(ChestShopModule.class).orElse(null);
    }

    @Nullable
    @Deprecated
    public AuctionManager getAuction() {
        return this.getModule(AuctionManager.class).orElse(null);
    }

    @NonNull
    @Deprecated
    public <T extends Module> Optional<T> getModule(@NonNull Class<T> clazz) {
        return this.moduleRegistry.byType(clazz);
    }

    private void migrateModuleSettings(@NonNull FileConfig config) {
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
            if (enabledCur.isEmpty()) enabledCur.add(ShopPlaceholders.WILDCARD);

            config.set(writePath + ".Currency.Default", defCur);
            config.set(writePath + ".Currency.Enabled", enabledCur);
        }

        config.remove("Modules");
    }
}
