package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.data.UserDataHolder;
import su.nexmedia.engine.command.list.ReloadSubCommand;
import su.nexmedia.engine.utils.EngineUtils;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.auction.AuctionManager;
import su.nightexpress.nexshop.command.currency.CurrencyMainCommand;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.data.UserManager;
import su.nightexpress.nexshop.data.user.ShopUser;
import su.nightexpress.nexshop.hook.HookId;
import su.nightexpress.nexshop.hook.PlaceholderHook;
import su.nightexpress.nexshop.shop.ProductHandlerRegistry;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.compatibility.WorldGuardFlags;
import su.nightexpress.nexshop.shop.impl.handler.ItemsAdderHandler;
import su.nightexpress.nexshop.shop.impl.handler.OraxenItemHandler;
import su.nightexpress.nexshop.shop.menu.CartMenu;
import su.nightexpress.nexshop.shop.task.ShopUpdateTask;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;

public class ExcellentShop extends NexPlugin<ExcellentShop> implements UserDataHolder<ExcellentShop, ShopUser> {

    private DataHandler dataHandler;
    private UserManager userManager;

    private CartMenu        cartMenu;
    private CurrencyManager currencyManager;
    private VirtualShopModule virtualShop;
    private ChestShopModule chestShop;
    private AuctionManager auction;

    private ShopUpdateTask shopUpdateTask;

    @Override
    @NotNull
    protected ExcellentShop getSelf() {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.getServer().getPluginManager().getPlugin(HookId.WORLD_GUARD) != null) {
            WorldGuardFlags.setupFlag();
        }
    }

    @Override
    public void enable() {
        this.cartMenu = new CartMenu(this);

        this.currencyManager = new CurrencyManager(this);
        this.currencyManager.setup();
        if (!this.currencyManager.hasCurrency()) {
            this.error("No currencies are available! Plugin will be disabled.");
            return;
        }

        this.registerProductHandlers();

        if (Config.MODULES_VIRTUAL_SHOP_ENABLED.get()) {
            this.virtualShop = new VirtualShopModule(this);
            this.virtualShop.setup();
        }
        if (Config.MODULES_CHEST_SHOP_ENABLED.get()) {
            this.chestShop = new ChestShopModule(this);
            this.chestShop.setup();
        }
        if (Config.MODULES_AUCTION_ENABLED.get()) {
            this.auction = new AuctionManager(this);
            this.auction.setup();
        }

        this.shopUpdateTask = new ShopUpdateTask(this);
        this.shopUpdateTask.start();

        if (EngineUtils.hasPlaceholderAPI()) {
            PlaceholderHook.setup(this);
        }
    }

    @Override
    public void disable() {
        if (EngineUtils.hasPlaceholderAPI()) {
            PlaceholderHook.shutdown();
        }

        if (this.shopUpdateTask != null) {
            this.shopUpdateTask.stop();
            this.shopUpdateTask = null;
        }
        this.cartMenu.clear();
        if (this.virtualShop != null) {
            this.virtualShop.shutdown();
            this.virtualShop = null;
        }
        if (this.chestShop != null) {
            this.chestShop.shutdown();
            this.chestShop = null;
        }
        if (this.auction != null) {
            this.auction.shutdown();
            this.auction = null;
        }
        if (this.currencyManager != null) {
            this.currencyManager.shutdown();
            this.currencyManager = null;
        }
    }

    private void registerProductHandlers() {
        ProductHandlerRegistry.register(ProductHandlerRegistry.BUKKIT_ITEM);
        ProductHandlerRegistry.register(ProductHandlerRegistry.BUKKIT_COMMAND);

        if (EngineUtils.hasPlugin(HookId.ORAXEN)) {
            ProductHandlerRegistry.register(new OraxenItemHandler());
        }
        if (EngineUtils.hasPlugin(HookId.ITEMS_ADDER)) {
            ProductHandlerRegistry.register(new ItemsAdderHandler());
        }
    }

    @Override
    public boolean setupDataHandlers() {
        try {
            this.dataHandler = DataHandler.getInstance(this);
            this.dataHandler.setup();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        this.userManager = new UserManager(this);
        this.userManager.setup();

        return true;
    }

    @Override
    public void loadConfig() {
        this.getConfig().initializeOptions(Config.class);
    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(Lang.class);
        this.getLangManager().loadEnum(TradeType.class);
        this.getLangManager().loadEnum(PriceType.class);
        this.getLang().saveChanges();
    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<ExcellentShop> mainCommand) {
        mainCommand.addChildren(new CurrencyMainCommand(this));
        mainCommand.addChildren(new ReloadSubCommand<>(this, Perms.COMMAND_RELOAD));
    }

    @Override
    public void registerPermissions() {
        this.registerPermissions(Perms.class);
    }

    @Override
    public void registerHooks() {

    }

    @Override
    @NotNull
    public DataHandler getData() {
        return this.dataHandler;
    }

    @NotNull
    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    @NotNull
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    @NotNull
    public CartMenu getCartMenu() {
        return cartMenu;
    }

    @Nullable
    public VirtualShopModule getVirtualShop() {
        return this.virtualShop;
    }

    @Nullable
    public ChestShopModule getChestShop() {
        return this.chestShop;
    }

    @Nullable
    public AuctionManager getAuction() {
        return this.auction;
    }
}
