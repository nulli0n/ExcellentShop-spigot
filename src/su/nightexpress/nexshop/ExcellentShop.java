package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.data.UserDataHolder;
import su.nexmedia.engine.commands.api.IGeneralCommand;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.config.Lang;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.data.ShopDataHandler;
import su.nightexpress.nexshop.data.UserManager;
import su.nightexpress.nexshop.data.object.ShopUser;
import su.nightexpress.nexshop.hooks.EHook;
import su.nightexpress.nexshop.hooks.external.BrokerHook;
import su.nightexpress.nexshop.hooks.external.GamePointsHK;
import su.nightexpress.nexshop.hooks.external.MySQLTokensHK;
import su.nightexpress.nexshop.hooks.external.PlayerPointsHK;
import su.nightexpress.nexshop.modules.ModuleCache;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

public class ExcellentShop extends NexPlugin<ExcellentShop> implements UserDataHolder<ExcellentShop, ShopUser> {

    private static ExcellentShop instance;

    private Config config;
    private Lang   lang;

    private ShopDataHandler dataHandler;
    private UserManager userManager;

    private CurrencyManager currencyManager;
    private ModuleCache     moduleCache;

    public ExcellentShop() {
        instance = this;
    }

    @NotNull
    public static ExcellentShop getInstance() {
        return instance;
    }

    @Override
    public void enable() {
        this.currencyManager = new CurrencyManager(this);
        this.currencyManager.setup();

        this.moduleCache = new ModuleCache(this);
        this.moduleCache.setup();
    }

    @Override
    public void disable() {
        for (TradeType tradeType : TradeType.values()) {
            Config.getCartMenu(tradeType).clear();
        }
        if (this.currencyManager != null) {
            this.currencyManager.shutdown();
            this.currencyManager = null;
        }
    }

    @Override
    public boolean setupDataHandlers() {
        try {
            this.dataHandler = ShopDataHandler.getInstance(this);
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
    public boolean useNewCommandManager() {
        return true;
    }

    @Override
    @NotNull
    public Config cfg() {
        return this.config;
    }

    @Override
    @NotNull
    public Lang lang() {
        return this.lang;
    }

    @Override
    public void setConfig() {
        this.config = new Config(this);
        this.config.setup();

        this.lang = new Lang(this);
        this.lang.setup();
    }

    @Override
    public void registerCmds(@NotNull IGeneralCommand<ExcellentShop> mainCommand) {

    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<ExcellentShop> mainCommand) {
        super.registerCommands(mainCommand);
    }

    @Override
    public void registerHooks() {
        // Currency
        this.registerHook(EHook.MYSQL_TOKENS, MySQLTokensHK.class);
        this.registerHook(EHook.PLAYER_POINTS, PlayerPointsHK.class);
        this.registerHook(EHook.GAME_POINTS, GamePointsHK.class);
        this.registerHook(EHook.BROKER, BrokerHook.class);
    }

    @Override
    public ShopDataHandler getData() {
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
    public ModuleCache getModuleCache() {
        return this.moduleCache;
    }

    @Nullable
    public VirtualShop getVirtualShop() {
        return this.moduleCache.getVirtualShop();
    }

    @Nullable
    public ChestShop getChestShop() {
        return this.moduleCache.getChestShop();
    }

    @Nullable
    public AuctionManager getAuctionManager() {
        return this.moduleCache.getAuctionManager();
    }
}
